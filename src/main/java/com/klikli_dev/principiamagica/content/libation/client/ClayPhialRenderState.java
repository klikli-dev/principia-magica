// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Immutable render state of a clay phial. It only holds data that is not owned by the level, so that changes such as
 * the shift key trigger a re-render.
 *
 * @param principle the registry id of the stored principle, or null if the phial is empty
 * @param shiftDown whether the shift key is currently held
 */
public record ClayPhialRenderState(@Nullable Identifier principle, boolean shiftDown) {
}
