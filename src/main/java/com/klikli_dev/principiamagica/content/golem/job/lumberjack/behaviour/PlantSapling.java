// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.JobTargetBehaviour;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.LumberjackTargets;
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

/**
 * Plants a sapling on a replant spot left by a felled tree.
 */
public class PlantSapling extends JobTargetBehaviour {
    private static final float WORK_SPEED = 0.6F;

    public PlantSapling() {
        super(WORK_SPEED);
    }

    @Override
    protected Optional<BlockPos> findTarget(ServerLevel level, GolemEntity golem) {
        LumberjackTargets.pruneItemMemory(level, golem);
        return super.findTarget(level, golem);
    }

    @Override
    protected boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return LumberjackTargets.isPlantTarget(level, golem, pos);
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos pos) {
        Optional<Workplace> workplace = golem.getWorkplace(level);
        if (workplace.isEmpty()) {
            return;
        }

        Optional<Item> sapling = LumberjackTargets.chooseSapling(level, golem, workplace.get(), pos);
        if (sapling.isEmpty() || !(sapling.get() instanceof BlockItem blockItem)) {
            return;
        }

        BlockState plant = blockItem.getBlock().defaultBlockState();
        if (!plant.canSurvive(level, pos)) {
            return;
        }

        level.setBlock(pos, plant, Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(golem, plant));
        golem.getInventory().removeItemType(sapling.get(), 1);
    }
}
