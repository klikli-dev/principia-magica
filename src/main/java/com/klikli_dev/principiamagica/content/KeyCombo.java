// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

/**
 * Mouse controls named in tooltips and HUD hints. Each combo is its own translatable, rendered gold, so it stands out from the
 * text around it. Hint strings take it as their first argument, e.g. {@code "%s: erase work area"}.
 */
public enum KeyCombo {
    RIGHT_CLICK,
    CROUCH_RIGHT_CLICK,
    CROUCH_LEFT_CLICK,
    CROUCH_SCROLL;

    /**
     * @return the gold, translated name of this combo, to pass as an argument to a hint
     */
    public MutableComponent component() {
        return Component.translatable("key_combo.principiamagica." + this.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GOLD);
    }
}
