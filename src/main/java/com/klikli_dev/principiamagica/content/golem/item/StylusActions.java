// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pure workplace edits used by the golem stylus. No world access, so they can be unit tested as gametests.
 */
public final class StylusActions {

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

    /**
     * Carves the second glyph of a footprint, spanned between the two glyphs.
     *
     * @return the workplace with the footprint added, or empty if the footprint exceeds the maximum size
     */
    public static Optional<Workplace> carve(Workplace workplace, CarvedGlyph first, CarvedGlyph second) {
        WorkFootprint footprint = WorkFootprint.of(first, second);
        if (footprint.isTooLarge()) {
            return Optional.empty();
        }

        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        footprints.add(footprint);
        return Optional.of(workplace.withFootprints(footprints).markInitialized());
    }

    /**
     * Erases the footprint that owns the glyph at pos. Failing that, erases the smallest footprint whose box contains pos
     * for the given job reach.
     *
     * @return the workplace without the footprint, or empty if no footprint is at pos
     */
    public static Optional<Workplace> removeFootprintAt(Workplace workplace, BlockPos pos, int below, int above) {
        Optional<WorkFootprint> target = workplace.footprints().stream()
                .filter(footprint -> footprint.hasGlyphAt(pos))
                .findFirst()
                .or(() -> workplace.footprints().stream()
                        .filter(footprint -> footprint.zone(below, above).contains(pos))
                        .min(Comparator.comparingInt(footprint -> footprint.zone(below, above).volume())));

        return target.map(footprint -> {
            List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
            footprints.remove(footprint);
            return workplace.withFootprints(footprints).markInitialized();
        });
    }

    /**
     * Overwrites footprints and links of the target with the relative clipboard placed at the target anchor. Options are kept.
     */
    public static Workplace paste(Workplace target, Workplace relativeClipboard, BlockPos anchor) {
        Workplace placed = toAbsolute(relativeClipboard, anchor);
        return target.withFootprints(placed.footprints()).withLinks(placed.links()).markInitialized();
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
        List<WorkFootprint> footprints = workplace.footprints().stream()
                .map(footprint -> footprint.offset(dx, dy, dz))
                .toList();
        List<ContainerLink> links = workplace.links().stream()
                .map(link -> new ContainerLink(link.pos().offset(dx, dy, dz), link.side(), link.role()))
                .toList();

        Map<BlockPos, Holder<Item>> memory = clearMemory ? Map.of() : workplace.itemMemory();

        return new Workplace(footprints, links, workplace.options(), memory, workplace.initialized());
    }
}
