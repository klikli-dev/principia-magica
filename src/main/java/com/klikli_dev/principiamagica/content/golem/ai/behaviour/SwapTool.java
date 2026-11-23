// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;

import java.util.Map;

/**
 * Takes the tool back from the golem's back into its hand once the hand is free, e.g. after the carried items were deposited.
 * Instant: it moves the tool and stops.
 */
public class SwapTool extends Behavior<GolemEntity> {

    public SwapTool() {
        super(Map.of(), 1);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GolemEntity golem) {
        return golem.getHandItem().isEmpty() && golem.isTool(golem.getBackItem());
    }

    @Override
    protected void start(ServerLevel level, GolemEntity golem, long timestamp) {
        golem.swapToolIntoHand();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, GolemEntity golem, long timestamp) {
        return false;
    }
}
