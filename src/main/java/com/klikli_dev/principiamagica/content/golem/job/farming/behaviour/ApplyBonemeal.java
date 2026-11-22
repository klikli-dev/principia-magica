// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemBonemeal;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class ApplyBonemeal extends FarmingBehaviour {

    @Override
    protected boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return golem.getWorkplace(level)
                .map(workplace -> FarmingTargets.canBonemeal(level, golem, workplace, pos))
                .orElse(false);
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos pos) {
        GolemBonemeal.apply(level, golem, pos);
    }
}
