// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Quickened clay heals golems: right-click a damaged golem with it to restore one health per clay used.
 * Creative players do not use up the clay. Golems at full health are not affected, so the normal interactions still apply.
 */
public class QuickenedClayItem extends Item {

    /**
     * The health one clay item restores.
     */
    public static final float HEALING_PER_ITEM = 1.0F;

    public QuickenedClayItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof GolemEntity golem) || !golem.isAlive() || golem.getHealth() >= golem.getMaxHealth()) {
            return InteractionResult.PASS;
        }

        if (!(golem.level() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }

        golem.heal(HEALING_PER_ITEM);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, golem.getX(), golem.getY(0.5), golem.getZ(), 8, 0.3, 0.4, 0.3, 0.0);
        level.playSound(null, golem.blockPosition(), SoundRegistry.GOLEM_HEAL.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        player.swing(hand);

        return InteractionResult.SUCCESS;
    }
}
