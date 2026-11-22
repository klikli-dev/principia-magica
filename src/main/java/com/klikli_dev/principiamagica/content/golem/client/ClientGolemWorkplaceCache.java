// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.network.messages.MessageSyncGolemWorkplace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side copy of the workplace of the golem currently selected with a stylus. Entries expire when not refreshed.
 */
public class ClientGolemWorkplaceCache {

    private static final long MAX_AGE_TICKS = 40;
    private static final Map<Integer, Entry> ENTRIES = new ConcurrentHashMap<>();

    public static void accept(MessageSyncGolemWorkplace message) {
        ENTRIES.put(message.entityId(), new Entry(message.workplace(), message.anchor(), message.currentTarget(), currentTick()));
    }

    public static Optional<Entry> get(int entityId) {
        Entry entry = ENTRIES.get(entityId);
        if (entry == null || currentTick() - entry.receivedTick() > MAX_AGE_TICKS) {
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    /**
     * @return the last workplace received for the golem, regardless of age. Used by the golem GUI, which keeps it open.
     */
    public static Optional<Entry> latest(int entityId) {
        return Optional.ofNullable(ENTRIES.get(entityId));
    }

    private static long currentTick() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0 : minecraft.level.getGameTime();
    }

    public record Entry(Workplace workplace, Optional<GlobalPos> anchor, Optional<BlockPos> currentTarget, long receivedTick) {
    }
}
