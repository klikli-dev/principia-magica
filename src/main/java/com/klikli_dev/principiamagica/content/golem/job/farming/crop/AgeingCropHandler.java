// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.crop;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemDrops;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

/**
 * Handles every {@link CropBlock}, including beetroot and modded crops. Harvests at max age and replants the seed.
 */
public class AgeingCropHandler implements CropHandler {

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean canHandle(BlockState state) {
        return state.getBlock() instanceof CropBlock;
    }

    @Override
    public boolean isHarvestable(ServerLevel level, BlockPos pos, BlockState state) {
        return ((CropBlock) state.getBlock()).isMaxAge(state);
    }

    @Override
    public HarvestResult harvest(ServerLevel level, BlockPos pos, BlockState state, GolemEntity golem) {
        List<ItemStack> drops = GolemDrops.breakBlock(level, golem, pos, state);
        return new HarvestResult(drops, true);
    }

    @Override
    public Optional<Item> seedFor(ServerLevel level, BlockPos pos, BlockState state) {
        ItemStack seed = state.getCloneItemStack(level, pos, false);
        return seed.isEmpty() ? Optional.empty() : Optional.of(seed.getItem());
    }
}
