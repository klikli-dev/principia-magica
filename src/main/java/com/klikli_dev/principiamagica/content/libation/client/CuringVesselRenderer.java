// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.vessel.CuringVesselBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Renders the liquid of a curing vessel. The area of the liquid comes from the properties of the vessel.
 */
public class CuringVesselRenderer implements BlockEntityRenderer<CuringVesselBlockEntity, LiquidRenderState> {

    private final SpriteGetter sprites;

    public CuringVesselRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public LiquidRenderState createRenderState() {
        return new LiquidRenderState();
    }

    @Override
    public void extractRenderState(CuringVesselBlockEntity blockEntity, LiquidRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        LiquidRenderer.extract(state, blockEntity.properties().liquid(), blockEntity.getPrinciples());
    }

    @Override
    public void submit(LiquidRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        LiquidRenderer.submit(state, poseStack, submitNodeCollector, this.sprites);
    }
}
