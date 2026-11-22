// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared rendering of up to four items lying flat on a surface, in the same way as the campfire renders its items.
 */
public final class FlatItems {

    private FlatItems() {
    }

    public static List<ItemStackRenderState> extract(ItemModelResolver itemModelResolver, Level level, List<ItemStack> stacks, BlockPos pos) {
        int seed = (int) pos.asLong();
        List<ItemStackRenderState> states = new ArrayList<>(stacks.size());
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStackRenderState itemState = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(itemState, stacks.get(slot), ItemDisplayContext.FIXED, level, null, seed + slot);
            states.add(itemState);
        }
        return states;
    }

    /**
     * Submits the items at the four slot positions of a surface at the given height in blocks, rotated by the facing.
     * The dissolving slot of the state is scaled by its dissolve scale.
     */
    public static void submitSlots(PoseStack poseStack, SubmitNodeCollector collector, FlatItemsRenderState state, float surfaceY, float scale, int light) {
        poseStack.pushPose();
        LibationGeometry.applyFacing(poseStack, state.facing);
        for (int slot = 0; slot < state.items.size(); slot++) {
            ItemStackRenderState item = state.items.get(slot);
            if (!item.isEmpty()) {
                float slotScale = slot == state.dissolvingSlot ? scale * state.dissolveScale : scale;
                submit(poseStack, collector, item,
                        LibationGeometry.SLOT_X[slot] * LibationGeometry.PIXEL,
                        surfaceY,
                        LibationGeometry.SLOT_Z[slot] * LibationGeometry.PIXEL,
                        0.0F, slotScale, light);
            }
        }
        poseStack.popPose();
    }

    /**
     * Submits one item lying flat, centred on the given position in blocks, spun around the vertical axis by the given degrees.
     */
    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, ItemStackRenderState item, float x, float y, float z, float spinDegrees, float scale, int light) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(spinDegrees));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
