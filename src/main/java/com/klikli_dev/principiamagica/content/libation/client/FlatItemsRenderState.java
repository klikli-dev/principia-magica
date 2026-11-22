// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

import java.util.Collections;
import java.util.List;

public class FlatItemsRenderState extends BlockEntityRenderState {
    public List<ItemStackRenderState> items = Collections.emptyList();
    public Direction facing = Direction.NORTH;
    public int dissolvingSlot = -1;
    public float dissolveScale = 1.0F;
}
