// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceTargeting.TargetedCorner;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceTargeting.TargetedLink;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceTargeting.TargetedZone;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageEraseZone;
import com.klikli_dev.principiamagica.network.messages.MessageLiftPending;
import com.klikli_dev.principiamagica.network.messages.MessagePickUpCorner;
import com.klikli_dev.principiamagica.network.messages.MessageResizeCorner;
import com.klikli_dev.principiamagica.network.messages.MessageTargetLink;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.InputEvent;

/**
 * Turns the player's clicks and scrolls on a workplace into server messages:
 * <ul>
 *     <li>Shift+scroll over a corner: resizes that corner's face up/down and engages a sticky corner lock</li>
 *     <li>Right-click on a corner: picks up that corner so it can be moved and re-placed</li>
 *     <li>Right-click on a container link: cycles it, also when the container is gone. Shift+right-click removes it</li>
 *     <li>Shift+right-click on a corner or zone box: erases the work area (never triggers on golems)</li>
 * </ul>
 */
public class WorkplaceInput {

    /**
     * Handles plain right-click (pick up corner) and sneak-right-click (erase work area on corner or zone box).
     * Golems are never intercepted so selection and Shift+right-click copy/paste always work.
     */
    public static void onInteractionKeyMapping(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null) {
            return;
        }

        if (WorkplaceTargeting.isLookingAtGolem(minecraft)) {
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

        TargetedLink link = WorkplaceTargeting.targetedLink(minecraft);
        boolean linkedContainer = link != null;
        TargetedCorner corner = WorkplaceTargeting.targetedCorner(minecraft);

        if (player.isShiftKeyDown()) {
            if (corner != null && (corner.locked() || !linkedContainer)) {
                event.setCanceled(true);
                event.setSwingHand(true);
                WorkplaceTargeting.clearCornerLock();
                Networking.sendToServer(new MessageEraseZone(corner.golemId(), corner.footprintIndex()));
                return;
            }

            if (linkedContainer) {
                event.setCanceled(true);
                event.setSwingHand(true);
                Networking.sendToServer(new MessageTargetLink(link.golemId(), link.link().pos(), clickedFace(minecraft), true));
                return;
            }

            TargetedZone zoneTarget = WorkplaceTargeting.targetedZone(minecraft);
            if (zoneTarget != null) {
                event.setCanceled(true);
                event.setSwingHand(true);
                WorkplaceTargeting.clearCornerLock();
                Networking.sendToServer(new MessageEraseZone(zoneTarget.golemId(), zoneTarget.hit().index()));
            }
            return;
        }

        if (corner != null && (corner.locked() || !linkedContainer)) {
            event.setCanceled(true);
            event.setSwingHand(true);
            WorkplaceTargeting.clearCornerLock();
            Networking.sendToServer(new MessagePickUpCorner(corner.golemId(), corner.footprintIndex(), corner.cornerIndex()));
        } else if (linkedContainer) {
            event.setCanceled(true);
            event.setSwingHand(true);
            Networking.sendToServer(new MessageTargetLink(link.golemId(), link.link().pos(), clickedFace(minecraft), false));
        }
    }

    /**
     * @return the face of the crosshair block, or up when aiming at air
     */
    private static Direction clickedFace(Minecraft minecraft) {
        BlockHitResult hovered = WorkplaceTargeting.hoveredHit(minecraft);
        return hovered == null ? Direction.UP : hovered.getDirection();
    }

    /**
     * Shift+scroll with a stylus adjusts the pending corner height if there is one, else resizes the targeted (or locked)
     * corner's face up or down and maintains the sticky lock on the corner's starting block.
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

        TargetedCorner corner = WorkplaceTargeting.targetedCorner(minecraft);
        if (corner != null) {
            event.setCanceled(true);
            int cornerToSend = WorkplaceTargeting.lockAndAdvanceCorner(corner, delta);
            Networking.sendToServer(new MessageResizeCorner(corner.golemId(), corner.footprintIndex(), cornerToSend, delta));
        }
    }
}
