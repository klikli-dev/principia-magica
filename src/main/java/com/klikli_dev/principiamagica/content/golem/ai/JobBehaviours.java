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
        List<BehaviorControl<? super GolemEntity>> ordered = new ArrayList<>();
        ordered.add(new DepositItems(job, DepositItems.Mode.WHEN_FULL));
        ordered.add(new RestockItems(job));
        ordered.addAll(job.createJobBehaviours());
        ordered.add(new PickUpStrayItems(job));
        ordered.add(new DepositItems(job, DepositItems.Mode.LEFTOVER));

        return ImmutableList.of(Pair.of(0, gate(ordered)));
    }

    /**
     * Runs the behaviours one at a time, in the given order: the first one whose entry conditions pass runs until it
     * stops, then the next start is attempted from the beginning of the list.
     */
    public static GateBehavior<GolemEntity> gate(List<? extends BehaviorControl<? super GolemEntity>> behaviours) {
        List<Pair<? extends BehaviorControl<? super GolemEntity>, Integer>> weighted = new ArrayList<>();
        for (BehaviorControl<? super GolemEntity> behaviour : behaviours) {
            weighted.add(Pair.of(behaviour, 1));
        }

        return new GateBehavior<>(
                Map.of(),
                Set.of(),
                GateBehavior.OrderPolicy.ORDERED,
                GateBehavior.RunningPolicy.RUN_ONE,
                weighted
        );
    }
}
