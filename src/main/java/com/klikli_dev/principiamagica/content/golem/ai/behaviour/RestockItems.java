// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * Takes items the job needs (keepAmount above what the golem holds) from a SUPPLY-capable container.
 */
public class RestockItems extends MoveToAndInteract {
    private static final int COOLDOWN_TICKS = 100;

    private final GolemJob job;

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

    @Override
    protected void onNoTarget(ServerLevel level, GolemEntity golem) {
        golem.getBrain().setMemory(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value(), COOLDOWN_TICKS);
    }

    @Override
    protected boolean reserveTarget() {
        return false;
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
                        golem.getInventory().insert(resource.toStack(extracted));
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
        if (keep <= 0) {
            return 0;
        }

        int missing = keep - golem.getInventory().countItem(template.getItem());
        return Math.max(0, Math.min(missing, golem.getInventory().fitAmount(template)));
    }
}
