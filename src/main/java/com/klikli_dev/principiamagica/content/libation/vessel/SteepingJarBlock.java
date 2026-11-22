// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.vessel;

import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SteepingJarBlock extends CuringVesselBlock {
    public static final MapCodec<SteepingJarBlock> CODEC = simpleCodec(SteepingJarBlock::new);

    /**
     * True while the jar is curing. The lid is shown then, with a hole in its centre for principles to be added through.
     */
    public static final BooleanProperty LIDDED = BooleanProperty.create("lidded");

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 14, 14);

    public SteepingJarBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIDDED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIDDED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected BlockEntityType<CuringVesselBlockEntity> blockEntityType() {
        return BlockEntityTypeRegistry.STEEPING_JAR.get();
    }

    @Override
    protected VesselProperties vesselProperties() {
        return VesselProperties.STEEPING_JAR;
    }
}
