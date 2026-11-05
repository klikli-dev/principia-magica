// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * The footprint that is being carved with a stylus: a magenta glyph decal on the first corner plus a flat ghost of the
 * footprint that chases the block under the crosshair. The ghost position is lerped between client ticks, like theurgy's ChasingAABBOutline.
 */
public class PendingZoneRenderer {

    public static final int COLOR = 0xFF55FF;

    @Nullable
    private AABB previous;
    @Nullable
    private AABB current;

    /**
     * @param corner  the block of the pending glyph, or null if there is none
     * @param hovered the block under the crosshair, or null if there is none
     */
    public void tick(@Nullable BlockPos corner, @Nullable BlockPos hovered) {
        if (corner == null) {
            return;
        }

        AABB target = hovered == null
                ? new AABB(corner)
                : WorkFootprint.of(new CarvedGlyph(corner, Direction.UP), new CarvedGlyph(hovered, Direction.UP)).zone(0, 0).aabb();
        if (this.current == null) {
            this.current = target;
            this.previous = target;
            return;
        }

        this.previous = this.current;
        this.current = lerp(this.current, target, 0.5);
    }

    public void render(WorkplaceGeometry geometry, CarvedGlyph corner, float alpha, float partialTick, double time) {
        float pulse = WorkplaceGeometry.pulse(time, 0.25, 0.4F);
        ZoneRenderer.renderGlyph(geometry, corner, COLOR, alpha, time);

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
