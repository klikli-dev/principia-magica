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
    public static final int HIGHLIGHT_COLOR = 0xFFFFFF;
    public static final float GLYPH_SIZE = 0.8F;
    private static final float CARVED_MARK_SIZE = 0.4F;
    private static final float CORNER_NODE_SIZE = 0.35F;
    private static final float HIGHLIGHT_CORNER_SIZE = 0.55F;
    private static final float PLUMB_ALPHA = 0.6F;
    private static final float CARVED_MARK_ALPHA = 0.5F;
    private static final double PLUMB_OFFSET = 0.5 + 0.01;

    private ZoneRenderer() {
    }

    /**
     * Draws a footprint as a single solid box with 8 interactive corner handles.
     */
    public static void render(WorkplaceGeometry geometry, WorkFootprint footprint, int targetedCorner, float alpha, double time) {
        AABB box = footprint.zone(0, 0).aabb().inflate(0.01);
        float pulse = WorkplaceGeometry.pulse(time, 0.12, 0.7F);

        geometry.boxFaces(box, WorkplaceGeometry.argb(COLOR, alpha * 0.06F * pulse));
        geometry.boxEdges(box, WorkplaceGeometry.argb(COLOR, alpha * pulse));

        for (int i = 0; i < 8; i++) {
            boolean highlighted = i == targetedCorner;
            Vec3 vertex = WorkplaceGeometry.boxCorner(box, i);
            float size = highlighted ? HIGHLIGHT_CORNER_SIZE : CORNER_NODE_SIZE;
            int rgb = highlighted ? HIGHLIGHT_COLOR : COLOR;
            float cornerAlpha = highlighted ? alpha : alpha * 0.9F;
            float xrayAlpha = highlighted ? alpha * 0.55F : alpha * 0.35F;
            geometry.cornerNode(vertex, size, WorkplaceGeometry.argb(rgb, cornerAlpha), WorkplaceGeometry.argb(rgb, xrayAlpha));
        }
    }

    /**
     * Draws the tether connecting the sticky-locked anchor block/vertex to the moved corner of the box.
     */
    public static void renderCornerTether(WorkplaceGeometry geometry, Vec3 anchorVertex, BlockPos anchorBlock, Vec3 currentVertex, float alpha, double time) {
        if (anchorVertex.distanceToSqr(currentVertex) <= 1.0E-4) {
            return;
        }

        float pulse = WorkplaceGeometry.pulse(time, 0.25, 0.5F);
        geometry.beam(anchorVertex, currentVertex, WorkplaceGeometry.argb(HIGHLIGHT_COLOR, alpha * 0.75F));
        geometry.cornerNode(anchorVertex, CORNER_NODE_SIZE,
                WorkplaceGeometry.argb(COLOR, alpha * 0.8F),
                WorkplaceGeometry.argb(COLOR, alpha * 0.4F));
        geometry.boxEdges(new AABB(anchorBlock).inflate(0.005), WorkplaceGeometry.argb(COLOR, alpha * 0.35F * pulse));
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
