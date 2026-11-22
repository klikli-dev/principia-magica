// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Computes how long a golem needs to break a block with its tool, using the same formula as the vanilla player.
 */
public final class GolemBreaking {
    public static final int UNBREAKABLE_TICKS = 6000;
    private static final int CORRECT_TOOL_DIVISOR = 30;
    private static final int WRONG_TOOL_DIVISOR = 100;

    private GolemBreaking() {
    }

    /**
     * @return the ticks the golem's main hand tool needs to break the block, before the work speed is applied.
     * 0 for instantly breakable blocks, {@link #UNBREAKABLE_TICKS} for blocks that can never be broken.
     */
    public static int breakTicks(ServerLevel level, GolemEntity golem, BlockPos pos, BlockState state) {
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) {
            return UNBREAKABLE_TICKS;
        }

        if (hardness == 0) {
            return 0;
        }

        ItemStack tool = golem.getMainHandItem();
        float speed = Math.max(1, tool.getDestroySpeed(state));
        boolean canHarvest = !state.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(state);
        int divisor = canHarvest ? CORRECT_TOOL_DIVISOR : WRONG_TOOL_DIVISOR;
        return (int) Math.min(UNBREAKABLE_TICKS, Math.ceil(hardness * divisor / speed));
    }
}
