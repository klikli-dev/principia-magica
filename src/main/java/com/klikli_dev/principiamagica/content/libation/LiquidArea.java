// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import net.minecraft.util.Mth;

/**
 * Liquid area of a container in model pixels (1/16 block). The surface rises from bottom to top with the amount of principles.
 *
 * @param minX     the minimum x of the area.
 * @param maxX     the maximum x of the area.
 * @param minZ     the minimum z of the area.
 * @param maxZ     the maximum z of the area.
 * @param bottom   the surface height when empty.
 * @param top      the surface height when full.
 * @param capacity the total amount of principles in mB that fill the container.
 */
public record LiquidArea(float minX, float maxX, float minZ, float maxZ, float bottom, float top, int capacity) {

    /**
     * Lift of the liquid surface in blocks, so the empty level does not z-fight with the container.
     */
    private static final float LIQUID_LIFT = 0.01F;

    /**
     * Minimum height of the liquid film in pixels for any non-zero amount.
     */
    private static final float MIN_FILM = 0.5F;

    /**
     * @param total the amount of liquid in mB, may be eased on the client, see {@link AmountSmoother}.
     * @return the surface height in blocks for the given total amount of principles.
     */
    public float surfaceY(float total) {
        float fraction = Mth.clamp(total / (float) this.capacity, 0.0F, 1.0F);
        float level = this.bottom + fraction * (this.top - this.bottom);
        if (total > 0) {
            level = Math.max(level, this.bottom + MIN_FILM);
        }
        return level / 16.0F + LIQUID_LIFT;
    }
}
