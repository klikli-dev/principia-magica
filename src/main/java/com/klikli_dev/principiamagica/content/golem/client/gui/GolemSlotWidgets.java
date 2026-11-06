// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.style.BuiltinGuiParts;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprites;
import com.klikli_dev.codedefinedgui.api.widget.GuiSpriteWidget;
import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.widget.WidgetParts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

/**
 * Slot backgrounds and the greyed overlay for slots that are not usable.
 */
final class GolemSlotWidgets {

    static final int SLOT_SIZE = 18;
    private static final int OVERLAY_COLOR = 0xB0303030;

    private GolemSlotWidgets() {
    }

    /**
     * @return the slot background, drawn one pixel up and left of the slot's item position
     */
    static GuiSpriteWidget background(ContentRect frame, Slot slot) {
        GuiSprite sprite = WidgetParts.defaultStyle().get(BuiltinGuiParts.PLAYER_SLOT, GuiStyleProperties.SPRITE, GuiSprites.INVENTORY_SLOT);
        return new GuiSpriteWidget(frame.x() + slot.x - 1, frame.y() + slot.y - 1, sprite);
    }

    /**
     * A greyed, non-interactive overlay over a slot background. Shows the tooltip on hover.
     *
     * @param x left edge of the slot background
     * @param y top edge of the slot background
     */
    static Overlay overlay(int x, int y, Component tooltip) {
        return new Overlay(x, y, tooltip);
    }

    static final class Overlay extends AbstractWidget {

        Overlay(int x, int y, Component tooltip) {
            super(x, y, SLOT_SIZE, SLOT_SIZE, Component.empty());
            this.active = false;
            this.setTooltip(Tooltip.create(tooltip));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), OVERLAY_COLOR);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }
    }
}
