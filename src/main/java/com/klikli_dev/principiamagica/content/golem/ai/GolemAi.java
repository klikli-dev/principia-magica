// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai;

import com.google.common.collect.ImmutableList;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkplaceDefaults;
import com.klikli_dev.principiamagica.registry.ActivityRegistry;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import com.klikli_dev.principiamagica.registry.SensorTypeRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.StayCloseToTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GolemAi {
    private static final Set<MemoryModuleType<?>> MOVEMENT_MEMORIES = Set.of(MemoryModuleType.WALK_TARGET);

    @SuppressWarnings("deprecation")
    public static Brain<GolemEntity> createBrain(GolemEntity body, Brain.Packed packed) {
        return Brain.provider(
                List.of(
                        MemoryModuleTypeRegistry.OWNER.value(),
                        MemoryModuleTypeRegistry.ANCHOR.value(),
                        MemoryModuleTypeRegistry.WORK_CANDIDATES.value(),
                        MemoryModuleTypeRegistry.WORK_TARGET.value(),
                        MemoryModuleTypeRegistry.UNREACHABLE_TARGETS.value(),
                        MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value(),
                        MemoryModuleTypeRegistry.DEPOSIT_COOLDOWN.value(),
                        MemoryModuleTypeRegistry.PICKUP_COOLDOWN.value(),
                        MemoryModuleTypeRegistry.SAPLING_BONEMEAL_ATTEMPTS.value()
                ),
                List.of(
                        SensorType.NEAREST_LIVING_ENTITIES,
                        SensorType.HURT_BY,
                        SensorType.NEAREST_ITEMS,
                        SensorTypeRegistry.WORK_TARGETS.value()
                ),
                GolemAi::createActivities
        ).makeBrain(body, packed);
    }

    public static void updateActivity(GolemEntity golem) {
        if (golem.level() instanceof ServerLevel level) {
            WorkplaceDefaults.initializeIfNeeded(golem, level);
            updateStatus(level, golem);
        }

        Brain<GolemEntity> brain = golem.getBrain();
        switch (golem.getMode()) {
            case WORK -> brain.setActiveActivityToFirstValid(List.of(Activity.WORK, Activity.IDLE));
            case FOLLOW -> brain.setActiveActivityToFirstValid(List.of(ActivityRegistry.FOLLOW.value(), Activity.IDLE));
            case STAY -> brain.setActiveActivityToFirstValid(List.of(ActivityRegistry.STAY.value(), Activity.IDLE));
        }
    }

    private static void updateStatus(ServerLevel level, GolemEntity golem) {
        Optional<GolemJob> job = golem.getSpec().job().map(Holder::value);
        GolemStatus setupProblem = GolemStatus.OK;
        if (job.isEmpty()) {
            setupProblem = GolemStatus.NO_JOB;
        } else if (golem.getWorkplace(level).map(workplace -> workplace.zones().isEmpty()).orElse(true)) {
            setupProblem = GolemStatus.NO_ZONE;
        } else if (job.get().requiredTool().isPresent() && !golem.getMainHandItem().is(job.get().requiredTool().get())) {
            setupProblem = GolemStatus.MISSING_TOOL;
        }

        golem.setStatus(setupProblem != GolemStatus.OK ? setupProblem : golem.activeBlocker());
    }

    private static List<ActivityData<GolemEntity>> createActivities(GolemEntity body) {
        List<ActivityData<GolemEntity>> activities = new ArrayList<>();

        activities.add(ActivityData.create(Activity.CORE, 0, ImmutableList.of(
                new Swim<>(0.8F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                InteractWithDoor.create(),
                new CountDownCooldownTicks(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value()),
                new CountDownCooldownTicks(MemoryModuleTypeRegistry.DEPOSIT_COOLDOWN.value()),
                new CountDownCooldownTicks(MemoryModuleTypeRegistry.PICKUP_COOLDOWN.value())
        )));

        activities.add(activity(Activity.IDLE, Set.of(), List.of(
                StayCloseToTarget.create(GolemAi::anchorTracker, entity -> true, 2, 8, 0.6F),
                SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(40, 80)),
                new RunOne<GolemEntity>(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), List.of(
                        Pair.of(RandomStroll.stroll(0.6F, 4, 2), 2),
                        Pair.of(new DoNothing(30, 60), 1)
                ))
        )));

        activities.add(activity(ActivityRegistry.FOLLOW.value(), Set.of(), List.of(
                StayCloseToTarget.create(GolemAi::ownerTracker, entity -> true, 2, 6, 1.0F),
                SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(40, 80))
        )));

        activities.add(activity(ActivityRegistry.STAY.value(), Set.of(), List.of(
                SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(40, 80)),
                new DoNothing(30, 60)
        )));

        body.getSpec().job().ifPresent(job -> activities.add(createWorkActivity(job.value())));

        return activities;
    }

    private static ActivityData<GolemEntity> createWorkActivity(GolemJob job) {
        return ActivityData.create(
                Activity.WORK,
                job.createWorkBehaviours(),
                Set.of(requires(MemoryModuleTypeRegistry.ANCHOR.value(), MemoryStatus.VALUE_PRESENT)),
                MOVEMENT_MEMORIES
        );
    }

    private static ActivityData<GolemEntity> activity(Activity activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>> conditions, List<BehaviorControl<? super GolemEntity>> behaviours) {
        return ActivityData.create(activity, ActivityData.<GolemEntity>createPriorityPairs(0, ImmutableList.copyOf(behaviours)), conditions, MOVEMENT_MEMORIES);
    }

    private static Pair<MemoryModuleType<?>, MemoryStatus> requires(MemoryModuleType<?> memory, MemoryStatus status) {
        return Pair.of(memory, status);
    }

    private static Optional<PositionTracker> anchorTracker(LivingEntity body) {
        return body.getBrain().getMemory(MemoryModuleTypeRegistry.ANCHOR.value()).map(anchor -> new BlockPosTracker(anchor.pos()));
    }

    private static Optional<PositionTracker> ownerTracker(LivingEntity body) {
        if (body instanceof GolemEntity golem && golem.level() instanceof ServerLevel level) {
            return golem.getOwner(level).map(owner -> new EntityTracker(owner, true));
        }

        return Optional.empty();
    }
}
