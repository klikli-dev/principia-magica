// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import net.minecraft.util.Mth;

/**
 * Client-only state that eases a displayed amount, such as the liquid of a container, towards its synced amount. The synced
 * amount changes in steps, at most every {@link SyncThrottle#SYNC_INTERVAL} ticks, and the easing makes the steps look smooth.
 * <p>
 * The amount snaps when it is first shown, and when it empties or refills from empty, so liquid never grows out of nothing.
 * Otherwise it approaches the target exponentially, with a time constant of a third of the sync interval.
 */
public final class AmountSmoother {

    private static final float TIME_CONSTANT = SyncThrottle.SYNC_INTERVAL / 3.0F;

    private float amount;
    /**
     * The game time plus partial tick of the last update.
     */
    private double time;
    private boolean started;

    /**
     * Moves the displayed amount towards the synced amount by the time since the last update.
     *
     * @param target      the synced amount.
     * @param gameTime    the current game time.
     * @param partialTick the fraction of the current tick that has passed.
     * @return the amount to display.
     */
    public float update(int target, long gameTime, float partialTick) {
        double now = gameTime + partialTick;
        if (!this.started || target == 0 || this.amount <= 0.0F) {
            this.started = true;
            this.amount = target;
        } else {
            float elapsed = (float) Math.max(0.0, now - this.time);
            this.amount += (target - this.amount) * (1.0F - (float) Math.exp(-elapsed / TIME_CONSTANT));
        }
        this.time = now;
        return this.amount;
    }
}
