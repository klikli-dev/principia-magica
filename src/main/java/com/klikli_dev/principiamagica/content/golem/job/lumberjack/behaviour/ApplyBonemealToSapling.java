// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemBonemeal;
import com.klikli_dev.principiamagica.content.golem.job.JobTargetBehaviour;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.LumberjackTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Applies bone meal to a sapling in the work zone so it grows into a tree faster.
 */
public class ApplyBonemealToSapling extends JobTargetBehaviour {
    private static final float WORK_SPEED = 0.6F;

    public ApplyBonemealToSapling() {
        super(WORK_SPEED);
    }

    @Override
    protected boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return golem.getWorkplace(level)
                .map(workplace -> LumberjackTargets.canBonemeal(level, golem, workplace, pos))
                .orElse(false);
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos pos) {
        if (!this.canAct(level, golem, pos)) {
            return;
        }

        LumberjackTargets.recordBonemeal(golem, pos);
        GolemBonemeal.apply(level, golem, pos);
    }
}
