// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import com.klikli_dev.principiamagica.content.principle.PrincipleContainer;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Drains the principles of a storage through an open spout into the drain target below the spout, at the rate of
 * {@link LibationFlow}. Waits while the spout is closed, there is no target, or the target does not accept the portion.
 * <p>
 * Owns the cooldown and the colour of the last portion, so a block entity holds a drain and ticks it, and only the
 * drain-capable block entities pay for it.
 */
public final class PrincipleDrain {

    private static final String COLOR_KEY = "drain_color";

    private final PrincipleStorage storage;
    private int cooldown;
    private int color = PrincipleContents.DEFAULT_COLOR;

    public PrincipleDrain(PrincipleStorage storage) {
        this.storage = storage;
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
     * Ticks the drain of the host, which owns the storage. Syncs the host and sends the drip event to the clients when a
     * portion moved.
     */
    public void tick(BlockEntity host) {
        Level level = host.getLevel();
        if (level == null) {
            return;
        }
        BlockPos spout = LibationUtils.openSpout(level, host.getBlockPos());
        if (spout == null || this.storage.isEmpty()) {
            this.cooldown = 0;
            return;
        }
        if (++this.cooldown < LibationFlow.DRAIN_INTERVAL) {
            return;
        }
        this.cooldown = 0;

        PrincipleContainer target = LibationUtils.drainTarget(level, spout);
        if (target == null) {
            return;
        }
        PrincipleContents portion = PrincipleStorage.takeUpTo(this.storage.contents(), LibationFlow.DRAIN_AMOUNT);
        PrincipleContents accepted = portion.subtract(target.insert(portion, false));
        if (accepted.isEmpty()) {
            return;
        }
        this.storage.set(this.storage.contents().subtract(accepted));
        this.color = accepted.blendedColor();
        LibationUtils.sync(host);
        level.blockEvent(spout, level.getBlockState(spout).getBlock(), LibationFlow.EVENT_DRIP, accepted.total());
    }

    public void load(ValueInput input) {
        this.color = input.getIntOr(COLOR_KEY, PrincipleContents.DEFAULT_COLOR);
    }

    public void save(ValueOutput output) {
        output.putInt(COLOR_KEY, this.color);
    }
}
