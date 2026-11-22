// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.LiquidArea;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class LiquidRenderState extends BlockEntityRenderState {
    public LiquidArea liquid = LibationGeometry.TROUGH;
    public boolean visible;
    public float surfaceY;
    public int color;
    public ItemStackRenderState item = new ItemStackRenderState();
    public float time;
}
