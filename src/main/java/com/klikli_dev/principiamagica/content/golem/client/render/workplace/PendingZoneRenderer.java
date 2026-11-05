// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * The zone that is being created with a stylus: a magenta corner block plus a ghost box that chases the block under the
 * crosshair. The ghost box position is lerped between client ticks, like theurgy's ChasingAABBOutline.
 */
public class PendingZoneRenderer {

    public static final int COLOR = 0xFF55FF;

    @Nullable
    private AABB previous;
    @Nullable
    private AABB current;

    /**
     * @param corner  the pending corner, or null if there is none
     * @param hovered the block under the crosshair, or null if there is none
     */
    public void tick(@Nullable BlockPos corner, @Nullable BlockPos hovered) {
        if (corner == null) {
            return;
        }

        AABB target = hovered == null ? new AABB(corner) : WorkZone.of(corner, hovered).aabb();
        if (this.current == null) {
            this.current = target;
            this.previous = target;
            return;
        }

        this.previous = this.current;
        this.current = lerp(this.current, target, 0.5);
    }

    public void render(WorkplaceGeometry geometry, BlockPos corner, float alpha, float partialTick, double time) {
        float pulse = WorkplaceGeometry.pulse(time, 0.25, 0.4F);
        geometry.boxEdges(new AABB(corner).inflate(0.02), WorkplaceGeometry.argb(COLOR, alpha * pulse));

        if (this.previous == null || this.current == null) {
            return;
        }

        AABB ghost = lerp(this.previous, this.current, partialTick);
        geometry.boxFaces(ghost, WorkplaceGeometry.argb(COLOR, alpha * 0.06F));
        geometry.boxEdges(ghost.inflate(0.01), WorkplaceGeometry.argb(COLOR, alpha * 0.9F * pulse));
    }

    private static AABB lerp(AABB from, AABB to, double t) {
        return new AABB(
                Mth.lerp(t, from.minX, to.minX), Mth.lerp(t, from.minY, to.minY), Mth.lerp(t, from.minZ, to.minZ),
                Mth.lerp(t, from.maxX, to.maxX), Mth.lerp(t, from.maxY, to.maxY), Mth.lerp(t, from.maxZ, to.maxZ)
        );
    }
}
