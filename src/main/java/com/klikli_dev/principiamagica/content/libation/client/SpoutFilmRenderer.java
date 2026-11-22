// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

/**
 * Renders the water of the libation slab as it leaves through the rim gap, and the thin film of the run-off lying in the spout channel.
 * Uses the animated vanilla flow sprites. Geometry is in pixels, in the model space of a block facing north.
 */
public final class SpoutFilmRenderer {

    private static final SpriteId WATER_FLOW = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/water_flow"));
    private static final SpriteId WATER_STILL = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/water_still"));
    private static final int SEGMENTS = 6;
    /**
     * Fraction of the channel length (t) that the film skips at the mouth, see {@link LibationGeometry#FILM_MOUTH_INSET}.
     */
    private static final float MOUTH_INSET_T = LibationGeometry.FILM_MOUTH_INSET / LibationGeometry.SPOUT_LENGTH;
    private static final float HALF_WIDTH = 0.75F;
    /**
     * Pixels of film covered by the full sprite. Smaller than a block (16) so the texture is not stretched, but large enough to stay readable.
     */
    private static final float TEXTURE_PIXELS = 8.0F;
    private static final int FILM_ALPHA = 0xB0;

    private SpoutFilmRenderer() {
    }

    /**
     * Closes the water at the spout mouth with a vertical face across the rim gap, so the gap between the water surface and the
     * spout floor cannot be seen through. The face runs from the mouth up to the water surface. Colour and texture match the water
     * surface. The pose must already be transformed by the side of the slab that holds the spout.
     */
    public static void submitWaterFace(LiquidRenderState water, PoseStack poseStack, SubmitNodeCollector collector, SpriteGetter sprites) {
        if (!water.visible) {
            return;
        }

        TextureAtlasSprite sprite = sprites.get(WATER_STILL);
        float bottom = LibationGeometry.spoutPoint(0.0F).y();
        float top = water.surfaceY / LibationGeometry.PIXEL;
        if (top <= bottom) {
            return;
        }

        float x0 = LibationGeometry.RIM_GAP_MIN_X;
        float x1 = LibationGeometry.RIM_GAP_MAX_X;
        float z = LibationGeometry.RIM_INNER_Z;
        int color = water.color;
        int light = water.lightCoords;
        float vBottom = (top - bottom) / TEXTURE_PIXELS;
        float uMax = (x1 - x0) / TEXTURE_PIXELS;

        collector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, buffer) -> quad(pose, buffer, sprite, color, light,
                new float[]{x0, bottom, z, 0.0F, vBottom},
                new float[]{x1, bottom, z, uMax, vBottom},
                new float[]{x1, top, z, uMax, 0.0F},
                new float[]{x0, top, z, 0.0F, 0.0F}));
    }

    /**
     * Film of the run-off in the channel of an open spout, from the mouth to the tip. The pose must already be transformed by the facing of the spout.
     */
    public static void submit(LibationSpoutRenderState state, PoseStack poseStack, SubmitNodeCollector collector, SpriteGetter sprites) {
        if (!state.flowing) {
            return;
        }

        TextureAtlasSprite sprite = sprites.get(WATER_FLOW);
        int color = FILM_ALPHA << 24 | state.flowColor;
        int light = state.lightCoords;

        collector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, buffer) -> {
            float uHalf = HALF_WIDTH / TEXTURE_PIXELS;
            float v = 0.0F;
            for (int i = 0; i < SEGMENTS; i++) {
                LibationGeometry.Point a = LibationGeometry.spoutPoint(MOUTH_INSET_T + (1.0F - MOUTH_INSET_T) * i / SEGMENTS);
                LibationGeometry.Point b = LibationGeometry.spoutPoint(MOUTH_INSET_T + (1.0F - MOUTH_INSET_T) * (i + 1) / SEGMENTS);
                float dx = b.x() - a.x();
                float dy = b.y() - a.y();
                float dz = b.z() - a.z();
                float v2 = v + (float) Math.sqrt(dx * dx + dy * dy + dz * dz) / TEXTURE_PIXELS;
                quad(pose, buffer, sprite, color, light,
                        new float[]{a.x() - HALF_WIDTH, a.y(), a.z(), 0.5F - uHalf, v},
                        new float[]{a.x() + HALF_WIDTH, a.y(), a.z(), 0.5F + uHalf, v},
                        new float[]{b.x() + HALF_WIDTH, b.y(), b.z(), 0.5F + uHalf, v2},
                        new float[]{b.x() - HALF_WIDTH, b.y(), b.z(), 0.5F - uHalf, v2});
                v = v2;
            }
        });
    }

    /**
     * Emits a quad on both sides. Each corner is {x, y, z, u, v} with the position in pixels and the uv in the sprite fraction.
     */
    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite, int color, int light, float[]... corners) {
        for (int i = 0; i < corners.length; i++) {
            vertex(pose, buffer, sprite, corners[i], color, light);
        }
        for (int i = corners.length - 1; i >= 0; i--) {
            vertex(pose, buffer, sprite, corners[i], color, light);
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite, float[] corner, int color, int light) {
        buffer.addVertex(pose, corner[0] * LibationGeometry.PIXEL, corner[1] * LibationGeometry.PIXEL, corner[2] * LibationGeometry.PIXEL)
                .setColor(color)
                .setUv(sprite.getU(corner[3]), sprite.getV(corner[4]))
                .setLight(light);
    }
}
