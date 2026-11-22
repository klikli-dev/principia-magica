// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemBreaking;
import com.klikli_dev.principiamagica.content.golem.job.GolemDrops;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingTargets;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.CropHandler;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.CropHandlers;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.HarvestResult;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class HarvestCrop extends FarmingBehaviour {
    private static final int MAX_HARVEST_TICKS = 60;

    @Override
    protected boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return FarmingTargets.isHarvestable(level, pos);
    }

    /**
     * Only blocks that need a tool to break (hardness above zero) show the crack overlay. Crops that are reset in place
     * or break instantly, such as wheat and sweet berry bushes, are harvested without it.
     */
    @Override
    protected @Nullable BlockPos breakingPos(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return level.getBlockState(pos).getDestroySpeed(level, pos) > 0 ? pos : null;
    }

    @Override
    protected int breakTicks(ServerLevel level, GolemEntity golem, BlockPos pos) {
        int breakTicks = GolemBreaking.breakTicks(level, golem, pos, level.getBlockState(pos));
        return Math.min(MAX_HARVEST_TICKS, Math.max(this.baseWorkTicks(), breakTicks));
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Optional<CropHandler> handler = CropHandlers.find(state);
        if (handler.isEmpty()) {
            return;
        }

        Optional<Item> seed = handler.get().seedFor(level, pos, state);
        HarvestResult result = handler.get().harvest(level, pos, state, golem);
        GolemDrops.collect(level, golem, pos, result.drops());
        if (result.removed() && seed.isPresent()) {
            FarmingTargets.remember(golem, pos, seed.get());
        }
    }
}
