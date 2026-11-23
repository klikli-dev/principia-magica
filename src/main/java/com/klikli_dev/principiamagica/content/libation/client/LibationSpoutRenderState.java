// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class LibationSpoutRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean flowing;
    public int flowColor;
}
