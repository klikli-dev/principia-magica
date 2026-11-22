// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour;

import com.klikli_dev.principiamagica.content.golem.ai.behaviour.MoveToAndInteract;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemBreaking;
import com.klikli_dev.principiamagica.content.golem.job.GolemDrops;
import com.klikli_dev.principiamagica.content.golem.job.JobTargetBehaviour;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.LumberjackTargets;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.TreeAccess;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.TreeDetector;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Carves a corridor through the natural leaves that block a stump, one leaf at a time, from the golem's side. The golem
 * stands on the planned stand cell while it breaks the leaf. Player-placed leaves are never broken.
 */
public class ClearTreeAccess extends JobTargetBehaviour {
    private static final float WORK_SPEED = 0.6F;
    private static final int MIN_CLEAR_TICKS = 10;

    private TreeAccess.@Nullable Plan plan;

    public ClearTreeAccess() {
        super(WORK_SPEED);
    }

    @Override
    protected Optional<BlockPos> findTarget(ServerLevel level, GolemEntity golem) {
        this.plan = null;
        List<BlockPos> candidates = golem.getBrain().getMemory(MemoryModuleTypeRegistry.WORK_CANDIDATES.value()).orElse(List.of());
        for (BlockPos stump : candidates) {
            if (!MoveToAndInteract.isAvailable(level, golem, stump)) {
                continue;
            }

            Optional<TreeAccess.Plan> found = this.carvePlan(level, golem, stump);
            if (found.isPresent()) {
                this.plan = found.get();
                return Optional.of(this.plan.leaf());
            }
        }

        return Optional.empty();
    }

    private Optional<TreeAccess.Plan> carvePlan(ServerLevel level, GolemEntity golem, BlockPos stump) {
        if (!LumberjackTargets.holdsAxe(golem) || !LumberjackTargets.clearsLeaves(level, golem) || !LumberjackTargets.isFellTarget(level, stump)) {
            return Optional.empty();
        }

        return TreeAccess.plan(level, stump, golem).filter(plan -> plan.kind() == TreeAccess.Kind.CARVE);
    }

    @Override
    protected boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return this.carvePlan(level, golem, pos).isPresent();
    }

    @Override
    protected boolean isTargetValid(ServerLevel level, GolemEntity golem, BlockPos leaf) {
        return LumberjackTargets.holdsAxe(golem)
                && LumberjackTargets.clearsLeaves(level, golem)
                && TreeDetector.isNaturalLeaf(level.getBlockState(leaf));
    }

    @Override
    protected BlockPos approachPos(ServerLevel level, GolemEntity golem, BlockPos leaf) {
        return this.plan != null && this.plan.leaf().equals(leaf) ? this.plan.stand() : leaf;
    }

    @Override
    protected @Nullable BlockPos breakingPos(ServerLevel level, GolemEntity golem, BlockPos leaf) {
        return leaf;
    }

    @Override
    protected int breakTicks(ServerLevel level, GolemEntity golem, BlockPos leaf) {
        return Math.max(MIN_CLEAR_TICKS, GolemBreaking.breakTicks(level, golem, leaf, level.getBlockState(leaf)));
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos leaf) {
        BlockState state = level.getBlockState(leaf);
        if (!TreeDetector.isNaturalLeaf(state)) {
            return;
        }

        List<ItemStack> drops = GolemDrops.breakBlock(level, golem, leaf, state);
        GolemDrops.collect(level, golem, leaf, drops);
    }
}
