// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

/**
 * Shared numbers of the libation blocks. Transfer rate of the curing vessels and the run-off of the libation slab:
 * {@link #DRAIN_AMOUNT} mB every {@link #DRAIN_INTERVAL} ticks.
 */
public final class LibationFlow {

    public static final int DRAIN_INTERVAL = 10;
    public static final int DRAIN_AMOUNT = 25;

    /**
     * Block event sent to the clients when a portion moved. The parameter is the amount in mB.
     */
    public static final int EVENT_DRIP = 1;

    /**
     * Amount of liquid in mB that makes one drip on the clients.
     */
    public static final int MB_PER_DRIP = 5;

    /**
     * Capacity in mB of the curing vessels, the water and the stored principles of the libation slab.
     */
    public static final int CAPACITY = 1000;

    /**
     * Light level at full daylight. Light-scaled curing takes its recipe time at this light.
     */
    public static final int FULL_LIGHT = 15;

    /**
     * Highest comparator signal, reached when a container is full.
     */
    public static final int MAX_SIGNAL = 15;

    private LibationFlow() {
    }
}
