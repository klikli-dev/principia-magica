// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Throttles the client syncs of a libation block entity, the one throttle of the libation blocks.
 * <p>
 * Changes that the clients must see at once are synced with {@link #syncNow}. All other changes are requested with
 * {@link #requestSync}. The owner flushes a pending request from its server tick with {@link #tick}, at most once per
 * {@link #SYNC_INTERVAL} ticks. A request is never dropped, so the last state is always synced.
 * <p>
 * Saving is not throttled: a request marks the owner as changed right away, and every sync does as well.
 */
public final class SyncThrottle {

    /**
     * Ticks between the syncs of a block entity whose changes are requested rather than synced at once.
     */
    public static final int SYNC_INTERVAL = 20;

    private final BlockEntity owner;
    private boolean pending;
    /**
     * Ticks since the last sync, capped at {@link #SYNC_INTERVAL}. An owner that was quiet for a while flushes its next request
     * on the next tick.
     */
    private int ticksSinceSync = SYNC_INTERVAL;

    /**
     * @param owner the block entity that is synced and saved.
     */
    public SyncThrottle(BlockEntity owner) {
        this.owner = owner;
    }

    /**
     * @return true if a requested sync waits for the next flush.
     */
    public boolean pending() {
        return this.pending;
    }

    /**
     * Requests a sync for the next flush. Marks the owner as changed when the request is new, so the change is saved.
     */
    public void requestSync() {
        if (!this.pending) {
            this.pending = true;
            this.owner.setChanged();
        }
    }

    /**
     * Syncs the owner now, and cancels a pending request, since the sync carries its state.
     */
    public void syncNow() {
        this.pending = false;
        this.ticksSinceSync = 0;
        LibationUtils.sync(this.owner);
    }

    /**
     * Flushes a pending request once {@link #SYNC_INTERVAL} ticks passed since the last sync. Called from the server tick of the
     * owner, after the owner changed its state in that tick.
     */
    public void tick() {
        if (this.ticksSinceSync < SYNC_INTERVAL) {
            this.ticksSinceSync++;
        }
        if (this.pending && this.ticksSinceSync >= SYNC_INTERVAL) {
            this.syncNow();
        }
    }
}
