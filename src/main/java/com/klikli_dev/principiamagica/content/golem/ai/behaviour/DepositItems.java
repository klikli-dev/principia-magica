// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemTask;

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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Returns what the golem carries in its hand to a DEPOSIT-capable container. Only the part the job does not keep is deposited:
 * seeds or bone meal the job keeps stay in the hand. A golem carrying harvest output goes here before it does anything else.
 */
public class DepositItems extends MoveToAndInteract {

    @Override
    protected GolemTask.TaskType taskType() {
        return GolemTask.TaskType.DEPOSIT;
    }
    private static final int COOLDOWN_TICKS = 100;
    /** After a switch the golem does not restock the item it just gave up, so the two cannot undo each other. */
    private static final int RESTOCK_AFTER_SWITCH_TICKS = 100;

    public enum Mode {
        /** Deposits what the job does not keep. Runs before any other work. */
        EXCESS,
        /** Gives up a kept supply when the job needs its tool and the tool is on the back. Runs only when no other work can run. */
        SWITCH
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
     * The amount of the golem's hand that is not kept, which is what gets deposited.
     */
    private record Excess(ItemStack stack, int amount) {
    }

    /**
     * @return true if the golem's hand holds an item the job does not keep, i.e. something that has to be deposited before other work
     */
    public static boolean carriesExcess(GolemEntity golem) {
        ItemStack hand = golem.getHandItem();
        if (hand.isEmpty() || golem.isTool(hand)) {
            return false;
        }

        return golem.getJob().map(job -> job.keepAmount(golem, hand) < hand.getCount()).orElse(false);
    }

    /**
     * @return true if the job needs its tool in the hand, which is on the back, while the hand holds something else, and there is work to do
     */
    static boolean needsToolSwitch(GolemEntity golem) {
        ItemStack hand = golem.getHandItem();
        return !hand.isEmpty()
                && !golem.isTool(hand)
                && golem.isTool(golem.getBackItem())
                && golem.getJob().map(job -> job.requiredTool().isPresent()).orElse(false);
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
        } else if (this.mode == Mode.SWITCH) {
            golem.getBrain().setMemory(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value(), RESTOCK_AFTER_SWITCH_TICKS);
        }
    }

    private boolean shouldDeposit(GolemEntity golem) {
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
     * @return the hand's stack with the amount of it this mode deposits, empty if there is nothing to deposit
     */
    private List<Excess> excess(GolemEntity golem) {
        ItemStack hand = golem.getHandItem();
        if (hand.isEmpty() || golem.isTool(hand)) {
            return List.of();
        }

        if (this.mode == Mode.SWITCH) {
            return needsToolSwitch(golem) ? List.of(new Excess(hand, hand.getCount())) : List.of();
        }

        int kept = Math.min(this.job.keepAmount(golem, hand), hand.getCount());
        int amount = hand.getCount() - kept;
        return amount > 0 ? List.of(new Excess(hand, amount)) : List.of();
    }

    private int depositExcess(GolemEntity golem, ResourceHandler<ItemResource> handler) {
        int moved = 0;
        for (Excess excess : this.excess(golem)) {
            try (Transaction transaction = Transaction.openRoot()) {
                int inserted = handler.insert(ItemResource.of(excess.stack()), excess.amount(), transaction);
                transaction.commit();
                excess.stack().shrink(inserted);
                golem.setHandItem(excess.stack());
                moved += inserted;
            }
        }

        return moved;
    }
}
