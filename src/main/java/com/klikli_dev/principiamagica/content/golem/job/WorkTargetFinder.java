// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * Decides which blocks in a job's zones are work targets, and why a block that is not one is blocked.
 *
 * @param check         whether the block at a position is currently a work target for the golem's job
 * @param blockedCheck  the status that keeps the block from being a target if the job would work on it otherwise
 */
public record WorkTargetFinder(TargetCheck check, BlockedCheck blockedCheck) {

    public boolean isTarget(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return this.check.test(level, golem, pos);
    }

    /**
     * @return the status that keeps the block from being a target, e.g. a missing tool or missing supplies. Empty if
     * there is nothing the golem could do about it.
     */
    public Optional<GolemStatus> blockedReason(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return this.blockedCheck.find(level, golem, pos);
    }

    @FunctionalInterface
    public interface TargetCheck {
        boolean test(ServerLevel level, GolemEntity golem, BlockPos pos);
    }

    @FunctionalInterface
    public interface BlockedCheck {
        Optional<GolemStatus> find(ServerLevel level, GolemEntity golem, BlockPos pos);
    }
}
