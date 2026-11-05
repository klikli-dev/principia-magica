// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * One core per job. Right-click an owned golem to give it the job. Right-clicking a golem that has a different job
 * swaps the cores: the previous job's core is returned to the player. Remove a job with an empty hand and shift-right-click.
 */
public class GolemCoreItem extends Item {
    private final ResourceKey<GolemJob> job;

    public GolemCoreItem(Properties properties, ResourceKey<GolemJob> job) {
        super(properties);
        this.job = job;
    }

    public ResourceKey<GolemJob> getJob() {
        return this.job;
    }

    /**
     * Creates the core item for the given job, or an empty stack if no core item is registered for it.
     */
    public static ItemStack createStack(Holder<GolemJob> job) {
        ResourceKey<GolemJob> key = job.unwrapKey().orElse(null);
        if (key == null) {
            return ItemStack.EMPTY;
        }

        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof GolemCoreItem core && core.getJob().equals(key))
                .findFirst()
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof GolemEntity golem)) {
            return InteractionResult.PASS;
        }

        if (golem.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!player.getUUID().equals(golem.getOwnerUUID())) {
            return InteractionResult.PASS;
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

        ItemStack previousCore = current != null ? createStack(current) : ItemStack.EMPTY;

        golem.setSpec(golem.getSpec().withJob(holder));
        stack.consume(1, player);

        if (!previousCore.isEmpty()) {
            player.getInventory().placeItemBackInInventory(previousCore);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.job_swapped"));
        }

        return InteractionResult.SUCCESS;
    }
}
