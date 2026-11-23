// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceTargeting.Targeted;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceTargeting.TargetedZone;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.network.messages.MessageEraseGlyph;
import com.klikli_dev.principiamagica.network.messages.MessageEraseZone;
import com.klikli_dev.principiamagica.network.messages.MessageLiftCorner;
import com.klikli_dev.principiamagica.network.messages.MessageLiftPending;
import com.klikli_dev.principiamagica.network.messages.MessageShiftZone;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;

import java.util.UUID;

/**
 * Turns the player's clicks and scrolls on a workplace into server messages. Sneak-right-click erases the targeted glyph
 * or zone box, and shift-scroll adjusts the pending corner, the targeted glyph's height, or moves the targeted zone up/down.
 */
public class WorkplaceInput {

    /**
     * Sneak-right-click on a targeted glyph or zone box erases its footprint.
     * Linked containers are left to {@code StylusItem.useOn} so they unlink normally.
     */
    public static void onInteractionKeyMapping(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || !player.isShiftKeyDown()) {
            return;
        }

        ItemStack stylus = WorkplaceTargeting.heldStylus(player);
        if (stylus == null || WorkplaceTargeting.pendingGlyph(stylus, level) != null) {
            return;
        }

        GolemEntity golem = WorkplaceTargeting.findGolem(level, stylus.get(DataComponentRegistry.STYLUS_SELECTION.get()));
        if (golem == null) {
            return;
        }

        Targeted glyphTarget = WorkplaceTargeting.targetedGlyph(minecraft);
        if (glyphTarget != null) {
            event.setCanceled(true);
            event.setSwingHand(true);
            Networking.sendToServer(new MessageEraseGlyph(glyphTarget.golemId(), glyphTarget.hit().glyph().corner()));
            return;
        }

        if (WorkplaceTargeting.isHoveredLinkedContainer(minecraft, golem.getId())) {
            return;
        }

        TargetedZone zoneTarget = WorkplaceTargeting.targetedZone(minecraft);
        if (zoneTarget != null) {
            event.setCanceled(true);
            event.setSwingHand(true);
            Networking.sendToServer(new MessageEraseZone(zoneTarget.golemId(), zoneTarget.hit().index()));
        }
    }

    /**
     * Shift+scroll with a stylus adjusts the pending corner height if there is one, else the targeted glyph's corner height,
     * else moves the targeted zone box up or down.
     */
    public static void onMouseScrolling(InputEvent.MouseScrollingEvent event) {
        if (event.getScrollDeltaY() == 0.0) {
            return;
        }

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
            Networking.sendToServer(new MessageLiftPending(delta));
            return;
        }

        UUID selected = stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        GolemEntity golem = WorkplaceTargeting.findGolem(level, selected);
        if (golem == null) {
            return;
        }

        Targeted target = WorkplaceTargeting.targetedGlyph(minecraft);
        if (target != null) {
            event.setCanceled(true);
            Networking.sendToServer(new MessageLiftCorner(golem.getId(), target.hit().glyph().corner(), delta));
            return;
        }

        TargetedZone zoneTarget = WorkplaceTargeting.targetedZone(minecraft);
        if (zoneTarget != null) {
            event.setCanceled(true);
            Networking.sendToServer(new MessageShiftZone(golem.getId(), zoneTarget.hit().index(), delta));
        }
    }
}
