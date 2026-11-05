// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LevelEvent;

public class ApplyBonemeal extends FarmingBehaviour {

    @Override
    protected boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return golem.getWorkplace(level)
                .map(workplace -> FarmingTargets.canBonemeal(level, golem, workplace, pos))
                .orElse(false);
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos pos) {
        ItemStack boneMeal = null;
        for (int slot = 0; slot < golem.getInventory().getUsableSlots(); slot++) {
            ItemStack stack = golem.getInventory().getItem(slot);
            if (stack.is(Items.BONE_MEAL)) {
                boneMeal = stack;
                break;
            }
        }

        if (boneMeal != null && BoneMealItem.growCrop(boneMeal, level, pos)) {
            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 15);
        }
    }
}
