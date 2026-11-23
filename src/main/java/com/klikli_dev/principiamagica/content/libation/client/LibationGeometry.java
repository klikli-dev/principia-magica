// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.LiquidArea;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.trough.CatchTroughBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Model geometry of the libation blocks. Values are given in pixels (1/16 block) for the model facing north, the front at -Z.
 * The vessels carry their liquid geometry in their properties, see {@code VesselProperties}.
 * <p>
 * Spouts and taps are blocks of their own in the cell in front of their host. Their model space is the cell of the spout, so
 * the host lies at z 16 and beyond. The rim of a slab is at z 1 to 2 of the slab cell, which is z 17 to 18 of the spout cell.
 */
public final class LibationGeometry {

    public static final float PIXEL = 1.0F / 16.0F;

    public static final LiquidArea TROUGH = new LiquidArea(2.0F, 14.0F, 2.0F, 14.0F, 1.0F, 6.5F, CatchTroughBlockEntity.CAPACITY);

    public static final float SLAB_SURFACE_Y = 11.0F;
    public static final float DRYING_SURFACE_Y = 3.5F;
    public static final float[] SLOT_X = {5.0F, 11.0F, 5.0F, 11.0F};
    public static final float[] SLOT_Z = {5.0F, 5.0F, 11.0F, 11.0F};
    public static final float DRYING_ITEM_CENTER = 8.0F;
    public static final float SLAB_ITEM_CENTER = 8.0F;

    /**
     * Water film on the libation slab. Items lie on or just above it, lifted by {@link #ITEM_LIFT} to avoid z-fighting.
     */
    public static final LiquidArea SLAB_WATER = new LiquidArea(2.0F, 14.0F, 2.0F, 14.0F, SLAB_SURFACE_Y, 11.8F, LibationSlabBlockEntity.WATER_CAPACITY);
    public static final float ITEM_LIFT = 0.01F;

    /**
     * Inner edge of the rim of the slab, in the slab cell. The mouth of a spout sits here, and the water face closes the rim gap at it.
     */
    public static final float RIM_INNER_Z = 2.0F;

    /**
     * Width of the gap that the spout cuts into the north rim of the slab, from x 6 to 10.
     */
    public static final float RIM_GAP_MIN_X = 6.0F;
    public static final float RIM_GAP_MAX_X = 10.0F;

    /**
     * Drip point of a Wabet Basin or Steeping Jar. The vessel tap nozzle ends above the centre of the cell below it, so drips fall straight down.
     */
    public static final Point VESSEL_DRIP = new Point(8.0F, 1.5F, 8.0F);

    private static final float SPOUT_PIVOT_Y = 11.0F;

    /**
     * The mouth of the channel of a spout cell, which is the inner rim edge of the host.
     */
    private static final float SPOUT_PIVOT_Z = 16.0F + RIM_INNER_Z;

    /**
     * Horizontal distance from the mouth to the tip of the channel: the tip reaches the centre of the spout cell at z 8.
     */
    private static final float SPOUT_REACH = SPOUT_PIVOT_Z - 8.0F;

    /**
     * Tilt of an open spout, the tip points down. A closed spout is tilted the opposite way.
     */
    public static final float SPOUT_ANGLE = 22.5F;

    /**
     * Length of the channel along its own axis, so that its horizontal reach is {@link #SPOUT_REACH}.
     */
    public static final float SPOUT_LENGTH = (float) (SPOUT_REACH / Math.cos(Math.toRadians(SPOUT_ANGLE)));

    /**
     * Distance in pixels along the channel axis that the film of the run-off starts short of the mouth, so it does not reach
     * into the slab.
     */
    public static final float FILM_MOUTH_INSET = 1.0F;

    /**
     * Thickness of the channel floor. Drips hang from its underside.
     */
    private static final float SPOUT_THICKNESS = 1.0F;

    /**
     * Lift of the film above the top of the channel floor, kept tiny so the film does not z-fight with the channel.
     */
    private static final float SPOUT_LIFT = 0.01F;

    /**
     * Drip point of an open libation spout, hanging from the underside of the tip above the centre of the cell below.
     */
    public static final Point SPOUT_DRIP = spoutDrip();

    private LibationGeometry() {
    }

    /**
     * Rotates the pose around the block centre so that model geometry follows the facing.
     */
    public static void applyFacing(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingAngle(facing)));
        poseStack.translate(-0.5F, 0.0F, -0.5F);
    }

    /**
     * Models are authored facing north, and blockstates rotate them clockwise (seen from above) by
     * {@code facing.getOpposite().toYRot()} degrees: north 0, east 90, south 180, west 270. A positive rotation around +Y is
     * counter-clockwise, so the angle is negated.
     *
     * @return the rotation around +Y in degrees that matches the blockstate rotation for the facing.
     */
    private static float facingAngle(Direction facing) {
        return -facing.getOpposite().toYRot();
    }

    /**
     * @return the world position of a model point in pixels, for a block at pos facing the given direction.
     */
    public static Vec3 toWorld(BlockPos pos, Direction facing, float x, float y, float z) {
        Vector3f local = new Matrix4f()
                .translate(0.5F, 0.0F, 0.5F)
                .rotateY((float) Math.toRadians(facingAngle(facing)))
                .translate(-0.5F, 0.0F, -0.5F)
                .transformPosition(x * PIXEL, y * PIXEL, z * PIXEL, new Vector3f());
        return new Vec3(pos.getX() + local.x, pos.getY() + local.y, pos.getZ() + local.z);
    }

    /**
     * @return the point on the centre line of an open spout channel, t = 0 at the mouth and t = 1 at the tip. The point lies on
     * the top face of the channel. The channel points down, so the tip is lower than the mouth.
     */
    public static Point spoutPoint(float t) {
        float dz = -SPOUT_LENGTH * t;
        float angle = (float) Math.toRadians(SPOUT_ANGLE);
        return new Point(8.0F, SPOUT_PIVOT_Y + Mth.sin(angle) * dz + SPOUT_LIFT, SPOUT_PIVOT_Z + Mth.cos(angle) * dz);
    }

    private static Point spoutDrip() {
        Point tip = spoutPoint(1.0F);
        return new Point(tip.x(), tip.y() - SPOUT_THICKNESS, tip.z());
    }

    /**
     * Pixel position of a point, used for spout and tap tips.
     */
    public record Point(float x, float y, float z) {
    }
}
