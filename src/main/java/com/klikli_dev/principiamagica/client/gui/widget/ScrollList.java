// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A scrollable list of fixed-height rows.
 * <ul>
 *     <li>Virtualised: only rows that intersect the list are rendered, so long lists are cheap.</li>
 *     <li>Pixel-smooth: mouse wheel moves a target offset, the rendered offset eases towards it every {@link #tick()}
 *     and is interpolated with the partial tick, so scrolling does not jump row by row.</li>
 *     <li>Draggable scrollbar on the right edge, shown only if the content does not fit.</li>
 *     <li>Rows are clipped to the list bounds with a scissor and drawn by a {@link ListEntryRenderer}.</li>
 *     <li>{@link #setEntries(List)} replaces the content but keeps the scroll offset (clamped to the new content).</li>
 * </ul>
 *
 * @param <T> the entry type
 */
public class ScrollList<T> extends AbstractWidget {

    public static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_GAP = 2;
    private static final int MIN_THUMB_HEIGHT = 12;
    private static final float EASING = 0.35F;
    private static final float SNAP_DISTANCE = 0.25F;
    private static final float WHEEL_ROWS_PER_NOTCH = 2.0F;
    private static final int DEFAULT_BACKGROUND = 0x26000000;
    private static final int DEFAULT_ROW_HIGHLIGHT = 0x22FFFFFF;
    private static final int DEFAULT_TRACK = 0x33000000;
    private static final int DEFAULT_THUMB = 0xFF8B8B8B;

    private final int rowHeight;
    private final ListEntryRenderer<T> renderer;
    private final Consumer<T> onSelect;
    private StyleContext style = WidgetParts.defaultStyle();
    private Predicate<T> selectable = entry -> true;
    private Component emptyText = Component.empty();
    private List<T> entries = List.of();
    private float previousOffset;
    private float offset;
    private float targetOffset;
    private boolean dragging;
    private double dragStartY;
    private float dragStartOffset;

    /**
     * @param x left edge
     * @param y top edge
     * @param width total width, including the scrollbar column
     * @param height total height
     * @param rowHeight height of one row in pixels
     * @param renderer draws one row
     * @param onSelect called with the entry when a selectable row is clicked
     */
    public ScrollList(int x, int y, int width, int height, int rowHeight, ListEntryRenderer<T> renderer, Consumer<T> onSelect) {
        super(x, y, width, height, Component.empty());
        this.rowHeight = rowHeight;
        this.renderer = renderer;
        this.onSelect = onSelect;
    }

    public ScrollList<T> withStyle(StyleContext style) {
        this.style = style;
        return this;
    }

    /**
     * @param selectable rows for which the predicate is false are drawn but ignore clicks, e.g. section headers
     */
    public ScrollList<T> withSelectable(Predicate<T> selectable) {
        this.selectable = selectable;
        return this;
    }

    public ScrollList<T> withEmptyText(Component emptyText) {
        this.emptyText = emptyText;
        return this;
    }

    /**
     * Replaces the entries. The scroll offset is kept and clamped to the new content height.
     */
    public void setEntries(List<T> entries) {
        this.entries = List.copyOf(entries);
        this.targetOffset = this.clamp(this.targetOffset);
        this.offset = this.clamp(this.offset);
    }

    public List<T> entries() {
        return this.entries;
    }

    /**
     * Scrolls by the given number of pixels. Positive values scroll down.
     */
    public void scrollBy(float pixels) {
        this.targetOffset = this.clamp(this.targetOffset + pixels);
    }

    /**
     * Advances the smooth scrolling animation. Call once per game tick.
     */
    public void tick() {
        this.previousOffset = this.offset;
        float difference = this.targetOffset - this.offset;
        this.offset = Math.abs(difference) < SNAP_DISTANCE ? this.targetOffset : this.offset + difference * EASING;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = this.getX();
        int top = this.getY();
        int right = left + this.getWidth();
        int bottom = top + this.getHeight();
        int rowsRight = right - SCROLLBAR_WIDTH - SCROLLBAR_GAP;

        graphics.fill(left, top, right, bottom, this.style.get(WidgetParts.LIST, GuiStyleProperties.COLOR, DEFAULT_BACKGROUND));

        if (this.entries.isEmpty()) {
            graphics.centeredText(Minecraft.getInstance().font, this.emptyText, left + this.getWidth() / 2, top + (this.getHeight() - 8) / 2,
                    this.style.get(WidgetParts.TEXT, GuiStyleProperties.TEXT_COLOR, WidgetParts.DEFAULT_MUTED_TEXT_COLOR));
            return;
        }

        int scroll = Math.round(Mth.lerp(partialTick, this.previousOffset, this.offset));
        int highlight = this.style.get(WidgetParts.ROW, GuiStyleProperties.COLOR, DEFAULT_ROW_HIGHLIGHT);
        int textColor = this.style.get(WidgetParts.TEXT, GuiStyleProperties.TEXT_COLOR, WidgetParts.DEFAULT_TEXT_COLOR);

        graphics.enableScissor(left, top, right, bottom);
        for (int i = Math.max(0, scroll / this.rowHeight); i < this.entries.size(); i++) {
            int rowY = top + i * this.rowHeight - scroll;
            if (rowY >= bottom) {
                break;
            }

            boolean hovered = this.isHovered() && mouseX < rowsRight && mouseY >= rowY && mouseY < rowY + this.rowHeight;
            if (hovered) {
                graphics.fill(left, rowY, rowsRight, rowY + this.rowHeight, highlight);
            }

            this.renderer.extract(graphics, this.entries.get(i), i, left + 2, rowY, rowsRight - left - 4, this.rowHeight, hovered, textColor);
        }
        graphics.disableScissor();

        if (this.maxOffset() > 0) {
            this.extractScrollbar(graphics, right, top, bottom, scroll);
        }
    }

    private void extractScrollbar(GuiGraphicsExtractor graphics, int right, int top, int bottom, int scroll) {
        int trackLeft = right - SCROLLBAR_WIDTH;
        graphics.fill(trackLeft, top, right, bottom, this.style.get(WidgetParts.SCROLLBAR, GuiStyleProperties.COLOR, DEFAULT_TRACK));

        int thumbHeight = this.thumbHeight();
        int thumbTop = top + Math.round((this.getHeight() - thumbHeight) * (scroll / (float) this.maxOffset()));
        graphics.fill(trackLeft + 1, thumbTop, right - 1, thumbTop + thumbHeight,
                this.style.get(WidgetParts.SCROLLBAR, GuiStyleProperties.TEXT_COLOR, DEFAULT_THUMB));
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (this.maxOffset() > 0 && event.x() >= this.getX() + this.getWidth() - SCROLLBAR_WIDTH) {
            this.dragging = true;
            this.dragStartY = event.y();
            this.dragStartOffset = this.targetOffset;
            return;
        }

        int index = this.rowIndexAt(event.y());
        if (index >= 0 && index < this.entries.size()) {
            T entry = this.entries.get(index);
            if (this.selectable.test(entry)) {
                this.onSelect.accept(entry);
            }
        }
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        this.dragging = false;
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        int travel = this.getHeight() - this.thumbHeight();
        if (!this.dragging || travel <= 0) {
            return;
        }

        float pixelsPerThumbPixel = this.maxOffset() / (float) travel;
        this.targetOffset = this.clamp(this.dragStartOffset + (float) (event.y() - this.dragStartY) * pixelsPerThumbPixel);
        this.offset = this.targetOffset;
        this.previousOffset = this.targetOffset;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.maxOffset() <= 0 || !this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        this.scrollBy((float) -scrollY * this.rowHeight * WHEEL_ROWS_PER_NOTCH);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    private int rowIndexAt(double mouseY) {
        int relative = (int) Math.floor(mouseY - this.getY()) + Math.round(this.offset);
        return Math.floorDiv(relative, this.rowHeight);
    }

    private int maxOffset() {
        return Math.max(0, this.entries.size() * this.rowHeight - this.getHeight());
    }

    private int thumbHeight() {
        int contentHeight = Math.max(1, this.entries.size() * this.rowHeight);
        return Math.max(MIN_THUMB_HEIGHT, this.getHeight() * this.getHeight() / contentHeight);
    }

    private float clamp(float value) {
        return Mth.clamp(value, 0.0F, this.maxOffset());
    }
}
