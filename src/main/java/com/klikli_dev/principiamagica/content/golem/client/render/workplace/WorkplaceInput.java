// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.client.network.ClientNetworking;
import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceTargeting.Targeted;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.network.messages.MessageEraseGlyph;
import com.klikli_dev.principiamagica.network.messages.MessageLiftCorner;
import com.klikli_dev.principiamagica.network.messages.MessageLiftPending;
import com.klikli_dev.principiamagica.network.messages.MessagePickUpGlyph;
import com.klikli_dev.principiamagica.network.messages.MessageShiftZone;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;

import java.util.UUID;

/**
 * Turns the player's clicks and scrolls on a workplace into server messages. Right-click picks up or sneak-right-click erases
 * the targeted glyph, shift-scroll lifts the pending corner, the targeted glyph's corner or moves the footprint under the cursor.
 */
public class WorkplaceInput {

    /**
     * Right-click on a glyph the stylus is targeting picks it up, or sneak-right-click erases its footprint. The click is cancelled
     * when a glyph is hit, so the block behind it is not used and the server does not also run the stylus action for that block.
     */
    public static void onInteractionKeyMapping(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Targeted target = WorkplaceTargeting.targetedGlyph(minecraft);
        if (target == null) {
            return;
        }

        event.setCanceled(true);
        event.setSwingHand(true);
        CarvedGlyph glyph = target.hit().glyph();
        if (minecraft.player != null && minecraft.player.isShiftKeyDown()) {
            ClientNetworking.sendToServer(new MessageEraseGlyph(target.golemId(), glyph.pos(), glyph.face()));
        } else {
            ClientNetworking.sendToServer(new MessagePickUpGlyph(target.golemId(), glyph.pos(), glyph.face()));
        }
    }

    /**
     * Shift+scroll with a stylus lifts the pending corner if there is one, else the targeted glyph's corner, else moves the
     * footprint under the cursor. Instead of changing the hotbar slot.
     */
    public static void onMouseScrolling(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null || !player.isShiftKeyDown()) {
            return;
        }

        ItemStack stylus = WorkplaceTargeting.heldStylus(player);
        if (stylus == null) {
            return;
        }

        int delta = event.getScrollDeltaY() > 0 ? 1 : -1;
        if (WorkplaceTargeting.pendingGlyph(stylus, level) != null) {
            event.setCanceled(true);
            ClientNetworking.sendToServer(new MessageLiftPending(delta));
            return;
        }

        UUID selected = stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        GolemEntity golem = WorkplaceTargeting.findGolem(level, selected);
        if (golem == null) {
            return;
        }

        Targeted target = WorkplaceTargeting.targetedGlyph(minecraft);
        if (target != null) {
            CarvedGlyph glyph = target.hit().glyph();
            event.setCanceled(true);
            ClientNetworking.sendToServer(new MessageLiftCorner(golem.getId(), glyph.pos(), glyph.face(), delta));
            return;
        }

        BlockPos hovered = WorkplaceTargeting.hoveredBlock(minecraft);
        if (hovered == null) {
            return;
        }

        int index = ClientGolemWorkplaceCache.latest(golem.getId())
                .map(entry -> entry.workplace().footprintIndexAt(hovered))
                .orElse(-1);
        if (index < 0) {
            return;
        }

        event.setCanceled(true);
        ClientNetworking.sendToServer(new MessageShiftZone(golem.getId(), index, delta));
    }
}
