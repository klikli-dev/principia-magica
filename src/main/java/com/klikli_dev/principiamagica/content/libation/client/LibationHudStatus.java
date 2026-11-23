// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.content.libation.drying.DryingSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.spout.VesselTapBlock;
import com.klikli_dev.principiamagica.content.libation.trough.CatchTroughBlockEntity;
import com.klikli_dev.principiamagica.content.libation.vessel.CuringVesselBlockEntity;
import com.klikli_dev.principiamagica.content.libation.vessel.VesselProperties;
import com.klikli_dev.principiamagica.content.principle.PrincipleContainer;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringEnvironment;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Extracts the status lines the libation HUD shows for each source: vessels, spouts and taps, the libation slab, the drying slab
 * and the catch trough. Only reads state, drawing is left to {@link LibationHud}.
 */
final class LibationHudStatus {

    static final int NEUTRAL_COLOR = 0xFFAAAAAA;
    static final int CURING_COLOR = 0xFFFFD36B;
    static final int OK_COLOR = 0xFF55DD55;
    static final int PROBLEM_COLOR = 0xFFFF5555;

    private LibationHudStatus() {
    }

    /**
     * A status line with its text and colour.
     */
    record Status(Component text, int color) {
    }

    /**
     * @return the status lines of a vessel: curing, its spout and the reason it does not cure. Empty for containers without status.
     */
    static List<Status> container(ClientLevel level, BlockPos pos, PrincipleContainer container) {
        List<Status> statuses = new ArrayList<>();
        if (!(container instanceof CuringVesselBlockEntity vessel)) {
            return statuses;
        }

        if (vessel.isCuring()) {
            statuses.add(of("hud.principiamagica.libation.status.curing", CURING_COLOR));
        }
        statuses.add(hostSpout(level, pos));
        VesselProperties properties = vessel.properties();
        if (properties.environment() == CuringEnvironment.DARK) {
            if (!vessel.getPrinciples().isEmpty() && LibationRecipes.curing(vessel.getPrinciples(), properties.environment()).isEmpty()) {
                statuses.add(of("hud.principiamagica.libation.status.no_recipe", PROBLEM_COLOR));
            }
        } else if (!properties.canCure(level.getMaxLocalRawBrightness(pos))) {
            statuses.add(of("hud.principiamagica.libation.status.too_dark", PROBLEM_COLOR));
        }
        return statuses;
    }

    /**
     * @return the curing progress in [0, 1] of a vessel that is curing, or empty.
     */
    static Optional<Float> curingProgress(ClientLevel level, BlockPos pos, PrincipleContainer container) {
        if (!(container instanceof CuringVesselBlockEntity vessel) || !vessel.isCuring()) {
            return Optional.empty();
        }

        VesselProperties properties = vessel.properties();
        Optional<CuringRecipe> recipe = LibationRecipes.curing(vessel.getPrinciples(), properties.environment());
        if (recipe.isEmpty()) {
            return Optional.empty();
        }

        int light = level.getMaxLocalRawBrightness(pos);
        if (!properties.canCure(light)) {
            return Optional.empty();
        }
        int time = properties.requiredTime(recipe.get().time(), light);
        if (time <= 0) {
            return Optional.empty();
        }
        return Optional.of(Mth.clamp(vessel.getProgress() / (float) time, 0.0F, 1.0F));
    }

    /**
     * @return the status lines of a spout block: open or closed, and whether an open spout has a container below it to drain into.
     */
    static List<Status> spout(ClientLevel level, BlockPos pos) {
        List<Status> statuses = new ArrayList<>();
        boolean open = level.getBlockState(pos).getValue(BlockStateProperties.OPEN);
        statuses.add(openStatus(level.getBlockState(pos).getBlock() instanceof VesselTapBlock, open));
        if (open && LibationUtils.drainTarget(level, pos) == null) {
            statuses.add(of("hud.principiamagica.libation.status.no_target", PROBLEM_COLOR));
        }
        return statuses;
    }

    /**
     * @return the status of the drying: the reason it does not progress, or drying. Empty when the slab holds no input.
     */
    static List<Status> drying(ClientLevel level, DryingSlabBlockEntity drying, Optional<DryingRecipe> recipe) {
        List<Status> statuses = new ArrayList<>(1);
        ItemStack input = drying.getInput();
        if (input.isEmpty()) {
            return statuses;
        }

        if (recipe.isEmpty()) {
            statuses.add(of("hud.principiamagica.libation.status.no_recipe", PROBLEM_COLOR));
        } else if (!drying.canMergeOutput(recipe.get().assemble(new SingleRecipeInput(input.copyWithCount(1))))) {
            statuses.add(of("hud.principiamagica.libation.status.output_full", PROBLEM_COLOR));
        } else if (level.getMaxLocalRawBrightness(drying.getBlockPos().above()) <= 0) {
            statuses.add(of("hud.principiamagica.libation.status.too_dark", PROBLEM_COLOR));
        } else {
            statuses.add(of("hud.principiamagica.libation.status.drying", CURING_COLOR));
        }
        return statuses;
    }

    /**
     * @return the status of the item in the trough: steeping, or the reason it does not steep. Empty when the trough holds no item.
     */
    static List<Status> trough(CatchTroughBlockEntity trough) {
        List<Status> statuses = new ArrayList<>(1);
        ItemStack item = trough.getItem();
        if (item.isEmpty()) {
            return statuses;
        }

        if (LibationRecipes.steepingFor(item).isEmpty()) {
            statuses.add(of("hud.principiamagica.libation.status.no_recipe", PROBLEM_COLOR));
        } else if (LibationRecipes.steeping(item, trough.getPrinciples()).isEmpty()) {
            statuses.add(of("hud.principiamagica.libation.status.missing_principles", PROBLEM_COLOR));
        } else {
            statuses.add(of("hud.principiamagica.libation.status.steeping", CURING_COLOR));
        }
        return statuses;
    }

    /**
     * @return the status of the spout that feeds a host: open, closed or missing. Shown for the host, which only drains through an open spout.
     */
    static Status hostSpout(ClientLevel level, BlockPos host) {
        boolean vessel = level.getBlockEntity(host) instanceof CuringVesselBlockEntity;
        Direction side = LibationUtils.spoutSide(level, host);
        if (side == null) {
            return of(vessel ? "hud.principiamagica.libation.status.no_tap" : "hud.principiamagica.libation.status.no_spout", PROBLEM_COLOR);
        }
        boolean open = level.getBlockState(host.relative(side)).getValue(BlockStateProperties.OPEN);
        return openStatus(vessel, open);
    }

    /**
     * @return the status of a spout or tap: "Tap" for the vessels, "Spout" for the slab.
     */
    static Status openStatus(boolean vessel, boolean open) {
        if (vessel) {
            return open
                    ? of("hud.principiamagica.libation.status.tap_open", OK_COLOR)
                    : of("hud.principiamagica.libation.status.tap_closed", NEUTRAL_COLOR);
        }
        return open
                ? of("hud.principiamagica.libation.status.spout_open", OK_COLOR)
                : of("hud.principiamagica.libation.status.spout_closed", NEUTRAL_COLOR);
    }

    private static Status of(String translationKey, int color) {
        return new Status(Component.translatable(translationKey), color);
    }
}
