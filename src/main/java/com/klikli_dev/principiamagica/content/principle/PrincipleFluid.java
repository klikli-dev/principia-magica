// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Supplier;

/**
 * Non-placeable, bucketless {@link Fluid} representing a single {@link Principle} for NeoForge fluid transfer
 * and cross-mod compatibility.
 */
public class PrincipleFluid extends Fluid {

    private static final float EXPLOSION_RESISTANCE = 100.0F;
    private static final int TICK_DELAY = 5;
    private static final int SOURCE_AMOUNT = 8;

    private final Supplier<? extends FluidType> fluidType;
    private final Holder<Principle> principle;

    public PrincipleFluid(Supplier<? extends FluidType> fluidType, Holder<Principle> principle) {
        this.fluidType = fluidType;
        this.principle = principle;
    }

    public Holder<Principle> principle() {
        return this.principle;
    }

    @Override
    public FluidType getFluidType() {
        return this.fluidType.get();
    }

    @Override
    public Item getBucket() {
        return Items.AIR;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid other, Direction direction) {
        return false;
    }

    @Override
    protected Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState fluidState) {
        return Vec3.ZERO;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return TICK_DELAY;
    }

    @Override
    protected float getExplosionResistance() {
        return EXPLOSION_RESISTANCE;
    }

    @Override
    public float getHeight(FluidState fluidState, BlockGetter level, BlockPos pos) {
        return 0.0F;
    }

    @Override
    public float getOwnHeight(FluidState fluidState) {
        return 0.0F;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState fluidState) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSource(FluidState fluidState) {
        return true;
    }

    @Override
    public int getAmount(FluidState fluidState) {
        return SOURCE_AMOUNT;
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }
}
