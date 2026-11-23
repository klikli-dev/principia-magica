// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.spout;

import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The stone channel of the libation slab. Tilts up when closed and down when open.
 */
public class LibationSpoutBlock extends SpoutBlock {
    public static final MapCodec<LibationSpoutBlock> CODEC = simpleCodec(LibationSpoutBlock::new);

    public LibationSpoutBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * The channel from the mouth at the cell edge to the tip above the centre, covering the closed pose as well as the open one.
     */
    @Override
    protected double[] northBounds() {
        return new double[]{6, 5, 7, 10, 16, 16};
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LibationSpoutBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends SpoutBlockEntity> blockEntityType() {
        return BlockEntityTypeRegistry.LIBATION_SPOUT.get();
    }

    @Override
    protected boolean acceptsHost(@Nullable BlockEntity host) {
        return host instanceof LibationSlabBlockEntity;
    }

    @Override
    protected void playToggleSound(Level level, BlockPos pos, boolean open) {
        level.playSound(null, pos, open ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.3F, 1.0F);
    }
}
