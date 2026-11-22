// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.vessel;

import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringEnvironment;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cures in light. Light must be at least {@link #MIN_LIGHT}, the time is scaled by full light over the current light.
 */
public class WabetBasinBlockEntity extends CuringVesselBlockEntity {
    public static final int CAPACITY = 1000;
    public static final int MIN_LIGHT = 9;
    private static final int FULL_LIGHT = 15;

    public WabetBasinBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.WABET_BASIN.get(), pos, state, CAPACITY);
    }

    @Override
    protected CuringEnvironment environment() {
        return CuringEnvironment.LIGHT;
    }

    @Override
    protected boolean canCure(int light) {
        return light >= MIN_LIGHT;
    }

    /**
     * @return the ticks a curing recipe takes in the given light, scaled from {@link #FULL_LIGHT}.
     */
    public static int requiredTime(int time, int light) {
        return time * FULL_LIGHT / light;
    }

    @Override
    protected int requiredTime(CuringRecipe recipe, int light) {
        return requiredTime(recipe.time(), light);
    }
}
