// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlock;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class LibationSlabRenderer implements BlockEntityRenderer<LibationSlabBlockEntity, LibationSlabRenderState> {

    private static final float ITEM_SCALE = 0.5F;
    private static final float MIN_DISSOLVE_SCALE = 0.05F;

    private final ItemModelResolver itemModelResolver;
    private final SpriteGetter sprites;

    public LibationSlabRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.sprites = context.sprites();
    }

    @Override
    public LibationSlabRenderState createRenderState() {
        return new LibationSlabRenderState();
    }

    @Override
    public void extractRenderState(LibationSlabBlockEntity blockEntity, LibationSlabRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.spoutSide = blockEntity.getBlockState().getValue(LibationSlabBlock.SPOUT).direction();
        state.items = FlatItems.extract(this.itemModelResolver, blockEntity.getLevel(), single(blockEntity.getStack()), blockEntity.getBlockPos());
        state.dissolvingSlot = blockEntity.getDissolveSlot();
        long gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0L;
        state.dissolveScale = blockEntity.isDissolving()
                ? Mth.clamp(1.0F - blockEntity.getDissolveProgress(gameTime, partialTicks) / LibationSlabBlockEntity.DISSOLVE_TIME, MIN_DISSOLVE_SCALE, 1.0F)
                : 1.0F;
        LiquidRenderer.extractWater(state.water, LibationGeometry.SLAB_WATER, blockEntity.getWater());
    }

    @Override
    public void submit(LibationSlabRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        state.water.lightCoords = state.lightCoords;
        LiquidRenderer.submit(state.water, poseStack, submitNodeCollector, this.sprites);

        if (state.spoutSide != null) {
            poseStack.pushPose();
            LibationGeometry.applyFacing(poseStack, state.spoutSide);
            SpoutFilmRenderer.submitWaterFace(state.water, poseStack, submitNodeCollector, this.sprites);
            poseStack.popPose();
        }

        if (!state.items.isEmpty()) {
            float surfaceY = state.water.visible
                    ? state.water.surfaceY + LibationGeometry.ITEM_LIFT
                    : LibationGeometry.SLAB_SURFACE_Y * LibationGeometry.PIXEL;
            float center = LibationGeometry.SLAB_ITEM_CENTER * LibationGeometry.PIXEL;
            FlatItems.submit(poseStack, submitNodeCollector, state.items.get(0), center, surfaceY, center, 0.0F,
                    ITEM_SCALE * state.dissolveScale, state.lightCoords);
        }
    }

    /**
     * A single copy of the stack, or an empty list for an empty stack. The slab shows one item regardless of the count.
     */
    private static List<ItemStack> single(ItemStack stack) {
        return stack.isEmpty() ? List.of() : List.of(stack.copyWithCount(1));
    }
}
