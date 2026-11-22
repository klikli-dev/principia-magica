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
import net.minecraft.world.entity.ai.behavior.GateBehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Composes the WORK activity: shared item logistics around the job's own behaviours.
 * <p>
 * All work behaviours walk somewhere, so they must never run at the same time: the brain starts every behaviour whose
 * entry conditions pass, regardless of priority, and concurrent behaviours would fight over the walk target. They are
 * therefore wrapped in a single ordered gate that runs one behaviour at a time, trying them in the order below.
 */
public final class JobBehaviours {

    private JobBehaviours() {
    }

    public static ImmutableList<Pair<Integer, BehaviorControl<? super GolemEntity>>> compose(GolemJob job) {
        List<Pair<? extends BehaviorControl<? super GolemEntity>, Integer>> ordered = new ArrayList<>();
        ordered.add(Pair.of(new DepositItems(job, DepositItems.Mode.WHEN_FULL), 1));
        ordered.add(Pair.of(new RestockItems(job), 1));
        for (BehaviorControl<? super GolemEntity> behaviour : job.createJobBehaviours()) {
            ordered.add(Pair.of(behaviour, 1));
        }
        ordered.add(Pair.of(new PickUpStrayItems(job), 1));
        ordered.add(Pair.of(new DepositItems(job, DepositItems.Mode.LEFTOVER), 1));

        GateBehavior<GolemEntity> work = new GateBehavior<>(
                Map.of(),
                Set.of(),
                GateBehavior.OrderPolicy.ORDERED,
                GateBehavior.RunningPolicy.RUN_ONE,
                ordered
        );

        return ImmutableList.of(Pair.of(0, work));
    }
}
