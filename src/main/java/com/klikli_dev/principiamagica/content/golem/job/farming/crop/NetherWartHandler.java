// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.crop;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public class NetherWartHandler implements CropHandler {

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean canHandle(BlockState state) {
        return state.getBlock() instanceof NetherWartBlock;
    }

    @Override
    public boolean isHarvestable(ServerLevel level, BlockPos pos, BlockState state) {
        return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
    }

    @Override
    public HarvestResult harvest(ServerLevel level, BlockPos pos, BlockState state, GolemEntity golem) {
        List<ItemStack> drops = CropHandlers.breakBlock(level, golem, pos, state);
        return new HarvestResult(drops, true);
    }

    @Override
    public Optional<Item> seedFor(ServerLevel level, BlockPos pos, BlockState state) {
        ItemStack seed = state.getCloneItemStack(level, pos, false);
        return seed.isEmpty() ? Optional.empty() : Optional.of(seed.getItem());
    }
}
