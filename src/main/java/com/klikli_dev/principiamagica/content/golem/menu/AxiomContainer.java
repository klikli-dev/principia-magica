// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Exposes the golem's job as a single axiom. The job in the golem's spec is the only state: the axiom stack is derived from it, and
 * changes go through {@link GolemEntity#installJob} and {@link GolemEntity#takeJob} on the server. The client only reads the synced spec.
 * <p>
 * The stack is cached per job, so vanilla can hold on to it. Every change drops the cache, so a stack that left the slot is never returned again.
 * The axiom is never destroyed by clearing: it can only leave the slot by being taken.
 */
final class AxiomContainer implements Container {
    private final GolemEntity golem;
    private final Player player;
    private @Nullable Holder<GolemJob> cachedJob;
    private ItemStack cachedStack = ItemStack.EMPTY;

    AxiomContainer(GolemEntity golem, Player player) {
        this.golem = golem;
        this.player = player;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.golem.getSpec().job().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        Optional<Holder<GolemJob>> job = this.golem.getSpec().job();
        if (job.isEmpty()) {
            this.invalidate();
            return ItemStack.EMPTY;
        }

        if (!job.get().equals(this.cachedJob) || this.cachedStack.isEmpty()) {
            this.cachedJob = job.get();
            this.cachedStack = AxiomItem.createStack(job.get());
        }

        return this.cachedStack;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return this.takeAxiom();
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.takeAxiom();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (this.golem.level().isClientSide()) {
            return;
        }

        this.invalidate();
        if (stack.isEmpty()) {
            this.golem.takeJob();
            return;
        }

        AxiomItem.jobOf(stack).ifPresent(job -> this.golem.installJob(job, this.player instanceof ServerPlayer serverPlayer ? serverPlayer : null));
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return this.golem.isAlive();
    }

    @Override
    public void clearContent() {
    }

    private ItemStack takeAxiom() {
        if (this.golem.level().isClientSide()) {
            return ItemStack.EMPTY;
        }

        this.invalidate();
        return this.golem.takeJob();
    }

    private void invalidate() {
        this.cachedJob = null;
        this.cachedStack = ItemStack.EMPTY;
    }
}
