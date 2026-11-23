// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui;

/**
 * An absolute, screen-space rectangle that pages and tabs lay their widgets out in.
 *
 * @param x left edge in screen coordinates
 * @param y top edge in screen coordinates
 * @param width width in pixels
 * @param height height in pixels
 */
public record ContentRect(int x, int y, int width, int height) {

    public int right() {
        return this.x + this.width;
    }

    public int bottom() {
        return this.y + this.height;
    }
}
