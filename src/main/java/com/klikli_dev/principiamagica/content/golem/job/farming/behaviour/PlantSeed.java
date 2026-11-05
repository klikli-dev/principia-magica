// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingTargets;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;

public class PlantSeed extends FarmingBehaviour {

    @Override
    protected Optional<BlockPos> findTarget(ServerLevel level, GolemEntity golem) {
        FarmingTargets.pruneItemMemory(level, golem);
        return super.findTarget(level, golem);
    }

    @Override
    protected boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return FarmingTargets.canPlant(level, golem, pos);
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos soilPos) {
        Optional<Workplace> workplace = golem.getWorkplace(level);
        if (workplace.isEmpty()) {
            return;
        }

        Optional<Item> seed = FarmingTargets.seedToPlant(level, golem, workplace.get(), soilPos);
        if (seed.isEmpty() || !(seed.get() instanceof BlockItem blockItem)) {
            return;
        }

        BlockPos plantPos = soilPos.above();
        BlockState plant = blockItem.getBlock().defaultBlockState();
        if (!plant.canSurvive(level, plantPos)) {
            return;
        }

        level.setBlock(plantPos, plant, Block.UPDATE_ALL);
        level.playSound(null, plantPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(GameEvent.BLOCK_PLACE, plantPos, GameEvent.Context.of(golem, plant));
        golem.getInventory().removeItemType(seed.get(), 1);
    }
}
