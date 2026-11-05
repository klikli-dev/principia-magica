// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingTargets;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.CropHandler;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.CropHandlers;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.HarvestResult;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class HarvestCrop extends FarmingBehaviour {

    @Override
    protected boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return FarmingTargets.isHarvestable(level, pos);
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
        CropHandlers.collect(level, golem, pos, result.drops());
        if (result.removed() && seed.isPresent()) {
            FarmingTargets.remember(golem, pos, seed.get());
        }
    }
}
