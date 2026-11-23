// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.crop;

import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
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
}
