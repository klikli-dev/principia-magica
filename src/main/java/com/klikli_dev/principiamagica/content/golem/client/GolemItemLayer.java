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
    static final String CARRY_BONE = "arm_left";
    static final String BACK_BONE = "BackItem";

    /**
     * Base carry pose of {@code arm_left} in {@code golem.animation.json} ({@code [-70, 25, 0]} deg).
     * GeckoLib negates X and Y when baking rotations ({@code Ry(-25) * Rx(+70)}), so undoing that base
     * pose at the arm pivot applies {@code Rx(-70) * Ry(+25)} before translating to the center between
     * the hands, leaving only the arm's animated sway.
     */
    private static final float CARRY_ARM_BASE_ROT_X = 70.0F;
    private static final float CARRY_ARM_BASE_ROT_Y = 25.0F;

    /**
     * Offset from the {@code arm_left} pivot (baked {@code (-5, 14, 0)} px) to the carry anchor
     * ({@code (0, 12, -3)} px in torso space), where the raised carrying arms meet ({@code y ≈ 11, z ≈ -7.5..-8})
     * after the third-person hand transform adds {@code (0, -1, -4.5)} px for blocks and {@code (0, 0, -5)} px for flat items.
     */
    private static final float CARRY_OFFSET_X = 5.0F / 16.0F;
    private static final float CARRY_OFFSET_Y = -2.0F / 16.0F;
    private static final float CARRY_OFFSET_Z = -3.0F / 16.0F;

    /**
     * Where an item is drawn, and which item it is.
     */
    private enum Placement {
        /**
         * The tool in the right hand, held like a player holds it. Not drawn while the tool is drawn on the back.
         */
        TOOL_IN_HAND(HAND_BONE, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND),
        /**
         * A carried item, which is not a tool. Bound to the left arm so it sways with the carrying animation while
         * staying centered between the hands and unaffected by right-arm work or stow animations.
         */
        CARRIED(CARRY_BONE, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND),
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
                    // Not drawn on the back here means the stow is still running: the synced state already has the tool on the back, but it
                    // is drawn in the hand until the switch point. Cargo may already fill the hand by then, so the hand is not checked.
                    yield back;
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
        if (CARRY_BONE.equals(bone.name())) {
            poseStack.mulPose(Axis.XN.rotationDegrees(CARRY_ARM_BASE_ROT_X));
            poseStack.mulPose(Axis.YP.rotationDegrees(CARRY_ARM_BASE_ROT_Y));
            poseStack.translate(CARRY_OFFSET_X, CARRY_OFFSET_Y, CARRY_OFFSET_Z);
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
