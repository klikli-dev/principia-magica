// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import com.klikli_dev.codedefinedgui.api.layout.LayoutSpec;
import com.klikli_dev.principiamagica.client.gui.ContentRect;

/**
 * Static geometry of the golem menu. The screen is a composition of a vertical tab rail, the golem panel and the player inventory panel
 * below it, centred as a whole. All coordinates here are relative to the screen's left and top edge, which is what vanilla slots use.
 * <p>
 * The golem panel has a border of {@link #CONTENT_X} and {@link #CONTENT_Y} pixels around its content. Content coordinates are relative to
 * the content's top left corner, see {@link #panelX(int)} and {@link #panelY(int)}. Slot coordinates are the position of the item, the slot
 * sprites are drawn around it.
 */
public final class GolemMenuLayout {

    /**
     * Width of the tab rail, which sits to the left of the golem panel and overlaps it by {@link #TAB_OVERLAP}.
     */
    public static final int RAIL_WIDTH = 28;
    public static final int TAB_OVERLAP = 4;

    public static final int PANEL_X = RAIL_WIDTH;
    public static final int PANEL_Y = 0;
    public static final int PANEL_WIDTH = 212;
    public static final int PANEL_HEIGHT = 132;

    /**
     * Padding between the golem panel's border and its content.
     */
    public static final int CONTENT_X = 4;
    public static final int CONTENT_Y = 3;

    /**
     * The player inventory panel, centred below the golem panel. Its padding is vanilla's 7 pixels, it has no title.
     */
    public static final int INVENTORY_PANEL_WIDTH = 176;
    public static final int INVENTORY_PANEL_HEIGHT = 90;
    public static final int INVENTORY_PANEL_X = PANEL_X + (PANEL_WIDTH - INVENTORY_PANEL_WIDTH) / 2;
    public static final int INVENTORY_PANEL_Y = PANEL_HEIGHT + 6;

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
     * The player's main inventory and hotbar. Vanilla's 7 pixel padding puts the first items at 8, and the hotbar follows a 4 pixel gap.
     */
    public static final int PLAYER_INVENTORY_X = INVENTORY_PANEL_X + 8;
    public static final int PLAYER_MAIN_Y = INVENTORY_PANEL_Y + 8;
    public static final int PLAYER_HOTBAR_Y = PLAYER_MAIN_Y + 3 * SLOT_SIZE + 4;

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
     * @return the screen x of a content x coordinate of the golem panel
     */
    public static int panelX(int x) {
        return PANEL_X + CONTENT_X + x;
    }

    /**
     * @return the screen y of a content y coordinate of the golem panel
     */
    public static int panelY(int y) {
        return PANEL_Y + CONTENT_Y + y;
    }

    /**
     * @param panel the golem panel's rectangle
     * @return the rectangle inside the panel's border, where the tabs lay out their widgets
     */
    public static ContentRect content(ContentRect panel) {
        return new ContentRect(panel.x() + CONTENT_X, panel.y() + CONTENT_Y, panel.width() - 2 * CONTENT_X, panel.height() - 2 * CONTENT_Y);
    }
}
