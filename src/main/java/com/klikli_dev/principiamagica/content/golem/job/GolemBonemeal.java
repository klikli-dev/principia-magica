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
    /**
     * Event data for the plant growth particles and sound.
     */
    private static final int GROWTH_EVENT_DATA = 15;

    private GolemBonemeal() {
    }

    public static boolean hasBoneMeal(GolemEntity golem) {
        return golem.countCarried(Items.BONE_MEAL) > 0;
    }

    /**
     * Applies one bone meal from the golem's inventory to pos.
     *
     * @return true if a bone meal was applied
     */
    public static boolean apply(ServerLevel level, GolemEntity golem, BlockPos pos) {
        ItemStack hand = golem.getHandItem();
        ItemStack boneMeal = hand.is(Items.BONE_MEAL) ? hand : null;

        if (boneMeal != null && BoneMealItem.growCrop(boneMeal, level, pos)) {
            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, GROWTH_EVENT_DATA);
            return true;
        }

        return false;
    }
}
