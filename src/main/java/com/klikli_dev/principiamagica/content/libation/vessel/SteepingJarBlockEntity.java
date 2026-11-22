// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.vessel;

import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringEnvironment;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cures in the dark. Curing runs with or without a spout, the jar has no light requirement. The lid is on while it cures.
 */
public class SteepingJarBlockEntity extends CuringVesselBlockEntity {
    public static final int CAPACITY = 1000;

    public SteepingJarBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.STEEPING_JAR.get(), pos, state, CAPACITY);
    }

    @Override
    protected CuringEnvironment environment() {
        return CuringEnvironment.DARK;
    }

    @Override
    protected boolean canCure(int light) {
        return true;
    }

    @Override
    protected int requiredTime(CuringRecipe recipe, int light) {
        return recipe.time();
    }

    @Override
    protected void onCuringChanged(boolean curing) {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        BlockState state = this.getBlockState();
        if (state.hasProperty(SteepingJarBlock.LIDDED) && state.getValue(SteepingJarBlock.LIDDED) != curing) {
            this.level.setBlock(this.worldPosition, state.setValue(SteepingJarBlock.LIDDED, curing), Block.UPDATE_ALL);
        }
    }
}
