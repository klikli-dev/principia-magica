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
 * Base for job actions: picks the nearest available work candidate this action can act on.
 */
public abstract class JobTargetBehaviour extends MoveToAndInteract {

    protected JobTargetBehaviour(float speed) {
        super(Map.of(), speed);
    }

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
