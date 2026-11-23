// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.KeyCombo;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Imprint tablet: copies the workplace of an owned golem and pastes it onto another one. Crouch-right-clicking a golem copies
 * its workplace if the tablet has no clipboard, else pastes the clipboard onto the golem. Crouch-right-clicking air clears the clipboard.
 */
public class ImprintTabletItem extends Item {

    public ImprintTabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof GolemEntity golem) || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer) || !(golem.level() instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (!player.getUUID().equals(golem.getOwnerUUID())) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.not_owner"));
            return InteractionResult.FAIL;
        }

        copyOrPaste(stack, serverPlayer, golem);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown() && player.getItemInHand(hand).remove(DataComponentRegistry.IMPRINT_CLIPBOARD.get()) != null) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.imprint_tablet.cleared"));
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Copies the workplace into the clipboard if it is empty, else pastes the clipboard at the golem's anchor.
     */
    private static void copyOrPaste(ItemStack stack, ServerPlayer player, GolemEntity golem) {
        ServerLevel level = (ServerLevel) golem.level();
        Workplace clipboard = stack.get(DataComponentRegistry.IMPRINT_CLIPBOARD.get());
        if (clipboard == null) {
            Workplace workplace = golem.getWorkplace(level).orElse(Workplace.EMPTY);
            Workplace copied = StylusActions.toRelative(workplace, StylusItem.anchorOf(golem), level.dimension());
            stack.set(DataComponentRegistry.IMPRINT_CLIPBOARD.get(), copied);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.imprint_tablet.copied",
                    copied.footprints().size(), copied.links().size()));
        } else {
            BlockPos anchor = StylusItem.anchorOf(golem);
            golem.updateWorkplace(current -> StylusActions.paste(current, clipboard, anchor, level.dimension()));
            player.sendOverlayMessage(Component.translatable("message.principiamagica.imprint_tablet.pasted"));
            StylusItem.syncWorkplace(player, golem);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
        Workplace clipboard = stack.get(DataComponentRegistry.IMPRINT_CLIPBOARD.get());
        adder.accept(clipboard == null
                ? Component.translatable("tooltip.principiamagica.imprint_tablet.clipboard_empty").withStyle(ChatFormatting.GRAY)
                : Component.translatable("tooltip.principiamagica.imprint_tablet.clipboard", clipboard.footprints().size(), clipboard.links().size()).withStyle(ChatFormatting.GRAY));

        adder.accept(Component.translatable("tooltip.principiamagica.imprint_tablet.hint_copy_paste", KeyCombo.CROUCH_RIGHT_CLICK.component()).withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.imprint_tablet.hint_clear", KeyCombo.CROUCH_RIGHT_CLICK.component()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
