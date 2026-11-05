// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import com.klikli_dev.codedefinedgui.api.layout.LayoutSpec;
import com.klikli_dev.principiamagica.content.golem.entity.GolemInventory;

/**
 * Static slot and background geometry shared by the golem menu (slot positions) and screen (backgrounds, lists).
 * The area above the slot row is drawn by the screen: header, mode buttons and the scrolling workplace list.
 */
public final class GolemMenuLayout {
    public static final int WIDTH = 208;
    public static final int HEIGHT = 332;
    public static final int SLOT_SIZE = 18;

    public static final int TOOL_X = 12;
    public static final int GOLEM_INVENTORY_X = 34;
    public static final int SLOT_ROW_Y = 218;

    public static final int PLAYER_INVENTORY_X = 23;
    public static final int PLAYER_MAIN_Y = 252;
    public static final int PLAYER_HOTBAR_Y = 306;

    public static final int PLAYER_MAIN_ROWS = 3;
    public static final int PLAYER_COLUMNS = 9;

    // Upgrade items are installed by right-clicking the golem. Their removal is a row in the golem GUI.
    public static final LayoutSpec LAYOUT = LayoutSpec.create(root -> {
        root.group("main", main -> {
            main.node("background").at(0, 0).size(WIDTH, HEIGHT);
            main.node("tool").at(TOOL_X, SLOT_ROW_Y).size(SLOT_SIZE, SLOT_SIZE);
        });

        root.group("golem", golem -> {
            for (int i = 0; i < GolemInventory.MAX_SLOTS; i++) {
                golem.node("slot_" + i).at(GOLEM_INVENTORY_X + i * SLOT_SIZE, SLOT_ROW_Y).size(SLOT_SIZE, SLOT_SIZE);
            }
        });

        root.group("player", player -> {
            for (int i = 0; i < PLAYER_MAIN_ROWS * PLAYER_COLUMNS; i++) {
                int row = i / PLAYER_COLUMNS;
                int column = i % PLAYER_COLUMNS;
                player.node("slot_" + (i + PLAYER_COLUMNS)).at(PLAYER_INVENTORY_X + column * SLOT_SIZE, PLAYER_MAIN_Y + row * SLOT_SIZE).size(SLOT_SIZE, SLOT_SIZE);
            }
            for (int column = 0; column < PLAYER_COLUMNS; column++) {
                player.node("hotbar_" + column).at(PLAYER_INVENTORY_X + column * SLOT_SIZE, PLAYER_HOTBAR_Y).size(SLOT_SIZE, SLOT_SIZE);
            }
        });
    });

    private GolemMenuLayout() {
    }
}
