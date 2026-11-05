// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.ai.behaviour.ContainerAccess;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.job.GolemBonemeal;
import com.klikli_dev.principiamagica.content.golem.job.JobOption;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.CropHandlers;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Decides which block the farming job works on. Targets are the block the golem interacts with:
 * the plant for harvest and bonemeal, the soil for planting and the dirt for tilling.
 */
public final class FarmingTargets {
    public static final int KEEP_AMOUNT = 64;
    private static final int WATER_RANGE = 4;

    public static final JobOption REPLANT = new JobOption("replant", true);
    public static final JobOption TILL = new JobOption("till", false);
    public static final JobOption BONEMEAL = new JobOption("bonemeal", false);
    public static final JobOption CLEAR_OBSTRUCTIONS = new JobOption("clear_obstructions", true);

    public static final TagKey<Block> CLEARABLE = TagKey.create(Registries.BLOCK, PrincipiaMagica.loc("golem_clearable"));

    private FarmingTargets() {
    }

    public static boolean isWorkTarget(ServerLevel level, GolemEntity golem, BlockPos pos) {
        Optional<Workplace> workplace = golem.getWorkplace(level);
        if (workplace.isEmpty()) {
            return false;
        }

        return isHarvestable(level, pos)
                || canClear(level, workplace.get(), pos)
                || canPlant(level, golem, pos)
                || canBonemeal(level, golem, workplace.get(), pos)
                || canTill(level, golem, workplace.get(), pos);
    }

    public static boolean canClear(ServerLevel level, Workplace workplace, BlockPos pos) {
        return workplace.options().get(CLEAR_OBSTRUCTIONS) && level.getBlockState(pos).is(CLEARABLE);
    }

    /**
     * Reports why a position is not a target although the job could work on it: tilling without a hoe, or planting
     * without seeds that no supply container holds.
     */
    public static Optional<GolemStatus> blockedReason(ServerLevel level, GolemEntity golem, BlockPos pos) {
        Optional<Workplace> workplace = golem.getWorkplace(level);
        if (workplace.isEmpty()) {
            return Optional.empty();
        }

        if (workplace.get().options().get(TILL) && !holdsHoe(golem) && isTillable(level, pos)) {
            return Optional.of(GolemStatus.MISSING_TOOL);
        }

        if (workplace.get().options().get(REPLANT)
                && isPlantableSoil(level, pos)
                && seedToPlant(level, golem, workplace.get(), pos).isEmpty()
                && !supplyHasSeeds(level, workplace.get())) {
            return Optional.of(GolemStatus.NEEDS_SUPPLIES);
        }

        return Optional.empty();
    }

    private static boolean supplyHasSeeds(ServerLevel level, Workplace workplace) {
        for (ContainerLink link : workplace.links()) {
            ResourceHandler<ItemResource> handler = link.role().canSupply() ? ContainerAccess.itemHandler(level, link) : null;
            if (handler == null) {
                continue;
            }

            for (int slot = 0; slot < handler.size(); slot++) {
                ItemResource resource = handler.getResource(slot);
                if (!resource.isEmpty() && isPlantSeed(resource.toStack())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isHarvestable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return CropHandlers.find(state).map(handler -> handler.isHarvestable(level, pos, state)).orElse(false);
    }

    /**
     * @param soilPos the farmland (or soul sand) the golem plants on, the plant goes on top of it
     */
    public static boolean canPlant(ServerLevel level, GolemEntity golem, BlockPos soilPos) {
        Optional<Workplace> workplace = golem.getWorkplace(level);
        if (workplace.isEmpty() || !workplace.get().options().get(REPLANT)) {
            return false;
        }

        return isPlantableSoil(level, soilPos) && seedToPlant(level, golem, workplace.get(), soilPos).isPresent();
    }

    private static boolean isPlantableSoil(ServerLevel level, BlockPos soilPos) {
        return isFarmSoil(level, soilPos) && level.getBlockState(soilPos.above()).isAir();
    }

    private static boolean isFarmSoil(ServerLevel level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos);
        return soil.is(Blocks.FARMLAND) || soil.is(Blocks.SOUL_SAND);
    }

    /**
     * Forgets the field memory of spots that left the zones or whose soil is no longer plantable, e.g. farmland that
     * was turned back into dirt. Only writes the workplace if something is stale.
     */
    public static void pruneItemMemory(ServerLevel level, GolemEntity golem) {
        golem.getWorkplace(level).ifPresent(workplace -> {
            boolean stale = workplace.itemMemory().keySet().stream()
                    .anyMatch(pos -> !workplace.containsInZone(pos) || !isFarmSoil(level, pos.below()));
            if (stale) {
                golem.updateWorkplace(current -> current.withItemMemory(current.itemMemory().entrySet().stream()
                        .filter(entry -> current.containsInZone(entry.getKey()) && isFarmSoil(level, entry.getKey().below()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
            }
        });
    }

    /**
     * The seed remembered for the plant position above the soil, if the golem carries it. Without a memory entry,
     * the most common plantable seed in the golem's inventory that survives on this soil.
     */
    public static Optional<Item> seedToPlant(ServerLevel level, GolemEntity golem, Workplace workplace, BlockPos soilPos) {
        BlockPos plantPos = soilPos.above();
        Holder<Item> remembered = workplace.itemMemory().get(plantPos);
        if (remembered != null) {
            Item item = remembered.value();
            return golem.getInventory().countItem(item) > 0 && survives(level, plantPos, item) ? Optional.of(item) : Optional.empty();
        }

        Map<Item, Integer> counts = new HashMap<>();
        for (int slot = 0; slot < golem.getInventory().getUsableSlots(); slot++) {
            ItemStack stack = golem.getInventory().getItem(slot);
            if (isPlantSeed(stack) && survives(level, plantPos, stack.getItem())) {
                counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }

        return counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }

    public static boolean isPlantSeed(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem
                && (blockItem.getBlock() instanceof CropBlock
                || blockItem.getBlock() instanceof NetherWartBlock
                || blockItem.getBlock() instanceof StemBlock);
    }

    private static boolean survives(ServerLevel level, BlockPos plantPos, Item item) {
        return item instanceof BlockItem blockItem && blockItem.getBlock().defaultBlockState().canSurvive(level, plantPos);
    }

    public static boolean canTill(ServerLevel level, GolemEntity golem, Workplace workplace, BlockPos pos) {
        return workplace.options().get(TILL)
                && golem.getMainHandItem().is(ItemTags.HOES)
                && isTillable(level, pos);
    }

    /**
     * @return whether the position could be tilled if the golem had a hoe. Used to report the missing tool.
     */
    public static boolean isTillable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.DIRT_PATH))
                && level.getBlockState(pos.above()).isAir()
                && isNearWater(level, pos);
    }

    public static boolean isNearWater(ServerLevel level, BlockPos pos) {
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-WATER_RANGE, 0, -WATER_RANGE), pos.offset(WATER_RANGE, 1, WATER_RANGE))) {
            if (level.getFluidState(near).is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }

    public static boolean canBonemeal(ServerLevel level, GolemEntity golem, Workplace workplace, BlockPos pos) {
        if (!workplace.options().get(BONEMEAL) || !GolemBonemeal.hasBoneMeal(golem)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BonemealableBlock block
                && block.isValidBonemealTarget(level, pos, state)
                && CropHandlers.find(state).isPresent();
    }

    public static boolean keepsItem(Workplace workplace, ItemStack stack) {
        if (stack.is(Items.BONE_MEAL)) {
            return workplace.options().get(BONEMEAL);
        }

        return workplace.options().get(REPLANT) && isPlantSeed(stack);
    }

    public static boolean holdsHoe(GolemEntity golem) {
        return golem.getMainHandItem().is(ItemTags.HOES);
    }

    public static void remember(GolemEntity golem, BlockPos plantPos, Item seed) {
        golem.updateWorkplace(workplace -> {
            Map<BlockPos, Holder<Item>> memory = new HashMap<>(workplace.itemMemory());
            memory.put(plantPos, BuiltInRegistries.ITEM.wrapAsHolder(seed));
            return workplace.withItemMemory(memory);
        });
    }
}
