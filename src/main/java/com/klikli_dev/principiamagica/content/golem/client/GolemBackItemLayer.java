// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Renders the golem's back item on the bone named {@link #BACK_BONE}, flat, as the item lies on the back. The bone sets the place and
 * the direction. The hand item is rendered by the built-in hand layer on the right hand bone.
 */
public class GolemBackItemLayer extends BlockAndItemGeoLayer<GolemEntity, Void, GolemRenderState> {
    public static final String BACK_BONE = "BackItem";
    private static final ItemDisplayContext DISPLAY_CONTEXT = ItemDisplayContext.FIXED;

    public GolemBackItemLayer(EntityRendererProvider.Context context, GeoRenderer<GolemEntity, Void, GolemRenderState> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(GolemEntity animatable, @Nullable Void relatedObject, GolemRenderState renderState, float partialTick) {
        ItemStack stack = animatable.getBackItem();
        if (stack.isEmpty()) {
            return List.of();
        }

        return List.of(RenderData.item(BACK_BONE, DISPLAY_CONTEXT, RenderUtil.createRenderStateForItem(stack, this.itemModelResolver, DISPLAY_CONTEXT, animatable)));
    }

    @Override
    public void addRenderData(GolemEntity animatable, @Nullable Void relatedObject, GolemRenderState renderState, float partialTick) {
        List<RenderData> contents = this.getRelevantBones(animatable, relatedObject, renderState, partialTick);
        if (!contents.isEmpty()) {
            renderState.addGeckolibData(CONTENTS, contents);
        }
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState stackState, ItemDisplayContext displayContext, GolemRenderState renderState, SubmitNodeCollector renderTasks, int packedLight) {
        // Lay the item flat: the sprite stands upright in its own plane, the back needs it tilted onto the bone.
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        super.submitItemStackRender(poseStack, bone, stackState, displayContext, renderState, renderTasks, packedLight);
        poseStack.popPose();
    }
}
