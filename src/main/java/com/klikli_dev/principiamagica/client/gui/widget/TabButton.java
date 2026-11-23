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
 * A tab of a vertical tab rail: a 32x28 tab with a 16x16 icon. The selected tab sits over the edge of the panel it belongs to, so its
 * right side is drawn into the panel. An unavailable tab is drawn dimmed and shows its unavailable tooltip instead of its title.
 * Usually placed by a {@link TabBar}.
 */
public class TabButton extends AbstractWidget {

    public static final int WIDTH = 32;
    public static final int HEIGHT = 28;
    private static final int ICON_SIZE = 16;
    /**
     * The icon is centred on the part of the tab that is not covered by the panel.
     */
    private static final int ICON_X = 6;
    private static final int ICON_Y = (HEIGHT - ICON_SIZE) / 2;
    private static final int DISABLED_TINT = 0xFF9A9A9A;

    private final TabIcon icon;
    private final Component title;
    private final Runnable onPress;
    private StyleContext style = WidgetParts.defaultStyle();
    private boolean selected;
    private Component unavailableTooltip = Component.empty();

    /**
     * @param x left edge
     * @param y top edge
     * @param icon the icon drawn in the center
     * @param title the tooltip title
     * @param onPress run when the button is clicked
     */
    public TabButton(int x, int y, TabIcon icon, Component title, Runnable onPress) {
        super(x, y, WIDTH, HEIGHT, title);
        this.icon = icon;
        this.title = title;
        this.onPress = onPress;
        this.setTooltip(Tooltip.create(title));
    }

    public TabButton withStyle(StyleContext style) {
        this.style = style;
        return this;
    }

    /**
     * @param tooltip shown instead of the title while the tab is unavailable, e.g. why it cannot be selected
     */
    public TabButton withUnavailableTooltip(Component tooltip) {
        this.unavailableTooltip = tooltip;
        return this;
    }

    /**
     * Enables or disables the tab. A disabled tab is dimmed, cannot be clicked, and shows the unavailable tooltip.
     */
    public void setAvailable(boolean available) {
        if (this.active == available) {
            return;
        }

        this.active = available;
        this.setTooltip(Tooltip.create(available ? this.title : this.unavailableTooltip));
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean selected() {
        return this.selected;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GuiSprite sprite;
        if (this.selected) {
            sprite = this.style.get(WidgetParts.TAB, GuiStyleProperties.ON_SPRITE, GuiStyles.sprite("tab_selected"));
        } else if (this.isHovered() && this.active) {
            sprite = this.style.get(WidgetParts.TAB, GuiStyleProperties.HOVER_SPRITE, GuiStyles.sprite("tab_hover"));
        } else {
            sprite = this.style.get(WidgetParts.TAB, GuiStyleProperties.SPRITE, GuiStyles.sprite("tab"));
        }

        if (!this.active) {
            sprite = sprite.tinted(DISABLED_TINT);
        }

        sprite.extractRenderState(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        this.icon.extract(graphics, this.getX() + ICON_X, this.getY() + ICON_Y);
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
