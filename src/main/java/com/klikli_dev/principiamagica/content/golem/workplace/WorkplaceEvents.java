// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

public final class WorkplaceEvents {

    /**
     * Golems further away than this are not checked when a block breaks.
     */
    public static final double GOLEM_SEARCH_RANGE = 128.0;

    private WorkplaceEvents() {
    }

    public static void onBlockBreak(BreakBlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            eraseGlyphsAt(level, event.getPos());
        }
    }

    /**
     * Erases the footprint of every nearby golem that has a glyph in the block at pos, e.g. because the block was broken.
     */
    public static void eraseGlyphsAt(ServerLevel level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(GOLEM_SEARCH_RANGE);
        for (GolemEntity golem : level.getEntitiesOfClass(GolemEntity.class, area)) {
            if (golem.getWorkplace(level).filter(workplace -> workplace.hasGlyphAt(pos)).isPresent()) {
                golem.updateWorkplace(current -> current.withoutGlyphAt(pos));
            }
        }
    }
}
