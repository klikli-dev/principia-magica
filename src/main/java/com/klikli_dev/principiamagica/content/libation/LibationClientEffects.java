// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import com.klikli_dev.principiamagica.content.libation.drying.DryingSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.spout.SpoutBlockEntity;
import com.klikli_dev.principiamagica.content.libation.vessel.CuringVesselBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The seam between the common libation code and the client-only effects.
 * <p>
 * The blocks and block entities only ever call the {@link Handler} returned by {@link #get()}, from their client tickers and
 * from the client side of their block events. The client installs its implementation on startup. A dedicated server never
 * installs one, so the common code never loads a client class.
 */
public final class LibationClientEffects {

    private static Handler handler = new Handler() {
    };

    private LibationClientEffects() {
    }

    /**
     * Installs the handler that runs the client effects. Called by the client on startup.
     */
    public static void install(Handler handler) {
        LibationClientEffects.handler = handler;
    }

    /**
     * @return the installed handler, or a handler that does nothing if none is installed, as on a dedicated server.
     */
    public static Handler get() {
        return handler;
    }

    /**
     * The client effects. Every method does nothing by default.
     */
    public interface Handler {

        default void tickSlab(Level level, BlockPos pos, BlockState state, LibationSlabBlockEntity slab) {
        }

        default void tickSpout(Level level, BlockPos pos, BlockState state, SpoutBlockEntity spout) {
        }

        default void tickDrying(Level level, BlockPos pos, BlockState state, DryingSlabBlockEntity drying) {
        }

        default void tickVessel(Level level, BlockPos pos, BlockState state, CuringVesselBlockEntity vessel) {
        }

        /**
         * Burst of effects when the catch trough steeps an item.
         *
         * @param color the blended colour of the principles used for the steeping.
         */
        default void onTroughSteep(Level level, BlockPos pos, int color) {
        }
    }
}
