// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import com.klikli_dev.codedefinedgui.api.layout.LayoutSpec;
import com.klikli_dev.principiamagica.content.golem.entity.GolemInventory;

/**
 * Static geometry of the golem menu. The GUI is a vanilla large chest sized 176x222 frame. The golem tab uses the area above the
 * golem inventory for the preview, the axiom and tool slots, the seals, the mode and the status. The job tab uses the same frame.
 */
public final class GolemMenuLayout {
    public static final int WIDTH = 176;
    public static final int HEIGHT = 222;
    public static final int SLOT_SIZE = 18;

    public static final int HEADER_Y = 6;

    public static final int PREVIEW_X = 8;
    public static final int PREVIEW_Y = 18;
    public static final int PREVIEW_WIDTH = 52;
    public static final int PREVIEW_HEIGHT = 72;

    public static final int AXIOM_X = 64;
    public static final int AXIOM_Y = 18;
    public static final int TOOL_X = 152;
    public static final int TOOL_Y = 18;

    public static final int SEAL_X = 64;
    public static final int SEAL_Y = 40;
    public static final int MODE_X = 64;
    public static final int MODE_Y = 62;
    public static final int STATUS_Y = 96;

    public static final int GOLEM_INVENTORY_X = 8;
    public static final int GOLEM_INVENTORY_Y = 108;

    public static final int INVENTORY_LABEL_Y = 129;
    public static final int PLAYER_INVENTORY_X = 8;
    public static final int PLAYER_MAIN_Y = 140;
    public static final int PLAYER_HOTBAR_Y = 198;

    public static final int PLAYER_MAIN_ROWS = 3;
    public static final int PLAYER_COLUMNS = 9;

    public static final LayoutSpec LAYOUT = LayoutSpec.create(root -> {
        root.group("main", main -> {
            main.node("axiom").at(AXIOM_X, AXIOM_Y).size(SLOT_SIZE, SLOT_SIZE);
            main.node("tool").at(TOOL_X, TOOL_Y).size(SLOT_SIZE, SLOT_SIZE);
        });

        root.group("golem", golem -> {
            for (int i = 0; i < GolemInventory.MAX_SLOTS; i++) {
                golem.node("slot_" + i).at(GOLEM_INVENTORY_X + i * SLOT_SIZE, GOLEM_INVENTORY_Y).size(SLOT_SIZE, SLOT_SIZE);
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
