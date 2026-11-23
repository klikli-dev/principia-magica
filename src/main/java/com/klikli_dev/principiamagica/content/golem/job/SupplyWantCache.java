// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Remembers per golem whether the job wants a supply item. Deciding it scans the job's zones, which is too much to repeat for every slot of a
 * container or every tick a held supply is checked. The answer is shared for {@link #LIFETIME_TICKS}. A different workplace (zones, options,
 * memory or a job change) is decided again at once.
 */
public final class SupplyWantCache {
    /**
     * How long an answer is reused. Changes in the world, such as a planting spot that becomes free, are picked up within this time.
     */
    public static final long LIFETIME_TICKS = 20;

    private final Map<Item, Entry> entries = new HashMap<>();

    /**
     * @param item the supply item the answer is for
     * @param decide computes the answer if no fresh answer for the item and workplace is cached
     * @return the cached or freshly decided answer
     */
    public boolean get(ServerLevel level, Workplace workplace, Item item, BooleanSupplier decide) {
        long now = level.getGameTime();
        Entry entry = this.entries.get(item);
        if (entry != null && entry.workplace().equals(workplace) && now - entry.time() < LIFETIME_TICKS) {
            return entry.wanted();
        }

        boolean wanted = decide.getAsBoolean();
        this.entries.put(item, new Entry(now, workplace, wanted));
        return wanted;
    }

    /**
     * Forgets all answers, e.g. when the golem gets a new job.
     */
    public void clear() {
        this.entries.clear();
    }

    private record Entry(long time, Workplace workplace, boolean wanted) {
    }
}
