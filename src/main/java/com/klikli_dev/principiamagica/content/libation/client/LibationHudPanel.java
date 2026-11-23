// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.client.principle.PrincipleRenderer;
import com.klikli_dev.principiamagica.content.libation.client.LibationHudStatus.Status;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Drawing primitives shared by the libation HUD panels: principle and item rows, status lines and the progress bar.
 */
final class LibationHudPanel {

    static final int PADDING = 3;
    static final int ROW_HEIGHT = 18;
    static final int TEXT_GAP = 4;
    static final int LINE_HEIGHT = 9;
    static final int STATUS_GAP = 3;
    static final int BAR_GAP = 3;
    static final int BAR_WIDTH = 60;
    static final int BAR_HEIGHT = 3;
    static final int BACKGROUND_COLOR = 0xA0000000;
    static final int BAR_TRACK_COLOR = 0xFF303030;
    static final int TEXT_COLOR = 0xFFFFFFFF;
    static final int SELECTED_ROW_COLOR = 0x45FFD36B;
    static final int SELECTED_ACCENT_COLOR = 0xFFFFD36B;

    private LibationHudPanel() {
    }

    /**
     * @return the row text of a principle: its name in its colour, followed by the amount in mB.
     */
    static Component principleRow(PrincipleStack stack) {
        return Component.empty().append(stack.principle().value().prefixedName()).append(" ").append(stack.amountText());
    }

    /**
     * @return the width of a principle row: its icon, the gap and the row text
     */
    static int principleRowWidth(Font font, PrincipleStack stack) {
        return PrincipleRenderer.ICON_SIZE + TEXT_GAP + font.width(principleRow(stack));
    }

    /**
     * Draws a principle icon at x with its row text next to it, vertically centred on the icon.
     */
    static void renderPrincipleRow(GuiGraphicsExtractor graphics, Font font, PrincipleStack stack, int x, int rowY) {
        renderPrincipleRow(graphics, font, stack, x, rowY, false, 0);
    }

    /**
     * Draws a principle icon at x with its row text next to it, optionally highlighted as the active selection.
     */
    static void renderPrincipleRow(GuiGraphicsExtractor graphics, Font font, PrincipleStack stack, int x, int rowY,
                                   boolean selected, int highlightWidth) {
        if (selected && highlightWidth > 0) {
            graphics.fill(x - 1, rowY, x + highlightWidth + 1, rowY + PrincipleRenderer.ICON_SIZE, SELECTED_ROW_COLOR);
            graphics.fill(x - 2, rowY, x - 1, rowY + PrincipleRenderer.ICON_SIZE, SELECTED_ACCENT_COLOR);
        }
        PrincipleRenderer.renderIcon(graphics, stack.principle(), x, rowY);
        graphics.text(font, principleRow(stack), x + PrincipleRenderer.ICON_SIZE + TEXT_GAP,
                rowY + (PrincipleRenderer.ICON_SIZE - LINE_HEIGHT) / 2, TEXT_COLOR, true);
    }

    /**
     * Draws an item icon at x with its label next to it, vertically centred on the icon.
     */
    static void renderItemRow(GuiGraphicsExtractor graphics, Font font, ItemStack stack, Component label, int x, int rowY) {
        graphics.item(stack, x, rowY);
        graphics.text(font, label, x + PrincipleRenderer.ICON_SIZE + TEXT_GAP,
                rowY + (PrincipleRenderer.ICON_SIZE - LINE_HEIGHT) / 2, TEXT_COLOR, true);
    }

    /**
     * @return the height of the status lines, including the gap above them. Zero without status lines.
     */
    static int statusHeight(List<Status> statuses) {
        return statuses.isEmpty() ? 0 : STATUS_GAP + statuses.size() * LINE_HEIGHT;
    }

    /**
     * @return the height of the progress bar, including the gap above it. Zero without a bar.
     */
    static int barHeight(boolean hasBar) {
        return hasBar ? BAR_GAP + BAR_HEIGHT : 0;
    }

    static int statusWidth(Font font, List<Status> statuses) {
        int width = 0;
        for (Status status : statuses) {
            width = Math.max(width, font.width(status.text()));
        }
        return width;
    }

    static void renderStatuses(GuiGraphicsExtractor graphics, Font font, List<Status> statuses, int x, int y) {
        for (int i = 0; i < statuses.size(); i++) {
            Status status = statuses.get(i);
            graphics.text(font, status.text(), x, y + i * LINE_HEIGHT, status.color(), true);
        }
    }

    /**
     * Draws a progress bar of the curing color on its track.
     *
     * @param fraction the progress in [0, 1].
     */
    static void renderBar(GuiGraphicsExtractor graphics, int x, int y, float fraction) {
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BAR_TRACK_COLOR);
        graphics.fill(x, y, x + Math.round(BAR_WIDTH * fraction), y + BAR_HEIGHT, LibationHudStatus.CURING_COLOR);
    }
}
