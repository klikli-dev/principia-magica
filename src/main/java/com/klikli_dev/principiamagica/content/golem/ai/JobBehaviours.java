// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai;

import com.google.common.collect.ImmutableList;
import com.klikli_dev.principiamagica.content.golem.ai.behaviour.DepositItems;
import com.klikli_dev.principiamagica.content.golem.ai.behaviour.PickUpStrayItems;
import com.klikli_dev.principiamagica.content.golem.ai.behaviour.RestockItems;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;

/**
 * Composes the WORK activity: shared item logistics around the job's own behaviours.
 * Lower priority numbers run first.
 */
public final class JobBehaviours {
    public static final int PRIORITY_DEPOSIT_WHEN_FULL = 0;
    public static final int PRIORITY_RESTOCK = 1;
    public static final int PRIORITY_JOB = 2;
    public static final int PRIORITY_PICKUP_STRAYS = 3;
    public static final int PRIORITY_DEPOSIT_LEFTOVER = 4;

    private JobBehaviours() {
    }

    public static ImmutableList<Pair<Integer, BehaviorControl<? super GolemEntity>>> compose(GolemJob job) {
        ImmutableList.Builder<Pair<Integer, BehaviorControl<? super GolemEntity>>> builder = ImmutableList.builder();
        builder.add(priority(PRIORITY_DEPOSIT_WHEN_FULL, new DepositItems(job, DepositItems.Mode.WHEN_FULL)));
        builder.add(priority(PRIORITY_RESTOCK, new RestockItems(job)));
        for (BehaviorControl<? super GolemEntity> behaviour : job.createJobBehaviours()) {
            builder.add(priority(PRIORITY_JOB, behaviour));
        }
        builder.add(priority(PRIORITY_PICKUP_STRAYS, new PickUpStrayItems(job)));
        builder.add(priority(PRIORITY_DEPOSIT_LEFTOVER, new DepositItems(job, DepositItems.Mode.LEFTOVER)));
        return builder.build();
    }

    private static Pair<Integer, BehaviorControl<? super GolemEntity>> priority(int priority, BehaviorControl<? super GolemEntity> behaviour) {
        return Pair.of(priority, behaviour);
    }
}
