// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.lumberjack;

import com.klikli_dev.principiamagica.content.golem.ai.behaviour.MoveToAndInteract;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Plans how a golem gets to a stump. There is one plan per stump, shared by the felling and the carving behaviour, so
 * exactly one of them acts.
 * <p>
 * The cardinal directions from the stump are ranked by how directly they point at the golem. The best direction that
 * works is used: walking outward at the stump's height, the first cell that is walkable (standable with headroom) and
 * usable is the stand cell. If natural leaves block the way, the plan is {@link Kind#CARVE}: the leaf nearest the golem
 * on the layer just before the stand cell is broken first. If nothing blocks the way, the plan is {@link Kind#APPROACH}:
 * the golem walks to the cell next to the stump. Only a non-leaf obstruction or an unusable stand cell makes the planner
 * fall back to the next direction, so the golem carves on its own side rather than walking around to a far gap.
 * <p>
 * The chosen direction is remembered per golem for a while, so the golem keeps its side while it carves.
 */
public final class TreeAccess {
    /**
     * How many cells outward from a stump are checked for the stand cell.
     */
    private static final int MAX_DEPTH = 5;
    private static final long PREFERRED_TICKS = 200;

    private static final Map<UUID, Preferred> PREFERRED = new HashMap<>();

    private TreeAccess() {
    }

    public enum Kind {
        /**
         * The golem stands next to the stump, nothing blocks the way.
         */
        APPROACH,
        /**
         * Natural leaves block the way: the golem breaks {@link Plan#leaf()} while standing on {@link Plan#stand()}.
         */
        CARVE
    }

    /**
     * @param kind  what to do
     * @param stand the walkable cell the golem stands on
     * @param leaf  the leaf to break for {@link Kind#CARVE}, null for {@link Kind#APPROACH}
     */
    public record Plan(Kind kind, BlockPos stand, @Nullable BlockPos leaf) {
    }

    private record Preferred(BlockPos stump, Direction direction, long gameTime) {
    }

    /**
     * @return true if the golem can stand on pos: no collision, no fluid, something solid below.
     */
    public static boolean isStandable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.getCollisionShape(level, pos).isEmpty() || !state.getFluidState().isEmpty()) {
            return false;
        }

        BlockPos below = pos.below();
        return !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
    }

    /**
     * @return true if the golem can stand on pos and has room above its head.
     */
    public static boolean isWalkable(ServerLevel level, BlockPos pos) {
        BlockPos above = pos.above();
        return isStandable(level, pos) && level.getBlockState(above).getCollisionShape(level, above).isEmpty();
    }

    /**
     * The plan for this golem to reach the stump, from the golem's current position.
     */
    public static Optional<Plan> plan(ServerLevel level, BlockPos stump, GolemEntity golem) {
        return plan(level, stump, golem.getUUID(), golem.blockPosition(), pos -> MoveToAndInteract.isAvailable(level, golem, pos));
    }

    public static Optional<Plan> plan(ServerLevel level, BlockPos stump, UUID golemId, BlockPos golemPos, Predicate<BlockPos> usable) {
        List<Direction> order = rank(stump, golemPos);
        Preferred preferred = PREFERRED.get(golemId);
        if (preferred != null && preferred.stump().equals(stump) && level.getGameTime() - preferred.gameTime() < PREFERRED_TICKS) {
            order.remove(preferred.direction());
            order.add(0, preferred.direction());
        }

        for (Direction direction : order) {
            Optional<Plan> plan = line(level, stump, direction, golemPos, usable);
            if (plan.isPresent()) {
                PREFERRED.put(golemId, new Preferred(stump, direction, level.getGameTime()));
                return plan;
            }
        }

        return Optional.empty();
    }

    /**
     * The cardinal directions, the one pointing most directly at the golem first.
     */
    private static List<Direction> rank(BlockPos stump, BlockPos golemPos) {
        int dx = golemPos.getX() - stump.getX();
        int dz = golemPos.getZ() - stump.getZ();
        List<Direction> directions = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            directions.add(direction);
        }

        directions.sort(Comparator.comparingInt((Direction direction) -> -(direction.getStepX() * dx + direction.getStepZ() * dz)));
        return directions;
    }

    /**
     * Walks outward from the stump in one direction. Every cell on the way must be standable or a natural leaf, and the
     * cell above it must be empty or a natural leaf, otherwise the direction is blocked.
     */
    private static Optional<Plan> line(ServerLevel level, BlockPos stump, Direction direction, BlockPos golemPos, Predicate<BlockPos> usable) {
        List<BlockPos> lastLayer = List.of();
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            BlockPos cell = stump.relative(direction, depth);
            if (isWalkable(level, cell)) {
                if (!usable.test(cell)) {
                    return Optional.empty();
                }

                if (depth == 1) {
                    return Optional.of(new Plan(Kind.APPROACH, cell, null));
                }

                return lastLayer.stream()
                        .filter(usable)
                        .min(Comparator.comparingDouble(leaf -> leaf.distSqr(golemPos)))
                        .map(leaf -> new Plan(Kind.CARVE, cell, leaf));
            }

            List<BlockPos> layer = new ArrayList<>();
            if (TreeDetector.isNaturalLeaf(level.getBlockState(cell))) {
                layer.add(cell);
            } else if (!isStandable(level, cell)) {
                return Optional.empty();
            }

            BlockPos above = cell.above();
            if (TreeDetector.isNaturalLeaf(level.getBlockState(above))) {
                layer.add(above);
            } else if (!level.getBlockState(above).getCollisionShape(level, above).isEmpty()) {
                return Optional.empty();
            }

            lastLayer = layer;
        }

        return Optional.empty();
    }
}
