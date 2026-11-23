// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * A stylus click on the owned golem: selects or deselects the golem.
 */
final class StylusEntityInteraction {

    private StylusEntityInteraction() {
    }

    static InteractionResult interact(ItemStack stack, ServerPlayer player, GolemEntity golem) {
        UUID selected = stack.get(DataComponentRegistry.STYLUS_SELECTION.get());
        if (StylusItem.cancelPending(stack, player) == StylusItem.CancelOutcome.MOVE_KEPT) {
            // The pending move belongs to the currently selected golem, so the selection must not change until it is restored
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_move_golem_not_found"));
            return InteractionResult.FAIL;
        }

        if (golem.getUUID().equals(selected)) {
            stack.remove(DataComponentRegistry.STYLUS_SELECTION.get());
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.deselected"));
        } else {
            stack.set(DataComponentRegistry.STYLUS_SELECTION.get(), golem.getUUID());
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.selected"));
            StylusItem.syncWorkplace(player, golem);
        }

        return InteractionResult.SUCCESS;
    }
}
