// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting.GlyphHit;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StylusGameTests {

    /**
     * The stylus is a tool in crafting recipes and must come back from the recipe, not be consumed.
     * Checks the real recipes from the recipe manager, which is also what the crafting result slot uses.
     */
    public static void copperStylusReturnedByCraftingRecipes(GameTestHelper helper) {
        var level = helper.getLevel();
        var stylus = new ItemStack(ItemRegistry.COPPER_STYLUS.get());
        var quickenedClay = new ItemStack(ItemRegistry.QUICKENED_CLAY.get());

        CraftingRecipe axiomTablet = craftingRecipe(helper, "axiom_tablet");
        CraftingInput axiomTabletInput = grid(stylus, quickenedClay, quickenedClay);
        helper.assertTrue(axiomTablet.matches(axiomTabletInput, level), "Axiom tablet should match stylus and two quickened clay");
        assertOnlyStylusReturned(helper, axiomTablet.getRemainingItems(axiomTabletInput), "axiom tablet");

        CraftingRecipe golemWood = craftingRecipe(helper, "golem/wood");
        var log = new ItemStack(Items.OAK_LOG);
        var plank = new ItemStack(Items.OAK_PLANKS);
        CraftingInput golemInput = grid(
                ItemStack.EMPTY, log, ItemStack.EMPTY,
                quickenedClay, log, quickenedClay,
                plank, stylus, plank);
        helper.assertTrue(golemWood.matches(golemInput, level), "Wood golem recipe should match its pattern");
        assertOnlyStylusReturned(helper, golemWood.getRemainingItems(golemInput), "wood golem");

        helper.succeed();
    }

    private static CraftingRecipe craftingRecipe(GameTestHelper helper, String path) {
        var key = ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc(path));
        return (CraftingRecipe) helper.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow().value();
    }

    private static void assertOnlyStylusReturned(GameTestHelper helper, NonNullList<ItemStack> remainders, String recipe) {
        helper.assertTrue(remainders.stream().filter(stack -> stack.is(ItemRegistry.COPPER_STYLUS.get())).count() == 1,
                "The copper stylus must be returned by the " + recipe + " recipe");
        helper.assertTrue(remainders.stream().filter(stack -> !stack.isEmpty()).count() == 1,
                "Only the copper stylus may be returned by the " + recipe + " recipe");
    }

    private static CraftingInput grid(ItemStack... stacks) {
        List<ItemStack> items = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
        for (int i = 0; i < stacks.length; i++) {
            items.set(i, stacks[i]);
        }

        return CraftingInput.of(3, 3, items);
    }


    public static void containerLinkCycles(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);

        Workplace workplace = StylusActions.cycleLink(Workplace.EMPTY, pos, Direction.UP);
        helper.assertTrue(workplace.links().size() == 1, "First cycle should add a link");
        helper.assertTrue(workplace.links().get(0).role() == ContainerRole.SUPPLY, "New link should start as SUPPLY");

        workplace = StylusActions.cycleLink(workplace, pos, Direction.NORTH);
        helper.assertTrue(workplace.links().get(0).role() == ContainerRole.DEPOSIT, "Second cycle should be DEPOSIT");

        workplace = StylusActions.cycleLink(workplace, pos, Direction.UP);
        helper.assertTrue(workplace.links().get(0).role() == ContainerRole.BOTH, "Third cycle should be BOTH");

        workplace = StylusActions.cycleLink(workplace, pos, Direction.UP);
        helper.assertTrue(workplace.links().isEmpty(), "Fourth cycle should remove the link");
        helper.assertTrue(workplace.initialized(), "Link edits should mark the workplace initialized");

        helper.succeed();
    }

    /**
     * Shift-right-click on a linked container removes only its link. The footprint around it is kept.
     * On an unlinked container there is nothing to remove, so the link edit is refused.
     */
    public static void removeLinkKeepsFootprint(GameTestHelper helper) {
        BlockPos chest = helper.absolutePos(new BlockPos(2, 2, 1));
        Workplace workplace = StylusActions.cycleLink(Workplace.EMPTY, chest, Direction.UP)
                .withFootprints(List.of(WorkFootprint.of(
                        new CarvedGlyph(helper.absolutePos(new BlockPos(0, 1, 0)), Direction.UP),
                        new CarvedGlyph(helper.absolutePos(new BlockPos(4, 1, 4)), Direction.UP))));

        Optional<Workplace> removed = StylusActions.removeLink(workplace, chest);
        helper.assertTrue(removed.isPresent(), "A linked container should be removable");
        helper.assertTrue(removed.get().links().isEmpty(), "Removing the link should drop it from the workplace");
        helper.assertTrue(removed.get().footprints().size() == 1, "Removing a link should keep the footprint around it");
        helper.assertTrue(removed.get().initialized(), "Removing a link should mark the workplace initialized");

        helper.assertTrue(StylusActions.removeLink(removed.get(), chest).isEmpty(), "An unlinked container should have nothing to remove");

        helper.succeed();
    }

    /**
     * Two glyphs carved on different faces make a footprint spanning both columns. The ground is the lower glyph block.
     */
    public static void carvingTwoGlyphsCreatesFootprint(GameTestHelper helper) {
        BlockPos first = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos second = helper.absolutePos(new BlockPos(6, 3, 5));

        Optional<Workplace> carved = StylusActions.carve(Workplace.EMPTY,
                new CarvedGlyph(first, Direction.UP), new CarvedGlyph(second, Direction.NORTH));
        helper.assertTrue(carved.isPresent(), "Two glyphs should carve a footprint");

        Workplace workplace = carved.get();
        helper.assertTrue(workplace.footprints().size() == 1, "Carving should add exactly one footprint, got " + workplace.footprints().size());
        WorkFootprint footprint = workplace.footprints().getFirst();
        helper.assertTrue(footprint.minX() == first.getX() && footprint.maxX() == second.getX(), "Footprint should span the glyph columns on X");
        helper.assertTrue(footprint.minZ() == first.getZ() && footprint.maxZ() == second.getZ(), "Footprint should span the glyph columns on Z");
        helper.assertTrue(footprint.minY() == first.getY() && footprint.maxY() == second.getY(),
                "The box should span both corner heights, got " + footprint.minY() + " to " + footprint.maxY());
        helper.assertTrue(!footprint.isFlat(), "Corners at different heights should make a volume");
        helper.assertTrue(footprint.glyphs().equals(List.of(new CarvedGlyph(first, Direction.UP), new CarvedGlyph(second, Direction.NORTH))),
                "The footprint should keep both carved glyphs unlifted");
        helper.assertTrue(workplace.initialized(), "Carving should mark the workplace initialized");

        helper.succeed();
    }

    public static void oversizedFootprintIsRejected(GameTestHelper helper) {
        Optional<Workplace> tooLarge = StylusActions.carve(Workplace.EMPTY,
                new CarvedGlyph(new BlockPos(0, 0, 0), Direction.UP),
                new CarvedGlyph(new BlockPos(WorkFootprint.MAX_SIZE, 0, 0), Direction.UP));
        helper.assertTrue(tooLarge.isEmpty(), "A footprint wider than the maximum should be rejected");

        Optional<Workplace> maximum = StylusActions.carve(Workplace.EMPTY,
                new CarvedGlyph(new BlockPos(0, 0, 0), Direction.UP),
                new CarvedGlyph(new BlockPos(WorkFootprint.MAX_SIZE - 1, 0, WorkFootprint.MAX_SIZE - 1), Direction.UP));
        helper.assertTrue(maximum.isPresent(), "A footprint of exactly the maximum size should be accepted");

        helper.succeed();
    }

    /**
     * Erasing the glyph erases its footprint. Without a glyph at the position, the smallest footprint containing it goes.
     */
    public static void shiftCarveErasesFootprint(GameTestHelper helper) {
        BlockPos first = helper.absolutePos(new BlockPos(0, 1, 0));
        BlockPos second = helper.absolutePos(new BlockPos(4, 1, 4));
        Workplace workplace = StylusActions.carve(Workplace.EMPTY,
                new CarvedGlyph(first, Direction.UP), new CarvedGlyph(second, Direction.UP)).orElseThrow();

        Optional<Workplace> erased = StylusActions.removeFootprintAt(workplace, first, 0, 3);
        helper.assertTrue(erased.isPresent() && erased.get().footprints().isEmpty(), "Erasing a glyph should erase its footprint");

        int ground = helper.absolutePos(new BlockPos(0, 1, 0)).getY();
        Workplace nested = Workplace.EMPTY.withFootprints(List.of(
                WorkFootprint.centered(helper.absolutePos(new BlockPos(2, 1, 2)), 4, ground),
                WorkFootprint.centered(helper.absolutePos(new BlockPos(2, 1, 2)), 1, ground)
        ));
        Workplace afterNested = StylusActions.removeFootprintAt(nested, helper.absolutePos(new BlockPos(2, 1, 2)), 0, 3).orElseThrow();
        helper.assertTrue(afterNested.footprints().size() == 1, "Erasing inside nested footprints should remove exactly one");
        helper.assertTrue(afterNested.footprints().get(0).width() == 9, "The smallest containing footprint should be the one removed");

        helper.assertTrue(StylusActions.removeFootprintAt(nested, helper.absolutePos(new BlockPos(50, 1, 50)), 0, 3).isEmpty(),
                "Erasing outside every footprint should change nothing");

        helper.succeed();
    }

    /**
     * A glyph is targeted straight on from up to 40 blocks away on every face. The hit is the entry into its hit box,
     * which sits in front of the block surface.
     */
    public static void glyphTargetHitsEveryFaceFromAfar(GameTestHelper helper) {
        BlockPos pos = new BlockPos(5, 2, 5);
        Vec3 center = Vec3.atCenterOf(pos);
        for (Direction face : Direction.values()) {
            CarvedGlyph glyph = new CarvedGlyph(pos, face);
            List<WorkFootprint> footprints = List.of(new WorkFootprint(0, 0, 0, 0, 0, 0, List.of(glyph)));
            Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());

            Vec3 from = center.add(normal.scale(40.0));
            Vec3 to = from.subtract(normal.scale(GlyphTargeting.MAX_RANGE));
            Optional<GlyphHit> hit = GlyphTargeting.pick(from, to, footprints);
            helper.assertTrue(hit.isPresent(), "A glyph on the " + face + " face should be targeted from 40 blocks away");
            helper.assertTrue(hit.get().glyph().equals(glyph), "The targeted glyph should be the one on the " + face + " face");
            helper.assertTrue(Math.abs(hit.get().distance() - 39.375) < 1.0E-6,
                    "The hit should be the entry into the hit box in front of the surface, got " + hit.get().distance());
            helper.assertTrue(GlyphTargeting.isInReach(from, pos), "A glyph 40 blocks away should be in reach");
        }

        helper.succeed();
    }

    public static void glyphTargetPrefersNearestGlyph(GameTestHelper helper) {
        CarvedGlyph near = new CarvedGlyph(new BlockPos(2, 2, 2), Direction.WEST);
        CarvedGlyph far = new CarvedGlyph(new BlockPos(6, 2, 2), Direction.WEST);
        WorkFootprint farFootprint = new WorkFootprint(0, 0, 0, 0, 0, 0, List.of(far));
        WorkFootprint nearFootprint = new WorkFootprint(0, 0, 0, 0, 0, 0, List.of(near));

        Vec3 from = new Vec3(-10.0, 2.5, 2.5);
        Vec3 to = new Vec3(20.0, 2.5, 2.5);
        Optional<GlyphHit> hit = GlyphTargeting.pick(from, to, List.of(farFootprint, nearFootprint));
        helper.assertTrue(hit.isPresent() && hit.get().glyph().equals(near), "The nearer of two glyphs on one ray should win");
        helper.assertTrue(hit.get().footprint().equals(nearFootprint), "The hit should report the footprint of the nearer glyph");

        Optional<GlyphHit> reversed = GlyphTargeting.pick(to, from, List.of(farFootprint, nearFootprint));
        helper.assertTrue(reversed.isPresent() && reversed.get().glyph().equals(far), "From the other side the far glyph is the nearest");

        helper.succeed();
    }

    /**
     * A ray passing beside the glyph misses it. A glyph that is nearer than the block in front of it wins, a farther one does not.
     */
    public static void glyphTargetMissesBesideAndYieldsToBlocks(GameTestHelper helper) {
        CarvedGlyph glyph = new CarvedGlyph(new BlockPos(5, 2, 5), Direction.UP);
        List<WorkFootprint> footprints = List.of(new WorkFootprint(0, 0, 0, 0, 0, 0, List.of(glyph)));

        Optional<GlyphHit> beside = GlyphTargeting.pick(new Vec3(8.5, 10.0, 5.5), new Vec3(8.5, -10.0, 5.5), footprints);
        helper.assertTrue(beside.isEmpty(), "A ray passing beside the glyph should miss it");

        Optional<GlyphHit> onTop = GlyphTargeting.pick(new Vec3(5.5, 10.0, 5.5), new Vec3(5.5, -10.0, 5.5), footprints);
        helper.assertTrue(onTop.isPresent(), "A ray straight down onto the glyph should hit it");
        double distance = onTop.get().distance();
        helper.assertTrue(GlyphTargeting.winsOverBlock(onTop.get(), 10.0), "A glyph nearer than the block should win");
        helper.assertTrue(GlyphTargeting.winsOverBlock(onTop.get(), distance), "A glyph on the face of the block should win over it");
        helper.assertTrue(!GlyphTargeting.winsOverBlock(onTop.get(), distance - 0.5), "A block nearer than the glyph should win");

        helper.succeed();
    }

    /**
     * Erasing by glyph matches both the block and the face, and only removes the footprint that carries the glyph.
     */
    public static void removeFootprintWithGlyphMatchesBlockAndFace(GameTestHelper helper) {
        CarvedGlyph first = new CarvedGlyph(new BlockPos(0, 1, 0), Direction.UP);
        CarvedGlyph second = new CarvedGlyph(new BlockPos(4, 1, 4), Direction.UP);
        CarvedGlyph otherFirst = new CarvedGlyph(new BlockPos(10, 1, 10), Direction.UP);
        CarvedGlyph otherSecond = new CarvedGlyph(new BlockPos(12, 1, 12), Direction.UP);
        WorkFootprint firstFootprint = WorkFootprint.of(first, second);
        WorkFootprint secondFootprint = WorkFootprint.of(otherFirst, otherSecond);
        Workplace workplace = Workplace.EMPTY.withFootprints(List.of(firstFootprint, secondFootprint));

        helper.assertTrue(StylusActions.removeFootprintWithGlyph(workplace, new CarvedGlyph(first.pos(), Direction.NORTH)).isEmpty(),
                "A glyph on the same block but another face should not erase the footprint");
        helper.assertTrue(StylusActions.removeFootprintWithGlyph(workplace, new CarvedGlyph(new BlockPos(2, 1, 2), Direction.UP)).isEmpty(),
                "A position without a glyph should not erase anything");

        Workplace afterSecond = StylusActions.removeFootprintWithGlyph(workplace, otherSecond).orElseThrow();
        helper.assertTrue(afterSecond.footprints().equals(List.of(firstFootprint)),
                "Erasing a glyph should remove only its footprint, got " + afterSecond.footprints());

        Workplace afterFirst = StylusActions.removeFootprintWithGlyph(workplace, first).orElseThrow();
        helper.assertTrue(afterFirst.footprints().equals(List.of(secondFootprint)), "The other footprint should stay");
        helper.assertTrue(afterFirst.initialized(), "Erasing a glyph should mark the workplace initialized");

        helper.assertTrue(GlyphTargeting.isInReach(new Vec3(0.5, 60.0, 0.5), new BlockPos(0, 60, 40)),
                "A glyph 40 blocks away should be in reach");
        helper.assertTrue(!GlyphTargeting.isInReach(new Vec3(0.5, 60.0, 0.5), new BlockPos(0, 60, 60)),
                "A glyph 60 blocks away should be out of reach");

        helper.succeed();
    }

    public static void copyAndPasteKeepsRelativeLayout(GameTestHelper helper) {
        BlockPos sourceAnchor = new BlockPos(10, 64, 10);
        Workplace source = Workplace.EMPTY
                .withFootprints(List.of(new WorkFootprint(11, 11, 13, 12, 64, 64, List.of())))
                .withLinks(List.of(new ContainerLink(new BlockPos(12, 64, 9), Direction.NORTH, ContainerRole.BOTH)));

        Workplace clipboard = StylusActions.toRelative(source, sourceAnchor);
        helper.assertTrue(clipboard.footprints().get(0).minX() == 1 && clipboard.footprints().get(0).minZ() == 1,
                "Clipboard footprints should be relative to the anchor");
        helper.assertTrue(clipboard.footprints().get(0).minY() == 0, "Clipboard height should be relative to the anchor");

        BlockPos targetAnchor = new BlockPos(20, 70, 30);
        Workplace target = StylusActions.paste(Workplace.EMPTY, clipboard, targetAnchor);
        WorkFootprint pasted = target.footprints().get(0);
        helper.assertTrue(pasted.minX() == 21 && pasted.minZ() == 31, "Pasted footprint should be placed at the target anchor");
        helper.assertTrue(pasted.minY() == 70, "Pasted footprint height should follow the target anchor height, got " + pasted.minY());
        helper.assertTrue(target.links().get(0).pos().equals(new BlockPos(22, 70, 29)), "Pasted link should be placed at the target anchor");
        helper.assertTrue(target.links().get(0).role() == ContainerRole.BOTH, "Pasted link should keep its role");

        helper.succeed();
    }

    /**
     * A lifted corner moves only its Y: it stays on the plumb line of its carved block. The lift is clamped to the lift range.
     */
    public static void cornerLiftKeepsPlumbLineAndClamps(GameTestHelper helper) {
        BlockPos first = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos second = helper.absolutePos(new BlockPos(6, 1, 5));
        Workplace workplace = StylusActions.carve(Workplace.EMPTY,
                new CarvedGlyph(first, Direction.UP), new CarvedGlyph(second, Direction.NORTH)).orElseThrow();

        Workplace lifted = StylusActions.liftCorner(workplace, second, Direction.NORTH, 4).orElseThrow();
        WorkFootprint footprint = lifted.footprints().getFirst();
        CarvedGlyph corner = footprint.findGlyph(second, Direction.NORTH).orElseThrow();
        helper.assertTrue(corner.lift() == 4 && corner.corner().equals(second.above(4)),
                "Lifting should raise the corner by the lift, got " + corner);
        helper.assertTrue(corner.corner().getX() == second.getX() && corner.corner().getZ() == second.getZ(),
                "A lifted corner should stay on its carved block's column");
        helper.assertTrue(footprint.minX() == first.getX() && footprint.maxX() == second.getX() && footprint.minZ() == first.getZ() && footprint.maxZ() == second.getZ(),
                "Lifting should not change the rectangle");
        helper.assertTrue(footprint.minY() == first.getY() && footprint.maxY() == second.getY() + 4,
                "The box should span both corners, got " + footprint.minY() + " to " + footprint.maxY());
        helper.assertTrue(!footprint.isFlat(), "A lifted corner should make a volume");

        helper.assertTrue(StylusActions.liftCorner(workplace, second, Direction.NORTH, 100).orElseThrow()
                        .footprints().getFirst().findGlyph(second, Direction.NORTH).orElseThrow().lift() == WorkFootprint.MAX_LIFT,
                "Lifting should be clamped to the maximum");
        helper.assertTrue(StylusActions.liftCorner(workplace, second, Direction.NORTH, -100).orElseThrow()
                        .footprints().getFirst().findGlyph(second, Direction.NORTH).orElseThrow().lift() == WorkFootprint.MIN_LIFT,
                "Lowering should be clamped to the minimum");

        helper.assertTrue(StylusActions.liftCorner(workplace, second, Direction.UP, 1).isEmpty(), "A corner should only be found on its own face");
        helper.assertTrue(StylusActions.liftCorner(workplace, helper.absolutePos(new BlockPos(9, 1, 9)), Direction.UP, 1).isEmpty(),
                "A position without a corner should be rejected");

        helper.succeed();
    }

    /**
     * Moving a flat footprint keeps it flat and moves all its corners. A volume cannot move past its most extreme corner.
     */
    public static void wholeMoveClampsCorners(GameTestHelper helper) {
        BlockPos first = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos second = helper.absolutePos(new BlockPos(6, 1, 5));
        WorkFootprint flat = WorkFootprint.of(new CarvedGlyph(first, Direction.UP), new CarvedGlyph(second, Direction.NORTH));

        WorkFootprint moved = flat.moved(3);
        helper.assertTrue(moved.isFlat() && moved.minY() == first.getY() + 3, "Moving a flat footprint should keep it flat, got " + moved.minY());
        helper.assertTrue(moved.glyphs().stream().allMatch(glyph -> glyph.lift() == 3), "Moving should lift every corner by the move");

        helper.assertTrue(flat.moved(100).minY() == first.getY() + WorkFootprint.MAX_LIFT, "Moving up should be clamped to the lift range");
        helper.assertTrue(flat.moved(-100).minY() == first.getY() + WorkFootprint.MIN_LIFT, "Moving down should be clamped to the lift range");

        WorkFootprint volume = flat.liftGlyph(new CarvedGlyph(second, Direction.NORTH), WorkFootprint.MAX_LIFT);
        helper.assertTrue(volume.moved(5).equals(volume), "A volume with a corner at the top of the lift range should not move further up");
        helper.assertTrue(volume.moved(-5).minY() == first.getY() - 5 && volume.moved(-5).maxY() == second.getY() + WorkFootprint.MAX_LIFT - 5,
                "A volume should move down as a whole, got " + volume.moved(-5).minY() + " to " + volume.moved(-5).maxY());

        helper.succeed();
    }

    /**
     * Picking up a corner removes its footprint and keeps the other corner and the lift. Re-carving with them keeps the lift.
     */
    public static void pickUpAndRecarveKeepsLift(GameTestHelper helper) {
        BlockPos first = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos second = helper.absolutePos(new BlockPos(6, 1, 5));
        Workplace workplace = StylusActions.carve(Workplace.EMPTY,
                new CarvedGlyph(first, Direction.UP), new CarvedGlyph(second, Direction.NORTH)).orElseThrow();
        workplace = StylusActions.liftCorner(workplace, second, Direction.NORTH, 3).orElseThrow();

        StylusActions.PickUp pickUp = StylusActions.pickUpGlyph(workplace, second, Direction.NORTH).orElseThrow();
        helper.assertTrue(pickUp.workplace().footprints().isEmpty(), "Picking up a corner should remove its footprint");
        helper.assertTrue(pickUp.remaining().isSameGlyph(new CarvedGlyph(first, Direction.UP)), "The other corner should stay as the first corner");
        helper.assertTrue(pickUp.picked().lift() == 3, "The picked corner's lift should be returned, got " + pickUp.picked().lift());
        helper.assertTrue(StylusActions.pickUpGlyph(pickUp.workplace(), second, Direction.NORTH).isEmpty(), "A removed corner should not be picked up again");

        BlockPos target = helper.absolutePos(new BlockPos(8, 1, 8));
        Workplace recarved = StylusActions.carve(pickUp.workplace(), pickUp.remaining(), new CarvedGlyph(target, Direction.UP, pickUp.picked().lift())).orElseThrow();
        WorkFootprint footprint = recarved.footprints().getFirst();
        helper.assertTrue(footprint.maxX() == target.getX() && footprint.maxZ() == target.getZ(), "Re-carving should move the corner to the new block");
        helper.assertTrue(footprint.maxY() == target.getY() + 3 && footprint.minY() == first.getY(),
                "Re-carving should keep the picked corner's lift, got " + footprint.minY() + " to " + footprint.maxY());

        helper.succeed();
    }

    /**
     * Cancelling a pick-up puts the footprint back. The pending corner keeps it through the component codec, so a
     * reloaded stylus restores it too.
     */
    public static void pickUpCancelRestoresFootprint(GameTestHelper helper) {
        BlockPos first = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos second = helper.absolutePos(new BlockPos(6, 1, 5));
        Workplace workplace = StylusActions.carve(Workplace.EMPTY,
                new CarvedGlyph(first, Direction.UP), new CarvedGlyph(second, Direction.NORTH)).orElseThrow();
        workplace = StylusActions.liftCorner(workplace, second, Direction.NORTH, 3).orElseThrow();
        WorkFootprint original = workplace.footprints().getFirst();

        StylusActions.PickUp pickUp = StylusActions.pickUpGlyph(workplace, second, Direction.NORTH).orElseThrow();
        helper.assertTrue(pickUp.workplace().footprints().isEmpty(), "Picking up a corner should remove its footprint");

        PendingGlyph pending = StylusActions.pendingOf(helper.getLevel().dimension(), pickUp);
        Workplace restored = StylusActions.restorePickUp(pickUp.workplace(), pending);
        helper.assertTrue(restored.footprints().equals(List.of(original)),
                "Cancelling the pick-up should restore the original footprint, got " + restored.footprints());

        Tag saved = PendingGlyph.CODEC.encodeStart(NbtOps.INSTANCE, pending).getOrThrow();
        PendingGlyph loaded = PendingGlyph.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
        helper.assertTrue(StylusActions.restorePickUp(pickUp.workplace(), loaded).footprints().equals(List.of(original)),
                "The restore should survive the pending component's codec");

        helper.assertTrue(StylusActions.restorePickUp(restored, PendingGlyph.of(pending.pos(), pending.face())).equals(restored),
                "A fresh pending corner should restore nothing");

        helper.succeed();
    }

    /**
     * Footprints saved before boxes loaded as a flat footprint at ground + raise, and save in the current format afterwards.
     */
    public static void legacyFootprintLoadsFlat(GameTestHelper helper) {
        BlockPos corner = helper.absolutePos(new BlockPos(0, 1, 0));
        CompoundTag legacy = new CompoundTag();
        legacy.putInt("min_x", corner.getX());
        legacy.putInt("min_z", corner.getZ());
        legacy.putInt("max_x", corner.getX() + 4);
        legacy.putInt("max_z", corner.getZ() + 4);
        legacy.putInt("ground_y", corner.getY());
        legacy.putInt("raise", 2);
        CompoundTag glyph = new CompoundTag();
        glyph.put("pos", BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, corner).getOrThrow());
        glyph.put("face", Direction.CODEC.encodeStart(NbtOps.INSTANCE, Direction.UP).getOrThrow());
        ListTag glyphs = new ListTag();
        glyphs.add(glyph);
        legacy.put("glyphs", glyphs);

        WorkFootprint loaded = WorkFootprint.CODEC.parse(NbtOps.INSTANCE, legacy).getOrThrow();
        int level = corner.getY() + 2;
        helper.assertTrue(loaded.isFlat() && loaded.minY() == level && loaded.maxY() == level,
                "A legacy footprint should load flat at ground + raise, got " + loaded.minY() + " to " + loaded.maxY());
        helper.assertTrue(loaded.glyphs().getFirst().corner().getY() == level, "A legacy glyph should sit on the level");
        helper.assertTrue(loaded.minX() == corner.getX() && loaded.maxZ() == corner.getZ() + 4, "A legacy rectangle should load unchanged");

        Tag saved = WorkFootprint.CODEC.encodeStart(NbtOps.INSTANCE, loaded).getOrThrow();
        helper.assertTrue(WorkFootprint.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow().equals(loaded),
                "A migrated footprint should save and load in the current format");

        helper.succeed();
    }
}
