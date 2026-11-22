// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Builds the camera-facing ribbons, glyphs and faces used by the workplace elements. Positions are given in world
 * coordinates and converted to camera-relative coordinates here, so no large float values reach the GPU.
 */
public final class WorkplaceGeometry {

    private static final float MIN_WIDTH = 0.02F;
    private static final float MAX_WIDTH = 0.12F;
    private static final float WIDTH_PER_BLOCK = 0.01F;
    private static final double DECAL_OFFSET = 0.01;

    /**
     * Corner index quads of a box, where bit 0 selects x, bit 1 y and bit 2 z of the corner (see {@link #boxCorner}).
     */
    private static final int[][] FACES = {{0, 2, 6, 4}, {1, 5, 7, 3}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 1, 3, 2}, {4, 6, 7, 5}};

    private final PoseStack.Pose pose;
    private final Vec3 camera;
    private final VertexConsumer ribbons;
    private final VertexConsumer beams;
    private final VertexConsumer fills;
    private final VertexConsumer decals;

    public WorkplaceGeometry(MultiBufferSource buffers, PoseStack.Pose pose, Vec3 camera) {
        this.pose = pose;
        this.camera = camera;
        this.ribbons = buffers.getBuffer(WorkplaceRenderTypes.RIBBON);
        this.beams = buffers.getBuffer(WorkplaceRenderTypes.BEAM);
        this.fills = buffers.getBuffer(WorkplaceRenderTypes.FILL);
        this.decals = buffers.getBuffer(WorkplaceRenderTypes.GLYPH);
    }

    public static int argb(int rgb, float alpha) {
        int a = Mth.clamp(Math.round(alpha * 255.0F), 0, 255);
        return a << 24 | rgb & 0xFFFFFF;
    }

    /**
     * Pulse factor in [min, 1] that oscillates with the given speed (radians per tick).
     */
    public static float pulse(double time, double speed, float min) {
        return min + (1.0F - min) * (0.5F + 0.5F * (float) Math.sin(time * speed));
    }

    public static boolean isWithin(Vec3 point, Vec3 camera, double rangeSqr) {
        return point.distanceToSqr(camera) <= rangeSqr;
    }

    public Vec3 camera() {
        return this.camera;
    }

    /**
     * Glowing camera-facing ribbon between two points, width scaled by distance to the camera.
     */
    public void ribbon(Vec3 from, Vec3 to, int argb) {
        this.segment(this.ribbons, from, to, this.width(from.add(to).scale(0.5)), argb);
    }

    /**
     * Same as {@link #ribbon}, but drawn with the dashed beam pipeline.
     */
    public void beam(Vec3 from, Vec3 to, int argb) {
        this.segment(this.beams, from, to, this.width(from.add(to).scale(0.5)), argb);
    }

    /**
     * Wireframe of a box made of camera-facing ribbons.
     */
    public void boxEdges(AABB box, int argb) {
        for (int i = 0; i < 8; i++) {
            for (int bit : new int[]{1, 2, 4}) {
                if ((i & bit) == 0) {
                    this.ribbon(boxCorner(box, i), boxCorner(box, i | bit), argb);
                }
            }
        }
    }

    /**
     * Faint translucent faces of a box.
     */
    public void boxFaces(AABB box, int argb) {
        for (int[] face : FACES) {
            for (int index : face) {
                this.vertex(this.fills, boxCorner(box, index), 0.0F, 0.0F, argb);
            }
        }
    }

    /**
     * Textured glyph decal lying flat on a face of a block, slightly off the face so it does not z-fight. Has no collision.
     */
    public void decal(BlockPos pos, Direction face, float size, int argb) {
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        Vec3 centre = Vec3.atCenterOf(pos).add(normal.scale(0.5 + DECAL_OFFSET));
        Vec3 u;
        Vec3 v;
        if (face.getAxis() == Direction.Axis.Y) {
            u = new Vec3(1.0, 0.0, 0.0);
            v = new Vec3(0.0, 0.0, 1.0);
        } else if (face.getAxis() == Direction.Axis.Z) {
            u = new Vec3(1.0, 0.0, 0.0);
            v = new Vec3(0.0, 1.0, 0.0);
        } else {
            u = new Vec3(0.0, 0.0, 1.0);
            v = new Vec3(0.0, 1.0, 0.0);
        }

        double half = size * 0.5;
        Vec3 du = u.scale(half);
        Vec3 dv = v.scale(half);
        this.vertex(this.decals, centre.subtract(du).subtract(dv), 0.0F, 0.0F, argb);
        this.vertex(this.decals, centre.subtract(du).add(dv), 0.0F, 1.0F, argb);
        this.vertex(this.decals, centre.add(du).add(dv), 1.0F, 1.0F, argb);
        this.vertex(this.decals, centre.add(du).subtract(dv), 1.0F, 0.0F, argb);
    }

    /**
     * Horizontal ring (in the XZ plane) of ribbon segments around the centre.
     */
    public void ring(Vec3 centre, double radius, double rotation, int segments, int argb) {
        double step = Math.PI * 2.0 / segments;
        Vec3 previous = centre.add(Math.cos(rotation) * radius, 0.0, Math.sin(rotation) * radius);
        for (int i = 1; i <= segments; i++) {
            double angle = rotation + step * i;
            Vec3 next = centre.add(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
            this.ribbon(previous, next, argb);
            previous = next;
        }
    }

    /**
     * Small sparkle glyph made of two crossing ribbons. Used to mark zone corners, link roles and targets.
     */
    public void glyph(Vec3 centre, float size, Glyph glyph, int argb) {
        float width = size * 0.3F;
        double half = size * 0.5;
        if (glyph == Glyph.PLUS || glyph == Glyph.STAR) {
            this.segment(this.ribbons, centre.add(-half, 0.0, 0.0), centre.add(half, 0.0, 0.0), width, argb);
            this.segment(this.ribbons, centre.add(0.0, -half, 0.0), centre.add(0.0, half, 0.0), width, argb);
        }
        if (glyph == Glyph.CROSS || glyph == Glyph.STAR) {
            double diagonal = half * Math.sqrt(0.5);
            this.segment(this.ribbons, centre.add(-diagonal, -diagonal, 0.0), centre.add(diagonal, diagonal, 0.0), width, argb);
            this.segment(this.ribbons, centre.add(-diagonal, diagonal, 0.0), centre.add(diagonal, -diagonal, 0.0), width, argb);
        }
    }

    private float width(Vec3 point) {
        return Mth.clamp((float) point.distanceTo(this.camera) * WIDTH_PER_BLOCK, MIN_WIDTH, MAX_WIDTH);
    }

    private void segment(VertexConsumer consumer, Vec3 from, Vec3 to, float width, int argb) {
        Vec3 direction = to.subtract(from);
        double length = direction.length();
        if (length < 1.0E-6) {
            return;
        }

        Vec3 unit = direction.scale(1.0 / length);
        Vec3 toCamera = this.camera.subtract(from.add(to).scale(0.5));
        Vec3 side = unit.cross(toCamera);
        if (side.lengthSqr() < 1.0E-10) {
            side = unit.cross(Math.abs(unit.y) < 0.99 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0));
        }

        Vec3 offset = side.scale(width * 0.5 / side.length());
        float uvLength = (float) length;
        this.vertex(consumer, from.subtract(offset), 0.0F, 0.0F, argb);
        this.vertex(consumer, from.add(offset), 0.0F, 1.0F, argb);
        this.vertex(consumer, to.add(offset), uvLength, 1.0F, argb);
        this.vertex(consumer, to.subtract(offset), uvLength, 0.0F, argb);
    }

    private void vertex(VertexConsumer consumer, Vec3 position, float u, float v, int argb) {
        consumer.addVertex(this.pose, (float) (position.x - this.camera.x), (float) (position.y - this.camera.y), (float) (position.z - this.camera.z))
                .setUv(u, v)
                .setColor(argb);
    }

    public static Vec3 boxCorner(AABB box, int index) {
        return new Vec3(
                (index & 1) == 0 ? box.minX : box.maxX,
                (index & 2) == 0 ? box.minY : box.maxY,
                (index & 4) == 0 ? box.minZ : box.maxZ
        );
    }

    public enum Glyph {
        PLUS,
        CROSS,
        STAR
    }
}
