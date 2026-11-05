// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.behaviour;

import com.klikli_dev.principiamagica.content.golem.ai.behaviour.MoveToAndInteract;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.Optional;

/**
 * Base for the farming actions: picks the nearest available candidate this action can act on.
 */
public abstract class FarmingBehaviour extends MoveToAndInteract {
    private static final float WORK_SPEED = 0.6F;

    protected FarmingBehaviour() {
        super(Map.of(), WORK_SPEED);
    }

    protected abstract boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos);

    @Override
    protected Optional<BlockPos> findTarget(ServerLevel level, GolemEntity golem) {
        return FarmingTargets.nearestCandidate(level, golem, pos -> this.canAct(level, golem, pos));
    }

    @Override
    protected boolean isTargetValid(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return this.canAct(level, golem, pos);
    }
}
