// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemInventory;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Moves items the job does not keep into a DEPOSIT-capable container.
 */
public class DepositItems extends MoveToAndInteract {
    private static final int COOLDOWN_TICKS = 100;

    public enum Mode {
        /** Only deposit when the inventory has no empty usable slot left. */
        WHEN_FULL,
        /** Deposit any excess whenever there is nothing more important to do. */
        LEFTOVER
    }

    private final GolemJob job;
    private final Mode mode;
    private @Nullable Container openContainer;

    public DepositItems(GolemJob job, Mode mode) {
        super(Map.of(MemoryModuleTypeRegistry.DEPOSIT_COOLDOWN.value(), MemoryStatus.VALUE_ABSENT), 0.6F);
        this.job = job;
        this.mode = mode;
    }

    @Override
    protected Optional<BlockPos> findTarget(ServerLevel level, GolemEntity golem) {
        if (!this.shouldDeposit(golem)) {
            return Optional.empty();
        }

        return golem.getWorkplace(level).flatMap(workplace -> nearestDepositLink(level, golem, workplace));
    }

    private Optional<BlockPos> nearestDepositLink(ServerLevel level, GolemEntity golem, Workplace workplace) {
        return workplace.links().stream()
                .filter(link -> link.role().canDeposit())
                .map(ContainerLink::pos)
                .distinct()
                .filter(pos -> isAvailable(level, golem, pos))
                .filter(pos -> ContainerAccess.linkAt(workplace, pos, ContainerRole::canDeposit)
                        .map(link -> ContainerAccess.itemHandler(level, link) != null)
                        .orElse(false))
                .min(Comparator.comparingDouble(pos -> pos.distSqr(golem.blockPosition())));
    }

    @Override
    protected boolean isTargetValid(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return this.shouldDeposit(golem) && golem.getWorkplace(level)
                .flatMap(workplace -> ContainerAccess.linkAt(workplace, pos, ContainerRole::canDeposit))
                .map(link -> ContainerAccess.itemHandler(level, link) != null)
                .orElse(false);
    }

    @Override
    protected void onNoTarget(ServerLevel level, GolemEntity golem) {
        if (this.shouldDeposit(golem)) {
            golem.reportBlocker(GolemStatus.NO_DEPOSIT);
            golem.getBrain().setMemory(MemoryModuleTypeRegistry.DEPOSIT_COOLDOWN.value(), COOLDOWN_TICKS);
        }
    }

    @Override
    protected boolean reserveTarget() {
        return false;
    }

    @Override
    protected void startInteraction(ServerLevel level, GolemEntity golem, BlockPos pos) {
        this.openContainer = ContainerAccess.containerAt(level, pos);
        if (this.openContainer != null) {
            golem.setOpenedContainerPos(pos);
            this.openContainer.startOpen(golem);
        }
    }

    @Override
    protected void stopInteraction(ServerLevel level, GolemEntity golem, BlockPos pos) {
        if (this.openContainer != null) {
            this.openContainer.stopOpen(golem);
            this.openContainer = null;
        }
        golem.setOpenedContainerPos(null);
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos pos) {
        ResourceHandler<ItemResource> handler = golem.getWorkplace(level)
                .flatMap(workplace -> ContainerAccess.linkAt(workplace, pos, ContainerRole::canDeposit))
                .map(link -> ContainerAccess.itemHandler(level, link))
                .orElse(null);

        if (handler == null) {
            golem.reportBlocker(GolemStatus.NO_DEPOSIT);
            golem.getBrain().setMemory(MemoryModuleTypeRegistry.DEPOSIT_COOLDOWN.value(), COOLDOWN_TICKS);
            return;
        }

        if (this.depositExcess(golem, handler) == 0) {
            golem.reportBlocker(GolemStatus.DEPOSIT_FULL);
            golem.getBrain().setMemory(MemoryModuleTypeRegistry.DEPOSIT_COOLDOWN.value(), COOLDOWN_TICKS);
        }
    }

    private boolean shouldDeposit(GolemEntity golem) {
        if (this.mode == Mode.WHEN_FULL && !golem.getInventory().isFull()) {
            return false;
        }

        return this.depositableCount(golem) > 0;
    }

    private int depositableCount(GolemEntity golem) {
        GolemInventory inventory = golem.getInventory();
        Map<Item, Integer> keepLeft = new HashMap<>();
        int total = 0;
        for (int slot = 0; slot < inventory.getUsableSlots(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            int left = keepLeft.computeIfAbsent(stack.getItem(), item -> this.job.keepAmount(golem, stack));
            int kept = Math.min(left, stack.getCount());
            keepLeft.put(stack.getItem(), left - kept);
            total += stack.getCount() - kept;
        }
        return total;
    }

    private int depositExcess(GolemEntity golem, ResourceHandler<ItemResource> handler) {
        GolemInventory inventory = golem.getInventory();
        Map<Item, Integer> keepLeft = new HashMap<>();
        int moved = 0;
        for (int slot = 0; slot < inventory.getUsableSlots(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            int left = keepLeft.computeIfAbsent(stack.getItem(), item -> this.job.keepAmount(golem, stack));
            int kept = Math.min(left, stack.getCount());
            keepLeft.put(stack.getItem(), left - kept);
            int excess = stack.getCount() - kept;
            if (excess <= 0) {
                continue;
            }

            try (Transaction transaction = Transaction.openRoot()) {
                int inserted = handler.insert(ItemResource.of(stack), excess, transaction);
                transaction.commit();
                stack.shrink(inserted);
                moved += inserted;
            }
        }

        inventory.setChanged();
        return moved;
    }
}
