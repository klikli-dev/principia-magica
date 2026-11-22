// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.jei;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class JeiText {

    public static final int TEXT_COLOR = 0xFF404040;
    public static final int SLOT_SIZE = 18;
    public static final int SLOT_PITCH = 30;
    /**
     * The plus sign is 13 pixels wide, but the frames of two slots one pitch apart are only 12 pixels apart. The second input
     * slot is therefore pushed 3 pixels to the right, which leaves the plus sign a pixel clear on each side.
     */
    public static final int SECOND_INPUT_SHIFT = 3;

    private JeiText() {
    }

    /**
     * The x of the input slot at index, counted from the first input slot at firstX. Every slot after the first is one pitch
     * to the right of the previous one, plus {@link #SECOND_INPUT_SHIFT} to clear the plus sign between the first two slots.
     */
    public static int inputX(int firstX, int index) {
        return index == 0 ? firstX : firstX + index * SLOT_PITCH + SECOND_INPUT_SHIFT;
    }

    /**
     * Draws a plus centred in the gap between the slot at slotX and the slot at nextSlotX. A slot's frame is 18 pixels, drawn one
     * pixel left and up of the slot's x and y, so the gap is the distance between the slots minus the frame.
     */
    public static void drawPlus(GuiGraphicsExtractor graphics, IDrawableStatic plus, int slotX, int nextSlotX, int slotY) {
        int gap = nextSlotX - slotX - SLOT_SIZE;
        int plusX = slotX + SLOT_SIZE - 1 + (gap - plus.getWidth()) / 2;
        plus.draw(graphics, plusX, slotY + (SLOT_SIZE - plus.getHeight()) / 2);
    }

    /**
     * @return the ticks as seconds, without a fraction if the time is a whole number of seconds.
     */
    public static String seconds(int ticks) {
        if (ticks % 20 == 0) {
            return String.valueOf(ticks / 20);
        }
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0F);
    }

    public static Component timeAtFullLight(int ticks) {
        return Component.translatable("jei.principiamagica.time_at_full_light", seconds(ticks));
    }

    /**
     * @return the time in seconds, for recipes whose time does not depend on the light.
     */
    public static Component time(int ticks) {
        return Component.translatable("jei.principiamagica.time", seconds(ticks));
    }

    public static void drawCentered(GuiGraphicsExtractor graphics, Font font, Component text, int width, int y) {
        graphics.text(font, text, (width - font.width(text)) / 2, y, TEXT_COLOR, false);
    }
}
