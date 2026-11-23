// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.spout;

import com.klikli_dev.principiamagica.content.libation.DripEmitter;
import com.klikli_dev.principiamagica.content.libation.LibationFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds the drip queue of a spout. The host sends a drain event to the spout, so the clients drip from the spout tip.
 * Spouts keep no server state: whether they are open is part of their block state.
 */
public abstract class SpoutBlockEntity extends BlockEntity {

    private final DripEmitter drips = new DripEmitter();

    protected SpoutBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * @return the drip queue of the client, filled by the drain events of the host.
     */
    public DripEmitter getDrips() {
        return this.drips;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == LibationFlow.EVENT_DRIP && this.level != null && this.level.isClientSide()) {
            this.drips.add(param);
        }
        return id == LibationFlow.EVENT_DRIP;
    }
}
