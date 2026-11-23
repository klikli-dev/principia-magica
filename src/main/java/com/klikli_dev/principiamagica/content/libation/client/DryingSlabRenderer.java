// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.drying.DryingSlabBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Renders a single item lying centred on the reed mat: the input while there is one, otherwise the output.
 */
public class DryingSlabRenderer implements BlockEntityRenderer<DryingSlabBlockEntity, FlatItemsRenderState> {

    private static final float ITEM_SCALE = 0.625F;

    private final ItemModelResolver itemModelResolver;

    public DryingSlabRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public FlatItemsRenderState createRenderState() {
        return new FlatItemsRenderState();
    }

    @Override
    public void extractRenderState(DryingSlabBlockEntity blockEntity, FlatItemsRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        ItemStack shown = blockEntity.getInput().isEmpty() ? blockEntity.getOutput() : blockEntity.getInput();
        state.items = FlatItems.extract(this.itemModelResolver, blockEntity.getLevel(), List.of(shown), blockEntity.getBlockPos());
    }

    @Override
    public void submit(FlatItemsRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        ItemStackRenderState item = state.items.get(0);
        if (item.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        LibationGeometry.applyFacing(poseStack, state.facing);
        FlatItems.submit(poseStack, submitNodeCollector, item,
                LibationGeometry.DRYING_ITEM_CENTER * LibationGeometry.PIXEL,
                LibationGeometry.DRYING_SURFACE_Y * LibationGeometry.PIXEL,
                LibationGeometry.DRYING_ITEM_CENTER * LibationGeometry.PIXEL,
                0.0F, ITEM_SCALE, state.lightCoords);
        poseStack.popPose();
    }
}
