// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.vessel;

import com.klikli_dev.principiamagica.content.libation.LibationFlow;
import com.klikli_dev.principiamagica.content.libation.LiquidArea;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringEnvironment;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.Nullable;

/**
 * Configuration of a curing vessel. A vessel is its block and block entity type plus these properties, so a new vessel is
 * configuration only.
 *
 * @param liquid      the area of the liquid in the vessel, which the renderer draws.
 * @param environment the environment whose curing recipes apply.
 * @param minLight    the lowest light at which curing progresses.
 * @param lid         the block state property that is set while curing, or null if the vessel has no lid.
 */
public record VesselProperties(LiquidArea liquid, CuringEnvironment environment, int minLight, @Nullable BooleanProperty lid) {

    /**
     * Light at which curing in the Wabet Basin needs the recipe time.
     */
    private static final int WABET_MIN_LIGHT = 9;

    public static final VesselProperties WABET_BASIN = new VesselProperties(
            new LiquidArea(3.0F, 13.0F, 3.0F, 13.0F, 3.0F, 11.5F, LibationFlow.CAPACITY),
            CuringEnvironment.LIGHT, WABET_MIN_LIGHT, null);

    public static final VesselProperties STEEPING_JAR = new VesselProperties(
            new LiquidArea(5.0F, 11.0F, 5.0F, 11.0F, 2.0F, 13.5F, LibationFlow.CAPACITY),
            CuringEnvironment.DARK, 0, SteepingJarBlock.LIDDED);

    /**
     * @return the total amount of principles the vessel holds in mB.
     */
    public int capacity() {
        return this.liquid.capacity();
    }

    /**
     * @return true if curing may progress at the given light level.
     */
    public boolean canCure(int light) {
        return light >= this.minLight;
    }

    /**
     * Light-scaled curing takes longer in dimmer light. Dark curing takes the recipe time.
     *
     * @return the number of ticks until a recipe with the given time completes at the given light level.
     */
    public int requiredTime(int time, int light) {
        return this.environment == CuringEnvironment.LIGHT ? time * LibationFlow.FULL_LIGHT / light : time;
    }
}
