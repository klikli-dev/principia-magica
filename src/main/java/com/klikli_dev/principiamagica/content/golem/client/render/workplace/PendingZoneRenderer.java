// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The footprint that is being carved with a stylus: a magenta glyph on the first corner plus a ghost corner on the block under
 * the crosshair, lifted by the pending ghost lift. The box spans both corners and chases the ghost position, which is lerped between
 * client ticks, like theurgy's ChasingAABBOutline.
 */
public class PendingZoneRenderer {

    public static final int COLOR = 0xFF55FF;
    private static final float GHOST_ALPHA = 0.6F;

    @Nullable
    private AABB previous;
    @Nullable
    private AABB current;
    @Nullable
    private CarvedGlyph ghost;

    /**
     * @param pending the pending corner, or null if there is none
     * @param hovered the block face under the crosshair, or null if there is none
     */
    public void tick(@Nullable PendingGlyph pending, @Nullable BlockHitResult hovered) {
        if (pending == null) {
            return;
        }

        AABB target;
        if (hovered == null) {
            this.ghost = null;
            target = new AABB(pending.glyph().corner());
        } else {
            this.ghost = new CarvedGlyph(hovered.getBlockPos(), hovered.getDirection(), pending.ghostLift());
            target = WorkFootprint.of(pending.glyph(), this.ghost).zone(0, 0).aabb();
        }

        if (this.current == null) {
            this.current = target;
            this.previous = target;
            return;
        }

        this.previous = this.current;
        this.current = lerp(this.current, target, 0.5);
    }

    public void render(WorkplaceGeometry geometry, PendingGlyph pending, float alpha, float partialTick, double time) {
        float pulse = WorkplaceGeometry.pulse(time, 0.25, 0.4F);
        ZoneRenderer.renderGlyph(geometry, pending.glyph(), COLOR, alpha, time);

        if (this.ghost != null) {
            ZoneRenderer.renderGlyph(geometry, this.ghost, COLOR, alpha * GHOST_ALPHA, time);
        }

        if (this.previous == null || this.current == null) {
            return;
        }

        AABB box = lerp(this.previous, this.current, partialTick);
        geometry.boxFaces(box, WorkplaceGeometry.argb(COLOR, alpha * 0.06F));
        geometry.boxEdges(box.inflate(0.01), WorkplaceGeometry.argb(COLOR, alpha * 0.9F * pulse));
    }

    private static AABB lerp(AABB from, AABB to, double t) {
        return new AABB(
                Mth.lerp(t, from.minX, to.minX), Mth.lerp(t, from.minY, to.minY), Mth.lerp(t, from.minZ, to.minZ),
                Mth.lerp(t, from.maxX, to.maxX), Mth.lerp(t, from.maxY, to.maxY), Mth.lerp(t, from.maxZ, to.maxZ)
        );
    }
}
