// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkReservations;
import com.klikli_dev.principiamagica.registry.AttributeRegistry;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Walks to a target block, then performs one interaction once the work delay has elapsed.
 * <p>
 * Handles reservations (so golems do not share targets), unreachable tracking (failed paths are blacklisted
 * for a while), interaction range and the work speed delay. Subclasses provide the target search, the validity
 * check and the interaction itself. The behaviour ends after a single interaction.
 */
public abstract class MoveToAndInteract extends Behavior<GolemEntity> {
    public static final int UNREACHABLE_TICKS = 600;
    public static final int RESERVATION_TICKS = 200;
    private static final int MAX_TRAVEL_TICKS = 400;
    private static final int PATH_GRACE_TICKS = 20;
    private static final double HORIZONTAL_REACH_SQR = 2.25;
    private static final double VERTICAL_REACH = 2.5;
    private static final int BASE_WORK_TICKS = 20;

    protected final float speed;
    private @Nullable BlockPos target;
    private @Nullable BlockPos reservedPos;
    private @Nullable BlockPos interactionPos;
    private int travelTicks;
    private int workTicks;

    protected MoveToAndInteract(Map<MemoryModuleType<?>, MemoryStatus> conditions, float speed) {
        super(conditions, 60);
        this.speed = speed;
    }

    /**
     * @return a target that is currently valid and available, or empty if none is found
     */
    protected abstract Optional<BlockPos> findTarget(ServerLevel level, GolemEntity golem);

    protected abstract boolean isTargetValid(ServerLevel level, GolemEntity golem, BlockPos pos);

    /**
     * Performs the actual work at the target once the golem is in range and the work delay has elapsed.
     */
    protected abstract void interact(ServerLevel level, GolemEntity golem, BlockPos pos);

    protected void onNoTarget(ServerLevel level, GolemEntity golem) {
    }

    protected void startInteraction(ServerLevel level, GolemEntity golem, BlockPos pos) {
    }

    protected void stopInteraction(ServerLevel level, GolemEntity golem, BlockPos pos) {
    }

    /**
     * @return whether the target block should be reserved so other golems do not target it
     */
    protected boolean reserveTarget() {
        return true;
    }

    protected int baseWorkTicks() {
        return BASE_WORK_TICKS;
    }

    protected int workDelayTicks(GolemEntity golem) {
        double workSpeed = Math.max(0.1, golem.getAttributeValue(AttributeRegistry.GOLEM_WORK_SPEED));
        return Math.max(1, (int) Math.ceil(this.baseWorkTicks() / workSpeed));
    }

    /**
     * @return the first candidate from WORK_CANDIDATES (sorted nearest first) that is available to this golem.
     */
    protected static Optional<BlockPos> nearestWorkCandidate(ServerLevel level, GolemEntity golem) {
        List<BlockPos> candidates = golem.getBrain().getMemory(MemoryModuleTypeRegistry.WORK_CANDIDATES.value()).orElse(List.of());
        return candidates.stream().filter(pos -> isAvailable(level, golem, pos)).findFirst();
    }

    /**
     * @return true if the position is not blacklisted as unreachable for this golem and not reserved by another golem.
     */
    public static boolean isAvailable(ServerLevel level, GolemEntity golem, BlockPos pos) {
        Set<BlockPos> unreachable = golem.getBrain().getMemory(MemoryModuleTypeRegistry.UNREACHABLE_TARGETS.value()).orElse(Set.of());
        return !unreachable.contains(pos) && !WorkReservations.isReservedByOther(level, pos, golem.getUUID());
    }

    protected static void markUnreachable(GolemEntity golem, BlockPos pos) {
        Set<BlockPos> unreachable = new HashSet<>(golem.getBrain().getMemory(MemoryModuleTypeRegistry.UNREACHABLE_TARGETS.value()).orElse(Set.of()));
        unreachable.add(pos);
        golem.getBrain().setMemoryWithExpiry(MemoryModuleTypeRegistry.UNREACHABLE_TARGETS.value(), unreachable, UNREACHABLE_TICKS);
    }

    /**
     * Player-like reach: close horizontally and within reach above or below the golem's feet, so a golem on the ground
     * can harvest the top of a 3-tall plant.
     */
    public static boolean isInReach(GolemEntity golem, BlockPos pos) {
        double dx = pos.getX() + 0.5 - golem.getX();
        double dy = pos.getY() + 0.5 - golem.getY();
        double dz = pos.getZ() + 0.5 - golem.getZ();
        return dx * dx + dz * dz <= HORIZONTAL_REACH_SQR && Math.abs(dy) <= VERTICAL_REACH;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GolemEntity golem) {
        Optional<BlockPos> found = this.findTarget(level, golem);
        if (found.isEmpty()) {
            this.onNoTarget(level, golem);
            return false;
        }

        this.target = found.get();
        return true;
    }

    @Override
    protected void start(ServerLevel level, GolemEntity golem, long timestamp) {
        this.travelTicks = 0;
        this.workTicks = 0;
        this.interactionPos = null;
        if (this.target != null && this.reserveTarget() && WorkReservations.tryReserve(level, this.target, golem.getUUID(), RESERVATION_TICKS)) {
            this.reservedPos = this.target;
        }
    }

    @Override
    protected boolean timedOut(long timestamp) {
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, GolemEntity golem, long timestamp) {
        return this.target != null && this.isTargetValid(level, golem, this.target);
    }

    @Override
    protected void tick(ServerLevel level, GolemEntity golem, long timestamp) {
        BlockPos pos = this.target;
        if (pos == null) {
            return;
        }

        if (this.reservedPos != null) {
            WorkReservations.tryReserve(level, this.reservedPos, golem.getUUID(), RESERVATION_TICKS);
        }

        if (isInReach(golem, pos)) {
            if (this.interactionPos == null) {
                this.interactionPos = pos;
                golem.getNavigation().stop();
                golem.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                this.startInteraction(level, golem, pos);
            }

            golem.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
            this.workTicks++;
            if (this.workTicks >= this.workDelayTicks(golem)) {
                golem.swingWorkArm();
                this.interact(level, golem, pos);
                this.target = null;
            }
            return;
        }

        this.travelTicks++;
        if (this.travelTicks > MAX_TRAVEL_TICKS || (this.travelTicks > PATH_GRACE_TICKS && golem.getNavigation().isDone())) {
            markUnreachable(golem, pos);
            this.target = null;
            return;
        }

        BehaviorUtils.setWalkAndLookTargetMemories(golem, pos, this.speed, 1);
    }

    @Override
    protected void stop(ServerLevel level, GolemEntity golem, long timestamp) {
        if (this.interactionPos != null) {
            this.stopInteraction(level, golem, this.interactionPos);
        }

        if (this.reservedPos != null) {
            WorkReservations.release(level, this.reservedPos, golem.getUUID());
        }

        golem.getNavigation().stop();
        golem.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        this.target = null;
        this.reservedPos = null;
        this.interactionPos = null;
        this.travelTicks = 0;
        this.workTicks = 0;
    }
}
