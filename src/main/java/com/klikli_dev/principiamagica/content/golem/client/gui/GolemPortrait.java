// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * Draws the golem in its niche, turning towards the mouse like the player in the inventory screen.
 * <p>
 * The widget works in screen coordinates, like the widgets around it. That matters for two things: the scissor clips the golem to the niche,
 * and the mouse position it turns towards is the screen position of the cursor. Labels are drawn in a translated coordinate space, so the
 * golem is not drawn from {@code extractLabels}.
 * <p>
 * The render state comes from the entity render dispatcher, which runs the full extraction. The GeckoLib renderer fills its bones there,
 * so a state from {@code createRenderState} alone is drawn without geometry. The transform follows Occultism's living entity widget.
 */
final class GolemPortrait extends AbstractWidget {

    private static final float ANGLE_SCALE = 40.0F;
    private static final float TURN_SCALE = 20.0F;
    private static final float DEGREES_TO_RADIANS = 0.017453292F;
    private static final float OFFSET_Y = 0.0625F;

    private final Supplier<GolemEntity> golem;
    private final int size;

    /**
     * @param x left edge of the niche interior
     * @param y top edge of the niche interior
     * @param width width of the niche interior
     * @param height height of the niche interior
     * @param size the model scale in pixels per block
     * @param golem the golem to draw, read each frame, nothing is drawn while it is null
     */
    GolemPortrait(int x, int y, int width, int height, int size, Supplier<GolemEntity> golem) {
        super(x, y, width, height, Component.empty());
        this.size = size;
        this.golem = golem;
        this.active = false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GolemEntity entity = this.golem.get();
        if (entity == null) {
            return;
        }

        int x0 = this.getX();
        int y0 = this.getY();
        int x1 = x0 + this.getWidth();
        int y1 = y0 + this.getHeight();

        graphics.enableScissor(x0, y0, x1, y1);
        this.extractGolem(graphics, entity, x0, y0, x1, y1, mouseX, mouseY, partialTick);
        graphics.disableScissor();
    }

    private void extractGolem(GuiGraphicsExtractor graphics, GolemEntity entity, int x0, int y0, int x1, int y1, float mouseX, float mouseY, float partialTick) {
        float centerX = (x0 + x1) / 2.0F;
        float centerY = (y0 + y1) / 2.0F;
        float xAngle = (float) Math.atan((centerX - mouseX) / ANGLE_SCALE);
        float yAngle = (float) Math.atan((centerY - mouseY) / ANGLE_SCALE);
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yAngle * TURN_SCALE * DEGREES_TO_RADIANS);
        rotation.mul(xRotation);

        EntityRenderState renderState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(entity, partialTick);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        renderState.nameTag = null;
        renderState.scoreText = null;
        renderState.nameTagAttachment = null;

        if (renderState instanceof LivingEntityRenderState living) {
            living.bodyRot = 180.0F + xAngle * TURN_SCALE;
            living.yRot = xAngle * TURN_SCALE;
            living.xRot = living.pose != Pose.FALL_FLYING ? -yAngle * TURN_SCALE : 0.0F;
        }

        Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F + OFFSET_Y, 0.0F);
        graphics.entity(renderState, this.size, translation, rotation, xRotation, x0, y0, x1, y1);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
