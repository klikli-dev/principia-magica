// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleResource;
import com.klikli_dev.principiamagica.content.principle.PrincipleTransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;

/**
 * Drains the principles of a storage through an open spout into the drain target below the spout, at the rate of
 * {@link LibationFlow}. Waits while the spout is closed, there is no target, or the target does not accept the portion.
 * <p>
 * Owns the cooldown and the colour of the last portion, so a block entity holds a drain and ticks it, and only the
 * drain-capable block entities pay for it. Starting and stopping the drain sync the host at once, portions are requested.
 */
public final class PrincipleDrain {

    private static final String COLOR_KEY = "drain_color";

    private final PrincipleStorage storage;
    private final SyncThrottle sync;
    private int cooldown;
    /**
     * True from the portion that starts the drain until the drain stops, so the stop is synced once.
     */
    private boolean draining;
    private int color = PrincipleContents.DEFAULT_COLOR;

    /**
     * @param storage the storage that is drained.
     * @param sync    the throttle of the host of the storage.
     */
    public PrincipleDrain(PrincipleStorage storage, SyncThrottle sync) {
        this.storage = storage;
        this.sync = sync;
    }

    /**
     * The vessel may already be empty while the last drained portion is still falling, so drain visuals use the colour of
     * what was drained rather than the current contents.
     *
     * @return the blended colour of the principles drained last.
     */
    public int color() {
        return this.color;
    }

    /**
     * Ticks the drain of the host, which owns the storage. Sends the drip event to the clients when a portion moved, and
     * syncs the host: at once when the drain starts or stops, otherwise with the portion as a request.
     */
    public void tick(BlockEntity host) {
        Level level = host.getLevel();
        if (level == null) {
            return;
        }
        BlockPos spout = LibationUtils.openSpout(level, host.getBlockPos());
        if (spout == null || this.storage.isEmpty()) {
            this.stop();
            return;
        }
        if (++this.cooldown < LibationFlow.DRAIN_INTERVAL) {
            return;
        }
        this.cooldown = 0;

        ResourceHandler<PrincipleResource> target = LibationUtils.drainTarget(level, spout);
        if (target == null) {
            this.stop();
            return;
        }
        PrincipleContents accepted = PrincipleTransferUtil.extractUpTo(this.storage, target, LibationFlow.DRAIN_AMOUNT, null);
        if (accepted.isEmpty()) {
            this.stop();
            return;
        }
        this.color = accepted.blendedColor();
        boolean starting = !this.draining;
        this.draining = !this.storage.isEmpty();
        if (starting || !this.draining) {
            this.sync.syncNow();
        } else {
            this.sync.requestSync();
        }
        level.blockEvent(spout, level.getBlockState(spout).getBlock(), LibationFlow.EVENT_DRIP, accepted.total());
    }

    /**
     * Resets the cooldown. Syncs at once if the drain was running, so the clients stop with the drain.
     */
    private void stop() {
        this.cooldown = 0;
        if (this.draining) {
            this.draining = false;
            this.sync.syncNow();
        }
    }

    public void load(ValueInput input) {
        this.color = input.getIntOr(COLOR_KEY, PrincipleContents.DEFAULT_COLOR);
    }

    public void save(ValueOutput output) {
        output.putInt(COLOR_KEY, this.color);
    }
}
