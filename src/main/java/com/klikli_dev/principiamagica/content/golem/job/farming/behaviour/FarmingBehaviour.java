// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.behaviour;

import com.klikli_dev.principiamagica.content.golem.job.JobTargetBehaviour;

/**
 * Base for the farming actions: picks the nearest available candidate this action can act on.
 */
public abstract class FarmingBehaviour extends JobTargetBehaviour {
    private static final float WORK_SPEED = 0.6F;

    protected FarmingBehaviour() {
        super(WORK_SPEED);
    }
}
