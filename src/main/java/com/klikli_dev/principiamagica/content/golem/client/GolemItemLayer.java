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

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the golem's held items: the tool in the right hand, a carried item between the hands, and the tool on the back.
 * <p>
 * All items go through this one layer. GeckoLib keeps the items of a layer in one data ticket that every
 * {@link BlockAndItemGeoLayer} shares, so a second item layer would draw the same items again on its own bone.
 * <p>
 * The tool moves with the stow and unstow animations: see {@link GolemEntity#isToolDrawnOnBack()}.
 */
public class GolemItemLayer extends BlockAndItemGeoLayer<GolemEntity, Void, GolemRenderState> {

    static final String HAND_BONE = "RightHandItem";
    static final String TORSO_BONE = "torso";
    static final String BACK_BONE = "BackItem";

    /**
     * The middle of the hands. The hands hang at x = ±5.25 and y = 4.5, the torso pivot is at y = 6, so the middle is 1.5 pixels below it.
     */
    private static final float CARRY_OFFSET_Y = 1.5F / 16.0F;

    /**
     * Where an item is drawn, and which item it is.
     */
    private enum Placement {
        /**
         * The tool in the right hand, held like a player holds it. Not drawn while the tool is drawn on the back.
         */
        TOOL_IN_HAND(HAND_BONE, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND),
        /**
         * A carried item, which is not a tool. Drawn in the middle of the hands, on the torso.
         */
        CARRIED(TORSO_BONE, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND),
        /**
         * The tool on the back. Lies on the back bone.
         */
        TOOL_ON_BACK(BACK_BONE, ItemDisplayContext.FIXED);

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
                    if (golem.isHeldTool(hand)) {
                        yield hand;
                    }
                    // During a stow the tool is still drawn in the hand, the synced state already has it on the back.
                    yield hand.isEmpty() && !back.isEmpty() ? back : ItemStack.EMPTY;
                }
                case CARRIED -> hand.isEmpty() || golem.isHeldTool(hand) ? ItemStack.EMPTY : hand;
                case TOOL_ON_BACK -> {
                    if (!golem.isToolDrawnOnBack()) {
                        yield ItemStack.EMPTY;
                    }
                    if (!back.isEmpty()) {
                        yield back;
                    }
                    // During an unstow the tool is still drawn on the back, the synced state already has it in the hand.
                    yield golem.isHeldTool(hand) ? hand : ItemStack.EMPTY;
                }
            };
        }
    }

    public GolemItemLayer(EntityRendererProvider.Context context, GeoRenderer<GolemEntity, Void, GolemRenderState> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(GolemEntity animatable, @Nullable Void relatedObject, GolemRenderState renderState, float partialTick) {
        List<RenderData> items = new ArrayList<>();
        for (Placement placement : Placement.values()) {
            ItemStack stack = placement.item(animatable);
            if (!stack.isEmpty()) {
                items.add(RenderData.item(placement.bone, placement.displayContext,
                        RenderUtil.createRenderStateForItem(stack, this.itemModelResolver, placement.displayContext, animatable)));
            }
        }

        return items;
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
        if (TORSO_BONE.equals(bone.name())) {
            poseStack.translate(0.0F, -CARRY_OFFSET_Y, 0.0F);
        }

        if (BACK_BONE.equals(bone.name())) {
            // The flat item lies in the plane of the back. It is turned about the normal of the back, so it runs up the spine instead of across it.
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }

        if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            // The hand transform of GeckoLib's ItemInHandGeoLayer: the handle is gripped and the head turns across the arm.
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            poseStack.translate(0.0F, 0.125F, -0.0625F);
        }

        super.submitItemStackRender(poseStack, bone, stackState, displayContext, renderState, renderTasks, packedLight);
        poseStack.popPose();
    }
}
