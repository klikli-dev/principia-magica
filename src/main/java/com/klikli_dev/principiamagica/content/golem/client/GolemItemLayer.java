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
 * Draws one of the golem's held items on a bone. What is drawn follows {@link GolemEntity#isToolDrawnOnBack()}, so the tool changes
 * places with the stow and unstow animations at their switch point.
 */
public class GolemItemLayer extends BlockAndItemGeoLayer<GolemEntity, Void, GolemRenderState> {

    /**
     * The middle of the hands. The hands hang at x = ±5.25 and y = 4.5, the torso pivot is at y = 6, so the middle is 1.5 pixels below it.
     */
    private static final float CARRY_OFFSET_Y = 1.5F / 16.0F;

    /**
     * Where an item is drawn, and which item it is.
     */
    public enum Placement {
        /**
         * The tool in the right hand. Not drawn while the tool is stowed on the back.
         */
        TOOL_IN_HAND("RightHandItem", ItemDisplayContext.THIRD_PERSON_RIGHT_HAND),
        /**
         * A carried item, which is not the tool. Drawn in the middle of the hands, on the torso.
         */
        CARRIED("torso", ItemDisplayContext.THIRD_PERSON_RIGHT_HAND),
        /**
         * The tool on the back. Lies on the back bone, flat.
         */
        TOOL_ON_BACK("BackItem", ItemDisplayContext.FIXED);

        private final String bone;
        private final ItemDisplayContext displayContext;

        Placement(String bone, ItemDisplayContext displayContext) {
            this.bone = bone;
            this.displayContext = displayContext;
        }

        /**
         * @return the item this placement draws right now. Empty while the item is drawn somewhere else.
         */
        ItemStack item(GolemEntity golem) {
            ItemStack hand = golem.getHandItem();
            ItemStack back = golem.getBackItem();
            return switch (this) {
                case TOOL_IN_HAND -> {
                    if (golem.isToolDrawnOnBack()) {
                        yield ItemStack.EMPTY;
                    }
                    if (golem.isTool(hand)) {
                        yield hand;
                    }
                    // During a stow the tool is still drawn in the hand, the synced state already has it on the back.
                    yield hand.isEmpty() && golem.isTool(back) ? back : ItemStack.EMPTY;
                }
                case CARRIED -> hand.isEmpty() || golem.isTool(hand) ? ItemStack.EMPTY : hand;
                case TOOL_ON_BACK -> {
                    if (!golem.isToolDrawnOnBack()) {
                        yield ItemStack.EMPTY;
                    }
                    if (!back.isEmpty()) {
                        yield back;
                    }
                    // During an unstow the tool is still drawn on the back, the synced state already has it in the hand.
                    yield golem.isTool(hand) ? hand : ItemStack.EMPTY;
                }
            };
        }
    }

    private final Placement placement;

    public GolemItemLayer(EntityRendererProvider.Context context, GeoRenderer<GolemEntity, Void, GolemRenderState> renderer, Placement placement) {
        super(context, renderer);
        this.placement = placement;
    }

    @Override
    protected List<RenderData> getRelevantBones(GolemEntity animatable, @Nullable Void relatedObject, GolemRenderState renderState, float partialTick) {
        ItemStack stack = this.placement.item(animatable);
        if (stack.isEmpty()) {
            return List.of();
        }

        ItemDisplayContext displayContext = this.placement.displayContext;
        return List.of(RenderData.item(this.placement.bone, displayContext, RenderUtil.createRenderStateForItem(stack, this.itemModelResolver, displayContext, animatable)));
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
        poseStack.pushPose();
        switch (this.placement) {
            // Lay the item flat on the back: the sprite is in its own plane, which the back bone needs turned.
            case TOOL_ON_BACK -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case CARRIED -> poseStack.translate(0.0F, -CARRY_OFFSET_Y, 0.0F);
            case TOOL_IN_HAND -> {
            }
        }

        super.submitItemStackRender(poseStack, bone, stackState, displayContext, renderState, renderTasks, packedLight);
        poseStack.popPose();
    }
}
