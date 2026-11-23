// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.geckolib.renderer.GeoEntityRenderer;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class GolemRenderer extends GeoEntityRenderer<GolemEntity, GolemRenderState> {

    /**
     * The same PNGs as the golem GUI status icons, drawn in the world as well.
     */
    private static final Identifier STATUS_WARNING_TEXTURE = PrincipiaMagica.loc("textures/gui/sprites/golem/status_warning.png");
    private static final Identifier STATUS_PROBLEM_TEXTURE = PrincipiaMagica.loc("textures/gui/sprites/golem/status_problem.png");

    /**
     * Half the edge length of the icon in blocks. The icon is 16 px, so this is half a block across.
     */
    private static final float ICON_HALF_SIZE = 0.25F;
    /**
     * Gap between the top of the golem's bounding box and the bottom of the icon.
     */
    private static final float ICON_BASE_HEIGHT = 0.5F;
    /**
     * Extra height while a name tag is shown, so the icon floats above the name tag instead of over it.
     */
    private static final float NAME_TAG_HEIGHT = 0.3F;
    private static final float BOB_AMPLITUDE = 0.04F;
    private static final float BOB_SPEED = 0.1F;
    /**
     * Matches the distance at which name tags are shown.
     */
    private static final double MAX_ICON_DISTANCE_SQ = 32.0 * 32.0;
    private static final int ICON_COLOR = 0xFFFFFFFF;

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel());

        // The tool in the right hand, the carried item between the hands, and the tool on the back. One layer, see GolemItemLayer.
        this.withRenderLayer(new GolemItemLayer(context, this));
    }

    @Override
    public GolemRenderState createRenderState(GolemEntity animatable, @Nullable Void relatedObject) {
        return new GolemRenderState();
    }

    @Override
    public void extractRenderState(GolemEntity entity, GolemRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);

        renderState.statusSeverity = entity.getStatus().severity();
    }

    @Override
    public void submit(GolemRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(renderState, poseStack, submitNodeCollector, camera);

        this.submitStatusIcon(renderState, poseStack, submitNodeCollector, camera);
    }

    /**
     * Draws the status icon as a camera-facing, unlit quad above the golem. Only problems and warnings get an icon, see {@link GolemStatus.Severity}.
     */
    private void submitStatusIcon(GolemRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        Identifier texture = statusTexture(renderState.statusSeverity);
        if (texture == null || renderState.isInvisible || renderState.distanceToCameraSq > MAX_ICON_DISTANCE_SQ) {
            return;
        }

        float height = renderState.boundingBoxHeight + ICON_BASE_HEIGHT + (renderState.nameTag != null ? NAME_TAG_HEIGHT : 0.0F)
                + Mth.sin(renderState.ageInTicks * BOB_SPEED) * BOB_AMPLITUDE;

        poseStack.pushPose();
        poseStack.translate(0.0F, height, 0.0F);
        poseStack.mulPose(camera.orientation);

        // Unlit: the emissive entity pipeline ignores the lightmap, and the full bright light is set as well.
        RenderType renderType = RenderTypes.entityTranslucentEmissive(texture, false);
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            vertex(buffer, pose, -ICON_HALF_SIZE, ICON_HALF_SIZE, 0.0F, 0.0F);
            vertex(buffer, pose, ICON_HALF_SIZE, ICON_HALF_SIZE, 1.0F, 0.0F);
            vertex(buffer, pose, ICON_HALF_SIZE, -ICON_HALF_SIZE, 1.0F, 1.0F);
            vertex(buffer, pose, -ICON_HALF_SIZE, -ICON_HALF_SIZE, 0.0F, 1.0F);
        });

        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v) {
        buffer.addVertex(pose, x, y, 0.0F)
                .setColor(ICON_COLOR)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    /**
     * @return the icon texture for the severity, or null if the golem shows no icon in the world.
     */
    private static @Nullable Identifier statusTexture(GolemStatus.Severity severity) {
        return switch (severity) {
            case WARNING -> STATUS_WARNING_TEXTURE;
            case PROBLEM -> STATUS_PROBLEM_TEXTURE;
            case OK, IDLE -> null;
        };
    }
}
