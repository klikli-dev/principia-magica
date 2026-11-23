// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprites;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * A square tab button with an icon. Shows its title as a tooltip and has a selected state.
 * Usually placed by a {@link TabBar}.
 */
public class TabButton extends AbstractWidget {

    /**
     * Width and height of the button in pixels.
     */
    public static final int SIZE = 28;
    private static final int ICON_SIZE = 16;

    private final TabIcon icon;
    private final Runnable onPress;
    private StyleContext style = WidgetParts.defaultStyle();
    private boolean selected;

    /**
     * @param x left edge
     * @param y top edge
     * @param icon the icon drawn in the center
     * @param title the tooltip title
     * @param onPress run when the button is clicked
     */
    public TabButton(int x, int y, TabIcon icon, Component title, Runnable onPress) {
        super(x, y, SIZE, SIZE, title);
        this.icon = icon;
        this.onPress = onPress;
        this.setTooltip(Tooltip.create(title));
    }

    public TabButton withStyle(StyleContext style) {
        this.style = style;
        return this;
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
            sprite = this.style.get(WidgetParts.TAB, GuiStyleProperties.ON_SPRITE, GuiSprites.FILTER_BUTTON_DOWN);
        } else if (this.isHovered()) {
            sprite = this.style.get(WidgetParts.TAB, GuiStyleProperties.HOVER_SPRITE, GuiSprites.FILTER_BUTTON_HOVER);
        } else {
            sprite = this.style.get(WidgetParts.TAB, GuiStyleProperties.SPRITE, GuiSprites.FILTER_BUTTON);
        }

        sprite.extractRenderState(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        this.icon.extract(graphics, this.getX() + (this.getWidth() - ICON_SIZE) / 2, this.getY() + (this.getHeight() - ICON_SIZE) / 2);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
