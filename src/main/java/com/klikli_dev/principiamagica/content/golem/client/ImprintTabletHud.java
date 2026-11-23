// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.ImprintTabletItem;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Crosshair HUD while an imprint tablet is held: shows the clipboard, and what a crouch-right-click does. Over one of the
 * player's own golems this is copy, or paste if the clipboard holds a workplace. Hidden with F1 and while a screen is open.
 */
public class ImprintTabletHud {

    private static final int OFFSET_X = 10;
    private static final int OFFSET_Y = 6;
    private static final int LINE_HEIGHT = 9;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int NEUTRAL_COLOR = 0xFFAAAAAA;

    public void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(PrincipiaMagica.loc("imprint_tablet_hud"), this::render);
    }

    private void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }

        ItemStack tablet = heldTablet(player);
        if (tablet == null) {
            return;
        }

        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + OFFSET_X;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + OFFSET_Y;
        Font font = minecraft.font;

        Workplace clipboard = tablet.get(DataComponentRegistry.IMPRINT_CLIPBOARD.get());
        Component clipboardText = clipboard == null
                ? Component.translatable("hud.principiamagica.imprint_tablet.clipboard_empty")
                : Component.translatable("hud.principiamagica.imprint_tablet.clipboard", clipboard.footprints().size(), clipboard.links().size());
        graphics.text(font, clipboardText, x, y, NEUTRAL_COLOR, true);

        List<Component> hints = hints(minecraft, player, clipboard != null);
        for (int i = 0; i < hints.size(); i++) {
            graphics.text(font, hints.get(i), x, y + (i + 1) * LINE_HEIGHT, TEXT_COLOR, true);
        }
    }

    private static List<Component> hints(Minecraft minecraft, Player player, boolean hasClipboard) {
        List<Component> hints = new ArrayList<>();
        if (minecraft.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof GolemEntity golem
                && player.getUUID().equals(golem.getOwnerUUID())) {
            hints.add(Component.translatable(hasClipboard
                    ? "hud.principiamagica.imprint_tablet.hint.paste"
                    : "hud.principiamagica.imprint_tablet.hint.copy"));
        } else if (hasClipboard) {
            hints.add(Component.translatable("hud.principiamagica.imprint_tablet.hint.clear"));
        }
        return hints;
    }

    /**
     * @return the imprint tablet the player holds in either hand, if any
     */
    private static ItemStack heldTablet(Player player) {
        if (player.getMainHandItem().getItem() instanceof ImprintTabletItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof ImprintTabletItem) {
            return player.getOffhandItem();
        }
        return null;
    }
}
