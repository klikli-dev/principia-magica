// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.spout;

import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class VesselTapBlockEntity extends SpoutBlockEntity {

    public VesselTapBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.VESSEL_TAP.get(), pos, state);
    }
}
