// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.content.golem.client.GolemRenderer;
import com.klikli_dev.principiamagica.content.golem.client.GolemRenderState;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Draws the golem in a GUI rectangle, turning towards the mouse like the player in the inventory screen.
 * <p>
 * Vanilla's inventory portrait only calls {@code createRenderState}, which is enough for vanilla models. GeckoLib fills its bones in
 * {@code extractRenderState}, so the golem needs that call too, or it is drawn without any geometry. The transform math is the same as
 * vanilla's {@code InventoryScreen.extractEntityInInventoryFollowsMouse}.
 */
final class GolemPortrait {

    private static final float ANGLE_SCALE = 40.0F;
    private static final float TURN_SCALE = 20.0F;
    private static final float DEGREES_TO_RADIANS = 0.017453292F;

    private GolemPortrait() {
    }

    /**
     * @param graphics the graphics to draw on, in the same coordinates as the rectangle
     * @param golem the golem to draw
     * @param x0 left edge of the rectangle
     * @param y0 top edge of the rectangle
     * @param x1 right edge of the rectangle
     * @param y1 bottom edge of the rectangle
     * @param size the scale of the model in pixels per block
     * @param offsetY vertical offset of the model
     * @param mouseX the mouse x in the same coordinates as the rectangle
     * @param mouseY the mouse y in the same coordinates as the rectangle
     */
    static void extract(GuiGraphicsExtractor graphics, GolemEntity golem, int x0, int y0, int x1, int y1, int size, float offsetY, float mouseX, float mouseY) {
        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(golem) instanceof GolemRenderer renderer)) {
            return;
        }

        GolemRenderState state = renderer.createRenderState(golem, null);
        renderer.extractRenderState(golem, state, 0.0F);
        state.shadowPieces.clear();
        state.outlineColor = 0;

        float centerX = (x0 + x1) / 2.0F;
        float centerY = (y0 + y1) / 2.0F;
        float xAngle = (float) Math.atan((centerX - mouseX) / ANGLE_SCALE);
        float yAngle = (float) Math.atan((centerY - mouseY) / ANGLE_SCALE);
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yAngle * TURN_SCALE * DEGREES_TO_RADIANS);
        rotation.mul(xRotation);

        state.bodyRot = 180.0F + xAngle * TURN_SCALE;
        state.yRot = xAngle * TURN_SCALE;
        state.xRot = -yAngle * TURN_SCALE;

        Vector3f translation = new Vector3f(0.0F, state.boundingBoxHeight / 2.0F + offsetY, 0.0F);
        graphics.entity(state, size, translation, rotation, xRotation, x0, y0, x1, y1);
    }
}
