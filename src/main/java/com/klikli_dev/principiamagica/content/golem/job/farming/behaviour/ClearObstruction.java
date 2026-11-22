// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemBreaking;
import com.klikli_dev.principiamagica.content.golem.job.GolemDrops;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Removes grass, flowers and similar plants (tag {@code golem_clearable}) from the work area, so the soil above
 * can be planted or tilled. Their drops, e.g. seeds from grass, go into the golem's inventory.
 */
public class ClearObstruction extends FarmingBehaviour {
    private static final int MAX_CLEAR_TICKS = 60;

    @Override
    protected boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return golem.getWorkplace(level).map(workplace -> FarmingTargets.canClear(level, workplace, pos)).orElse(false);
    }

    @Override
    protected @Nullable BlockPos breakingPos(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return level.getBlockState(pos).getDestroySpeed(level, pos) > 0 ? pos : null;
    }

    @Override
    protected int breakTicks(ServerLevel level, GolemEntity golem, BlockPos pos) {
        int breakTicks = GolemBreaking.breakTicks(level, golem, pos, level.getBlockState(pos));
        return Math.min(MAX_CLEAR_TICKS, Math.max(this.baseWorkTicks(), breakTicks));
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        List<ItemStack> drops = GolemDrops.breakBlock(level, golem, pos, state);
        GolemDrops.collect(level, golem, pos, drops);
    }
}
