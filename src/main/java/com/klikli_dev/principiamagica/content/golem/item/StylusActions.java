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
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Pure workplace edits used by the golem stylus. No world access, so they can be unit tested as gametests.
 */
public final class StylusActions {

    public enum EditFailure {
        CORNER_OCCUPIED,
        TOO_LARGE,
        OUT_OF_BOUNDS,
        NOT_FOUND
    }

    public record EditResult(@Nullable Workplace workplace, @Nullable EditFailure failure) {
        public static EditResult ok(Workplace workplace) {
            return new EditResult(workplace, null);
        }

        public static EditResult fail(EditFailure failure) {
            return new EditResult(null, failure);
        }

        public boolean isSuccess() {
            return this.workplace != null;
        }

        public Optional<Workplace> toOptional() {
            return Optional.ofNullable(this.workplace);
        }
    }

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
     * Drops the links whose position is marked stale by the predicate, e.g. because the container there was broken.
     *
     * @return the workplace without the stale links, or the same workplace if none was stale
     */
    public static Workplace pruneLinks(Workplace workplace, Predicate<BlockPos> stale) {
        List<ContainerLink> links = workplace.links().stream()
                .filter(link -> !stale.test(link.pos()))
                .toList();
        if (links.size() == workplace.links().size()) {
            return workplace;
        }

        return workplace.withLinks(links).markInitialized();
    }

    /**
     * Places a box footprint spanned between any two block corners, enforcing world height bounds and maximum dimensions.
     */
    public static EditResult placeFootprint(Workplace workplace, BlockPos firstCorner, BlockPos secondCorner) {
        return placeFootprint(workplace, WorkFootprint.ofBox(firstCorner, secondCorner));
    }

    public static EditResult placeFootprint(Workplace workplace, ResourceKey<Level> dimension, BlockPos firstCorner, BlockPos secondCorner) {
        return placeFootprint(workplace, WorkFootprint.ofBox(firstCorner, secondCorner, dimension));
    }

    private static EditResult placeFootprint(Workplace workplace, WorkFootprint footprint) {
        if (footprint.isOutOfBounds()) {
            return EditResult.fail(EditFailure.OUT_OF_BOUNDS);
        }

        if (footprint.isTooLarge()) {
            return EditResult.fail(EditFailure.TOO_LARGE);
        }

        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        footprints.add(footprint);
        return EditResult.ok(workplace.withFootprints(footprints).markInitialized());
    }

    /**
     * Restores a previously removed footprint (e.g. when cancelling a corner move).
     */
    public static Workplace restoreFootprint(Workplace workplace, WorkFootprint footprint) {
        return restoreFootprint(workplace, footprint, Map.of());
    }

    /**
     * Restores a previously removed footprint together with the item memory that was dropped when it was picked up.
     * Memory entries that do not lie inside the restored footprint are dropped.
     */
    public static Workplace restoreFootprint(Workplace workplace, WorkFootprint footprint, Map<BlockPos, Holder<Item>> memory) {
        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        footprints.add(footprint);
        return workplace.withFootprints(footprints).withRestoredMemory(memory).markInitialized();
    }

    /**
     * Resizes the footprint at {@code footprintIndex} vertically by moving one of its 8 corners.
     */
    public static EditResult resizeCornerY(Workplace workplace, int footprintIndex, int cornerIndex, int delta) {
        if (footprintIndex < 0 || footprintIndex >= workplace.footprints().size() || !WorkFootprint.isValidCorner(cornerIndex)) {
            return EditResult.fail(EditFailure.NOT_FOUND);
        }

        WorkFootprint footprint = workplace.footprints().get(footprintIndex);
        WorkFootprint updated = footprint.resizeCornerY(cornerIndex, delta);
        if (updated.isOutOfBounds()) {
            return EditResult.fail(EditFailure.OUT_OF_BOUNDS);
        }

        if (updated.isTooLarge()) {
            return EditResult.fail(EditFailure.TOO_LARGE);
        }

        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        footprints.set(footprintIndex, updated);
        return EditResult.ok(workplace.withFootprints(footprints).markInitialized());
    }

    public record PickUpResult(Workplace workplace, PendingGlyph pending) {
    }

    /**
     * Picks up one of the 8 corners of the footprint at {@code footprintIndex}, removing the footprint and returning
     * a pending state anchored at the opposite diagonal corner with {@code original} preserved for cancellation.
     */
    public static Optional<PickUpResult> pickUpCorner(Workplace workplace, ResourceKey<Level> dimension, int footprintIndex, int cornerIndex) {
        if (footprintIndex < 0 || footprintIndex >= workplace.footprints().size() || !WorkFootprint.isValidCorner(cornerIndex)) {
            return Optional.empty();
        }

        WorkFootprint original = workplace.footprints().get(footprintIndex);
        BlockPos fixedCorner = original.cornerBlock(WorkFootprint.oppositeCorner(cornerIndex));
        int initialGhostLift = WorkFootprint.isTopCorner(cornerIndex) ? (original.maxY() - original.minY()) : 0;

        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        footprints.remove(footprintIndex);
        Workplace updated = workplace.withFootprints(footprints).markInitialized();

        // The memory inside the picked up footprint is dropped by withFootprints, keep it on the pending move
        Map<BlockPos, Holder<Item>> droppedMemory = new HashMap<>();
        workplace.itemMemory().forEach((pos, item) -> {
            if (!updated.itemMemory().containsKey(pos)) {
                droppedMemory.put(pos, item);
            }
        });

        PendingGlyph pending = PendingGlyph.moving(GlobalPos.of(dimension, fixedCorner), initialGhostLift, original, droppedMemory);
        return Optional.of(new PickUpResult(updated, pending));
    }

    /**
     * Places the moved footprint of a pending corner move, spanned between the fixed corner and secondCorner. The item memory
     * that was dropped when the corner was picked up is restored where it still lies inside the new footprint.
     */
    public static EditResult completeMove(Workplace workplace, PendingGlyph pending, ResourceKey<Level> dimension, BlockPos secondCorner) {
        EditResult result = placeFootprint(workplace, dimension, pending.pos().pos(), secondCorner);
        if (!result.isSuccess()) {
            return result;
        }

        return EditResult.ok(result.workplace().withRestoredMemory(pending.originalMemory()));
    }

    /**
     * Carves the second glyph of a footprint, spanned between the two glyphs, enforcing unique 3D corner blocks,
     * world height bounds, and maximum dimensions.
     */
    public static EditResult carveResult(Workplace workplace, CarvedGlyph first, CarvedGlyph second) {
        if (first.corner().equals(second.corner()) || workplace.hasCornerAt(first.corner()) || workplace.hasCornerAt(second.corner())) {
            return EditResult.fail(EditFailure.CORNER_OCCUPIED);
        }

        WorkFootprint footprint = WorkFootprint.of(first, second);
        if (footprint.isOutOfBounds()) {
            return EditResult.fail(EditFailure.OUT_OF_BOUNDS);
        }

        if (footprint.isTooLarge()) {
            return EditResult.fail(EditFailure.TOO_LARGE);
        }

        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        footprints.add(footprint);
        return EditResult.ok(workplace.withFootprints(footprints).markInitialized());
    }

    /**
     * Carves the second glyph of a footprint, spanned between the two glyphs.
     *
     * @return the workplace with the footprint added, or empty if rejected
     */
    public static Optional<Workplace> carve(Workplace workplace, CarvedGlyph first, CarvedGlyph second) {
        return carveResult(workplace, first, second).toOptional();
    }

    /**
     * Erases the footprint that owns the glyph at pos. Failing that, erases the smallest footprint whose box contains pos
     * for the given job reach.
     *
     * @return the workplace without the footprint, or empty if no footprint is at pos
     */
    public static Optional<Workplace> removeFootprintAt(Workplace workplace, BlockPos pos, int below, int above) {
        return removeFootprintAt(workplace, null, pos, below, above);
    }

    public static Optional<Workplace> removeFootprintAt(Workplace workplace, @Nullable ResourceKey<Level> dimension, BlockPos pos, int below, int above) {
        Optional<WorkFootprint> target = workplace.footprints().stream()
                .filter(footprint -> dimension == null || footprint.isInDimension(dimension))
                .filter(footprint -> footprint.hasGlyphAt(pos))
                .findFirst()
                .or(() -> workplace.footprints().stream()
                        .filter(footprint -> dimension == null || footprint.isInDimension(dimension))
                        .filter(footprint -> footprint.zone(below, above).contains(pos))
                        .min(Comparator.comparingInt(footprint -> footprint.zone(below, above).volume())));

        return target.map(footprint -> {
            List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
            footprints.remove(footprint);
            return workplace.withFootprints(footprints).markInitialized();
        });
    }

    /**
     * Erases the footprint that carries a glyph whose 3D corner is at cornerPos.
     *
     * @return the workplace without the footprint, or empty if no footprint carries a corner there
     */
    public static Optional<Workplace> removeFootprintByCorner(Workplace workplace, BlockPos cornerPos) {
        Optional<WorkFootprint> target = workplace.footprints().stream()
                .filter(footprint -> footprint.hasCornerAt(cornerPos))
                .findFirst();

        return target.map(footprint -> {
            List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
            footprints.remove(footprint);
            return workplace.withFootprints(footprints).markInitialized();
        });
    }

    /**
     * Erases the footprint at the given index.
     */
    public static Optional<Workplace> removeFootprint(Workplace workplace, int index) {
        if (index < 0 || index >= workplace.footprints().size()) {
            return Optional.empty();
        }

        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        footprints.remove(index);
        return Optional.of(workplace.withFootprints(footprints).markInitialized());
    }

    /**
     * Lifts or lowers the corner at cornerPos, checking bounds, corner collisions, and maximum footprint height.
     */
    public static EditResult liftCorner(Workplace workplace, BlockPos cornerPos, int delta) {
        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        for (int i = 0; i < footprints.size(); i++) {
            WorkFootprint footprint = footprints.get(i);
            Optional<CarvedGlyph> glyph = footprint.findGlyphByCorner(cornerPos);
            if (glyph.isEmpty()) {
                continue;
            }

            CarvedGlyph target = glyph.get();
            int newY = target.corner().getY() + delta;
            if (newY < WorkFootprint.MIN_Y || newY > WorkFootprint.MAX_Y) {
                return EditResult.fail(EditFailure.OUT_OF_BOUNDS);
            }

            BlockPos newCorner = new BlockPos(target.pos().getX(), newY, target.pos().getZ());
            if (hasOtherCornerAt(workplace, target, newCorner)) {
                return EditResult.fail(EditFailure.CORNER_OCCUPIED);
            }

            WorkFootprint updated = footprint.liftGlyph(target, delta);
            if (updated.isTooLarge()) {
                return EditResult.fail(EditFailure.TOO_LARGE);
            }

            footprints.set(i, updated);
            return EditResult.ok(workplace.withFootprints(footprints).markInitialized());
        }

        return EditResult.fail(EditFailure.NOT_FOUND);
    }

    private static boolean hasOtherCornerAt(Workplace workplace, CarvedGlyph moving, BlockPos newCorner) {
        for (WorkFootprint footprint : workplace.footprints()) {
            for (CarvedGlyph other : footprint.glyphs()) {
                if (!other.corner().equals(moving.corner()) && other.corner().equals(newCorner)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Moves the footprint at index up or down by delta, checking bounds and corner collisions with other footprints.
     */
    public static EditResult shiftFootprint(Workplace workplace, int index, int delta) {
        if (index < 0 || index >= workplace.footprints().size()) {
            return EditResult.fail(EditFailure.NOT_FOUND);
        }

        WorkFootprint footprint = workplace.footprints().get(index);
        WorkFootprint moved = footprint.moved(delta);
        if (delta != 0 && moved.minY() == footprint.minY() && moved.maxY() == footprint.maxY()) {
            return EditResult.fail(EditFailure.OUT_OF_BOUNDS);
        }

        for (CarvedGlyph glyph : moved.glyphs()) {
            if (workplace.hasCornerAt(glyph.corner(), index)) {
                return EditResult.fail(EditFailure.CORNER_OCCUPIED);
            }
        }

        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        footprints.set(index, moved);
        return EditResult.ok(workplace.withFootprints(footprints).markInitialized());
    }

    /**
     * Overwrites footprints and links of the target with the relative clipboard placed at the target anchor. Options are kept.
     */
    public static Workplace paste(Workplace target, Workplace relativeClipboard, BlockPos anchor) {
        Workplace placed = toAbsolute(relativeClipboard, anchor);
        return target.withFootprints(placed.footprints()).withLinks(placed.links()).markInitialized();
    }

    public static Workplace paste(Workplace target, Workplace relativeClipboard, BlockPos anchor, ResourceKey<Level> dimension) {
        Workplace placed = toAbsoluteForDimension(relativeClipboard, anchor, dimension);
        return target.withFootprints(placed.footprints()).withLinks(placed.links()).markInitialized();
    }

    /**
     * Converts a workplace to positions relative to the anchor. Item memory is dropped, as it is absolute per position.
     */
    public static Workplace toRelative(Workplace workplace, BlockPos anchor) {
        return shift(workplace, -anchor.getX(), -anchor.getY(), -anchor.getZ(), true);
    }

    public static Workplace toRelative(Workplace workplace, BlockPos anchor, ResourceKey<Level> dimension) {
        Workplace shifted = shift(workplace, -anchor.getX(), -anchor.getY(), -anchor.getZ(), true);
        List<WorkFootprint> stamped = shifted.footprints().stream()
                .map(footprint -> footprint.dimension().isEmpty() ? footprint.withDimension(dimension) : footprint)
                .toList();
        return shifted.withFootprints(stamped);
    }

    /**
     * Places a relative workplace at the given anchor.
     */
    public static Workplace toAbsolute(Workplace relative, BlockPos anchor) {
        return shift(relative, anchor.getX(), anchor.getY(), anchor.getZ(), false);
    }

    /**
     * Places a relative workplace at the given anchor in {@code targetDimension}, dropping any footprints from other
     * dimensions. If any footprint was dropped for a dimension mismatch, container links are also cleared and
     * {@code initialized} is reset when no footprints remain, preventing cross-dimension weirdness.
     */
    public static Workplace toAbsoluteForDimension(Workplace relative, BlockPos anchor, ResourceKey<Level> targetDimension) {
        boolean droppedOtherDimension = relative.footprints().stream()
                .anyMatch(footprint -> !footprint.isInDimension(targetDimension));
        List<WorkFootprint> matching = relative.footprints().stream()
                .filter(footprint -> footprint.isInDimension(targetDimension))
                .map(footprint -> footprint.offset(anchor.getX(), anchor.getY(), anchor.getZ()).withDimension(targetDimension))
                .toList();
        if (droppedOtherDimension) {
            boolean initialized = !matching.isEmpty() && relative.initialized();
            return new Workplace(matching, List.of(), relative.options(), Map.of(), initialized);
        }

        List<ContainerLink> links = relative.links().stream()
                .map(link -> new ContainerLink(link.pos().offset(anchor.getX(), anchor.getY(), anchor.getZ()), link.side(), link.role()))
                .toList();
        return new Workplace(matching, links, relative.options(), relative.itemMemory(), relative.initialized());
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
