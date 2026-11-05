// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkplaceDefaults;
import com.klikli_dev.principiamagica.registry.CriterionTriggerRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * One axiom per job. Right-click an owned golem to give it the job. Right-clicking a golem that has a different job
 * swaps the axioms: the previous job's axiom is returned to the player. Remove a job with an empty hand and shift-right-click.
 */
public class AxiomItem extends Item {
    public static final TagKey<Item> AXIOMS = TagKey.create(Registries.ITEM, PrincipiaMagica.loc("axioms"));

    private final ResourceKey<GolemJob> job;

    public AxiomItem(Properties properties, ResourceKey<GolemJob> job) {
        super(properties);
        this.job = job;
    }

    public ResourceKey<GolemJob> getJob() {
        return this.job;
    }

    /**
     * Creates the axiom item for the given job, or an empty stack if no axiom item is registered for it.
     */
    public static ItemStack createStack(Holder<GolemJob> job) {
        ResourceKey<GolemJob> key = job.unwrapKey().orElse(null);
        if (key == null) {
            return ItemStack.EMPTY;
        }

        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof AxiomItem axiom && axiom.getJob().equals(key))
                .findFirst()
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    /**
     * Installs the axiom into an owned golem. A purposeless golem gets the default work area around its position (unless it already has zones)
     * and switches to Work. Right-clicking a golem that has a different job swaps the axioms: the previous job's axiom is returned to the player.
     */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof GolemEntity golem)) {
            return InteractionResult.PASS;
        }

        if (!(golem.level() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }

        if (!player.getUUID().equals(golem.getOwnerUUID())) {
            player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.not_owner"));
            return InteractionResult.FAIL;
        }

        Holder<GolemJob> holder = PrincipiaMagicaRegistries.GOLEM_JOB.get(this.job).orElse(null);
        if (holder == null) {
            return InteractionResult.FAIL;
        }

        Holder<GolemJob> current = golem.getSpec().job().orElse(null);
        if (this.job.equals(current != null ? current.unwrapKey().orElse(null) : null)) {
            player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.job_already_set"));
            return InteractionResult.FAIL;
        }

        ItemStack previousAxiom = current != null ? createStack(current) : ItemStack.EMPTY;

        golem.setSpec(golem.getSpec().withJob(holder));
        stack.consume(1, player);

        if (current == null) {
            WorkplaceDefaults.applyIfEmpty(golem, level);
            golem.setMode(GolemMode.WORK);
        }

        if (!previousAxiom.isEmpty()) {
            player.getInventory().placeItemBackInInventory(previousAxiom);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.job_swapped"));
        }

        if (player instanceof ServerPlayer serverPlayer) {
            CriterionTriggerRegistry.GOLEM_GIVEN_AXIOM.get().trigger(serverPlayer);
        }

        level.sendParticles(ParticleTypes.ENCHANT, golem.getX(), golem.getY(0.5), golem.getZ(), 30, 0.4, 0.5, 0.4, 0.2);
        level.playSound(null, golem.blockPosition(), SoundEvents.DECORATED_POT_INSERT, SoundSource.NEUTRAL, 1.0F, 1.0F);
        player.swing(hand);

        return InteractionResult.SUCCESS;
    }
}

