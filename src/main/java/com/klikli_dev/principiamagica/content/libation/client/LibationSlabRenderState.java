// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class LibationSlabRenderState extends FlatItemsRenderState {
    public final LiquidRenderState water = new LiquidRenderState();
    /**
     * The side of the slab that holds a spout, or null. The water face is drawn on this side.
     */
    public @Nullable Direction spoutSide;
}
