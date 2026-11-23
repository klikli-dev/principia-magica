// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Progress of a recipe that advances by a rate per tick, for block entities that process one item.
 * <p>
 * The rate changes and the completion are synced at once. The progress in between is requested, so it is synced at the
 * {@link SyncThrottle#SYNC_INTERVAL} of the owner. Clients interpolate the progress between syncs, see {@link #display}.
 */
public final class ProgressTracker {

    private static final String PROGRESS_KEY = "progress";
    private static final String RATE_KEY = "rate";
    private static final String SYNC_TICK_KEY = "sync_tick";

    private final SyncThrottle sync;
    private float progress;
    /**
     * The progress per tick. Zero while the progress is paused or stalled.
     */
    private float rate;
    /**
     * The game time at which the progress and rate were saved, set on the client when the data is loaded.
     */
    private long syncTick;

    /**
     * @param sync the throttle of the owning block entity, which syncs it to the clients.
     */
    public ProgressTracker(SyncThrottle sync) {
        this.sync = sync;
    }

    /**
     * @return the progress in ticks at the rate of the recipe.
     */
    public float progress() {
        return this.progress;
    }

    /**
     * @return the progress interpolated from the synced progress and rate, so that it is smooth on the client.
     * @see LibationUtils#displayProgress
     */
    public float display(long gameTime, float partialTick) {
        return LibationUtils.displayProgress(this.progress, this.rate, this.syncTick, gameTime, partialTick);
    }

    /**
     * Restarts the progress without syncing. The caller syncs.
     */
    public void restart() {
        this.progress = 0;
    }

    /**
     * Stops the progress and its rate, for when there is nothing to progress. Syncs now if either was set.
     */
    public void stop() {
        if (this.progress != 0 || this.rate != 0) {
            this.progress = 0;
            this.rate = 0;
            this.sync.syncNow();
        }
    }

    /**
     * Pauses the rate while the progress waits, for example because the output is full. Syncs now if the rate was set.
     */
    public void pause() {
        if (this.rate != 0) {
            this.rate = 0;
            this.sync.syncNow();
        }
    }

    /**
     * Advances the progress by one tick at the given rate. When the progress reaches the required amount, it restarts from
     * zero, runs the completion and syncs now. A change of the rate syncs now too. Otherwise the progress is requested.
     *
     * @param newRate  the progress per tick.
     * @param required the progress at which the recipe completes.
     * @param onComplete runs on completion, before the sync, so the synced state is the completed one.
     */
    public void advance(float newRate, float required, Runnable onComplete) {
        this.progress += newRate;
        if (this.progress >= required) {
            this.progress = 0;
            this.rate = newRate;
            onComplete.run();
            this.sync.syncNow();
            return;
        }

        boolean rateChanged = this.rate != newRate;
        this.rate = newRate;
        if (rateChanged) {
            this.sync.syncNow();
        } else {
            this.sync.requestSync();
        }
    }

    public void load(ValueInput input) {
        this.progress = input.getFloatOr(PROGRESS_KEY, 0.0F);
        this.rate = input.getFloatOr(RATE_KEY, 0.0F);
        this.syncTick = input.getLongOr(SYNC_TICK_KEY, 0L);
    }

    /**
     * @param gameTime the current game time, which the clients use to interpolate the progress after loading.
     */
    public void save(ValueOutput output, long gameTime) {
        output.putFloat(PROGRESS_KEY, this.progress);
        output.putFloat(RATE_KEY, this.rate);
        output.putLong(SYNC_TICK_KEY, gameTime);
    }
}
