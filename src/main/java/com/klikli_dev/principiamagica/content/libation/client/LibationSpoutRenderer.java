// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.spout.LibationSpoutBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Renders the film of the run-off in the channel of an open libation spout.
 */
public class LibationSpoutRenderer implements BlockEntityRenderer<LibationSpoutBlockEntity, LibationSpoutRenderState> {

    private final SpriteGetter sprites;

    public LibationSpoutRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public LibationSpoutRenderState createRenderState() {
        return new LibationSpoutRenderState();
    }

    @Override
    public void extractRenderState(LibationSpoutBlockEntity blockEntity, LibationSpoutRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        state.flowing = blockEntity.getDrips().isActive();
        state.flowColor = LibationEffects.hostColor(blockEntity.getLevel(), blockEntity.getBlockPos(), state.facing);
    }

    @Override
    public void submit(LibationSpoutRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        LibationGeometry.applyFacing(poseStack, state.facing);
        SpoutFilmRenderer.submit(state, poseStack, submitNodeCollector, this.sprites);
        poseStack.popPose();
    }
}
