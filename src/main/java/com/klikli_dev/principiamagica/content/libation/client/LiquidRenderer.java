// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.AmountSmoother;
import com.klikli_dev.principiamagica.content.libation.LiquidArea;
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
 * Renders the surface of a liquid as a single quad with the animated still water sprite, tinted by the principles.
 */
public final class LiquidRenderer {

    public static final int SURFACE_ALPHA = 0xCC;
    public static final int WATER_COLOR = 0x3F76E4;

    private static final SpriteId WATER_STILL = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/water_still"));

    private LiquidRenderer() {
    }

    /**
     * Extracts a liquid, with its surface height scaled by the amount. Draws nothing for an amount of zero.
     *
     * @param amount the amount in mB to draw, which the clients ease, see {@link AmountSmoother}.
     * @param tint   the RGB colour of the liquid, such as {@link #WATER_COLOR} or the blended colour of principles.
     */
    public static void extract(LiquidRenderState state, LiquidArea liquid, float amount, int tint) {
        state.liquid = liquid;
        state.visible = amount > 0.0F;
        state.surfaceY = liquid.surfaceY(amount);
        state.color = SURFACE_ALPHA << 24 | tint;
    }

    public static void submit(LiquidRenderState state, PoseStack poseStack, SubmitNodeCollector collector, SpriteGetter sprites) {
        if (!state.visible) {
            return;
        }

        TextureAtlasSprite sprite = sprites.get(WATER_STILL);
        LiquidArea liquid = state.liquid;
        float x0 = liquid.minX() * LibationGeometry.PIXEL;
        float x1 = liquid.maxX() * LibationGeometry.PIXEL;
        float z0 = liquid.minZ() * LibationGeometry.PIXEL;
        float z1 = liquid.maxZ() * LibationGeometry.PIXEL;
        float y = state.surfaceY;
        int color = state.color;
        int light = state.lightCoords;

        collector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, buffer) -> {
            vertex(pose, buffer, x0, y, z0, sprite.getU(0.0F), sprite.getV(0.0F), color, light);
            vertex(pose, buffer, x0, y, z1, sprite.getU(0.0F), sprite.getV(1.0F), color, light);
            vertex(pose, buffer, x1, y, z1, sprite.getU(1.0F), sprite.getV(1.0F), color, light);
            vertex(pose, buffer, x1, y, z0, sprite.getU(1.0F), sprite.getV(0.0F), color, light);
        });
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int color, int light) {
        buffer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setLight(light);
    }
}
