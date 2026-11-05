// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.lumberjack;

import com.klikli_dev.principiamagica.content.golem.ai.behaviour.ContainerAccess;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.job.GolemBonemeal;
import com.klikli_dev.principiamagica.content.golem.job.JobOption;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Decides which blocks the lumberjack job works on. Targets are the stump of a natural tree, which is felled, and the
 * empty spot where a sapling should be replanted.
 */
public final class LumberjackTargets {
    public static final int KEEP_AMOUNT = 64;

    public static final JobOption REPLANT = new JobOption("replant_saplings", true);
    public static final JobOption BONEMEAL = new JobOption("bonemeal_saplings", false);
    public static final JobOption CLEAR_LEAVES = new JobOption("clear_leaves", true);

    /**
     * A sapling that got this many bone meal applications without growing into a tree is skipped for a while. Saplings
     * need two successful growth stages and each application only succeeds part of the time, so a low count would skip
     * saplings that are still growing. Without a limit, a sapling without room would eat bone meal forever.
     */
    private static final int BONEMEAL_STALL_ATTEMPTS = 12;
    private static final int BONEMEAL_STALL_TICKS = 1200;

    private LumberjackTargets() {
    }

    public static boolean isWorkTarget(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return isFellTarget(level, pos)
                || isPlantTarget(level, golem, pos)
                || golem.getWorkplace(level).filter(workplace -> canBonemeal(level, golem, workplace, pos)).isPresent();
    }

    /**
     * Reports the replanting spot that needs saplings the golem does not carry and no supply container has.
     */
    public static Optional<GolemStatus> blockedReason(ServerLevel level, GolemEntity golem, BlockPos pos) {
        Optional<Workplace> workplace = golem.getWorkplace(level);
        if (workplace.isEmpty() || !workplace.get().options().get(REPLANT) || !isPlantSpot(level, workplace.get(), pos)) {
            return Optional.empty();
        }

        if (chooseSapling(level, golem, workplace.get(), pos).isEmpty() && !supplyHasSaplings(level, workplace.get())) {
            return Optional.of(GolemStatus.NEEDS_SUPPLIES);
        }

        return Optional.empty();
    }

    public static boolean isFellTarget(ServerLevel level, BlockPos pos) {
        return TreeDetector.isStumpCandidate(level, pos) && TreeDetector.naturalTree(level, pos).isPresent();
    }

    public static boolean isPlantTarget(ServerLevel level, GolemEntity golem, BlockPos pos) {
        Optional<Workplace> workplace = golem.getWorkplace(level);
        return workplace.isPresent()
                && workplace.get().options().get(REPLANT)
                && isPlantSpot(level, workplace.get(), pos)
                && chooseSapling(level, golem, workplace.get(), pos).isPresent();
    }

    public static boolean holdsAxe(GolemEntity golem) {
        return golem.getMainHandItem().is(ItemTags.AXES);
    }

    public static boolean clearsLeaves(ServerLevel level, GolemEntity golem) {
        return golem.getWorkplace(level).map(workplace -> workplace.options().get(CLEAR_LEAVES)).orElse(false);
    }

    /**
     * A sapling in the zone that the golem can apply bone meal to, and that has not stalled (see BONEMEAL_STALL_ATTEMPTS).
     */
    public static boolean canBonemeal(ServerLevel level, GolemEntity golem, Workplace workplace, BlockPos pos) {
        if (!workplace.options().get(BONEMEAL) || !GolemBonemeal.hasBoneMeal(golem) || isBonemealStalled(golem, pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.SAPLINGS)
                && state.getBlock() instanceof BonemealableBlock block
                && block.isValidBonemealTarget(level, pos, state);
    }

    private static boolean isBonemealStalled(GolemEntity golem, BlockPos pos) {
        return golem.getBrain().getMemory(MemoryModuleTypeRegistry.SAPLING_BONEMEAL_ATTEMPTS.value()).orElse(Map.of()).getOrDefault(pos, 0) >= BONEMEAL_STALL_ATTEMPTS;
    }

    /**
     * Counts a bone meal application on pos. The counts expire together after BONEMEAL_STALL_TICKS without further
     * applications, which makes stalled saplings eligible again.
     */
    public static void recordBonemeal(GolemEntity golem, BlockPos pos) {
        Map<BlockPos, Integer> attempts = new HashMap<>(golem.getBrain().getMemory(MemoryModuleTypeRegistry.SAPLING_BONEMEAL_ATTEMPTS.value()).orElse(Map.of()));
        attempts.merge(pos, 1, Integer::sum);
        golem.getBrain().setMemoryWithExpiry(MemoryModuleTypeRegistry.SAPLING_BONEMEAL_ATTEMPTS.value(), attempts, BONEMEAL_STALL_TICKS);
    }

    /**
     * A remembered replant spot that is inside the zone and currently free for a sapling.
     */
    private static boolean isPlantSpot(ServerLevel level, Workplace workplace, BlockPos pos) {
        if (!workplace.itemMemory().containsKey(pos) || !workplace.containsInZone(LumberjackJob.BELOW, LumberjackJob.ABOVE, pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }

    /**
     * Forgets replant spots that left the zones or where no sapling can survive anymore. Only writes the workplace if something is stale.
     */
    public static void pruneItemMemory(ServerLevel level, GolemEntity golem) {
        golem.getWorkplace(level).ifPresent(workplace -> {
            boolean stale = workplace.itemMemory().entrySet().stream()
                    .anyMatch(entry -> !isValidMemory(level, workplace, entry.getKey(), entry.getValue()));
            if (stale) {
                golem.updateWorkplace(current -> current.withItemMemory(current.itemMemory().entrySet().stream()
                        .filter(entry -> isValidMemory(level, current, entry.getKey(), entry.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
            }
        });
    }

    private static boolean isValidMemory(ServerLevel level, Workplace workplace, BlockPos pos, Holder<Item> item) {
        return workplace.containsInZone(LumberjackJob.BELOW, LumberjackJob.ABOVE, pos) && survives(level, pos, item.value());
    }

    /**
     * The sapling to plant at pos: the remembered one if the golem carries it, otherwise the most common carried sapling
     * that can survive at pos.
     */
    public static Optional<Item> chooseSapling(ServerLevel level, GolemEntity golem, Workplace workplace, BlockPos pos) {
        Holder<Item> remembered = workplace.itemMemory().get(pos);
        if (remembered != null && golem.getInventory().countItem(remembered.value()) > 0 && survives(level, pos, remembered.value())) {
            return Optional.of(remembered.value());
        }

        Map<Item, Integer> counts = new HashMap<>();
        for (int slot = 0; slot < golem.getInventory().getUsableSlots(); slot++) {
            ItemStack stack = golem.getInventory().getItem(slot);
            if (Saplings.isSapling(stack.getItem()) && survives(level, pos, stack.getItem())) {
                counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }

        return counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }

    public static boolean survives(ServerLevel level, BlockPos pos, Item item) {
        return item instanceof BlockItem blockItem && blockItem.getBlock().defaultBlockState().canSurvive(level, pos);
    }

    private static boolean supplyHasSaplings(ServerLevel level, Workplace workplace) {
        for (ContainerLink link : workplace.links()) {
            ResourceHandler<ItemResource> handler = link.role().canSupply() ? ContainerAccess.itemHandler(level, link) : null;
            if (handler == null) {
                continue;
            }

            for (int slot = 0; slot < handler.size(); slot++) {
                ItemResource resource = handler.getResource(slot);
                if (!resource.isEmpty() && Saplings.isSapling(resource.toStack().getItem())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean keepsItem(Workplace workplace, ItemStack stack) {
        if (stack.is(Items.BONE_MEAL)) {
            return workplace.options().get(BONEMEAL);
        }

        return workplace.options().get(REPLANT) && Saplings.isSapling(stack.getItem());
    }

    /**
     * Remembers the sapling for each stump of a felled tree, so the golem plants it back on the same spot.
     */
    public static void rememberReplants(ServerLevel level, GolemEntity golem, TreeDetector.Tree tree) {
        Optional<Workplace> workplace = golem.getWorkplace(level);
        if (workplace.isEmpty() || !workplace.get().options().get(REPLANT)) {
            return;
        }

        Optional<Item> resolved = Saplings.forLeaves(tree.leaves());
        for (BlockPos stump : tree.stumps()) {
            Optional<Item> sapling = resolved.filter(item -> survives(level, stump, item))
                    .or(() -> chooseSapling(level, golem, workplace.get(), stump));
            sapling.ifPresent(item -> remember(golem, stump, item));
        }
    }

    public static void remember(GolemEntity golem, BlockPos pos, Item sapling) {
        golem.updateWorkplace(workplace -> {
            Map<BlockPos, Holder<Item>> memory = new HashMap<>(workplace.itemMemory());
            memory.put(pos, BuiltInRegistries.ITEM.wrapAsHolder(sapling));
            return workplace.withItemMemory(memory);
        });
    }
}
