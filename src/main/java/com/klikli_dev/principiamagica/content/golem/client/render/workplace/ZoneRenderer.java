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
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public final class ZoneRenderer {

    public static final int COLOR = 0x55FFFF;
    public static final int HIGHLIGHT_COLOR = 0xFFFFFF;
    public static final float GLYPH_SIZE = 0.8F;
    public static final float HIGHLIGHT_GLYPH_SIZE = 1.0F;
    private static final float CORNER_NODE_SIZE = 0.35F;
    private static final float HIGHLIGHT_CORNER_SIZE = 0.55F;
    private static final float CARVED_MARK_SIZE = 0.4F;
    private static final float PLUMB_ALPHA = 0.6F;
    private static final float CARVED_MARK_ALPHA = 0.5F;
    private static final double PLUMB_OFFSET = 0.5 + 0.01;

    private ZoneRenderer() {
    }

    /**
     * Draws a footprint as a single solid box with a glyph on each of its 8 virtual corner blocks. Each corner glyph is drawn
     * on the one face of its block that points towards the camera, without depth testing so corners below ground stay visible.
     */
    public static void render(WorkplaceGeometry geometry, WorkFootprint footprint, @Nullable BlockPos highlightedCornerBlock, float alpha, double time) {
        AABB box = footprint.zone(0, 0).aabb().inflate(0.01);
        float pulse = WorkplaceGeometry.pulse(time, 0.12, 0.7F);

        geometry.boxFaces(box, WorkplaceGeometry.argb(COLOR, alpha * 0.06F * pulse));
        geometry.boxEdges(box, WorkplaceGeometry.argb(COLOR, alpha * pulse));

        Set<BlockPos> renderedCorners = new HashSet<>(8);
        for (int i = 0; i < 8; i++) {
            BlockPos cornerPos = footprint.cornerBlock(i);
            boolean highlighted = cornerPos.equals(highlightedCornerBlock);

            Vec3 vertex = WorkplaceGeometry.boxCorner(box, i);
            float markSize = highlighted ? HIGHLIGHT_CORNER_SIZE : CORNER_NODE_SIZE;
            int markRgb = highlighted ? HIGHLIGHT_COLOR : COLOR;
            float cornerAlpha = highlighted ? alpha : alpha * 0.9F;
            float xrayAlpha = highlighted ? alpha * 0.55F : alpha * 0.35F;
            geometry.cornerNode(vertex, markSize, WorkplaceGeometry.argb(markRgb, cornerAlpha), WorkplaceGeometry.argb(markRgb, xrayAlpha));

            if (!renderedCorners.add(cornerPos)) {
                continue;
            }

            int rgb = highlighted ? HIGHLIGHT_COLOR : COLOR;
            float size = highlighted ? HIGHLIGHT_GLYPH_SIZE : GLYPH_SIZE;
            int decalArgb = WorkplaceGeometry.argb(rgb, alpha * (highlighted ? 1.0F : pulse));

            geometry.xrayDecal(cornerPos, facingCamera(geometry, cornerPos), size, decalArgb);
        }
    }

    /**
     * The face of the block that points most towards the camera, so the glyph is readable from where the player looks.
     */
    private static Direction facingCamera(WorkplaceGeometry geometry, BlockPos pos) {
        Vec3 toCamera = geometry.camera().subtract(Vec3.atCenterOf(pos));
        return Direction.getApproximateNearest(toCamera);
    }

    /**
     * Draws the tether connecting the sticky-locked anchor block to the moved corner block of the box.
     */
    public static void renderCornerTether(WorkplaceGeometry geometry, BlockPos anchorBlock, BlockPos currentBlock, float alpha, double time) {
        if (anchorBlock.equals(currentBlock)) {
            return;
        }

        float pulse = WorkplaceGeometry.pulse(time, 0.25, 0.5F);
        geometry.beam(Vec3.atCenterOf(anchorBlock), Vec3.atCenterOf(currentBlock), WorkplaceGeometry.argb(HIGHLIGHT_COLOR, alpha * 0.75F));
        geometry.decal(anchorBlock, Direction.UP, CARVED_MARK_SIZE, WorkplaceGeometry.argb(COLOR, alpha * CARVED_MARK_ALPHA));
        geometry.boxEdges(new AABB(anchorBlock).inflate(0.01), WorkplaceGeometry.argb(COLOR, alpha * 0.45F * pulse));
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
