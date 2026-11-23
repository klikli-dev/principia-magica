// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import com.klikli_dev.principiamagica.content.principle.Principle;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks each player's HUD-selected {@link Principle} on the server so phials and natron sponges draw or blot
 * the exact principle the player chose via Crouch + Scroll.
 */
public final class PrincipleSelection {

    private static final Map<UUID, Holder<Principle>> SELECTIONS = new ConcurrentHashMap<>();

    private PrincipleSelection() {
    }

    public static void set(Player player, @Nullable Holder<Principle> principle) {
        if (principle == null) {
            SELECTIONS.remove(player.getUUID());
        } else {
            SELECTIONS.put(player.getUUID(), principle);
        }
    }

    public static @Nullable Holder<Principle> get(Player player) {
        return SELECTIONS.get(player.getUUID());
    }
}
