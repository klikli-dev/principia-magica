// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.vessel;

import com.klikli_dev.principiamagica.content.libation.client.VesselEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared behaviour of the Wabet Basin and Steeping Jar: curing principles and draining them through a spout.
 * The spout is a separate vessel tap block placed beside the vessel.
 */
public abstract class CuringVesselBlock extends BaseEntityBlock {

    protected CuringVesselBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * @return the block entity type of this vessel, used for the ticker.
     */
    protected abstract BlockEntityType<? extends CuringVesselBlockEntity> blockEntityType();

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return createTickerHelper(type, this.blockEntityType(), VesselEffects::tick);
        }
        return createTickerHelper(type, this.blockEntityType(), CuringVesselBlockEntity::tick);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof CuringVesselBlockEntity vessel ? vessel.comparatorSignal() : 0;
    }
}
