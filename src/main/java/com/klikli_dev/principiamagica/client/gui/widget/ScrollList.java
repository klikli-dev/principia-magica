// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.principiamagica.client.gui.GuiStyles;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A scrollable list of fixed-height rows.
 * <ul>
 *     <li>Virtualised: only rows that intersect the list are rendered, so long lists are cheap.</li>
 *     <li>Pixel-smooth: mouse wheel moves a target offset, the rendered offset eases towards it every {@link #tick()}
 *     and is interpolated with the partial tick, so scrolling does not jump row by row.</li>
 *     <li>Draggable scrollbar on the right edge, shown only if the content does not fit. Its sprites come from the style.</li>
 *     <li>Rows are clipped to the list bounds with a scissor and drawn by a {@link ListEntryRenderer}.</li>
 *     <li>Optional per-row tooltip: while a row is hovered, the list shows the tooltip of that row.</li>
 *     <li>Keyboard: while the list is focused, the arrow keys move a focus row and Enter or Space selects it. The focus row is scrolled into view.</li>
 *     <li>Optional narration: the hovered row, or the focus row while the list is focused, is narrated with its text.</li>
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

    private final int rowHeight;
    private final ListEntryRenderer<T> renderer;
    private final Consumer<T> onSelect;
    private StyleContext style = WidgetParts.defaultStyle();
    private Function<T, Component> tooltips = entry -> null;
    private Function<T, Component> narrations = entry -> null;
    private List<T> entries = List.of();
    private float previousOffset;
    private float offset;
    private float targetOffset;
    private boolean dragging;
    private double dragStartY;
    private float dragStartOffset;
    private int hoveredRow = -1;
    private int focusedRow = -1;

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
     * @param tooltips the tooltip of a row, or null for rows without one. Shown while the row is hovered.
     */
    public ScrollList<T> withTooltips(Function<T, Component> tooltips) {
        this.tooltips = tooltips;
        return this;
    }

    /**
     * @param narrations the text a row is narrated with, e.g. its name and state, or null for rows without narration. Called when the
     *                   narration is built, so it reads the current state of the entry.
     */
    public ScrollList<T> withNarration(Function<T, Component> narrations) {
        this.narrations = narrations;
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

        int scroll = Math.round(Mth.lerp(partialTick, this.previousOffset, this.offset));
        int hoveredIndex = this.isHovered() && mouseX < rowsRight ? this.rowIndexAt(mouseY) : -1;
        this.updateHoveredRow(hoveredIndex);

        if (this.entries.isEmpty()) {
            return;
        }

        int highlight = this.style.color(WidgetParts.ROW, GuiStyles.ROW_HIGHLIGHT);
        int textColor = this.style.textColor(WidgetParts.TEXT, WidgetParts.DEFAULT_TEXT_COLOR);

        graphics.enableScissor(left, top, right, bottom);
        for (int i = Math.max(0, scroll / this.rowHeight); i < this.entries.size(); i++) {
            int rowY = top + i * this.rowHeight - scroll;
            if (rowY >= bottom) {
                break;
            }

            boolean hovered = i == hoveredIndex || (this.isFocused() && i == this.focusedRow);
            if (hovered) {
                graphics.fill(left, rowY, rowsRight, rowY + this.rowHeight, highlight);
            }

            this.renderer.extract(graphics, this.entries.get(i), i, left + 2, rowY, rowsRight - left - 4, this.rowHeight, hovered, textColor);
        }
        graphics.disableScissor();

        if (this.maxOffset() > 0) {
            this.extractScrollbar(graphics, right, top, bottom, scroll, mouseX, mouseY);
        }
    }

    private void updateHoveredRow(int row) {
        if (row == this.hoveredRow) {
            return;
        }

        this.hoveredRow = row;
        Component tooltip = row >= 0 && row < this.entries.size() ? this.tooltips.apply(this.entries.get(row)) : null;
        this.setTooltip(tooltip != null ? Tooltip.create(tooltip) : null);
    }

    private void extractScrollbar(GuiGraphicsExtractor graphics, int right, int top, int bottom, int scroll, int mouseX, int mouseY) {
        int trackLeft = right - SCROLLBAR_WIDTH;
        GuiSprite track = this.style.sprite(WidgetParts.SCROLLBAR, GuiStyles.sprite("scrollbar_track"));
        track.extractRenderState(graphics, trackLeft, top, SCROLLBAR_WIDTH, bottom - top);

        int thumbHeight = this.thumbHeight();
        int thumbTop = top + Math.round((this.getHeight() - thumbHeight) * (scroll / (float) this.maxOffset()));
        boolean thumbHovered = mouseX >= trackLeft && mouseX < right && mouseY >= thumbTop && mouseY < thumbTop + thumbHeight;
        GuiSprite handle = thumbHovered || this.dragging
                ? this.style.get(WidgetParts.SCROLLBAR, WidgetParts.HANDLE_HOVER_SPRITE, GuiStyles.sprite("scrollbar_handle_hover"))
                : this.style.get(WidgetParts.SCROLLBAR, WidgetParts.HANDLE_SPRITE, GuiStyles.sprite("scrollbar_handle"));
        handle.extractRenderState(graphics, trackLeft, thumbTop, SCROLLBAR_WIDTH, thumbHeight);
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
            WidgetParts.playClickSound();
            this.onSelect.accept(this.entries.get(index));
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
    public boolean keyPressed(KeyEvent event) {
        switch (event.key()) {
            case InputConstants.KEY_UP -> this.moveFocus(-1);
            case InputConstants.KEY_DOWN -> this.moveFocus(1);
            case InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER, InputConstants.KEY_SPACE -> this.selectFocusedRow();
            default -> {
                return super.keyPressed(event);
            }
        }

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        int row = this.isFocused() ? this.focusedRow : this.hoveredRow;
        if (this.narrations == null || row < 0 || row >= this.entries.size()) {
            return;
        }

        Component narration = this.narrations.apply(this.entries.get(row));
        if (narration != null) {
            output.add(NarratedElementType.TITLE, narration);
        }

        if (this.isFocused()) {
            output.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
        }
    }

    /**
     * Moves the focus row by the given number of rows. The first move starts at the first row, and the focus stays inside the list.
     */
    private void moveFocus(int delta) {
        if (this.entries.isEmpty()) {
            return;
        }

        int next = this.focusedRow < 0 ? 0 : this.focusedRow + delta;
        this.focusedRow = Mth.clamp(next, 0, this.entries.size() - 1);
        this.revealRow(this.focusedRow);
    }

    private void selectFocusedRow() {
        if (this.focusedRow >= 0 && this.focusedRow < this.entries.size()) {
            WidgetParts.playClickSound();
            this.onSelect.accept(this.entries.get(this.focusedRow));
        }
    }

    /**
     * Scrolls the smallest amount that brings the row fully into view.
     */
    private void revealRow(int index) {
        float top = index * this.rowHeight;
        float bottom = top + this.rowHeight;
        if (top < this.targetOffset) {
            this.targetOffset = this.clamp(top);
        } else if (bottom > this.targetOffset + this.getHeight()) {
            this.targetOffset = this.clamp(bottom - this.getHeight());
        }
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
