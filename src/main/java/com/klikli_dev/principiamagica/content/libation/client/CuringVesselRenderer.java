// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.vessel.CuringVesselBlockEntity;
import com.klikli_dev.principiamagica.content.libation.vessel.SteepingJarBlockEntity;
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
 * Renders the liquid of the Wabet Basin and the Steeping Jar.
 */
public class CuringVesselRenderer<T extends CuringVesselBlockEntity> implements BlockEntityRenderer<T, LiquidRenderState> {

    private final SpriteGetter sprites;

    public CuringVesselRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public LiquidRenderState createRenderState() {
        return new LiquidRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, LiquidRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        boolean jar = blockEntity instanceof SteepingJarBlockEntity;
        LiquidRenderer.extract(state, jar ? LibationGeometry.JAR : LibationGeometry.BASIN, blockEntity.getPrinciples());
    }

    @Override
    public void submit(LiquidRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        LiquidRenderer.submit(state, poseStack, submitNodeCollector, this.sprites);
    }
}
