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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    /**
     * A stack of the golem's inventory and the amount of it that is not kept, which is what gets deposited.
     */
    private record Excess(ItemStack stack, int amount) {
    }

    @Override
    protected Optional<BlockPos> findTarget(ServerLevel level, GolemEntity golem) {
        if (!this.shouldDeposit(golem)) {
            return Optional.empty();
        }

        return golem.getWorkplace(level).flatMap(workplace -> nearestDepositLink(level, golem, workplace));
    }

    /**
     * Only chests that can take at least one of the excess items are candidates. A full chest is skipped, so the golem
     * does not keep walking back to it.
     */
    private Optional<BlockPos> nearestDepositLink(ServerLevel level, GolemEntity golem, Workplace workplace) {
        return workplace.links().stream()
                .filter(link -> link.role().canDeposit())
                .map(ContainerLink::pos)
                .distinct()
                .filter(pos -> isAvailable(level, golem, pos))
                .filter(pos -> this.canAcceptExcess(level, golem, workplace, pos))
                .min(Comparator.comparingDouble(pos -> pos.distSqr(golem.blockPosition())));
    }

    @Override
    protected boolean isTargetValid(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return this.shouldDeposit(golem) && golem.getWorkplace(level)
                .map(workplace -> this.canAcceptExcess(level, golem, workplace, pos))
                .orElse(false);
    }

    @Override
    protected void onNoTarget(ServerLevel level, GolemEntity golem) {
        if (this.shouldDeposit(golem)) {
            // A chest that exists but cannot take the items is full, which is more useful to report than a missing chest.
            golem.reportBlocker(this.hasDepositChest(level, golem) ? GolemStatus.DEPOSIT_FULL : GolemStatus.NO_DEPOSIT);
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

    private boolean hasDepositChest(ServerLevel level, GolemEntity golem) {
        return golem.getWorkplace(level).map(workplace -> workplace.links().stream()
                .anyMatch(link -> link.role().canDeposit() && ContainerAccess.itemHandler(level, link) != null)).orElse(false);
    }

    /**
     * @return true if the deposit link at pos can take at least one of the excess items. The insert is simulated, nothing moves.
     */
    private boolean canAcceptExcess(ServerLevel level, GolemEntity golem, Workplace workplace, BlockPos pos) {
        ResourceHandler<ItemResource> handler = ContainerAccess.linkAt(workplace, pos, ContainerRole::canDeposit)
                .map(link -> ContainerAccess.itemHandler(level, link))
                .orElse(null);
        if (handler == null) {
            return false;
        }

        for (Excess excess : this.excess(golem)) {
            // Closing a transaction without committing it aborts the insert.
            try (Transaction transaction = Transaction.openRoot()) {
                if (handler.insert(ItemResource.of(excess.stack()), excess.amount(), transaction) > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private int depositableCount(GolemEntity golem) {
        return this.excess(golem).stream().mapToInt(Excess::amount).sum();
    }

    /**
     * @return the usable stacks with the amount of each that is not kept. The job keeps keepAmount of each item type across
     * all stacks, in slot order.
     */
    private List<Excess> excess(GolemEntity golem) {
        GolemInventory inventory = golem.getInventory();
        Map<Item, Integer> keepLeft = new HashMap<>();
        List<Excess> result = new ArrayList<>();
        for (int slot = 0; slot < inventory.getUsableSlots(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            int left = keepLeft.computeIfAbsent(stack.getItem(), item -> this.job.keepAmount(golem, stack));
            int kept = Math.min(left, stack.getCount());
            keepLeft.put(stack.getItem(), left - kept);
            int amount = stack.getCount() - kept;
            if (amount > 0) {
                result.add(new Excess(stack, amount));
            }
        }
        return result;
    }

    private int depositExcess(GolemEntity golem, ResourceHandler<ItemResource> handler) {
        int moved = 0;
        for (Excess excess : this.excess(golem)) {
            try (Transaction transaction = Transaction.openRoot()) {
                int inserted = handler.insert(ItemResource.of(excess.stack()), excess.amount(), transaction);
                transaction.commit();
                excess.stack().shrink(inserted);
                moved += inserted;
            }
        }

        golem.getInventory().setChanged();
        return moved;
    }
}
