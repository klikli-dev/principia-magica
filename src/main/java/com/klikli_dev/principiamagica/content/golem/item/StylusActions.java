// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pure workplace edits used by the golem stylus. No world access, so they can be unit tested as gametests.
 */
public final class StylusActions {

    public static final int MAX_ZONE_WIDTH = 64;
    public static final int MAX_ZONE_HEIGHT = 16;
    public static final int MAX_ZONE_DEPTH = 64;

    private StylusActions() {
    }

    /**
     * Cycles the link at pos: none -> SUPPLY -> DEPOSIT -> BOTH -> removed.
     */
    public static Workplace cycleLink(Workplace workplace, BlockPos pos, Direction side) {
        List<ContainerLink> links = new ArrayList<>(workplace.links());
        for (int i = 0; i < links.size(); i++) {
            ContainerLink link = links.get(i);
            if (!link.pos().equals(pos)) {
                continue;
            }

            if (link.role() == ContainerRole.BOTH) {
                links.remove(i);
            } else {
                links.set(i, link.withRole(link.role().next()));
            }
            return workplace.withLinks(links).markInitialized();
        }

        links.add(new ContainerLink(pos, side, ContainerRole.SUPPLY));
        return workplace.withLinks(links).markInitialized();
    }

    public static boolean isTooLarge(WorkZone zone) {
        return zone.width() > MAX_ZONE_WIDTH || zone.height() > MAX_ZONE_HEIGHT || zone.depth() > MAX_ZONE_DEPTH;
    }

    /**
     * @return the workplace with the zone added, or empty if the zone exceeds the maximum size
     */
    public static Optional<Workplace> addZone(Workplace workplace, WorkZone zone) {
        if (isTooLarge(zone)) {
            return Optional.empty();
        }

        List<WorkZone> zones = new ArrayList<>(workplace.zones());
        zones.add(zone);
        return Optional.of(workplace.withZones(zones).markInitialized());
    }

    /**
     * Removes the smallest zone containing pos. Returns the workplace unchanged if no zone contains pos.
     */
    public static Workplace removeZoneAt(Workplace workplace, BlockPos pos) {
        WorkZone smallest = null;
        for (WorkZone zone : workplace.zones()) {
            if (zone.contains(pos) && (smallest == null || zone.volume() < smallest.volume())) {
                smallest = zone;
            }
        }

        if (smallest == null) {
            return workplace;
        }

        List<WorkZone> zones = new ArrayList<>(workplace.zones());
        zones.remove(smallest);
        return workplace.withZones(zones).markInitialized();
    }

    /**
     * Overwrites zones and links of the target with the relative clipboard placed at the target anchor. Options are kept.
     */
    public static Workplace paste(Workplace target, Workplace relativeClipboard, BlockPos anchor) {
        Workplace placed = toAbsolute(relativeClipboard, anchor);
        return target.withZones(placed.zones()).withLinks(placed.links()).markInitialized();
    }

    /**
     * Converts a workplace to positions relative to the anchor. Item memory is dropped, as it is absolute per position.
     */
    public static Workplace toRelative(Workplace workplace, BlockPos anchor) {
        return shift(workplace, -anchor.getX(), -anchor.getY(), -anchor.getZ(), true);
    }

    /**
     * Places a relative workplace at the given anchor.
     */
    public static Workplace toAbsolute(Workplace relative, BlockPos anchor) {
        return shift(relative, anchor.getX(), anchor.getY(), anchor.getZ(), false);
    }

    private static Workplace shift(Workplace workplace, int dx, int dy, int dz, boolean clearMemory) {
        List<WorkZone> zones = workplace.zones().stream()
                .map(zone -> new WorkZone(zone.min().offset(dx, dy, dz), zone.max().offset(dx, dy, dz)))
                .toList();
        List<ContainerLink> links = workplace.links().stream()
                .map(link -> new ContainerLink(link.pos().offset(dx, dy, dz), link.side(), link.role()))
                .toList();

        Map<BlockPos, Holder<Item>> memory = clearMemory ? Map.of() : workplace.itemMemory();

        return new Workplace(zones, links, workplace.options(), memory, workplace.initialized());
    }
}
