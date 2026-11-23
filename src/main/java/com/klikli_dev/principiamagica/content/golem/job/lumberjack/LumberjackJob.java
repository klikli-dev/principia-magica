// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.lumberjack;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.ai.JobBehaviours;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.CommonJobOptions;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.WorkJob;
import com.klikli_dev.principiamagica.content.golem.job.WorkTargetFinder;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour.ApplyBonemealToSapling;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour.ClearTreeAccess;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour.FellTree;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour.PlantSapling;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;

import java.util.List;
import java.util.Optional;

/**
 * Fells natural trees with an axe and replants a sapling on each stump. Only trees whose leaves can still decay are felled,
 * so wood placed by players is never touched. Leaves are left to decay, saplings and sticks that fall into the zone are picked up.
 * The footprint only defines the trunk area: the tree is felled upwards from its stump, beyond the footprint.
 */
public final class LumberjackJob {
    public static final ResourceKey<GolemJob> KEY = ResourceKey.create(PrincipiaMagicaRegistries.Keys.GOLEM_JOB, PrincipiaMagica.loc("lumberjack"));

    /**
     * Stumps stand on the working level, the sapling spot is one block above it.
     */
    public static final int BELOW = 0;
    public static final int ABOVE = 1;

    private LumberjackJob() {
    }

    public static WorkJob create() {
        return new WorkJob(
                "lumberjack",
                BELOW,
                ABOVE,
                Optional.of(ItemTags.AXES),
                Optional.of(ItemTags.AXES),
                List.of(LumberjackTargets.REPLANT, LumberjackTargets.CLEAR_LEAVES, LumberjackTargets.BONEMEAL, CommonJobOptions.PICKUP_ITEMS),
                LumberjackTargets::keepsItem,
                LumberjackTargets::wantsSupply,
                new WorkTargetFinder(LumberjackTargets::isWorkTarget, LumberjackTargets::blockedReason),
                LumberjackJob::behaviours
        );
    }

    private static List<BehaviorControl<? super GolemEntity>> behaviours() {
        return List.of(JobBehaviours.gate(List.of(
                new FellTree(),
                new ClearTreeAccess(),
                new PlantSapling(),
                new ApplyBonemealToSapling()
        )));
    }
}
