// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

@FunctionalInterface
public interface WorkTargetFinder {

    /**
     * @return true if the block at pos is currently a valid work target for the golem's job.
     */
    boolean isTarget(ServerLevel level, GolemEntity golem, BlockPos pos);

    /**
     * @return the status that keeps the block from being a target, if the job would work on it otherwise (e.g. a missing
     * tool or missing supplies). Empty if there is nothing the golem could do about it.
     */
    default Optional<GolemStatus> blockedReason(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return Optional.empty();
    }
}
