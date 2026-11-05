// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.CommonJobOptions;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * Picks up dropped items lying inside the workplace zones that fit into the golem's inventory.
 * Gated by the shared {@link CommonJobOptions#PICKUP_ITEMS} option.
 */
public class PickUpStrayItems extends MoveToAndInteract {
    private static final int COOLDOWN_TICKS = 100;

    private final GolemJob job;

    public PickUpStrayItems(GolemJob job) {
        super(Map.of(MemoryModuleTypeRegistry.PICKUP_COOLDOWN.value(), MemoryStatus.VALUE_ABSENT), 0.6F);
        this.job = job;
    }

    @Override
    protected Optional<BlockPos> findTarget(ServerLevel level, GolemEntity golem) {
        return golem.getWorkplace(level)
                .filter(workplace -> this.isEnabled(workplace))
                .flatMap(workplace -> workplace.zones().stream()
                        .flatMap(zone -> level.getEntitiesOfClass(ItemEntity.class, zone.aabb(), item -> canPickUp(golem, item.getItem())).stream())
                        .filter(item -> isAvailable(level, golem, item.blockPosition()))
                        .min(Comparator.comparingDouble(item -> item.distanceToSqr(golem)))
                        .map(ItemEntity::blockPosition));
    }

    @Override
    protected boolean isTargetValid(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return golem.getWorkplace(level)
                .filter(workplace -> this.isEnabled(workplace) && workplace.containsInZone(pos))
                .map(workplace -> !level.getEntitiesOfClass(ItemEntity.class, pickupArea(pos), item -> canPickUp(golem, item.getItem())).isEmpty())
                .orElse(false);
    }

    @Override
    protected void onNoTarget(ServerLevel level, GolemEntity golem) {
        golem.getBrain().setMemory(MemoryModuleTypeRegistry.PICKUP_COOLDOWN.value(), COOLDOWN_TICKS);
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos pos) {
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, pickupArea(pos), entity -> canPickUp(golem, entity.getItem()))) {
            ItemStack remaining = golem.getInventory().insert(item.getItem());
            if (remaining.isEmpty()) {
                item.discard();
            } else {
                item.setItem(remaining);
            }
        }
    }

    private boolean isEnabled(Workplace workplace) {
        return this.job.options().contains(CommonJobOptions.PICKUP_ITEMS)
                && workplace.options().get(CommonJobOptions.PICKUP_ITEMS)
                && !workplace.zones().isEmpty();
    }

    private static boolean canPickUp(GolemEntity golem, ItemStack stack) {
        return golem.getInventory().fitAmount(stack) > 0;
    }

    private static AABB pickupArea(BlockPos pos) {
        return new AABB(pos).inflate(1.0);
    }
}
