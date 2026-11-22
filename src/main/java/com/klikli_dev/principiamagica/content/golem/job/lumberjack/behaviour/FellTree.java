// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.lumberjack.behaviour;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemBreaking;
import com.klikli_dev.principiamagica.content.golem.job.GolemDrops;
import com.klikli_dev.principiamagica.content.golem.job.JobTargetBehaviour;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.LumberjackTargets;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.TreeAccess;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.TreeDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Fells a natural tree from its stump, top down. Each log is broken and its drops go into the golem's inventory, and
 * the axe loses one durability per log. Leaves are left alone, they decay on their own.
 */
public class FellTree extends JobTargetBehaviour {
    private static final float WORK_SPEED = 0.6F;
    private static final int MIN_FELL_TICKS = 20;

    public FellTree() {
        super(WORK_SPEED);
    }

    @Override
    protected boolean canAct(ServerLevel level, GolemEntity golem, BlockPos pos) {
        if (!LumberjackTargets.holdsAxe(golem) || !LumberjackTargets.isFellTarget(level, pos)) {
            return false;
        }

        return !(LumberjackTargets.clearsLeaves(level, golem)
                && TreeAccess.plan(level, pos, golem).map(plan -> plan.kind() == TreeAccess.Kind.CARVE).orElse(false));
    }

    /**
     * The golem walks to the planned stand cell next to the stump, the stump itself is solid and the pathfinder may
     * otherwise end up on the leaves around it. Falls back to the stump if there is no plan.
     */
    @Override
    protected BlockPos approachPos(ServerLevel level, GolemEntity golem, BlockPos stump) {
        return TreeAccess.plan(level, stump, golem)
                .filter(plan -> plan.kind() == TreeAccess.Kind.APPROACH)
                .map(TreeAccess.Plan::stand)
                .orElse(stump);
    }


    @Override
    protected int baseWorkTicks() {
        return 40;
    }

    @Override
    protected @Nullable BlockPos breakingPos(ServerLevel level, GolemEntity golem, BlockPos stump) {
        return stump;
    }

    @Override
    protected int breakTicks(ServerLevel level, GolemEntity golem, BlockPos stump) {
        return TreeDetector.naturalTree(level, stump)
                .map(tree -> tree.logs().stream()
                        .mapToInt(log -> GolemBreaking.breakTicks(level, golem, log, level.getBlockState(log)))
                        .sum())
                .map(ticks -> Math.max(MIN_FELL_TICKS, ticks))
                .orElse(this.baseWorkTicks());
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos stump) {
        Optional<TreeDetector.Tree> found = TreeDetector.scan(level, stump);
        if (found.isEmpty()) {
            return;
        }

        TreeDetector.Tree tree = found.get();
        LumberjackTargets.rememberReplants(level, golem, tree);
        for (BlockPos log : tree.logs()) {
            BlockState state = level.getBlockState(log);
            if (!TreeDetector.isLog(state)) {
                continue;
            }

            List<ItemStack> drops = GolemDrops.breakBlock(level, golem, log, state);
            GolemDrops.collect(level, golem, log, drops);
            golem.getMainHandItem().hurtAndBreak(1, golem, EquipmentSlot.MAINHAND);
        }

        TreeDetector.invalidate(level, tree);
    }
}
