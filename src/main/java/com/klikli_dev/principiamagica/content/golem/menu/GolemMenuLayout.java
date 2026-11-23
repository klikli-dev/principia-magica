// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import com.klikli_dev.codedefinedgui.api.layout.LayoutSpec;

/**
 * Static geometry of the golem menu. The screen is a composition of a vertical tab rail, the golem panel and the player inventory panel
 * below it. All coordinates here are relative to the screen's left and top edge, which is what vanilla slots use.
 * <p>
 * Frame-relative coordinates (relative to the golem panel's top left corner) are converted with {@link #panelX(int)} and
 * {@link #panelY(int)}. Slot coordinates are the position of the item, the slot sprites are drawn around it.
 */
public final class GolemMenuLayout {

    /**
     * Width of the tab rail, which sits to the left of the golem panel and overlaps it by {@link #TAB_OVERLAP}.
     */
    public static final int RAIL_WIDTH = 28;
    public static final int TAB_OVERLAP = 4;

    public static final int PANEL_X = RAIL_WIDTH;
    public static final int PANEL_Y = 0;
    public static final int PANEL_WIDTH = 204;
    public static final int PANEL_HEIGHT = 126;

    public static final int INVENTORY_PANEL_X = PANEL_X + 14;
    public static final int INVENTORY_PANEL_Y = 130;
    public static final int INVENTORY_PANEL_WIDTH = 176;
    public static final int INVENTORY_PANEL_HEIGHT = 98;

    /**
     * The whole screen: the rail plus the golem panel wide, and the golem panel plus the inventory panel tall.
     */
    public static final int WIDTH = RAIL_WIDTH + PANEL_WIDTH;
    public static final int HEIGHT = INVENTORY_PANEL_Y + INVENTORY_PANEL_HEIGHT;

    public static final int SLOT_SIZE = 18;

    /**
     * Item positions of the golem slots. The axiom slot sprite is 26x26 and is drawn 5 pixels around its item, the others 18x18 and 1 pixel.
     */
    public static final int AXIOM_X = panelX(79);
    public static final int AXIOM_Y = panelY(31);
    public static final int HAND_X = panelX(75);
    public static final int HAND_Y = panelY(59);
    public static final int BACK_X = panelX(97);
    public static final int BACK_Y = panelY(59);

    /**
     * The player's main inventory and hotbar. The inventory panel has its label above the slots, so the main rows start 18 pixels down.
     */
    public static final int PLAYER_INVENTORY_X = INVENTORY_PANEL_X + 8;
    public static final int PLAYER_MAIN_Y = INVENTORY_PANEL_Y + 18;
    public static final int PLAYER_HOTBAR_Y = INVENTORY_PANEL_Y + 76;

    public static final int PLAYER_MAIN_ROWS = 3;
    public static final int PLAYER_COLUMNS = 9;

    public static final LayoutSpec LAYOUT = LayoutSpec.create(root -> {
        root.group("main", main -> {
            main.node("axiom").at(AXIOM_X, AXIOM_Y).size(SLOT_SIZE, SLOT_SIZE);
            main.node("hand").at(HAND_X, HAND_Y).size(SLOT_SIZE, SLOT_SIZE);
            main.node("back").at(BACK_X, BACK_Y).size(SLOT_SIZE, SLOT_SIZE);
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

    /**
     * @return the screen x of a golem panel x coordinate
     */
    public static int panelX(int x) {
        return PANEL_X + x;
    }

    /**
     * @return the screen y of a golem panel y coordinate
     */
    public static int panelY(int y) {
        return PANEL_Y + y;
    }
}
