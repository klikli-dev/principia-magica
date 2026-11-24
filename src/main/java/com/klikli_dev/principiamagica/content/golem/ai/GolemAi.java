// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai;

import com.google.common.collect.ImmutableList;
import com.klikli_dev.principiamagica.content.golem.ai.behaviour.ContainerAccess;
import com.klikli_dev.principiamagica.content.golem.ai.behaviour.DepositItems;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.entity.GolemTask;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
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
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GolemAi {
    private static final Set<MemoryModuleType<?>> MOVEMENT_MEMORIES = Set.of(MemoryModuleType.WALK_TARGET);

    private static final float SWIM_SPEED = 0.8F;
    private static final int LOOK_AROUND_MIN_TICKS = 45;
    private static final int LOOK_AROUND_MAX_TICKS = 90;

    private static final float IDLE_SPEED = 0.6F;
    private static final int ANCHOR_CLOSE_RANGE = 2;
    private static final int ANCHOR_MAX_RANGE = 8;
    private static final int STROLL_HORIZONTAL_RANGE = 4;
    private static final int STROLL_VERTICAL_RANGE = 2;
    private static final int STROLL_WEIGHT = 2;
    private static final int PAUSE_WEIGHT = 1;
    private static final int PAUSE_MIN_TICKS = 30;
    private static final int PAUSE_MAX_TICKS = 60;

    private static final float FOLLOW_SPEED = 1.0F;
    private static final int OWNER_CLOSE_RANGE = 2;
    private static final int OWNER_MAX_RANGE = 6;

    private static final float PLAYER_LOOK_RANGE = 6.0F;
    private static final UniformInt PLAYER_LOOK_INTERVAL_TICKS = UniformInt.of(40, 80);

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
            updateStatus(level, golem);
        }

        Brain<GolemEntity> brain = golem.getBrain();
        switch (golem.getMode()) {
            case WORK -> {
                // A golem that holds something it has to deposit, with no deposit container to go to, waits with the item instead of working.
                boolean waiting = golem.level() instanceof ServerLevel level && isWaitingForDeposit(level, golem);
                showTask(golem, waiting ? GolemTask.TaskType.WAIT : null);
                brain.setActiveActivityToFirstValid(waiting ? List.of(Activity.IDLE) : List.of(Activity.WORK, Activity.IDLE));
            }
            case FOLLOW -> {
                showTask(golem, GolemTask.TaskType.FOLLOW);
                brain.setActiveActivityToFirstValid(List.of(ActivityRegistry.FOLLOW.value(), Activity.IDLE));
            }
            case STAY -> {
                showTask(golem, GolemTask.TaskType.STAY);
                brain.setActiveActivityToFirstValid(List.of(ActivityRegistry.STAY.value(), Activity.IDLE));
            }
        }
    }

    /**
     * Shows a mode-level task. Behaviours set their own tasks while they run, so this only replaces the task of a golem that is not
     * running a behaviour: the given type, or idle if the golem is not in a mode task.
     */
    private static void showTask(GolemEntity golem, GolemTask.TaskType type) {
        GolemTask.TaskType current = golem.getTask().type();
        boolean modeTask = current == GolemTask.TaskType.WAIT || current == GolemTask.TaskType.FOLLOW || current == GolemTask.TaskType.STAY;
        if (type != null && current != type) {
            golem.setTask(GolemTask.of(type, Optional.empty()));
        } else if (type == null && modeTask) {
            golem.setTask(GolemTask.IDLE);
        }
    }

    private static void updateStatus(ServerLevel level, GolemEntity golem) {
        Optional<GolemJob> job = golem.getSpec().job().map(Holder::value);
        GolemStatus setupProblem = GolemStatus.OK;
        if (job.isEmpty()) {
            setupProblem = GolemStatus.NO_PURPOSE;
        } else if (golem.getWorkplace(level).map(workplace -> workplace.footprintsIn(level.dimension()).isEmpty()).orElse(true)) {
            setupProblem = GolemStatus.NO_ZONE;
        } else if (job.get().requiredTool().isPresent() && !golem.isTool(golem.getHandItem()) && !golem.isTool(golem.getBackItem())) {
            setupProblem = GolemStatus.MISSING_TOOL;
        } else if (isWaitingForDeposit(level, golem)) {
            setupProblem = GolemStatus.NO_DEPOSIT;
        }

        golem.setStatus(setupProblem != GolemStatus.OK ? setupProblem : golem.activeBlocker());
    }

    /**
     * @return true if the golem holds an item it has to deposit, but its workplace has no deposit container that is available.
     * A deposit link whose container is gone does not count, like having no deposit link at all.
     */
    public static boolean isWaitingForDeposit(ServerLevel level, GolemEntity golem) {
        return golem.getSpec().job().isPresent()
                && DepositItems.carriesExcess(golem)
                && golem.getWorkplace(level).map(workplace -> workplace.links().stream()
                .noneMatch(link -> link.role().canDeposit() && ContainerAccess.isUsable(level, link))).orElse(true);
    }

    private static List<ActivityData<GolemEntity>> createActivities(GolemEntity body) {
        List<ActivityData<GolemEntity>> activities = new ArrayList<>();

        activities.add(ActivityData.create(Activity.CORE, 0, ImmutableList.of(
                new Swim<>(SWIM_SPEED),
                new LookAtTargetSink(LOOK_AROUND_MIN_TICKS, LOOK_AROUND_MAX_TICKS),
                new MoveToTargetSink(),
                InteractWithDoor.create(),
                new CountDownCooldownTicks(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value()),
                new CountDownCooldownTicks(MemoryModuleTypeRegistry.DEPOSIT_COOLDOWN.value()),
                new CountDownCooldownTicks(MemoryModuleTypeRegistry.PICKUP_COOLDOWN.value())
        )));

        activities.add(activity(Activity.IDLE, Set.of(), List.of(
                StayCloseToTarget.create(GolemAi::anchorTracker, entity -> true, ANCHOR_CLOSE_RANGE, ANCHOR_MAX_RANGE, IDLE_SPEED),
                lookAtPlayers(),
                new RunOne<GolemEntity>(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), List.of(
                        Pair.of(RandomStroll.stroll(IDLE_SPEED, STROLL_HORIZONTAL_RANGE, STROLL_VERTICAL_RANGE), STROLL_WEIGHT),
                        Pair.of(new DoNothing(PAUSE_MIN_TICKS, PAUSE_MAX_TICKS), PAUSE_WEIGHT)
                ))
        )));

        activities.add(activity(ActivityRegistry.FOLLOW.value(), Set.of(), List.of(
                StayCloseToTarget.create(GolemAi::ownerTracker, entity -> true, OWNER_CLOSE_RANGE, OWNER_MAX_RANGE, FOLLOW_SPEED),
                lookAtPlayers()
        )));

        activities.add(activity(ActivityRegistry.STAY.value(), Set.of(), List.of(
                lookAtPlayers(),
                new DoNothing(PAUSE_MIN_TICKS, PAUSE_MAX_TICKS)
        )));

        body.getSpec().job().ifPresent(job -> activities.add(createWorkActivity(job.value())));

        return activities;
    }

    /**
     * Glances at nearby players now and then, in every activity.
     */
    private static BehaviorControl<? super GolemEntity> lookAtPlayers() {
        return SetEntityLookTargetSometimes.create(EntityType.PLAYER, PLAYER_LOOK_RANGE, PLAYER_LOOK_INTERVAL_TICKS);
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
