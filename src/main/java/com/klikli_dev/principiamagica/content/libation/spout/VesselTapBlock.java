// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.spout;

import com.klikli_dev.principiamagica.content.libation.vessel.CuringVesselBlockEntity;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The bronze tap that drains a Wabet Basin or Steeping Jar. Its valve is open when the tap is open.
 */
public class VesselTapBlock extends SpoutBlock {
    public static final MapCodec<VesselTapBlock> CODEC = simpleCodec(VesselTapBlock::new);

    public VesselTapBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * The nozzle, pipe and valve, the part of the tap inside its own cell.
     */
    @Override
    protected double[] northBounds() {
        return new double[]{5.5, 1, 7, 10.5, 9, 16};
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VesselTapBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends SpoutBlockEntity> blockEntityType() {
        return BlockEntityTypeRegistry.VESSEL_TAP.get();
    }

    @Override
    protected boolean acceptsHost(@Nullable BlockEntity host) {
        return host instanceof CuringVesselBlockEntity;
    }

    @Override
    protected void playToggleSound(Level level, BlockPos pos, boolean open) {
        level.playSound(null, pos, open ? SoundRegistry.VESSEL_TAP_OPEN.get() : SoundRegistry.VESSEL_TAP_CLOSE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
