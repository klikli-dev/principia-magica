// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.trough.CatchTroughBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CatchTroughRenderer implements BlockEntityRenderer<CatchTroughBlockEntity, LiquidRenderState> {

    private static final float ITEM_Y = 4.0F;
    private static final float BOB_AMPLITUDE = 0.6F;
    private static final float BOB_SPEED = 0.08F;
    private static final float SPIN_SPEED = 2.0F;
    private static final float ITEM_SCALE = 0.3125F;

    private final ItemModelResolver itemModelResolver;
    private final SpriteGetter sprites;

    public CatchTroughRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.sprites = context.sprites();
    }

    @Override
    public LiquidRenderState createRenderState() {
        return new LiquidRenderState();
    }

    @Override
    public void extractRenderState(CatchTroughBlockEntity blockEntity, LiquidRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        long gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0L;
        LiquidRenderer.extract(state, LibationGeometry.TROUGH, blockEntity.getDisplayedPrinciples(gameTime, partialTicks), blockEntity.getPrinciples().blendedColor());
        this.itemModelResolver.updateForTopItem(state.item, blockEntity.getItem(), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, (int) blockEntity.getBlockPos().asLong());
        state.time = gameTime + partialTicks;
    }

    @Override
    public void submit(LiquidRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        LiquidRenderer.submit(state, poseStack, submitNodeCollector, this.sprites);

        if (!state.item.isEmpty()) {
            float bob = Mth.sin(state.time * BOB_SPEED) * BOB_AMPLITUDE;
            FlatItems.submit(poseStack, submitNodeCollector, state.item,
                    8.0F * LibationGeometry.PIXEL,
                    (ITEM_Y + bob) * LibationGeometry.PIXEL,
                    8.0F * LibationGeometry.PIXEL,
                    state.time * SPIN_SPEED, ITEM_SCALE, state.lightCoords);
        }
    }
}
