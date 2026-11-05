// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LevelEvent;

public final class GolemBonemeal {

    private GolemBonemeal() {
    }

    public static boolean hasBoneMeal(GolemEntity golem) {
        return golem.getInventory().countItem(Items.BONE_MEAL) > 0;
    }

    /**
     * Applies one bone meal from the golem's inventory to pos.
     *
     * @return true if a bone meal was applied
     */
    public static boolean apply(ServerLevel level, GolemEntity golem, BlockPos pos) {
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
            return true;
        }

        return false;
    }
}
