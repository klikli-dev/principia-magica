// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A row of mutually exclusive options. Exactly one option is drawn as selected; clicking another option calls
 * {@code onSelect} with it. The selected option is read from a supplier each frame, so the control follows the data.
 *
 * @param <E> the option type
 */
public class SegmentedControl<E> extends AbstractWidget {

    private final List<E> options;
    private final Function<E, Component> labels;
    private final Supplier<E> selected;
    private final Consumer<E> onSelect;
    private StyleContext style = WidgetParts.defaultStyle();
    private @Nullable Function<E, Component> tooltips;
    private int tooltipSegment = -2;

    /**
     * @param x left edge
     * @param y top edge
     * @param width total width, split evenly between the options
     * @param height height in pixels
     * @param options the options, left to right
     * @param labels the label of each option
     * @param selected the currently selected option
     * @param onSelect called with the clicked option if it is not the selected one
     */
    public SegmentedControl(int x, int y, int width, int height, List<E> options, Function<E, Component> labels, Supplier<E> selected, Consumer<E> onSelect) {
        super(x, y, width, height, Component.empty());
        this.options = List.copyOf(options);
        this.labels = labels;
        this.selected = selected;
        this.onSelect = onSelect;
    }

    public SegmentedControl<E> withStyle(StyleContext style) {
        this.style = style;
        return this;
    }

    /**
     * @param tooltips the tooltip of the option under the mouse
     */
    public SegmentedControl<E> withTooltip(Function<E, Component> tooltips) {
        this.tooltips = tooltips;
        return this;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        E current = this.selected.get();
        int hovered = this.isHovered() ? this.segmentAt(mouseX) : -1;
        if (this.tooltips != null && hovered != this.tooltipSegment) {
            this.tooltipSegment = hovered;
            this.setTooltip(hovered >= 0 ? Tooltip.create(this.tooltips.apply(this.options.get(hovered))) : null);
        }

        int textColor = this.style.get(WidgetParts.SEGMENT, GuiStyleProperties.TEXT_COLOR, WidgetParts.DEFAULT_TEXT_COLOR);
        for (int i = 0; i < this.options.size(); i++) {
            E option = this.options.get(i);
            int left = this.segmentLeft(i);
            int right = this.segmentLeft(i + 1);

            GuiSprite sprite;
            if (option.equals(current)) {
                sprite = this.style.get(WidgetParts.SEGMENT, GuiStyleProperties.ON_SPRITE, GuiSprites.FILTER_BUTTON_DOWN);
            } else if (i == hovered) {
                sprite = this.style.get(WidgetParts.SEGMENT, GuiStyleProperties.HOVER_SPRITE, GuiSprites.FILTER_BUTTON_HOVER);
            } else {
                sprite = this.style.get(WidgetParts.SEGMENT, GuiStyleProperties.SPRITE, GuiSprites.FILTER_BUTTON);
            }

            sprite.extractRenderState(graphics, left, this.getY(), right - left, this.getHeight());
            graphics.centeredText(Minecraft.getInstance().font, this.labels.apply(option), (left + right) / 2, this.getY() + (this.getHeight() - 8) / 2, textColor);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        E option = this.options.get(this.segmentAt(event.x()));
        if (!option.equals(this.selected.get())) {
            this.onSelect.accept(option);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    private int segmentAt(double mouseX) {
        int index = (int) ((mouseX - this.getX()) * this.options.size() / this.getWidth());
        return Math.max(0, Math.min(index, this.options.size() - 1));
    }

    private int segmentLeft(int index) {
        return this.getX() + index * this.getWidth() / this.options.size();
    }
}
