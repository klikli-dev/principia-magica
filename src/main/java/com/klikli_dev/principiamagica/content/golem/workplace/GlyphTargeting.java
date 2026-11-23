// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;
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
    private static final double CORNER_HIT_SIZE = 0.5;
    private static final double CORNER_OCCLUSION_MARGIN = 2.0;

    private GlyphTargeting() {
    }

    /**
     * @param footprintIndex the index of the footprint in the workplace
     * @param footprint      the footprint the corner belongs to
     * @param cornerIndex    the box corner index (0..7)
     * @param vertex         the 3D vertex position of the corner
     * @param block          the block coordinate of the corner
     * @param distance       the distance from the ray origin to the entry point of the corner's hit box
     */
    public record CornerHit(int footprintIndex, WorkFootprint footprint, int cornerIndex, Vec3 vertex, BlockPos block, double distance) {
    }

    /**
     * @param footprint the footprint the glyph belongs to
     * @param glyph     the glyph that was hit
     * @param distance  the distance from the ray origin to the entry point of the glyph's hit box
     */
    public record GlyphHit(WorkFootprint footprint, CarvedGlyph glyph, double distance) {
    }

    /**
     * @param index     the index of the hit footprint in the workplace
     * @param footprint the footprint that was hit
     * @param distance  the distance from the ray origin to the entry point of the box (0 when inside)
     * @param inside    whether the ray origin is inside the box
     */
    public record ZoneHit(int index, WorkFootprint footprint, double distance, boolean inside) {
    }

    /**
     * @return the virtual box of the glyph: the full face of its corner's block, extended outward from the block surface.
     * A lifted glyph floats at its corner, so its box follows the lift.
     */
    public static AABB hitBox(CarvedGlyph glyph) {
        Direction face = glyph.face();
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        Vec3 surface = Vec3.atCenterOf(glyph.corner()).add(normal.scale(0.5));
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
     * Picks the nearest footprint box hit by the ray before {@code blockDistance}.
     * Falls back to the smallest footprint containing {@code from} if no external box face was hit.
     */
    public static Optional<ZoneHit> pickZone(Vec3 from, Vec3 to, List<WorkFootprint> footprints, double blockDistance) {
        ZoneHit nearest = null;
        int nearestVolume = Integer.MAX_VALUE;

        for (int i = 0; i < footprints.size(); i++) {
            WorkFootprint footprint = footprints.get(i);
            AABB box = footprint.zone(0, 0).aabb();
            if (box.contains(from)) {
                continue;
            }

            Optional<Vec3> entry = box.clip(from, to);
            if (entry.isEmpty()) {
                continue;
            }

            double distance = from.distanceTo(entry.get());
            if (distance > blockDistance + BLOCK_EPSILON) {
                continue;
            }

            int volume = footprint.width() * footprint.depth() * footprint.height();
            if (nearest == null || distance < nearest.distance() - BLOCK_EPSILON
                    || (Math.abs(distance - nearest.distance()) <= BLOCK_EPSILON && volume < nearestVolume)) {
                nearest = new ZoneHit(i, footprint, distance, false);
                nearestVolume = volume;
            }
        }

        if (nearest != null) {
            return Optional.of(nearest);
        }

        ZoneHit containing = null;
        int containingVolume = Integer.MAX_VALUE;
        for (int i = 0; i < footprints.size(); i++) {
            WorkFootprint footprint = footprints.get(i);
            if (!footprint.zone(0, 0).aabb().contains(from)) {
                continue;
            }

            int volume = footprint.width() * footprint.depth() * footprint.height();
            if (containing == null || volume < containingVolume) {
                containing = new ZoneHit(i, footprint, 0.0, true);
                containingVolume = volume;
            }
        }

        return Optional.ofNullable(containing);
    }

    /**
     * @return the virtual hit box around one of the 8 box corner vertices
     */
    public static AABB cornerHitBox(WorkFootprint footprint, int cornerIndex) {
        return AABB.ofSize(footprint.cornerVertex(cornerIndex), CORNER_HIT_SIZE, CORNER_HIT_SIZE, CORNER_HIT_SIZE);
    }

    /**
     * Picks the nearest of the 8 corner vertices of all footprints hit by the ray, allowing a small occlusion margin
     * so bottom corners sitting at ground depth can still be targeted through a block of soil or grass.
     */
    public static Optional<CornerHit> pickCorner(Vec3 from, Vec3 to, List<WorkFootprint> footprints, double blockDistance) {
        CornerHit nearest = null;
        for (int footprintIndex = 0; footprintIndex < footprints.size(); footprintIndex++) {
            WorkFootprint footprint = footprints.get(footprintIndex);
            for (int cornerIndex = 0; cornerIndex < 8; cornerIndex++) {
                AABB box = cornerHitBox(footprint, cornerIndex);
                double distance;
                if (box.contains(from)) {
                    distance = 0.0;
                } else {
                    Optional<Vec3> entry = box.clip(from, to);
                    if (entry.isEmpty()) {
                        continue;
                    }
                    distance = from.distanceTo(entry.get());
                }

                if (distance > blockDistance + CORNER_OCCLUSION_MARGIN) {
                    continue;
                }

                if (nearest == null || distance < nearest.distance()) {
                    nearest = new CornerHit(
                            footprintIndex,
                            footprint,
                            cornerIndex,
                            footprint.cornerVertex(cornerIndex),
                            footprint.cornerBlock(cornerIndex),
                            distance
                    );
                }
            }
        }
        return Optional.ofNullable(nearest);
    }

    /**
     * @return whether the eye is close enough to the footprint (or its vertical column) for the server to accept corner edits
     */
    public static boolean isCornerEditable(Vec3 eye, WorkFootprint footprint) {
        AABB box = footprint.zone(0, 0).aabb();
        if (isBoxInReach(eye, box)) {
            return true;
        }
        double reach = MAX_RANGE + REACH_MARGIN;
        double closestX = Math.clamp(eye.x, box.minX, box.maxX);
        double closestZ = Math.clamp(eye.z, box.minZ, box.maxZ);
        double dx = eye.x - closestX;
        double dz = eye.z - closestZ;
        if (dx * dx + dz * dz > reach * reach) {
            return false;
        }
        double closestY = Math.clamp(eye.y, box.minY, box.maxY);
        double dy = Math.abs(eye.y - closestY);
        return dy <= reach + WorkFootprint.MAX_SIZE;
    }

    /**
     * @return whether the eye is close enough to any point of the box for the server to accept editing it
     */
    public static boolean isBoxInReach(Vec3 eye, AABB box) {
        double reach = MAX_RANGE + REACH_MARGIN;
        double closestX = Math.clamp(eye.x, box.minX, box.maxX);
        double closestY = Math.clamp(eye.y, box.minY, box.maxY);
        double closestZ = Math.clamp(eye.z, box.minZ, box.maxZ);
        return eye.distanceToSqr(closestX, closestY, closestZ) <= reach * reach;
    }

    /**
     * @return whether the eye is close enough to the glyph for the server to accept erasing it
     */
    public static boolean isInReach(Vec3 eye, BlockPos glyphPos) {
        double reach = MAX_RANGE + REACH_MARGIN;
        return eye.distanceToSqr(Vec3.atCenterOf(glyphPos)) <= reach * reach;
    }
}
