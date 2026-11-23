// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.screen.ContentRect;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.widget.GuiSpriteWidget;
import com.klikli_dev.codedefinedgui.api.widget.TooltipAreaWidget;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenuLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

/**
 * Decorations around the menu slots. A slot's item position is {@code slot.x} and {@code slot.y} on the screen. The slot sprites and the
 * ghost icons are drawn relative to that position.
 */
final class GolemSlotWidgets {

    private GolemSlotWidgets() {
    }

    /**
     * @param screen the screen's rectangle, the slot positions are relative to it
     * @param slot the slot whose sprite is drawn
     * @param sprite the slot sprite
     * @param dx horizontal offset of the sprite from the item position, e.g. -1 for an 18x18 slot
     * @param dy vertical offset of the sprite from the item position
     * @return a widget that draws the slot sprite
     */
    static GuiSpriteWidget background(ContentRect screen, Slot slot, GuiSprite sprite, int dx, int dy) {
        return new GuiSpriteWidget(screen.x() + slot.x + dx, screen.y() + slot.y + dy, sprite.width(), sprite.height(), sprite);
    }

    /**
     * @return a 16x16 sprite drawn on the item position of the slot, e.g. the ghost icon of an empty slot
     */
    static GuiSpriteWidget itemSprite(ContentRect screen, Slot slot, GuiSprite sprite) {
        return new GuiSpriteWidget(screen.x() + slot.x, screen.y() + slot.y, sprite.width(), sprite.height(), sprite);
    }

    /**
     * @return an invisible tooltip area covering the 16x16 item position of the slot and its 1 pixel border
     */
    static TooltipAreaWidget hint(ContentRect screen, Slot slot, Component tooltip) {
        return new TooltipAreaWidget(screen.x() + slot.x - 1, screen.y() + slot.y - 1, GolemMenuLayout.SLOT_SIZE, GolemMenuLayout.SLOT_SIZE, tooltip);
    }
}
