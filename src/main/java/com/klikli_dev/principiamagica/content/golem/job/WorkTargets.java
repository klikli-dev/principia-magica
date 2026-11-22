// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

import com.klikli_dev.principiamagica.content.golem.ai.behaviour.MoveToAndInteract;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class WorkTargets {

    private WorkTargets() {
    }

    /**
     * @return the nearest work candidate that is available to the golem and passes the action predicate
     */
    public static Optional<BlockPos> nearestCandidate(ServerLevel level, GolemEntity golem, Predicate<BlockPos> action) {
        return golem.getBrain().getMemory(MemoryModuleTypeRegistry.WORK_CANDIDATES.value()).orElse(List.of()).stream()
                .filter(pos -> MoveToAndInteract.isAvailable(level, golem, pos))
                .filter(action)
                .findFirst();
    }
}
