// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ZoneRenderer {

    public static final int COLOR = 0x55FFFF;
    public static final float GLYPH_SIZE = 0.8F;
    private static final float CARVED_MARK_SIZE = 0.4F;
    private static final float REACH_ALPHA = 0.3F;
    private static final float PLUMB_ALPHA = 0.6F;
    private static final float CARVED_MARK_ALPHA = 0.5F;
    private static final double PLUMB_OFFSET = 0.5 + 0.01;

    private ZoneRenderer() {
    }

    /**
     * Draws a footprint as its solid box, the layer or volume it covers, with the job's reach around it as a faint
     * outline. The corners of the solid box are marked.
     */
    public static void render(WorkplaceGeometry geometry, WorkFootprint footprint, int below, int above, float alpha, double time) {
        AABB box = footprint.zone(0, 0).aabb().inflate(0.01);
        float pulse = WorkplaceGeometry.pulse(time, 0.12, 0.7F);

        geometry.boxFaces(box, WorkplaceGeometry.argb(COLOR, alpha * 0.06F * pulse));
        geometry.boxEdges(box, WorkplaceGeometry.argb(COLOR, alpha * pulse));

        if (below != 0 || above != 0) {
            AABB reach = footprint.zone(below, above).aabb().inflate(0.01);
            geometry.boxEdges(reach, WorkplaceGeometry.argb(COLOR, alpha * REACH_ALPHA * pulse));
        }

        int corner = WorkplaceGeometry.argb(COLOR, alpha);
        for (int i = 0; i < 8; i++) {
            geometry.glyph(WorkplaceGeometry.boxCorner(box, i), 0.3F, WorkplaceGeometry.Glyph.PLUS, corner);
        }
    }

    /**
     * A carved glyph decal on the block face it was carved into. Glows, so it reads while the stylus is held.
     * A lifted glyph floats at its corner, joined to its carved block by a plumb line.
     */
    public static void renderGlyph(WorkplaceGeometry geometry, CarvedGlyph glyph, int color, float alpha, double time) {
        renderGlyph(geometry, glyph, color, GLYPH_SIZE, alpha, time);
    }

    /**
     * Same as {@link #renderGlyph(WorkplaceGeometry, CarvedGlyph, int, float, double)} with a custom decal size.
     */
    public static void renderGlyph(WorkplaceGeometry geometry, CarvedGlyph glyph, int color, float size, float alpha, double time) {
        float pulse = WorkplaceGeometry.pulse(time, 0.12, 0.7F);
        geometry.decal(glyph.corner(), glyph.face(), size, WorkplaceGeometry.argb(color, alpha * pulse));
        renderPlumbLine(geometry, glyph.pos(), glyph.face(), glyph.lift(), color, alpha);
    }

    /**
     * A thin dashed line on the face of a carved block, from the block up to the lifted corner, with a faint mark on the carved block.
     * Draws nothing for an unlifted corner.
     */
    public static void renderPlumbLine(WorkplaceGeometry geometry, BlockPos pos, Direction face, int lift, int color, float alpha) {
        if (lift == 0) {
            return;
        }

        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        Vec3 base = Vec3.atCenterOf(pos).add(normal.scale(PLUMB_OFFSET));
        Vec3 top = Vec3.atCenterOf(pos.offset(0, lift, 0)).add(normal.scale(PLUMB_OFFSET));
        geometry.beam(base, top, WorkplaceGeometry.argb(color, alpha * PLUMB_ALPHA));
        geometry.decal(pos, face, CARVED_MARK_SIZE, WorkplaceGeometry.argb(color, alpha * CARVED_MARK_ALPHA));
    }
}
