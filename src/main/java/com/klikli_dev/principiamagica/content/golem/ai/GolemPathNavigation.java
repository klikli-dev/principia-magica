// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai;

import com.klikli_dev.principiamagica.content.golem.job.lumberjack.TreeDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Ground navigation that keeps golems off natural leaves. Vanilla treats a cell standing on leaves as open ground, so a
 * golem walking to a tree trunk can route over a leaf mound, or pick a leaf top as the closest point it can reach.
 * <p>
 * Standing on leaves is blocked while the golem is on the ground. A golem that is already on leaves may path over them at
 * a high cost, so it can always come back down. Blocks are not changed, only the path costs.
 */
public class GolemPathNavigation extends GroundPathNavigation {
    /**
     * The path type for a cell that stands on natural leaves. Golems never breach blocks, so this type is free for them.
     */
    public static final PathType ON_LEAVES = PathType.BREACH;
    public static final float ON_LEAVES_MALUS = 16.0F;

    public GolemPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new LeafAvoidingNodeEvaluator();
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    private static class LeafAvoidingNodeEvaluator extends WalkNodeEvaluator {
        private boolean standingOnLeaves;

        @Override
        public void prepare(PathNavigationRegion level, Mob entity) {
            super.prepare(level, entity);
            this.standingOnLeaves = isNaturalLeaf(level, entity.blockPosition().below());
        }

        @Override
        public PathType getPathType(PathfindingContext context, int x, int y, int z) {
            PathType type = super.getPathType(context, x, y, z);
            if ((type == PathType.OPEN || type == PathType.WALKABLE) && isNaturalLeaf(context.level(), new BlockPos(x, y - 1, z))) {
                return this.standingOnLeaves ? ON_LEAVES : PathType.BLOCKED;
            }

            return type;
        }

        private static boolean isNaturalLeaf(BlockGetter level, BlockPos pos) {
            return TreeDetector.isNaturalLeaf(level.getBlockState(pos));
        }
    }
}
