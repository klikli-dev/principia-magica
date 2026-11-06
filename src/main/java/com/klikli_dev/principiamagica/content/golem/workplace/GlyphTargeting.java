// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Optional;

/**
 * Pure ray picking of carved glyphs. Each glyph has a thin virtual box on its carved face that sits in front of the block
 * surface, so a glyph can be targeted from a distance without the block behind it getting in the way.
 */
public final class GlyphTargeting {

    /**
     * The farthest a glyph can be targeted from the eye. Glyphs are always drawn at this range, see the overlay renderer.
     */
    public static final double MAX_RANGE = 48.0;

    /**
     * Extra reach the server allows on top of {@link #MAX_RANGE}, to absorb the difference between eye and feet position.
     */
    public static final double REACH_MARGIN = 4.0;

    private static final double HIT_THICKNESS = 0.125;
    private static final double HIT_INFLATE = 0.0625;
    private static final double BLOCK_EPSILON = 1.0E-3;

    private GlyphTargeting() {
    }

    /**
     * @param footprint the footprint the glyph belongs to
     * @param glyph     the glyph that was hit
     * @param distance  the distance from the ray origin to the entry point of the glyph's hit box
     */
    public record GlyphHit(WorkFootprint footprint, CarvedGlyph glyph, double distance) {
    }

    /**
     * @return the virtual box of the glyph: the full face of its block, extended outward from the block surface
     */
    public static AABB hitBox(CarvedGlyph glyph) {
        Direction face = glyph.face();
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        Vec3 surface = Vec3.atCenterOf(glyph.pos()).add(normal.scale(0.5));
        Vec3 outer = surface.add(normal.scale(HIT_THICKNESS));

        double half = 0.5 + HIT_INFLATE;
        return new AABB(surface, outer).inflate(
                face.getAxis() == Direction.Axis.X ? 0.0 : half,
                face.getAxis() == Direction.Axis.Y ? 0.0 : half,
                face.getAxis() == Direction.Axis.Z ? 0.0 : half
        );
    }

    /**
     * @return the glyph whose hit box the segment from {@code from} to {@code to} enters first, if any
     */
    public static Optional<GlyphHit> pick(Vec3 from, Vec3 to, Collection<WorkFootprint> footprints) {
        GlyphHit nearest = null;
        for (WorkFootprint footprint : footprints) {
            for (CarvedGlyph glyph : footprint.glyphs()) {
                Optional<Vec3> entry = hitBox(glyph).clip(from, to);
                if (entry.isEmpty()) {
                    continue;
                }

                double distance = from.distanceTo(entry.get());
                if (nearest == null || distance < nearest.distance()) {
                    nearest = new GlyphHit(footprint, glyph, distance);
                }
            }
        }

        return Optional.ofNullable(nearest);
    }

    /**
     * A glyph wins over the block in front of it if it is nearer, with a small tolerance for the glyph on the face of that block.
     *
     * @param blockDistance the distance to the block the ray hits, or {@link Double#POSITIVE_INFINITY} if it hits none
     */
    public static boolean winsOverBlock(GlyphHit hit, double blockDistance) {
        return hit.distance() <= blockDistance + BLOCK_EPSILON;
    }

    /**
     * @return whether the eye is close enough to the glyph for the server to accept erasing it
     */
    public static boolean isInReach(Vec3 eye, BlockPos glyphPos) {
        double reach = MAX_RANGE + REACH_MARGIN;
        return eye.distanceToSqr(Vec3.atCenterOf(glyphPos)) <= reach * reach;
    }
}
