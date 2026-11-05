// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.crop;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Harvests the segments of stacked plants (tag {@code golem_harvestable_stacked}) that sit on top of another segment.
 * The base segment is never touched, so the plant regrows from it.
 */
public class StackedPlantHandler implements CropHandler {
    public static final TagKey<Block> HARVESTABLE = TagKey.create(Registries.BLOCK, PrincipiaMagica.loc("golem_harvestable_stacked"));

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean canHandle(BlockState state) {
        return state.is(HARVESTABLE);
    }

    @Override
    public boolean isHarvestable(ServerLevel level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.below()).is(state.getBlock());
    }

    @Override
    public HarvestResult harvest(ServerLevel level, BlockPos pos, BlockState state, GolemEntity golem) {
        List<ItemStack> drops = CropHandlers.breakBlock(level, golem, pos, state);
        return new HarvestResult(drops, true);
    }
}
