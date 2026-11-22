// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

/**
 * Transfer rate shared by the curing vessels and the run-off of the libation slab: {@link #DRAIN_AMOUNT} mB every
 * {@link #DRAIN_INTERVAL} ticks.
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

    private LibationFlow() {
    }
}
