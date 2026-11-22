// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.job.CommonJobOptions;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.JobOption;
import com.klikli_dev.principiamagica.content.golem.job.WorkTargetFinder;
import com.klikli_dev.principiamagica.content.golem.job.farming.behaviour.ApplyBonemeal;
import com.klikli_dev.principiamagica.content.golem.job.farming.behaviour.ClearObstruction;
import com.klikli_dev.principiamagica.content.golem.job.farming.behaviour.HarvestCrop;
import com.klikli_dev.principiamagica.content.golem.job.farming.behaviour.PlantSeed;
import com.klikli_dev.principiamagica.content.golem.job.farming.behaviour.TillSoil;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Harvests ripe crops, replants them, optionally applies bone meal and tills. The four actions run as one gate, so
 * only one of them is active at a time: harvesting wins over planting, planting over bone meal, and bone meal over tilling.
 */
public class FarmingJob implements GolemJob {
    public static final ResourceKey<GolemJob> KEY = ResourceKey.create(PrincipiaMagicaRegistries.Keys.GOLEM_JOB, PrincipiaMagica.loc("farming"));

    /**
     * The soil is the working level itself. Three blocks above it are the crop, a tall crop and a stacked plant on top of it.
     */
    public static final int BELOW = 0;
    public static final int ABOVE = 3;

    private static final WorkTargetFinder TARGET_FINDER = new WorkTargetFinder() {
        @Override
        public boolean isTarget(ServerLevel level, GolemEntity golem, BlockPos pos) {
            return FarmingTargets.isWorkTarget(level, golem, pos);
        }

        @Override
        public Optional<GolemStatus> blockedReason(ServerLevel level, GolemEntity golem, BlockPos pos) {
            return FarmingTargets.blockedReason(level, golem, pos);
        }
    };

    @Override
    public String getDescriptionId() {
        return "golem_job." + PrincipiaMagica.MODID + ".farming";
    }

    @Override
    public List<JobOption> options() {
        return List.of(FarmingTargets.REPLANT, FarmingTargets.CLEAR_OBSTRUCTIONS, FarmingTargets.TILL, FarmingTargets.BONEMEAL, CommonJobOptions.PICKUP_ITEMS);
    }

    @Override
    public int below() {
        return BELOW;
    }

    @Override
    public int above() {
        return ABOVE;
    }

    @Override
    public int keepAmount(GolemEntity golem, ItemStack stack) {
        if (!(golem.level() instanceof ServerLevel level)) {
            return 0;
        }

        return golem.getWorkplace(level)
                .filter(workplace -> FarmingTargets.keepsItem(workplace, stack))
                .map(workplace -> FarmingTargets.KEEP_AMOUNT)
                .orElse(0);
    }

    @Override
    public WorkTargetFinder targetFinder() {
        return TARGET_FINDER;
    }

    @Override
    public List<BehaviorControl<? super GolemEntity>> createJobBehaviours() {
        GateBehavior<GolemEntity> actions = new GateBehavior<>(
                Map.of(),
                Set.of(),
                GateBehavior.OrderPolicy.ORDERED,
                GateBehavior.RunningPolicy.RUN_ONE,
                List.of(
                        Pair.of(new HarvestCrop(), 1),
                        Pair.of(new ClearObstruction(), 1),
                        Pair.of(new PlantSeed(), 1),
                        Pair.of(new ApplyBonemeal(), 1),
                        Pair.of(new TillSoil(), 1)
                )
        );

        return List.of(actions);
    }
}
