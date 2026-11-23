// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.ai.JobBehaviours;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.CommonJobOptions;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.WorkJob;
import com.klikli_dev.principiamagica.content.golem.job.WorkTargetFinder;
import com.klikli_dev.principiamagica.content.golem.job.farming.behaviour.ApplyBonemeal;
import com.klikli_dev.principiamagica.content.golem.job.farming.behaviour.ClearObstruction;
import com.klikli_dev.principiamagica.content.golem.job.farming.behaviour.HarvestCrop;
import com.klikli_dev.principiamagica.content.golem.job.farming.behaviour.PlantSeed;
import com.klikli_dev.principiamagica.content.golem.job.farming.behaviour.TillSoil;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;

import java.util.List;
import java.util.Optional;

/**
 * Harvests ripe crops, replants them, optionally applies bone meal and tills. The four actions run as one gate, so
 * only one of them is active at a time: harvesting wins over planting, planting over bone meal, and bone meal over tilling.
 */
public final class FarmingJob {
    public static final ResourceKey<GolemJob> KEY = ResourceKey.create(PrincipiaMagicaRegistries.Keys.GOLEM_JOB, PrincipiaMagica.loc("farming"));

    /**
     * The soil is the working level itself. Three blocks above it are the crop, a tall crop and a stacked plant on top of it.
     */
    public static final int BELOW = 0;
    public static final int ABOVE = 3;

    private FarmingJob() {
    }

    public static WorkJob create() {
        return new WorkJob(
                "farming",
                BELOW,
                ABOVE,
                Optional.empty(),
                Optional.of(ItemTags.HOES),
                List.of(FarmingTargets.REPLANT, FarmingTargets.CLEAR_OBSTRUCTIONS, FarmingTargets.TILL, FarmingTargets.BONEMEAL, CommonJobOptions.PICKUP_ITEMS),
                FarmingTargets::keepsItem,
                new WorkTargetFinder(FarmingTargets::isWorkTarget, FarmingTargets::blockedReason),
                FarmingJob::behaviours
        );
    }

    private static List<BehaviorControl<? super GolemEntity>> behaviours() {
        return List.of(JobBehaviours.gate(List.of(
                new HarvestCrop(),
                new ClearObstruction(),
                new PlantSeed(),
                new ApplyBonemeal(),
                new TillSoil()
        )));
    }
}
