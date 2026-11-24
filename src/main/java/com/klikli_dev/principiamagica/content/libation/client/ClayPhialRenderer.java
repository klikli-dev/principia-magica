// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.item.ClayPhialItem;
import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Draws a fired clay phial, modelled on the alchemical derivative renderer of Theurgy.
 * <p>
 * An empty phial is the open body. A phial holding a principle is the closed body with a label on the belly and the icon
 * of the principle on top of the label. Shift in the GUI shows the principle at full size.
 * <p>
 * The label and the body are dummy items (flat models) drawn in the same pose. The principle icon is a raw texture, so it
 * is drawn as a custom quad.
 */
public class ClayPhialRenderer implements SpecialModelRenderer<ClayPhialRenderState> {

    private static final float PIXEL = 1f / 16f;

    /**
     * The belly sits 3.5 px below the centre of the item canvas. Label and principle are centred on it.
     */
    private static final float BELLY_OFFSET_Y = -3.5f * PIXEL;

    /**
     * The label texture covers 10x8 px of its 16x16 canvas. Scaled to 0.6 it becomes about 6x4.8 px, which fits the belly.
     */
    private static final float LABEL_SCALE = 0.6f;

    /**
     * Pushes the label in front of the body.
     */
    private static final float LABEL_OFFSET_Z = 1f * PIXEL;

    /**
     * Principle icon at 0.26 of the canvas, which is about 4.2 px. It fits inside the label interior.
     */
    private static final float ICON_SCALE = 0.26f;

    /**
     * Pushes the principle icon in front of the label.
     */
    private static final float ICON_OFFSET_Z = 1.5f * PIXEL;

    /**
     * Principle icon shown at full size while shift is held.
     */
    private static final float FULL_ICON_SCALE = 1f;

    /**
     * Flattens the flat item models so that they behave as planes in the pose.
     */
    private static final float FLATTEN = 0.01f;

    @Override
    public void submit(@Nullable ClayPhialRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean hasFoil, int outlineColor) {
        if (state == null) return;

        // The base model has no elements, so the origin is the corner of the unit cube. Move it to the centre so that the
        // sub-items are centred.
        poseStack.translate(0.5f, 0.5f, 0.5f);

        // Shift in the GUI shows only the principle at full size.
        Identifier principle = state.principle();
        if (state.shiftDown() && principle != null) {
            this.submitPrinciple(principle, FULL_ICON_SCALE, 0f, 0f, poseStack, submitNodeCollector, light);
            return;
        }

        // Empty phials are open. Phials holding a principle are closed with a cork.
        Item body = principle == null ? ItemRegistry.CLAY_PHIAL_OPEN_ICON.get() : ItemRegistry.CLAY_PHIAL_CLOSED_ICON.get();
        this.submitItem(new ItemStack(body), ItemDisplayContext.NONE, poseStack, submitNodeCollector, light, overlay, outlineColor);

        if (principle == null) return;

        // Label on the belly, in front of the body and flattened.
        poseStack.pushPose();
        poseStack.translate(0f, BELLY_OFFSET_Y, LABEL_OFFSET_Z);
        poseStack.scale(LABEL_SCALE, LABEL_SCALE, FLATTEN);
        this.submitItem(new ItemStack(ItemRegistry.PHIAL_LABEL_ICON.get()), ItemDisplayContext.GUI, poseStack, submitNodeCollector, light, overlay, outlineColor);
        poseStack.popPose();

        // Principle icon on the label, slightly further in front.
        this.submitPrinciple(principle, ICON_SCALE, BELLY_OFFSET_Y, ICON_OFFSET_Z, poseStack, submitNodeCollector, light);
    }

    private void submitItem(ItemStack itemStack, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, int outlineColor) {
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemModelResolver().updateForTopItem(renderState, itemStack, displayContext, minecraft.level, null, 0);
        renderState.submit(poseStack, submitNodeCollector, light, overlay, outlineColor);
    }

    /**
     * Draws the 16x16 icon of the principle as a square of the given size, centred on the given offset, facing the viewer.
     * The icon uses the item cutout render type, like item quads, so it is lit and drawn with the item light.
     */
    private void submitPrinciple(Identifier principle, float size, float offsetY, float offsetZ, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light) {
        RenderType renderType = RenderTypes.itemCutout(Principle.texture(principle));
        float half = size / 2f;

        poseStack.pushPose();
        poseStack.translate(0f, offsetY, offsetZ);
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            // Counter-clockwise when seen from +Z, the front of item quads. The texture's top row is at the top.
            addPrincipleVertex(pose, buffer, -half, half, 0f, 0f, light);
            addPrincipleVertex(pose, buffer, -half, -half, 0f, 1f, light);
            addPrincipleVertex(pose, buffer, half, -half, 1f, 1f, light);
            addPrincipleVertex(pose, buffer, half, half, 1f, 0f, light);
        });
        poseStack.popPose();
    }

    private static void addPrincipleVertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float u, float v, int light) {
        buffer.addVertex(pose, x, y, 0f)
                .setColor(-1)
                .setUv(u, v)
                .setLight(light)
                .setNormal(pose, 0f, 0f, 1f);
    }

    @Override
    public @Nullable ClayPhialRenderState extractArgument(ItemStack stack) {
        if (stack.isEmpty()) return null;

        PrincipleStack stored = ClayPhialItem.getStored(stack);
        Identifier principle = stored == null ? null : PrincipiaMagicaRegistries.PRINCIPLE.getKey(stored.principle().value());
        boolean shiftDown = Minecraft.getInstance().hasShiftDown();
        return new ClayPhialRenderState(principle, shiftDown);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(-0.5f, -0.5f, -0.5f));
        output.accept(new Vector3f(0.5f, 0.5f, 0.5f));
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<ClayPhialRenderState> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public @Nullable SpecialModelRenderer<ClayPhialRenderState> bake(SpecialModelRenderer.BakingContext context) {
            return new ClayPhialRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<ClayPhialRenderState>> type() {
            return MAP_CODEC;
        }
    }
}
