// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

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
     * Removes the link at pos regardless of its role.
     *
     * @return the workplace without the link, or empty if no link is at pos
     */
    public static Optional<Workplace> removeLink(Workplace workplace, BlockPos pos) {
        List<ContainerLink> links = workplace.links().stream()
                .filter(link -> !link.pos().equals(pos))
                .toList();
        if (links.size() == workplace.links().size()) {
            return Optional.empty();
        }

        return Optional.of(workplace.withLinks(links).markInitialized());
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
     * Erases the footprint that carries exactly this glyph, matching both its block and its face.
     *
     * @return the workplace without the footprint, or empty if no footprint carries the glyph
     */
    public static Optional<Workplace> removeFootprintWithGlyph(Workplace workplace, CarvedGlyph glyph) {
        Optional<WorkFootprint> target = workplace.footprints().stream()
                .filter(footprint -> footprint.glyphs().stream().anyMatch(candidate -> candidate.isSameGlyph(glyph)))
                .findFirst();

        return target.map(footprint -> {
            List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
            footprints.remove(footprint);
            return workplace.withFootprints(footprints).markInitialized();
        });
    }

    /**
     * @return the glyph carved into pos on the face, as stored in the workplace, so its lift is the stored one
     */
    public static Optional<CarvedGlyph> findGlyph(Workplace workplace, BlockPos pos, Direction face) {
        return workplace.footprints().stream()
                .map(footprint -> footprint.findGlyph(pos, face))
                .flatMap(Optional::stream)
                .findFirst();
    }

    /**
     * Lifts or lowers the corner carried by the glyph at pos on the face, clamped to the lift range.
     *
     * @return the workplace with the corner moved, or empty if no footprint carries the glyph
     */
    public static Optional<Workplace> liftCorner(Workplace workplace, BlockPos pos, Direction face, int delta) {
        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        for (int i = 0; i < footprints.size(); i++) {
            WorkFootprint footprint = footprints.get(i);
            Optional<CarvedGlyph> glyph = footprint.findGlyph(pos, face);
            if (glyph.isPresent()) {
                footprints.set(i, footprint.liftGlyph(glyph.get(), delta));
                return Optional.of(workplace.withFootprints(footprints).markInitialized());
            }
        }

        return Optional.empty();
    }

    /**
     * Removes the footprint of the glyph at pos on the face and returns its other corner, so the player can move this
     * corner. The picked glyph's lift is returned too, so the moved corner keeps its height.
     *
     * @return the workplace without the footprint and the pickup, or empty if no two-corner footprint carries the glyph
     */
    public static Optional<PickUp> pickUpGlyph(Workplace workplace, BlockPos pos, Direction face) {
        Optional<WorkFootprint> target = workplace.footprints().stream()
                .filter(footprint -> footprint.glyphs().size() == 2 && footprint.findGlyph(pos, face).isPresent())
                .findFirst();
        if (target.isEmpty()) {
            return Optional.empty();
        }

        CarvedGlyph picked = target.get().findGlyph(pos, face).orElseThrow();
        CarvedGlyph remaining = target.get().glyphs().stream()
                .filter(glyph -> !glyph.isSameGlyph(picked))
                .findFirst()
                .orElseThrow();

        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        footprints.remove(target.get());
        return Optional.of(new PickUp(workplace.withFootprints(footprints).markInitialized(), remaining, picked, target.get()));
    }

    /**
     * @param workplace the workplace without the picked footprint
     * @param remaining the corner that stays, it becomes the first corner of the next carve
     * @param picked    the picked corner with its lift, it is the one the player moves
     * @param original  the footprint as it was before the pick-up, restored if the pick-up is cancelled
     */
    public record PickUp(Workplace workplace, CarvedGlyph remaining, CarvedGlyph picked, WorkFootprint original) {
    }

    /**
     * The pending corner of a pick-up: the remaining corner is the first corner, the picked corner's lift is the ghost lift,
     * and the original footprint is kept for restoring.
     */
    public static PendingGlyph pendingOf(ResourceKey<Level> dimension, PickUp pickUp) {
        return new PendingGlyph(GlobalPos.of(dimension, pickUp.remaining().pos()), pickUp.remaining().face(),
                pickUp.remaining().lift(), pickUp.picked().lift(), List.of(pickUp.original()));
    }

    /**
     * Puts the footprint of a cancelled pick-up back.
     *
     * @return the workplace with the original footprint added back, or unchanged if the pending corner is not a pick-up
     */
    public static Workplace restorePickUp(Workplace workplace, PendingGlyph pending) {
        if (pending.restore().isEmpty()) {
            return workplace;
        }

        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        footprints.addAll(pending.restore());
        return workplace.withFootprints(footprints).markInitialized();
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
