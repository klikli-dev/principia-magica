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
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
 * An empty phial is the open body. A phial holding a principle is the closed body with the label painted on the belly,
 * and the icon of the principle on the belly with a dark outline. Shift in the GUI shows the principle at full size.
 * <p>
 * The body is a dummy item (flat model) drawn in the same pose. The principle icon is a raw texture, so it is drawn as a
 * custom quad with its outline.
 */
public class ClayPhialRenderer implements SpecialModelRenderer<ClayPhialRenderState> {

    /**
     * Whether holding shift shows the principle at full size. Disabled outside the GUI.
     */
    private final boolean shiftView;

    public ClayPhialRenderer(boolean shiftView) {
        this.shiftView = shiftView;
    }

    private static final float PIXEL = 1f / 16f;

    /**
     * The belly label is centred at canvas y 11.0 in the art. The icon sits 3.25 px below the item centre at 8, which is
     * canvas y 11.25, a quarter pixel above the old 11.5.
     */
    private static final float BELLY_OFFSET_Y = -3.25f * PIXEL;

    /**
     * Principle icon at 0.252 of the canvas, which is about 4 px. The outline is one texel of the icon, so it follows.
     */
    private static final float ICON_SCALE = 0.252f;

    /**
     * Pushes the principle icon in front of the body. The body's front face is at 0.5 px.
     */
    private static final float ICON_Z = 1.5f * PIXEL;

    /**
     * Principle icon shown at full size while shift is held.
     */
    private static final float FULL_ICON_SCALE = 1f;

    /**
     * Colour of the outline. It multiplies the principle texture, so it is a dark silhouette.
     */
    private static final int OUTLINE_COLOR = 0xFF2B1D14;

    /**
     * Colour of the icon itself. White leaves the texture unchanged.
     */
    private static final int ICON_COLOR = 0xFFFFFFFF;

    /**
     * The outline sits this far behind the icon, so the icon always draws over it.
     */
    private static final float OUTLINE_BEHIND = 0.5f * PIXEL;

    @Override
    public void submit(@Nullable ClayPhialRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean hasFoil, int outlineColor) {
        if (state == null) return;

        // The base model has no elements, so the origin is the corner of the unit cube. Move it to the centre so that the
        // sub-items are centred.
        poseStack.translate(0.5f, 0.5f, 0.5f);

        // Shift in the GUI shows only the principle at full size.
        Identifier principle = state.principle();
        if (state.shiftDown() && principle != null) {
            this.submitPrinciple(principle, FULL_ICON_SCALE, 0f, 0f, poseStack, submitNodeCollector, light, overlay);
            return;
        }

        // Empty phials are open. Phials holding a principle use the body with the label painted on.
        Item body = principle == null ? ItemRegistry.CLAY_PHIAL_OPEN_ICON.get() : ItemRegistry.CLAY_PHIAL_LABELED_ICON.get();
        this.submitItem(new ItemStack(body), ItemDisplayContext.NONE, poseStack, submitNodeCollector, light, overlay, outlineColor);

        if (principle == null) return;

        this.submitPrinciple(principle, ICON_SCALE, BELLY_OFFSET_Y, ICON_Z, poseStack, submitNodeCollector, light, overlay);
    }

    private void submitItem(ItemStack itemStack, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, int outlineColor) {
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemModelResolver().updateForTopItem(renderState, itemStack, displayContext, minecraft.level, null, 0);
        renderState.submit(poseStack, submitNodeCollector, light, overlay, outlineColor);
    }

    /**
     * Draws the principle icon as a square of the given size, centred on the given offset, with a one-texel dark outline
     * behind it. The outline is eight copies of the square, offset by one texel of the icon in each direction, so the
     * outline is one texel of the principle texture wide, not one phial pixel. All copies go into one buffer.
     * <p>
     * The icon uses the item cutout render type, like item quads, so it uses the item light and overlay.
     *
     * @param iconZ the z of the icon. The outline is placed {@link #OUTLINE_BEHIND} further away.
     */
    private void submitPrinciple(Identifier principle, float size, float offsetY, float iconZ, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay) {
        RenderType renderType = RenderTypes.itemCutout(Principle.texture(principle));
        float half = size / 2f;
        float texel = size / 16f;
        float outlineZ = iconZ - OUTLINE_BEHIND;

        poseStack.pushPose();
        poseStack.translate(0f, offsetY, 0f);
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    addPrincipleQuad(pose, buffer, half, dx * texel, dy * texel, outlineZ, OUTLINE_COLOR, light, overlay);
                }
            }
            addPrincipleQuad(pose, buffer, half, 0f, 0f, iconZ, ICON_COLOR, light, overlay);
        });
        poseStack.popPose();
    }

    /**
     * Adds one square facing the viewer (+Z), centred on (dx, dy) at depth z. The texture's top row is at the top.
     */
    private static void addPrincipleQuad(PoseStack.Pose pose, VertexConsumer buffer, float half, float dx, float dy, float z, int color, int light, int overlay) {
        addPrincipleVertex(pose, buffer, -half + dx, half + dy, z, 0f, 0f, color, light, overlay);
        addPrincipleVertex(pose, buffer, -half + dx, -half + dy, z, 0f, 1f, color, light, overlay);
        addPrincipleVertex(pose, buffer, half + dx, -half + dy, z, 1f, 1f, color, light, overlay);
        addPrincipleVertex(pose, buffer, half + dx, half + dy, z, 1f, 0f, color, light, overlay);
    }

    /**
     * Supplies every element of the item cutout vertex format (position, colour, UV0, UV1 overlay, UV2 light, normal).
     * Missing elements make the buffer fail when the vertex is ended.
     */
    private static void addPrincipleVertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int color, int light, int overlay) {
        buffer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0f, 0f, 1f);
    }

    @Override
    public @Nullable ClayPhialRenderState extractArgument(ItemStack stack) {
        if (stack.isEmpty()) return null;

        PrincipleStack stored = ClayPhialItem.getStored(stack);
        Identifier principle = stored == null ? null : PrincipiaMagicaRegistries.PRINCIPLE.getKey(stored.principle().value());
        boolean shiftDown = this.shiftView && Minecraft.getInstance().hasShiftDown();
        return new ClayPhialRenderState(principle, shiftDown);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(-0.5f, -0.5f, -0.5f));
        output.accept(new Vector3f(0.5f, 0.5f, 0.5f));
    }

    /**
     * @param shiftView whether holding shift shows the principle at full size. Only the GUI uses this, so the item
     *                  definition enables it for the GUI display context only.
     */
    public record Unbaked(boolean shiftView) implements SpecialModelRenderer.Unbaked<ClayPhialRenderState> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("shift_view", false).forGetter(Unbaked::shiftView)
        ).apply(instance, Unbaked::new));

        @Override
        public @Nullable SpecialModelRenderer<ClayPhialRenderState> bake(SpecialModelRenderer.BakingContext context) {
            return new ClayPhialRenderer(this.shiftView);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<ClayPhialRenderState>> type() {
            return MAP_CODEC;
        }
    }
}
