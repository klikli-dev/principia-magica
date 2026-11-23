// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class GolemDrops {

    private GolemDrops() {
    }

    /**
     * Removes the block without spawning its drops, returning the drops that the block would have produced
     * for the golem's tool.
     */
    public static List<ItemStack> breakBlock(ServerLevel level, GolemEntity golem, BlockPos pos, BlockState state) {
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, golem, golem.getMainHandItem());
        level.destroyBlock(pos, false, golem, 512);
        return drops;
    }

    /**
     * Puts the drops into the golem's inventory. Whatever does not fit is dropped at pos.
     */
    public static void collect(ServerLevel level, GolemEntity golem, BlockPos pos, List<ItemStack> drops) {
        for (ItemStack stack : drops) {
            ItemStack remaining = golem.getInventory().insert(stack);
            if (!remaining.isEmpty()) {
                Block.popResource(level, pos, remaining);
            }
        }
    }
}
