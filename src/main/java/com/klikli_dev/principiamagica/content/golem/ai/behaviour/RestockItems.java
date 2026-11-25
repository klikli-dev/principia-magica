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
import java.util.Map;
import java.util.Optional;

/**
 * Takes items the job needs (keepAmount above what the golem holds) from a SUPPLY-capable container.
 */
public class RestockItems extends MoveToAndInteract {

    @Override
    protected GolemTask.TaskType taskType() {
        return GolemTask.TaskType.RESTOCK;
    }

    @Override
    protected ItemStack displayItem(ServerLevel level, GolemEntity golem, BlockPos target) {
        return blockItem(level, target);
    }
    private static final int COOLDOWN_TICKS = 100;

    private final GolemJob job;
    private @Nullable Container openContainer;

    public RestockItems(GolemJob job) {
        super(Map.of(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value(), MemoryStatus.VALUE_ABSENT), 0.6F);
        this.job = job;
    }

    @Override
    protected Optional<BlockPos> findTarget(ServerLevel level, GolemEntity golem) {
        return golem.getWorkplace(level).flatMap(workplace -> workplace.links().stream()
                .filter(link -> link.role().canSupply())
                .map(ContainerLink::pos)
                .distinct()
                .filter(pos -> isAvailable(level, golem, pos))
                .filter(pos -> this.hasNeededItems(level, golem, workplace, pos))
                .min(Comparator.comparingDouble(pos -> pos.distSqr(golem.blockPosition()))));
    }

    private boolean hasNeededItems(ServerLevel level, GolemEntity golem, Workplace workplace, BlockPos pos) {
        return ContainerAccess.linkAt(workplace, pos, ContainerRole::canSupply)
                .map(link -> ContainerAccess.itemHandler(level, link))
                .map(handler -> {
                    for (int slot = 0; slot < handler.size(); slot++) {
                        ItemResource resource = handler.getResource(slot);
                        if (!resource.isEmpty() && this.neededAmount(golem, resource.toStack()) > 0) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    @Override
    protected boolean isTargetValid(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return golem.getWorkplace(level)
                .flatMap(workplace -> ContainerAccess.linkAt(workplace, pos, ContainerRole::canSupply))
                .map(link -> ContainerAccess.itemHandler(level, link))
                .map(handler -> true)
                .orElse(false);
    }

    /**
     * The cooldown only applies while the hand is free (or holding a tool that can be stowed). A busy hand is checked cheaply every time,
     * and a cooldown set now would otherwise still run when the hand becomes free, e.g. right after the golem deposited its harvest.
     */
    @Override
    protected void onNoTarget(ServerLevel level, GolemEntity golem) {
        if (golem.getHandItem().isEmpty() || canStowTool(level, golem, this.job)) {
            golem.getBrain().setMemory(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value(), COOLDOWN_TICKS);
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
                .flatMap(workplace -> ContainerAccess.linkAt(workplace, pos, ContainerRole::canSupply))
                .map(link -> ContainerAccess.itemHandler(level, link))
                .orElse(null);

        int moved = 0;
        if (handler != null) {
            for (int slot = 0; slot < handler.size(); slot++) {
                ItemResource resource = handler.getResource(slot);
                if (resource.isEmpty()) {
                    continue;
                }

                int needed = this.neededAmount(golem, resource.toStack());
                if (needed <= 0) {
                    continue;
                }

                int amount = Math.min(needed, handler.getAmountAsInt(slot));
                try (Transaction transaction = Transaction.openRoot()) {
                    int extracted = handler.extract(slot, resource, amount, transaction);
                    if (extracted > 0) {
                        golem.insertCarried(resource.toStack(extracted));
                        transaction.commit();
                        moved += extracted;
                    }
                }
            }
        }

        if (moved == 0) {
            golem.reportBlocker(GolemStatus.NEEDS_SUPPLIES);
            golem.getBrain().setMemory(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value(), COOLDOWN_TICKS);
        }
    }

    private int neededAmount(GolemEntity golem, ItemStack template) {
        int keep = this.job.keepAmount(golem, template);
        if (keep <= 0 || !(golem.level() instanceof ServerLevel level)) {
            return 0;
        }

        // Supplies go into a free hand, onto the same supply, or into the hand after stowing the job's tool on an empty back once no
        // tool work is waiting. The job must also have a target for the item, otherwise the golem would fetch it, find nothing to do with it
        // and put it away again.
        ItemStack hand = golem.getHandItem();
        if (!hand.isEmpty() && !ItemStack.isSameItemSameComponents(hand, template) && !canStowTool(level, golem, this.job)) {
            return 0;
        }

        if (golem.getWorkplace(level).filter(workplace -> this.job.wantsSupply(level, golem, workplace, template)).isEmpty()) {
            return 0;
        }


        int missing = keep - golem.countCarried(template.getItem());
        return Math.max(0, Math.min(missing, golem.carryFitAmount(template)));
    }
}
