// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

import com.klikli_dev.principiamagica.content.golem.ai.behaviour.MoveToAndInteract;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.Optional;

/**
 * Base for job actions: picks the nearest available work candidate this action can act on, and walks to it at the work speed.
 */
public abstract class JobTargetBehaviour extends MoveToAndInteract {
    private static final float WORK_SPEED = 0.6F;

    protected JobTargetBehaviour() {
        super(Map.of(), WORK_SPEED);
    }

    /**
     * @return whether the action can act on the candidate at pos right now
     */
    protected abstract boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos);

    @Override
    protected Optional<BlockPos> findTarget(ServerLevel level, GolemEntity golem) {
        return WorkTargets.nearestCandidate(level, golem, pos -> this.canAct(level, golem, pos));
    }

    @Override
    protected boolean isTargetValid(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return this.canAct(level, golem, pos);
    }
}
