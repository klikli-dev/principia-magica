// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * A stylus click on the owned golem, while the player is not sneaking: selects or deselects the golem. Sneaking copies the
 * golem workplace to the stylus clipboard, or pastes the clipboard onto the golem.
 */
final class StylusEntityInteraction {

    private StylusEntityInteraction() {
    }

    static InteractionResult interact(ItemStack stack, ServerPlayer player, GolemEntity golem) {
        if (player.isShiftKeyDown()) {
            copyOrPaste(stack, player, golem);
            return InteractionResult.SUCCESS;
        }

        UUID selected = stack.get(DataComponentRegistry.STYLUS_SELECTION.get());
        if (!StylusItem.cancelPending(player, stack)) {
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

    /**
     * Copies the workplace into the clipboard if it is empty, else pastes the clipboard at the golem's anchor.
     */
    private static void copyOrPaste(ItemStack stack, ServerPlayer player, GolemEntity golem) {
        Workplace clipboard = stack.get(DataComponentRegistry.STYLUS_CLIPBOARD.get());
        if (clipboard == null) {
            Workplace workplace = golem.getWorkplace((ServerLevel) golem.level()).orElse(Workplace.EMPTY);
            Workplace copied = StylusActions.toRelative(workplace, StylusItem.anchorOf(golem));
            stack.set(DataComponentRegistry.STYLUS_CLIPBOARD.get(), copied);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.copied",
                    copied.footprints().size(), copied.links().size()));
        } else {
            BlockPos anchor = StylusItem.anchorOf(golem);
            golem.updateWorkplace(current -> StylusActions.paste(current, clipboard, anchor));
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.pasted"));
            StylusItem.syncWorkplace(player, golem);
        }
    }
}
