// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.style.GuiPartKey;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperty;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.client.gui.GuiStyles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/**
 * Style part keys used by the widgets in this package, and the style they resolve against by default.
 * <p>
 * Every widget reads its look from a {@link StyleContext} (see {@code withStyle}) using the part keys below and the properties of
 * {@link com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties} plus the extra properties of this class. The default is
 * {@link GuiStyles#context()}, so a screen only passes a style when it wants another theme.
 * <ul>
 *     <li>{@link #BUTTON}: {@code SPRITE}, {@code HOVER_SPRITE}, {@code TEXT_COLOR}, {@code COLOR} (disabled text)</li>
 *     <li>{@link #ICON_BUTTON}: {@code SPRITE}, {@code HOVER_SPRITE}, {@code ON_SPRITE} (selected), {@link #DISABLED_SPRITE}</li>
 *     <li>{@link #TAB}: {@code SPRITE}, {@code HOVER_SPRITE}, {@code ON_SPRITE} (selected)</li>
 *     <li>{@link #TOGGLE} and {@link #TOGGLE_HOVER}: {@code ON_SPRITE}, {@code OFF_SPRITE}</li>
 *     <li>{@link #ROW}: {@code COLOR} (hover highlight)</li>
 *     <li>{@link #SCROLLBAR}: {@code SPRITE} (track), {@link #HANDLE_SPRITE}, {@link #HANDLE_HOVER_SPRITE}</li>
 *     <li>{@link #PROGRESS}: {@code SPRITE} (background), {@link #FILL_SPRITE}</li>
 *     <li>{@link #TEXT}: {@code TEXT_COLOR}, {@code COLOR} (secondary text)</li>
 * </ul>
 */
public final class WidgetParts {

    public static final GuiPartKey BUTTON = key("button");
    public static final GuiPartKey ICON_BUTTON = key("icon_button");
    public static final GuiPartKey TAB = key("tab");
    public static final GuiPartKey TOGGLE = key("toggle");
    public static final GuiPartKey TOGGLE_HOVER = key("toggle_hover");
    public static final GuiPartKey ROW = key("row");
    public static final GuiPartKey SCROLLBAR = key("scrollbar");
    public static final GuiPartKey PROGRESS = key("progress");
    public static final GuiPartKey TEXT = key("text");

    public static final GuiStyleProperty<GuiSprite> DISABLED_SPRITE = GuiStyleProperty.of("disabled_sprite");
    public static final GuiStyleProperty<GuiSprite> FILL_SPRITE = GuiStyleProperty.of("fill_sprite");
    public static final GuiStyleProperty<GuiSprite> HANDLE_SPRITE = GuiStyleProperty.of("handle_sprite");
    public static final GuiStyleProperty<GuiSprite> HANDLE_HOVER_SPRITE = GuiStyleProperty.of("handle_hover_sprite");

    public static final int DEFAULT_TEXT_COLOR = GuiStyles.TEXT_PRIMARY;
    public static final int DEFAULT_MUTED_TEXT_COLOR = GuiStyles.TEXT_SECONDARY;
    public static final int DISABLED_TEXT_COLOR = GuiStyles.TEXT_DISABLED;

    private WidgetParts() {
    }

    /**
     * @return the style the widgets use unless they are given another one
     */
    public static StyleContext defaultStyle() {
        return GuiStyles.context();
    }

    /**
     * Plays the vanilla button click. Used by the clickable widgets of this package, so they all sound the same.
     */
    public static void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private static GuiPartKey key(String path) {
        return GuiPartKey.of(PrincipiaMagica.loc("widget/" + path));
    }
}
