// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import org.jspecify.annotations.Nullable;

/**
 * What a stylus click on a block does, decided from the stylus state and the block alone. The server click handling and the stylus HUD
 * both use it, so the hints always describe the click that happens. Pure, so it can be unit tested.
 */
public final class StylusClick {

    public enum Outcome {
        /** Sets or finishes the pending work-area corner, or places a corner. */
        PLACE_CORNER,
        /** Cycles the container link of the block: none, supply, deposit, both, removed. */
        CYCLE_LINK,
        /** Removes the container link of the block. */
        REMOVE_LINK,
        /** Erases the footprint at the block. */
        ERASE_FOOTPRINT
    }

    private StylusClick() {
    }

    /**
     * @param pending   whether the stylus holds a pending corner in this dimension
     * @param sneaking  whether the player is sneaking
     * @param container whether the block is a container, or a linked position whose container is gone
     */
    public static Outcome blockOutcome(boolean pending, boolean sneaking, boolean container) {
        if (pending && !sneaking) {
            return Outcome.PLACE_CORNER;
        }

        if (container && sneaking) {
            return Outcome.REMOVE_LINK;
        }

        if (container) {
            return Outcome.CYCLE_LINK;
        }

        if (sneaking) {
            return Outcome.ERASE_FOOTPRINT;
        }

        return Outcome.PLACE_CORNER;
    }

    /**
     * @param current the role of the link, or null if the position is not linked
     * @return the role a plain click sets next, mirroring {@code StylusActions.cycleLink}, or null if the click unlinks
     */
    public static @Nullable ContainerRole nextRole(@Nullable ContainerRole current) {
        if (current == null) {
            return ContainerRole.SUPPLY;
        }

        if (current == ContainerRole.BOTH) {
            return null;
        }

        return current.next();
    }
}
