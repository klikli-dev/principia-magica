// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.crop;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Knows how to harvest one family of plants for the farming job. Registered in the static crop_handler registry.
 */
public interface CropHandler {

    /**
     * @return lower values are checked first when several handlers claim the same block
     */
    int priority();

    boolean canHandle(BlockState state);

    boolean isHarvestable(ServerLevel level, BlockPos pos, BlockState state);

    /**
     * Harvests the block at pos. The drops are not spawned, the caller collects them into the golem's inventory.
     */
    HarvestResult harvest(ServerLevel level, BlockPos pos, BlockState state, GolemEntity golem);

    /**
     * @return the item that should be planted at pos to recreate the crop, if the harvest removed the block
     */
    default Optional<Item> seedFor(ServerLevel level, BlockPos pos, BlockState state) {
        return Optional.empty();
    }
}
