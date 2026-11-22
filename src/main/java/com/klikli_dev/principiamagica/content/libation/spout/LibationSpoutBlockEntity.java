// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.spout;

import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LibationSpoutBlockEntity extends SpoutBlockEntity {

    public LibationSpoutBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.LIBATION_SPOUT.get(), pos, state);
    }
}
