// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui;

import com.klikli_dev.codedefinedgui.api.style.BuiltinGuiParts;
import com.klikli_dev.codedefinedgui.api.style.GuiPartKey;
import com.klikli_dev.codedefinedgui.api.style.GuiStyle;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleContext;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleKey;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleRegistry;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.client.gui.widget.WidgetParts;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The "clay tablet and faience" theme of the golem GUI, registered as {@link #GOLEM}.
 * <p>
 * The style maps two things: CodeDefinedGui's player inventory parts ({@link BuiltinGuiParts}) and the parts of the framework widgets
 * ({@link WidgetParts}), so the widgets stay generic. Screen decorations (panels, insets, icons) are looked up with {@link #part(String)}
 * and {@link #sprite(String)}, which share one table, so the default sprite and the style never drift apart.
 */
public final class GuiStyles {

    public static final GuiStyleKey GOLEM = GuiStyleKey.of(PrincipiaMagica.loc("golem"));

    public static final int TEXT_PRIMARY = 0xFF3B0F13;
    public static final int TEXT_SECONDARY = 0xFF7A4A32;
    public static final int TEXT_ON_DARK = 0xFFF6E2B8;
    public static final int TEXT_DISABLED = 0xFF9C7F69;
    public static final int ROW_HIGHLIGHT = 0x33F1C995;

    /**
     * Sprite name to the sprite's size. The sprite id is {@code principiamagica:golem/<name>}.
     */
    private static final Map<String, GuiSprite> SPRITES = new LinkedHashMap<>();

    static {
        sprite("panel", 48, 48);
        sprite("inventory_panel", 48, 48);
        sprite("inset", 16, 16);
        sprite("cartouche", 24, 16);
        sprite("portrait_frame", 58, 80);
        sprite("slot", 18, 18);
        sprite("slot_axiom", 26, 26);
        sprite("ghost_axiom", 16, 16);
        sprite("ghost_hand", 16, 16);
        sprite("ghost_back", 16, 16);
        sprite("tab", 32, 28);
        sprite("tab_hover", 32, 28);
        sprite("tab_selected", 32, 28);
        sprite("icon_golem", 16, 16);
        sprite("button", 20, 20);
        sprite("button_hover", 20, 20);
        sprite("button_selected", 20, 20);
        sprite("button_disabled", 20, 20);
        sprite("mode_work", 16, 16);
        sprite("mode_follow", 16, 16);
        sprite("mode_stay", 16, 16);
        sprite("toggle_off", 26, 12);
        sprite("toggle_off_hover", 26, 12);
        sprite("toggle_on", 26, 12);
        sprite("toggle_on_hover", 26, 12);
        sprite("status_ok", 16, 16);
        sprite("status_idle", 16, 16);
        sprite("status_warning", 16, 16);
        sprite("status_problem", 16, 16);
        sprite("bar_background", 58, 7);
        sprite("bar_fill", 56, 5);
        sprite("scrollbar_track", 6, 16);
        sprite("scrollbar_handle", 6, 12);
        sprite("scrollbar_handle_hover", 6, 12);
        sprite("icon_zone", 16, 16);
        sprite("icon_deposit", 16, 16);
        sprite("icon_supply", 16, 16);
        sprite("divider", 32, 3);
    }

    private GuiStyles() {
    }

    private static void sprite(String name, int width, int height) {
        SPRITES.put(name, new GuiSprite(PrincipiaMagica.loc("golem/" + name), width, height));
    }

    /**
     * @return the default sprite of a golem GUI sprite, as in the texture folder. Used as fallback and to size the sprite
     */
    public static GuiSprite sprite(String name) {
        GuiSprite sprite = SPRITES.get(name);
        if (sprite == null) {
            throw new IllegalArgumentException("Unknown golem gui sprite: " + name);
        }
        return sprite;
    }

    /**
     * @return the part key of a decoration of the golem GUI, mapped to its sprite by the style
     */
    public static GuiPartKey part(String name) {
        return GuiPartKey.of(PrincipiaMagica.loc("gui/golem/" + name));
    }

    /**
     * Registers the golem style. Call once, during client setup, before any screen is opened.
     */
    public static void register() {
        GuiStyleRegistry.register(GOLEM, build());
    }

    /**
     * @return the style of the golem GUI, the default of all framework widgets
     */
    public static StyleContext context() {
        return new GuiStyleContext(GuiStyleRegistry.get(GOLEM));
    }

    private static GuiStyle build() {
        GuiStyle.Builder builder = GuiStyle.builder()
                .set(BuiltinGuiParts.PLAYER_INVENTORY_BACKGROUND, GuiStyleProperties.SPRITE, sprite("inventory_panel"))
                .set(BuiltinGuiParts.PLAYER_SLOT, GuiStyleProperties.SPRITE, sprite("slot"));

        for (Map.Entry<String, GuiSprite> entry : SPRITES.entrySet()) {
            builder.set(part(entry.getKey()), GuiStyleProperties.SPRITE, entry.getValue());
        }

        return builder
                .set(WidgetParts.TAB, GuiStyleProperties.SPRITE, sprite("tab"))
                .set(WidgetParts.TAB, GuiStyleProperties.HOVER_SPRITE, sprite("tab_hover"))
                .set(WidgetParts.TAB, GuiStyleProperties.ON_SPRITE, sprite("tab_selected"))
                .set(WidgetParts.BUTTON, GuiStyleProperties.SPRITE, sprite("button"))
                .set(WidgetParts.BUTTON, GuiStyleProperties.HOVER_SPRITE, sprite("button_hover"))
                .set(WidgetParts.BUTTON, GuiStyleProperties.TEXT_COLOR, TEXT_PRIMARY)
                .set(WidgetParts.BUTTON, GuiStyleProperties.COLOR, TEXT_DISABLED)
                .set(WidgetParts.ICON_BUTTON, GuiStyleProperties.SPRITE, sprite("button"))
                .set(WidgetParts.ICON_BUTTON, GuiStyleProperties.HOVER_SPRITE, sprite("button_hover"))
                .set(WidgetParts.ICON_BUTTON, GuiStyleProperties.ON_SPRITE, sprite("button_selected"))
                .set(WidgetParts.ICON_BUTTON, WidgetParts.DISABLED_SPRITE, sprite("button_disabled"))
                .set(WidgetParts.TOGGLE, GuiStyleProperties.ON_SPRITE, sprite("toggle_on"))
                .set(WidgetParts.TOGGLE, GuiStyleProperties.OFF_SPRITE, sprite("toggle_off"))
                .set(WidgetParts.TOGGLE_HOVER, GuiStyleProperties.ON_SPRITE, sprite("toggle_on_hover"))
                .set(WidgetParts.TOGGLE_HOVER, GuiStyleProperties.OFF_SPRITE, sprite("toggle_off_hover"))
                .set(WidgetParts.TEXT, GuiStyleProperties.TEXT_COLOR, TEXT_PRIMARY)
                .set(WidgetParts.TEXT, GuiStyleProperties.COLOR, TEXT_SECONDARY)
                .set(WidgetParts.ROW, GuiStyleProperties.COLOR, ROW_HIGHLIGHT)
                .set(WidgetParts.SCROLLBAR, GuiStyleProperties.SPRITE, sprite("scrollbar_track"))
                .set(WidgetParts.SCROLLBAR, WidgetParts.HANDLE_SPRITE, sprite("scrollbar_handle"))
                .set(WidgetParts.SCROLLBAR, WidgetParts.HANDLE_HOVER_SPRITE, sprite("scrollbar_handle_hover"))
                .set(WidgetParts.PROGRESS, GuiStyleProperties.SPRITE, sprite("bar_background"))
                .set(WidgetParts.PROGRESS, WidgetParts.FILL_SPRITE, sprite("bar_fill"))
                .build();
    }
}
