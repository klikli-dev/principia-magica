// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.principiamagica.client.gui.GuiStyles;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * A square button with a 16x16 icon that stays pressed while it is selected, e.g. one of several modes.
 * Its look follows its state: disabled, selected (gold), hovered or normal. The owner sets the selected flag, the enabled flag and the
 * tooltip, usually from its tick, because the state can change without the player touching the button.
 */
public class IconToggleButton extends AbstractWidget {

    /**
     * The icon is drawn at half the size of its 32x32 sprite, centred in the button.
     */
    private static final int ICON_SIZE = 16;

    private final GuiSprite icon;
    private final Runnable onPress;
    private StyleContext style = WidgetParts.defaultStyle();
    private boolean selected;

    /**
     * @param x left edge
     * @param y top edge
     * @param size width and height in pixels, the sprites are 20x20
     * @param icon the 16x16 icon
     * @param tooltip the initial tooltip
     * @param onPress run when the button is clicked while it is active
     */
    public IconToggleButton(int x, int y, int size, GuiSprite icon, Component tooltip, Runnable onPress) {
        super(x, y, size, size, tooltip);
        this.icon = icon;
        this.onPress = onPress;
        this.setTooltip(Tooltip.create(tooltip));
    }

    public IconToggleButton withStyle(StyleContext style) {
        this.style = style;
        return this;
    }

    /**
     * @param selected whether the button is the current choice. A selected button draws the gold sprite.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Replaces the tooltip. Creates a new tooltip object, so call it only when the text changes.
     */
    public void setTooltipText(Component tooltip) {
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GuiSprite sprite;
        if (!this.active) {
            sprite = this.style.get(WidgetParts.ICON_BUTTON, WidgetParts.DISABLED_SPRITE, GuiStyles.sprite("button_disabled"));
        } else if (this.selected) {
            sprite = this.style.get(WidgetParts.ICON_BUTTON, GuiStyleProperties.ON_SPRITE, GuiStyles.sprite("button_selected"));
        } else if (this.isHovered()) {
            sprite = this.style.get(WidgetParts.ICON_BUTTON, GuiStyleProperties.HOVER_SPRITE, GuiStyles.sprite("button_hover"));
        } else {
            sprite = this.style.get(WidgetParts.ICON_BUTTON, GuiStyleProperties.SPRITE, GuiStyles.sprite("button"));
        }

        sprite.extractRenderState(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        int iconOffset = (this.getWidth() - ICON_SIZE) / 2;
        this.icon.extractRenderState(graphics, this.getX() + iconOffset, this.getY() + iconOffset, ICON_SIZE, ICON_SIZE);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        WidgetParts.playClickSound();
        this.onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
