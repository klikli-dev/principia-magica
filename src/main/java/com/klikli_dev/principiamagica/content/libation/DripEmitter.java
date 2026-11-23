// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

/**
 * Spaces the drips of a transferred portion evenly over the transfer interval: one drip per {@link LibationFlow#MB_PER_DRIP} mB,
 * and the rest carries over to the next portion. Only the clients use it, driven by the block event of the server.
 */
public final class DripEmitter {

    /**
     * Ticks a portion keeps the emitter active: the transfer interval plus a small grace, so that back-to-back portions
     * read as one continuous flow.
     */
    private static final int ACTIVE_TICKS = LibationFlow.DRAIN_INTERVAL + 4;

    private int activeTicks;
    private int leftover;
    private int count;
    private int spawned;
    private int elapsed;

    /**
     * Queues the drips of a portion of the given amount in mB.
     */
    public void add(int mb) {
        this.activeTicks = ACTIVE_TICKS;
        int total = this.leftover + mb;
        int drips = total / LibationFlow.MB_PER_DRIP;
        this.leftover = total % LibationFlow.MB_PER_DRIP;
        if (drips == 0) {
            return;
        }

        this.count = this.count - this.spawned + drips;
        this.spawned = 0;
        this.elapsed = 0;
    }

    /**
     * Advances one tick.
     *
     * @return true if a drip spawns this tick.
     */
    public boolean tick() {
        if (this.activeTicks > 0) {
            this.activeTicks--;
        }
        if (this.spawned >= this.count) {
            return false;
        }

        boolean spawn = this.elapsed >= this.spawned * LibationFlow.DRAIN_INTERVAL / this.count;
        this.elapsed++;
        if (spawn) {
            this.spawned++;
        }
        return spawn;
    }

    /**
     * @return true while liquid is visibly flowing: portions keep arriving or the last one is still dripping.
     */
    public boolean isActive() {
        return this.activeTicks > 0 || this.spawned < this.count;
    }
}
