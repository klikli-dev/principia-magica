// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Zero-config workplace: a 9x9 zone centred on the anchor, plus any container touching the anchor as BOTH.
 */
public final class WorkplaceDefaults {
    public static final int RADIUS = 4;
    public static final int DOWN = 1;
    public static final int UP = 2;

    private WorkplaceDefaults() {
    }

    public static void initializeIfNeeded(GolemEntity golem, ServerLevel level) {
        golem.getAnchor()
                .filter(anchor -> anchor.dimension().equals(level.dimension()))
                .ifPresent(anchor -> golem.getWorkplace(level)
                        .filter(workplace -> !workplace.initialized())
                        .ifPresent(workplace -> golem.updateWorkplace(current -> initialize(level, anchor.pos(), current))));
    }

    public static Workplace initialize(ServerLevel level, BlockPos anchor, Workplace workplace) {
        if (workplace.initialized()) {
            return workplace;
        }

        List<ContainerLink> links = new ArrayList<>(workplace.links());
        for (Direction direction : Direction.values()) {
            BlockPos pos = anchor.relative(direction);
            Direction side = direction.getOpposite();
            boolean alreadyLinked = links.stream().anyMatch(link -> link.pos().equals(pos));
            if (!alreadyLinked && level.getCapability(Capabilities.Item.BLOCK, pos, side) != null) {
                links.add(new ContainerLink(pos, side, ContainerRole.BOTH));
            }
        }

        return workplace
                .withZones(List.of(WorkZone.centered(anchor, RADIUS, DOWN, UP)))
                .withLinks(links)
                .markInitialized();
    }
}
