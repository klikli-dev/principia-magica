// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.KeyCombo;
import com.klikli_dev.principiamagica.content.Plurals;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
 * Imprint tablet: copies the workplace of an owned golem and pastes it onto another one. Right-clicking a golem copies its
 * workplace if the tablet has no clipboard, else pastes the clipboard onto the golem. Crouch-right-clicking clears the clipboard.
 */
public class ImprintTabletItem extends Item {

    public ImprintTabletItem(Properties properties) {
        super(properties);
    }

    /**
     * Right-click on an owned golem copies its workplace, or pastes the clipboard if it is set. Crouch-right-click clears the clipboard.
     * Vanilla tries the golem's own interaction first, which passes for a held non-tool, so this is reached for every right-click.
     * The clipboard is written to the held stack from the player, not the stack passed in: in creative mode vanilla passes a copy.
     */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof GolemEntity golem)) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer) || !(golem.level() instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (!player.getUUID().equals(golem.getOwnerUUID())) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.not_owner"));
            return InteractionResult.FAIL;
        }

        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            clear(held, serverPlayer);
        } else {
            copyOrPaste(held, serverPlayer, golem);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            clear(player.getItemInHand(hand), serverPlayer);
        }

        return InteractionResult.SUCCESS;
    }

    private static void clear(ItemStack stack, ServerPlayer player) {
        if (stack.remove(DataComponentRegistry.IMPRINT_CLIPBOARD.get()) != null) {
            player.level().playSound(null, player.blockPosition(), SoundRegistry.IMPRINT_TABLET_CLEAR.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.imprint_tablet.cleared"));
        }
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
            level.playSound(null, golem.blockPosition(), SoundRegistry.IMPRINT_TABLET_COPY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.imprint_tablet.copied",
                    Plurals.of(Plurals.FOOTPRINTS, copied.footprints().size()), Plurals.of(Plurals.CONTAINER_LINKS, copied.links().size())));
        } else {
            BlockPos anchor = StylusItem.anchorOf(golem);
            golem.updateWorkplace(current -> StylusActions.paste(current, clipboard, anchor, level.dimension()));
            level.playSound(null, golem.blockPosition(), SoundRegistry.IMPRINT_TABLET_PASTE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.imprint_tablet.pasted"));
            StylusItem.syncWorkplace(player, golem);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
        Workplace clipboard = stack.get(DataComponentRegistry.IMPRINT_CLIPBOARD.get());
        adder.accept(clipboard == null
                ? Component.translatable("tooltip.principiamagica.imprint_tablet.clipboard_empty").withStyle(ChatFormatting.GRAY)
                : Component.translatable("tooltip.principiamagica.imprint_tablet.clipboard", Plurals.of(Plurals.FOOTPRINTS, clipboard.footprints().size()), Plurals.of(Plurals.CONTAINER_LINKS, clipboard.links().size())).withStyle(ChatFormatting.GRAY));

        adder.accept(Component.translatable("tooltip.principiamagica.imprint_tablet.hint_copy_paste", KeyCombo.RIGHT_CLICK.component()).withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.imprint_tablet.hint_clear", KeyCombo.CROUCH_RIGHT_CLICK.component()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
