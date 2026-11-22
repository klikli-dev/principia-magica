// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.lumberjack;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.job.CommonJobOptions;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.JobOption;
import com.klikli_dev.principiamagica.content.golem.job.WorkTargetFinder;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour.ApplyBonemealToSapling;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour.ClearTreeAccess;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour.FellTree;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour.PlantSapling;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Fells natural trees with an axe and replants a sapling on each stump. Only trees whose leaves can still decay are felled,
 * so wood placed by players is never touched. Leaves are left to decay, saplings and sticks that fall into the zone are picked up.
 * The footprint only defines the trunk area: the tree is felled upwards from its stump, beyond the footprint.
 */
public class LumberjackJob implements GolemJob {
    public static final ResourceKey<GolemJob> KEY = ResourceKey.create(PrincipiaMagicaRegistries.Keys.GOLEM_JOB, PrincipiaMagica.loc("lumberjack"));

    /**
     * Stumps stand on the working level, the sapling spot is one block above it.
     */
    public static final int BELOW = 0;
    public static final int ABOVE = 1;

    private static final WorkTargetFinder TARGET_FINDER = new WorkTargetFinder() {
        @Override
        public boolean isTarget(ServerLevel level, GolemEntity golem, BlockPos pos) {
            return LumberjackTargets.isWorkTarget(level, golem, pos);
        }

        @Override
        public Optional<GolemStatus> blockedReason(ServerLevel level, GolemEntity golem, BlockPos pos) {
            return LumberjackTargets.blockedReason(level, golem, pos);
        }
    };

    @Override
    public String getDescriptionId() {
        return "golem_job." + PrincipiaMagica.MODID + ".lumberjack";
    }

    @Override
    public List<JobOption> options() {
        return List.of(LumberjackTargets.REPLANT, LumberjackTargets.CLEAR_LEAVES, LumberjackTargets.BONEMEAL, CommonJobOptions.PICKUP_ITEMS);
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
    public Optional<TagKey<Item>> requiredTool() {
        return Optional.of(ItemTags.AXES);
    }

    @Override
    public int keepAmount(GolemEntity golem, ItemStack stack) {
        if (!(golem.level() instanceof ServerLevel level)) {
            return 0;
        }

        return golem.getWorkplace(level)
                .filter(workplace -> LumberjackTargets.keepsItem(workplace, stack))
                .map(workplace -> LumberjackTargets.KEEP_AMOUNT)
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
                        Pair.of(new FellTree(), 1),
                        Pair.of(new ClearTreeAccess(), 1),
                        Pair.of(new PlantSapling(), 1),
                        Pair.of(new ApplyBonemealToSapling(), 1)
                )
        );

        return List.of(actions);
    }
}
