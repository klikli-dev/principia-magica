// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.network.messages.MessageSyncGolemWorkplace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side copy of the workplace of the golem currently selected with a stylus or opened in the golem GUI.
 * <p>
 * Entries are kept while their golem is loaded in the client level, so an open GUI keeps its data. They are dropped when
 * the golem leaves the level, when a new sync arrives for a golem that is no longer loaded, and on logout.
 */
public class ClientGolemWorkplaceCache {

    private static final Map<Integer, Entry> ENTRIES = new ConcurrentHashMap<>();

    public static void accept(MessageSyncGolemWorkplace message) {
        removeEntriesOfUnloadedGolems();
        ENTRIES.put(message.entityId(), new Entry(message.workplace(), message.anchor(), message.currentTarget()));
    }

    /**
     * @return the last workplace received for the golem. Used by the golem GUI, which keeps it open.
     */
    public static Optional<Entry> latest(int entityId) {
        return Optional.ofNullable(ENTRIES.get(entityId));
    }

    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof GolemEntity golem) {
            ENTRIES.remove(golem.getId());
        }
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ENTRIES.clear();
    }

    private static void removeEntriesOfUnloadedGolems() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            ENTRIES.clear();
            return;
        }

        ENTRIES.keySet().removeIf(entityId -> level.getEntity(entityId) == null);
    }

    public record Entry(Workplace workplace, Optional<GlobalPos> anchor, Optional<BlockPos> currentTarget) {
    }
}
