// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.CodeDefinedGui;
import com.klikli_dev.codedefinedgui.api.style.BuiltinGuiStyles;
import com.klikli_dev.codedefinedgui.api.style.GuiPartKey;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleContext;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleRegistry;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import net.minecraft.resources.Identifier;

/**
 * Style part keys used by the widgets in this package, and the style they resolve against by default.
 * <p>
 * Every widget reads its look from a {@link StyleContext} (see {@code withStyle}) using the part keys below and the
 * properties of {@link com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties}, with a fallback to CodeDefinedGui's
 * built-in sprites and colors. The keys live in the CodeDefinedGui namespace, so a style can override them without
 * referencing any mod.
 * <ul>
 *     <li>{@link #BUTTON}: {@code SPRITE}, {@code HOVER_SPRITE}, {@code TEXT_COLOR}, {@code COLOR} (disabled text)</li>
 *     <li>{@link #TAB}: {@code SPRITE}, {@code HOVER_SPRITE}, {@code ON_SPRITE} (selected)</li>
 *     <li>{@link #TOGGLE}: {@code ON_SPRITE}, {@code OFF_SPRITE} (indicator), {@code TEXT_COLOR}</li>
 *     <li>{@link #SEGMENT}: {@code SPRITE}, {@code HOVER_SPRITE}, {@code ON_SPRITE} (selected), {@code TEXT_COLOR}</li>
 *     <li>{@link #LIST}: {@code COLOR} (background), {@link #ROW}: {@code COLOR} (hover highlight)</li>
 *     <li>{@link #SCROLLBAR}: {@code COLOR} (track), {@code TEXT_COLOR} (thumb)</li>
 *     <li>{@link #TEXT}: {@code TEXT_COLOR}, {@code COLOR} (secondary text)</li>
 * </ul>
 */
public final class WidgetParts {

    public static final GuiPartKey BUTTON = key("button");
    public static final GuiPartKey TAB = key("tab");
    public static final GuiPartKey TOGGLE = key("toggle");
    public static final GuiPartKey SEGMENT = key("segment");
    public static final GuiPartKey LIST = key("list");
    public static final GuiPartKey ROW = key("row");
    public static final GuiPartKey SCROLLBAR = key("scrollbar");
    public static final GuiPartKey TEXT = key("text");

    public static final int DEFAULT_TEXT_COLOR = 0xFF404040;
    public static final int DEFAULT_MUTED_TEXT_COLOR = 0xFF7A7A7A;
    public static final int DISABLED_TEXT_COLOR = 0xFF9A9A9A;

    private WidgetParts() {
    }

    /**
     * @return the style of the default CodeDefinedGui style key. Parts that are not defined there fall back to built-in sprites.
     */
    public static StyleContext defaultStyle() {
        return new GuiStyleContext(GuiStyleRegistry.get(BuiltinGuiStyles.DEFAULT));
    }

    private static GuiPartKey key(String path) {
        return GuiPartKey.of(Identifier.fromNamespaceAndPath(CodeDefinedGui.MODID, "widget/" + path));
    }
}
