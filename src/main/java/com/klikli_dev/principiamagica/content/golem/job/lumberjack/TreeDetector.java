// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.lumberjack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Decides whether a log is the stump of a natural tree. A tree is natural only if its connected logs touch leaves that
 * can still decay naturally, and its topmost logs touch such leaves too. Player-placed leaves are persistent and do not
 * count, so player builds made of wood are never felled. Huge fungi and fallen logs have no decaying leaves and are never felled.
 */
public final class TreeDetector {
    public static final int MAX_LOGS = 256;
    public static final int MAX_HORIZONTAL_DISTANCE = 10;
    public static final int MAX_HEIGHT = 48;
    public static final int CACHE_TICKS = 100;
    private static final int MAX_CACHE_ENTRIES = 512;

    private static final Map<ServerLevel, Map<BlockPos, CacheEntry>> CACHE = new WeakHashMap<>();

    private TreeDetector() {
    }

    /**
     * A natural tree found from one of its stumps.
     *
     * @param logs    all logs of the tree, sorted from the top down
     * @param stumps  the logs at the bottom that stand on something that is not part of the tree
     * @param leaves  the most common natural leaf block of the tree, used to resolve the matching sapling
     */
    public record Tree(List<BlockPos> logs, List<BlockPos> stumps, Block leaves) {
    }

    private record CacheEntry(long gameTime, Optional<Tree> tree) {
    }

    public static boolean isNaturalLeaf(BlockState state) {
        return state.is(BlockTags.LEAVES) && state.hasProperty(LeavesBlock.PERSISTENT) && !state.getValue(LeavesBlock.PERSISTENT);
    }

    public static boolean isLog(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    /**
     * @return true if the log at pos stands on solid ground that is not part of a tree, i.e. the golem can reach it
     * from below and it is the bottom of its trunk.
     */
    public static boolean isStumpCandidate(ServerLevel level, BlockPos pos) {
        if (!isLog(level.getBlockState(pos))) {
            return false;
        }

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return !isLog(belowState) && !belowState.is(BlockTags.LEAVES) && !belowState.isAir() && !belowState.canBeReplaced();
    }

    /**
     * @return the natural tree the stump belongs to, if any. Results are cached for a short time, because the work
     * target sensor re-validates candidates every few ticks.
     */
    public static Optional<Tree> naturalTree(ServerLevel level, BlockPos stump) {
        if (!isStumpCandidate(level, stump)) {
            return Optional.empty();
        }

        long now = level.getGameTime();
        Map<BlockPos, CacheEntry> entries = CACHE.computeIfAbsent(level, key -> new HashMap<>());
        CacheEntry cached = entries.get(stump);
        if (cached != null && isFresh(cached, now)) {
            return cached.tree();
        }

        Optional<Tree> tree = scan(level, stump);
        if (entries.size() >= MAX_CACHE_ENTRIES) {
            entries.values().removeIf(entry -> !isFresh(entry, now));
        }

        if (entries.size() >= MAX_CACHE_ENTRIES) {
            entries.clear();
        }

        entries.put(stump, new CacheEntry(now, tree));
        return tree;
    }

    private static boolean isFresh(CacheEntry entry, long now) {
        return now >= entry.gameTime() && now - entry.gameTime() < CACHE_TICKS;
    }

    /**
     * Forgets the cached results for the stumps of a tree that was felled, so the tree is not treated as still standing.
     */
    public static void invalidate(ServerLevel level, Tree tree) {
        Map<BlockPos, CacheEntry> entries = CACHE.get(level);
        if (entries != null) {
            tree.stumps().forEach(entries::remove);
        }
    }

    /**
     * Scans the connected logs from the stump without using the cache.
     */
    public static Optional<Tree> scan(ServerLevel level, BlockPos stump) {
        if (!isStumpCandidate(level, stump)) {
            return Optional.empty();
        }

        int minX = stump.getX() - MAX_HORIZONTAL_DISTANCE;
        int maxX = stump.getX() + MAX_HORIZONTAL_DISTANCE;
        int minZ = stump.getZ() - MAX_HORIZONTAL_DISTANCE;
        int maxZ = stump.getZ() + MAX_HORIZONTAL_DISTANCE;
        int maxY = stump.getY() + MAX_HEIGHT;

        Set<BlockPos> logSet = new HashSet<>();
        Set<BlockPos> leafSet = new HashSet<>();
        List<BlockPos> logs = new ArrayList<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        logSet.add(stump);
        logs.add(stump);
        queue.add(stump);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos face = current.relative(direction);
                if (isNaturalLeaf(level.getBlockState(face))) {
                    leafSet.add(face);
                }
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos next = current.offset(dx, dy, dz);
                        if ((dx == 0 && dy == 0 && dz == 0) || logSet.contains(next)) {
                            continue;
                        }

                        if (next.getX() < minX || next.getX() > maxX || next.getZ() < minZ || next.getZ() > maxZ
                                || next.getY() < stump.getY() || next.getY() > maxY) {
                            continue;
                        }

                        if (!isLog(level.getBlockState(next))) {
                            continue;
                        }

                        if (logs.size() >= MAX_LOGS) {
                            return Optional.empty();
                        }

                        logSet.add(next);
                        logs.add(next);
                        queue.add(next);
                    }
                }
            }
        }

        if (leafSet.isEmpty() || !touchesNaturalLeaf(level, topLayer(logs))) {
            return Optional.empty();
        }

        int lowestY = logs.stream().mapToInt(BlockPos::getY).min().orElse(stump.getY());
        List<BlockPos> stumps = logs.stream()
                .filter(pos -> pos.getY() == lowestY && !logSet.contains(pos.below()))
                .toList();

        Block leaves = dominantLeaf(level, leafSet);
        List<BlockPos> sorted = new ArrayList<>(logs);
        sorted.sort(Comparator.comparingInt((BlockPos pos) -> pos.getY()).reversed());
        return Optional.of(new Tree(sorted, stumps, leaves));
    }

    private static List<BlockPos> topLayer(List<BlockPos> logs) {
        int highest = logs.stream().mapToInt(BlockPos::getY).max().orElse(0);
        return logs.stream().filter(pos -> pos.getY() == highest).toList();
    }

    private static boolean touchesNaturalLeaf(ServerLevel level, List<BlockPos> top) {
        for (BlockPos log : top) {
            for (Direction direction : Direction.values()) {
                if (isNaturalLeaf(level.getBlockState(log.relative(direction)))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Block dominantLeaf(ServerLevel level, Set<BlockPos> leafSet) {
        Map<Block, Integer> counts = new HashMap<>();
        for (BlockPos leaf : leafSet) {
            counts.merge(level.getBlockState(leaf).getBlock(), 1, Integer::sum);
        }

        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow();
    }
}
