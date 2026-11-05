// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.crop;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class CropHandlers {
    private static @Nullable List<CropHandler> sorted;

    private CropHandlers() {
    }

    /**
     * @return all crop handlers ordered by priority. Cached after the first call, when the registry is frozen.
     */
    public static List<CropHandler> all() {
        if (sorted == null) {
            sorted = PrincipiaMagicaRegistries.CROP_HANDLER.stream()
                    .sorted(Comparator.comparingInt(CropHandler::priority))
                    .toList();
        }

        return sorted;
    }

    public static Optional<CropHandler> find(BlockState state) {
        return all().stream().filter(handler -> handler.canHandle(state)).findFirst();
    }

    /**
     * Removes the block without spawning its drops, returning the drops that the block would have produced
     * for the golem's tool.
     */
    public static List<ItemStack> breakBlock(ServerLevel level, GolemEntity golem, BlockPos pos, BlockState state) {
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, golem, golem.getMainHandItem());
        level.destroyBlock(pos, false, golem, 512);
        return drops;
    }

    /**
     * Puts the drops into the golem's inventory. Whatever does not fit is dropped at pos.
     */
    public static void collect(ServerLevel level, GolemEntity golem, BlockPos pos, List<ItemStack> drops) {
        for (ItemStack stack : drops) {
            ItemStack remaining = golem.getInventory().insert(stack);
            if (!remaining.isEmpty()) {
                Block.popResource(level, pos, remaining);
            }
        }
    }
}
