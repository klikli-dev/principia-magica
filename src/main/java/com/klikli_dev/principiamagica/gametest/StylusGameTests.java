// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions.EditFailure;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions.EditResult;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions.PickUpResult;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting.CornerHit;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting.GlyphHit;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting.ZoneHit;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * Erasing by corner matches the 3D corner position, including when one corner was lifted away from its base block
     * and another footprint carved a new corner on that vacated base block.
     */
    public static void removeFootprintWithGlyphMatchesBlockAndFace(GameTestHelper helper) {
        CarvedGlyph first = new CarvedGlyph(new BlockPos(0, 1, 0), Direction.UP);
        CarvedGlyph second = new CarvedGlyph(new BlockPos(4, 1, 4), Direction.UP);
        CarvedGlyph otherFirst = new CarvedGlyph(new BlockPos(10, 1, 10), Direction.UP);
        CarvedGlyph otherSecond = new CarvedGlyph(new BlockPos(12, 1, 12), Direction.UP);
        WorkFootprint firstFootprint = WorkFootprint.of(first, second);
        WorkFootprint secondFootprint = WorkFootprint.of(otherFirst, otherSecond);
        Workplace workplace = Workplace.EMPTY.withFootprints(List.of(firstFootprint, secondFootprint));

        helper.assertTrue(StylusActions.removeFootprintByCorner(workplace, new BlockPos(2, 1, 2)).isEmpty(),
                "A position without a corner should not erase anything");

        Workplace afterSecond = StylusActions.removeFootprintByCorner(workplace, otherSecond.corner()).orElseThrow();
        helper.assertTrue(afterSecond.footprints().equals(List.of(firstFootprint)),
                "Erasing a corner should remove only its footprint, got " + afterSecond.footprints());

        Workplace lifted = StylusActions.liftCorner(workplace, first.corner(), 2).toOptional().orElseThrow();
        Workplace withVacatedBase = StylusActions.carve(lifted,
                new CarvedGlyph(first.pos(), Direction.NORTH),
                new CarvedGlyph(new BlockPos(1, 1, 1), Direction.UP)).orElseThrow();
        Workplace afterLiftedRemoved = StylusActions.removeFootprintByCorner(withVacatedBase, first.pos().above(2)).orElseThrow();
        helper.assertTrue(afterLiftedRemoved.footprints().size() == 2
                        && afterLiftedRemoved.hasCornerAt(first.pos())
                        && !afterLiftedRemoved.hasCornerAt(first.pos().above(2)),
                "Erasing by 3D corner should remove only the footprint owning that 3D corner");

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
     * A lifted corner moves only its Y: it stays on the plumb line of its carved block.
     * Corners can be lifted beyond 16 blocks up to MAX_SIZE, and cannot exceed MAX_SIZE or world height limits.
     */
    public static void cornerLiftKeepsPlumbLineAndClamps(GameTestHelper helper) {
        BlockPos first = new BlockPos(2, 0, 2);
        BlockPos second = new BlockPos(6, 0, 5);
        Workplace workplace = StylusActions.carve(Workplace.EMPTY,
                new CarvedGlyph(first, Direction.UP), new CarvedGlyph(second, Direction.NORTH)).orElseThrow();

        Workplace lifted = StylusActions.liftCorner(workplace, second, 20).toOptional().orElseThrow();
        WorkFootprint footprint = lifted.footprints().getFirst();
        CarvedGlyph corner = footprint.findGlyphByCorner(second.above(20)).orElseThrow();
        helper.assertTrue(corner.lift() == 20 && corner.corner().equals(second.above(20)),
                "Lifting should raise the corner by 20, got " + corner);
        helper.assertTrue(corner.corner().getX() == second.getX() && corner.corner().getZ() == second.getZ(),
                "A lifted corner should stay on its carved block's column");
        helper.assertTrue(footprint.minY() == 0 && footprint.maxY() == 20,
                "The box should span both corners, got " + footprint.minY() + " to " + footprint.maxY());
        helper.assertTrue(!footprint.isFlat(), "A lifted corner should make a volume");

        EditResult tooTall = StylusActions.liftCorner(workplace, second, WorkFootprint.MAX_SIZE);
        helper.assertTrue(!tooTall.isSuccess() && tooTall.failure() == EditFailure.TOO_LARGE,
                "Lifting past MAX_SIZE height should fail with TOO_LARGE");

        EditResult outOfBounds = StylusActions.liftCorner(workplace, second, WorkFootprint.MIN_Y - 1);
        helper.assertTrue(!outOfBounds.isSuccess() && outOfBounds.failure() == EditFailure.OUT_OF_BOUNDS,
                "Lowering past MIN_Y should fail with OUT_OF_BOUNDS");

        EditResult missing = StylusActions.liftCorner(workplace, new BlockPos(9, 1, 9), 1);
        helper.assertTrue(!missing.isSuccess() && missing.failure() == EditFailure.NOT_FOUND,
                "A position without a corner should fail with NOT_FOUND");

        helper.succeed();
    }

    /**
     * Moving a flat or volume footprint moves all its corners and clamps to world build limits [MIN_Y, MAX_Y].
     */
    public static void wholeMoveClampsCorners(GameTestHelper helper) {
        BlockPos first = new BlockPos(2, 10, 2);
        BlockPos second = new BlockPos(6, 10, 5);
        WorkFootprint flat = WorkFootprint.of(new CarvedGlyph(first, Direction.UP), new CarvedGlyph(second, Direction.NORTH));

        WorkFootprint moved = flat.moved(25);
        helper.assertTrue(moved.isFlat() && moved.minY() == 35, "Moving a flat footprint should keep it flat, got " + moved.minY());
        helper.assertTrue(moved.glyphs().stream().allMatch(glyph -> glyph.lift() == 25), "Moving should lift every corner by 25");

        helper.assertTrue(flat.moved(1000).minY() == WorkFootprint.MAX_Y, "Moving up should clamp to MAX_Y");
        helper.assertTrue(flat.moved(-1000).minY() == WorkFootprint.MIN_Y, "Moving down should clamp to MIN_Y");

        WorkFootprint volume = flat.liftGlyph(new CarvedGlyph(second, Direction.NORTH), 10);
        WorkFootprint topClamped = volume.moved(1000);
        helper.assertTrue(topClamped.maxY() == WorkFootprint.MAX_Y && topClamped.minY() == WorkFootprint.MAX_Y - 10,
                "A volume moved to MAX_Y should preserve its height, got " + topClamped.minY() + " to " + topClamped.maxY());

        helper.succeed();
    }

    public static void cornerCollisionBlockedOnPlacement(GameTestHelper helper) {
        BlockPos a = new BlockPos(0, 1, 0);
        BlockPos b = new BlockPos(4, 1, 4);
        BlockPos c = new BlockPos(1, 1, 1);
        BlockPos d = new BlockPos(5, 1, 5);

        EditResult sameCorner = StylusActions.carveResult(Workplace.EMPTY,
                new CarvedGlyph(a, Direction.UP), new CarvedGlyph(a, Direction.NORTH));
        helper.assertTrue(!sameCorner.isSuccess() && sameCorner.failure() == EditFailure.CORNER_OCCUPIED,
                "Carving both corners on the same block should be rejected");

        Workplace first = StylusActions.carve(Workplace.EMPTY,
                new CarvedGlyph(a, Direction.UP), new CarvedGlyph(b, Direction.UP)).orElseThrow();

        Workplace overlapping = StylusActions.carve(first,
                new CarvedGlyph(c, Direction.UP), new CarvedGlyph(d, Direction.UP)).orElseThrow();
        helper.assertTrue(overlapping.footprints().size() == 2,
                "Overlapping footprints with distinct corners should be allowed");

        EditResult collidingFirst = StylusActions.carveResult(first,
                new CarvedGlyph(a, Direction.NORTH), new CarvedGlyph(c, Direction.UP));
        helper.assertTrue(!collidingFirst.isSuccess() && collidingFirst.failure() == EditFailure.CORNER_OCCUPIED,
                "Carving a corner onto an occupied 3D block should be rejected");

        EditResult collidingSecond = StylusActions.carveResult(first,
                new CarvedGlyph(c, Direction.UP), new CarvedGlyph(b, Direction.SOUTH));
        helper.assertTrue(!collidingSecond.isSuccess() && collidingSecond.failure() == EditFailure.CORNER_OCCUPIED,
                "Carving the second corner onto an occupied 3D block should be rejected");

        helper.succeed();
    }

    public static void cornerCollisionBlockedOnLiftAndShift(GameTestHelper helper) {
        BlockPos base = new BlockPos(0, 10, 0);
        Workplace pillar = StylusActions.carve(Workplace.EMPTY,
                new CarvedGlyph(base, Direction.UP, 0), new CarvedGlyph(base, Direction.UP, 1)).orElseThrow();

        EditResult selfCollision = StylusActions.liftCorner(pillar, base, 1);
        helper.assertTrue(!selfCollision.isSuccess() && selfCollision.failure() == EditFailure.CORNER_OCCUPIED,
                "Lifting a corner into the other corner of the same footprint should be rejected");

        BlockPos a1 = new BlockPos(2, 10, 2);
        BlockPos a2 = new BlockPos(4, 10, 4);
        BlockPos b1 = new BlockPos(2, 12, 2);
        BlockPos b2 = new BlockPos(5, 12, 5);
        Workplace twoZones = StylusActions.carve(Workplace.EMPTY,
                new CarvedGlyph(a1, Direction.UP), new CarvedGlyph(a2, Direction.UP)).orElseThrow();
        twoZones = StylusActions.carve(twoZones,
                new CarvedGlyph(b1, Direction.UP), new CarvedGlyph(b2, Direction.UP)).orElseThrow();

        EditResult liftCollision = StylusActions.liftCorner(twoZones, a1, 2);
        helper.assertTrue(!liftCollision.isSuccess() && liftCollision.failure() == EditFailure.CORNER_OCCUPIED,
                "Lifting a corner into another footprint's corner should be rejected");

        EditResult shiftCollision = StylusActions.shiftFootprint(twoZones, 0, 2);
        helper.assertTrue(!shiftCollision.isSuccess() && shiftCollision.failure() == EditFailure.CORNER_OCCUPIED,
                "Shifting a footprint so a corner collides with another footprint's corner should be rejected");

        helper.succeed();
    }

    public static void zoneBoxTargetingPicksNearestAndFloatingBoxes(GameTestHelper helper) {
        WorkFootprint near = WorkFootprint.of(
                new CarvedGlyph(new BlockPos(2, 10, 2), Direction.UP),
                new CarvedGlyph(new BlockPos(4, 12, 4), Direction.UP));
        WorkFootprint far = WorkFootprint.of(
                new CarvedGlyph(new BlockPos(10, 10, 2), Direction.UP),
                new CarvedGlyph(new BlockPos(12, 12, 4), Direction.UP));
        WorkFootprint outer = WorkFootprint.of(
                new CarvedGlyph(new BlockPos(0, 8, 0), Direction.UP),
                new CarvedGlyph(new BlockPos(14, 14, 6), Direction.UP));

        Vec3 from = new Vec3(-5.0, 11.5, 3.5);
        Vec3 to = new Vec3(20.0, 11.5, 3.5);

        Optional<ZoneHit> hit = GlyphTargeting.pickZone(from, to, List.of(far, near), 100.0);
        helper.assertTrue(hit.isPresent() && hit.get().index() == 1 && !hit.get().inside(),
                "Ray should hit the nearer floating box first");

        Optional<ZoneHit> blocked = GlyphTargeting.pickZone(from, to, List.of(far, near), 3.0);
        helper.assertTrue(blocked.isEmpty(), "A solid block nearer than the box should win");

        Vec3 insideEye = new Vec3(3.5, 11.5, 3.5);
        Optional<ZoneHit> insideHit = GlyphTargeting.pickZone(insideEye, insideEye.add(10.0, 0.0, 0.0), List.of(outer, near), 100.0);
        helper.assertTrue(insideHit.isPresent() && insideHit.get().index() == 1 && insideHit.get().inside(),
                "When eyes are inside nested boxes, the smallest containing box should win");

        helper.assertTrue(GlyphTargeting.isBoxInReach(from, near.zone(0, 0).aabb()),
                "A box 7 blocks away should be in reach");
        helper.assertTrue(!GlyphTargeting.isBoxInReach(new Vec3(-100.0, 11.5, 3.5), near.zone(0, 0).aabb()),
                "A box 100 blocks away should be out of reach");

        helper.succeed();
    }

    public static void rightClickBlockSuppressesBlockUseWhenGolemSelected(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stylus = new ItemStack(ItemRegistry.COPPER_STYLUS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stylus);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);

        PlayerInteractEvent.RightClickBlock unselected = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, hit);
        StylusItem.onRightClickBlock(unselected);
        helper.assertTrue(unselected.getUseBlock() == TriState.DEFAULT,
                "Without a selected golem, block interaction should not be suppressed");

        stylus.set(DataComponentRegistry.STYLUS_SELECTION.get(), UUID.randomUUID());
        PlayerInteractEvent.RightClickBlock selected = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, hit);
        StylusItem.onRightClickBlock(selected);
        helper.assertTrue(selected.getUseBlock() == TriState.FALSE,
                "With a selected golem, block interaction should be suppressed so containers link on right-click");

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

    public static void eightCornersRaycastAndOcclusion(GameTestHelper helper) {
        WorkFootprint flat = WorkFootprint.ofBox(new BlockPos(2, 64, 2), new BlockPos(5, 64, 5), Level.OVERWORLD);
        WorkFootprint volume = WorkFootprint.ofBox(new BlockPos(10, 64, 10), new BlockPos(14, 68, 14), Level.OVERWORLD);
        WorkFootprint nether = WorkFootprint.ofBox(new BlockPos(2, 64, 2), new BlockPos(5, 64, 5), Level.NETHER);

        for (int i = 0; i < 8; i++) {
            BlockPos block = volume.cornerBlock(i);
            Vec3 center = Vec3.atCenterOf(block);
            Vec3 from = center.add(17.0, 23.0, 11.0);
            Vec3 to = center.subtract(17.0, 23.0, 11.0);
            Optional<CornerHit> hit = GlyphTargeting.pickCorner(from, to, List.of(volume), Level.OVERWORLD);
            helper.assertTrue(hit.isPresent() && hit.get().cornerIndex() == i && hit.get().block().equals(block),
                    "Ray through virtual corner block " + i + " from afar should hit corner " + i + ", got " + hit);
        }

        // Virtual corner block slightly proud (1.0625) wins over a solid block at that same coordinate
        BlockPos corner0Block = flat.cornerBlock(0);
        Vec3 fromAbove = Vec3.atCenterOf(corner0Block).add(0.0, 10.0, 0.0);
        Vec3 toBelow = Vec3.atCenterOf(corner0Block).subtract(0.0, 10.0, 0.0);
        Optional<CornerHit> onCornerBlock = GlyphTargeting.pickCorner(fromAbove, toBelow, List.of(flat), Level.OVERWORLD);
        helper.assertTrue(onCornerBlock.isPresent() && onCornerBlock.get().block().equals(corner0Block),
                "Virtual corner block should win over the block surface at the same coordinate");

        // Dimension filtering ignores footprints from other dimensions
        Optional<CornerHit> wrongDim = GlyphTargeting.pickCorner(fromAbove, toBelow, List.of(nether), Level.OVERWORLD);
        helper.assertTrue(wrongDim.isEmpty(), "Overworld raycast must ignore Nether footprints");

        helper.succeed();
    }

    public static void cornerResizeMovesCoplanarCornersAndPushesFlatBox(GameTestHelper helper) {
        WorkFootprint flat = WorkFootprint.ofBox(new BlockPos(1, 64, 1), new BlockPos(4, 64, 4), Level.OVERWORLD);
        Workplace workplace = Workplace.EMPTY.withFootprints(List.of(flat));

        // Top corner (index 2 = minX, maxY, minZ) up by 3 -> maxY becomes 67, minY stays 64
        Workplace raisedTop = StylusActions.resizeCornerY(workplace, 0, 2, 3).workplace();
        WorkFootprint raised = raisedTop.footprints().getFirst();
        helper.assertTrue(raised.minY() == 64 && raised.maxY() == 67,
                "Raising top corner should raise maxY to 67, got " + raised.minY() + ".." + raised.maxY());
        helper.assertTrue(raised.cornerBlock(6).getY() == 67 && raised.cornerBlock(7).getY() == 67,
                "All 4 top corners should move together");
        helper.assertTrue(raised.cornerBlock(0).getY() == 64 && raised.cornerBlock(5).getY() == 64,
                "Bottom 4 corners should remain at 64");

        // Moving top corner down past minY (67 -> 62, delta -5) swaps minY/maxY cleanly so 64 becomes maxY and corner flips to bottom
        Workplace crossedDown = StylusActions.resizeCornerY(raisedTop, 0, 2, -5).workplace();
        WorkFootprint crossedDownFp = crossedDown.footprints().getFirst();
        helper.assertTrue(crossedDownFp.minY() == 62 && crossedDownFp.maxY() == 64,
                "Moving top corner below bottom should result in [62..64], got " + crossedDownFp.minY() + ".." + crossedDownFp.maxY());
        helper.assertTrue(raised.resizedCornerIndex(2, -5) == 0,
                "Top corner 2 crossing below minY should become bottom corner 0");

        // Moving that bottom corner back up past maxY (62 -> 69, delta +7) swaps back so 64 becomes minY and corner flips to top
        Workplace crossedUp = StylusActions.resizeCornerY(crossedDown, 0, 0, 7).workplace();
        WorkFootprint crossedUpFp = crossedUp.footprints().getFirst();
        helper.assertTrue(crossedUpFp.minY() == 64 && crossedUpFp.maxY() == 69,
                "Moving bottom corner above top should result in [64..69], got " + crossedUpFp.minY() + ".." + crossedUpFp.maxY());
        helper.assertTrue(crossedDownFp.resizedCornerIndex(0, 7) == 2,
                "Bottom corner 0 crossing above maxY should become top corner 2");

        // On a flat box, scrolling a top corner down moves it down cleanly to [63..64]
        Workplace flatDown = StylusActions.resizeCornerY(workplace, 0, 2, -1).workplace();
        helper.assertTrue(flatDown.footprints().getFirst().minY() == 63 && flatDown.footprints().getFirst().maxY() == 64,
                "Scrolling top corner down on a flat box should move it to [63..64]");

        EditResult tooLarge = StylusActions.resizeCornerY(workplace, 0, 2, WorkFootprint.MAX_SIZE);
        helper.assertTrue(!tooLarge.isSuccess() && tooLarge.failure() == EditFailure.TOO_LARGE,
                "Resizing past MAX_SIZE height should fail with TOO_LARGE");

        EditResult outOfBounds = StylusActions.resizeCornerY(workplace, 0, 0, WorkFootprint.MIN_Y - 100);
        helper.assertTrue(!outOfBounds.isSuccess() && outOfBounds.failure() == EditFailure.OUT_OF_BOUNDS,
                "Resizing past MIN_Y should fail with OUT_OF_BOUNDS");

        helper.succeed();
    }

    public static void pickUpMoveAndCancelCorner(GameTestHelper helper) {
        WorkFootprint original = WorkFootprint.ofBox(new BlockPos(2, 64, 2), new BlockPos(6, 67, 5));
        Workplace workplace = Workplace.EMPTY.withFootprints(List.of(original));

        // Pick up top-max corner (index 7 = 6,67,5); opposite fixed corner is index 0 (2,64,2) with ghostLift = 3
        PickUpResult picked = StylusActions.pickUpCorner(workplace, Level.OVERWORLD, 0, 7).orElseThrow();
        helper.assertTrue(picked.workplace().footprints().isEmpty(), "Picking up corner should remove the footprint while moving");
        PendingGlyph pending = picked.pending();
        helper.assertTrue(pending.pos().pos().equals(new BlockPos(2, 64, 2)),
                "Anchor should be opposite corner (2,64,2), got " + pending.pos().pos());
        helper.assertTrue(pending.ghostLift() == 3, "Top corner pickup should preserve height as ghostLift=3");
        helper.assertTrue(pending.original().equals(Optional.of(original)), "Pending should keep original footprint for cancel");

        // PendingGlyph codec round-trip preserves original
        Tag encoded = PendingGlyph.CODEC.encodeStart(NbtOps.INSTANCE, pending).getOrThrow();
        PendingGlyph decoded = PendingGlyph.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        helper.assertTrue(decoded.equals(pending), "PendingGlyph with original should round-trip via codec");

        // Cancelling restores the exact original footprint
        Workplace restored = StylusActions.restoreFootprint(picked.workplace(), decoded.original().orElseThrow());
        helper.assertTrue(restored.footprints().equals(List.of(original)), "Cancelling corner move should restore original footprint");

        // Placing at a new position creates the resized footprint (even 1x1x1 on the anchor itself)
        EditResult placed = StylusActions.placeFootprint(picked.workplace(), pending.pos().pos(), new BlockPos(2, 64, 2));
        helper.assertTrue(placed.isSuccess(), "Placing moved corner at anchor should succeed as 1x1x1 box");
        WorkFootprint shrunk = placed.workplace().footprints().getFirst();
        helper.assertTrue(shrunk.width() == 1 && shrunk.depth() == 1 && shrunk.height() == 1,
                "Shrunk box should be 1x1x1");

        helper.succeed();
    }

    public static void cancelMoveKeptUntilGolemIsReachable(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(2, 2, 2));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        golem.setOwnerUUID(player.getUUID());

        WorkFootprint original = WorkFootprint.ofBox(new BlockPos(1, 1, 1), new BlockPos(5, 1, 5), helper.getLevel().dimension());
        Workplace workplace = Workplace.EMPTY.withFootprints(List.of(original)).markInitialized();
        PickUpResult picked = StylusActions.pickUpCorner(workplace, helper.getLevel().dimension(), 0, 7).orElseThrow();
        golem.updateWorkplace(current -> picked.workplace());

        ItemStack stylus = new ItemStack(ItemRegistry.COPPER_STYLUS.get());
        stylus.set(DataComponentRegistry.STYLUS_SELECTION.get(), golem.getUUID());
        stylus.set(DataComponentRegistry.STYLUS_PENDING_GLYPH.get(), picked.pending());

        // Out of reach: the move must stay on the stylus instead of being dropped with the footprint
        player.setPos(golem.getX() + 100.0, golem.getY(), golem.getZ());
        helper.assertTrue(StylusItem.cancelPending(stylus, player) == StylusItem.CancelOutcome.MOVE_KEPT,
                "Cancelling a move with the golem out of reach should keep the move");
        helper.assertTrue(stylus.has(DataComponentRegistry.STYLUS_PENDING_GLYPH.get()),
                "Pending move should stay on the stylus while the golem is out of reach");
        helper.assertTrue(golem.getWorkplace(helper.getLevel()).orElseThrow().footprints().isEmpty(),
                "Footprint should not be restored while the golem is out of reach");

        // Back in reach: the cancel restores the footprint and clears the pending move
        player.setPos(golem.getX(), golem.getY(), golem.getZ());
        helper.assertTrue(StylusItem.cancelPending(stylus, player) == StylusItem.CancelOutcome.MOVE_CANCELLED,
                "Cancelling a move with the golem in reach should restore the footprint");
        helper.assertTrue(!stylus.has(DataComponentRegistry.STYLUS_PENDING_GLYPH.get()),
                "Pending move should be cleared once restored");
        helper.assertTrue(golem.getWorkplace(helper.getLevel()).orElseThrow().footprints().equals(List.of(original)),
                "Cancelling in reach should restore the original footprint");

        helper.succeed();
    }

    public static void sneakRightClickInsideFootprintErasesFootprint(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(2, 2, 2));
        Player player = helper.makeMockServerPlayerInLevel();
        golem.setOwnerUUID(player.getUUID());

        BlockPos minCorner = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos maxCorner = helper.absolutePos(new BlockPos(5, 1, 5));
        BlockPos inside = helper.absolutePos(new BlockPos(3, 1, 3));
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.DIRT);

        WorkFootprint netherFootprint = WorkFootprint.ofBox(minCorner, maxCorner, Level.NETHER);
        WorkFootprint overworldFootprint = WorkFootprint.ofBox(minCorner, maxCorner, helper.getLevel().dimension());
        golem.updateWorkplace(current -> Workplace.EMPTY.withFootprints(List.of(netherFootprint, overworldFootprint)).markInitialized());

        ItemStack stylus = new ItemStack(ItemRegistry.COPPER_STYLUS.get());
        stylus.set(DataComponentRegistry.STYLUS_SELECTION.get(), golem.getUUID());
        player.setItemInHand(InteractionHand.MAIN_HAND, stylus);
        player.setPos(Vec3.atCenterOf(inside));
        player.setShiftKeyDown(true);

        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(inside), Direction.UP, inside, false);
        stylus.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));

        List<WorkFootprint> remaining = golem.getWorkplace(helper.getLevel()).orElseThrow().footprints();
        helper.assertTrue(remaining.equals(List.of(netherFootprint)),
                "Sneak-right-clicking inside the zone should erase the current dimension's footprint and keep other dimensions intact, got " + remaining);

        helper.succeed();
    }

    /**
     * A creative player right-clicking a golem with a stylus selects it. Vanilla hands the item a copy of the held stack in creative,
     * so the selection must be written to the player's held stack.
     */
    public static void creativeStylusSelectsGolem(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(2, 2, 2));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = true;
        golem.setOwnerUUID(player.getUUID());

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.COPPER_STYLUS.get()));
        player.interactOn(golem, InteractionHand.MAIN_HAND, Vec3.ZERO);

        UUID selected = player.getItemInHand(InteractionHand.MAIN_HAND).get(DataComponentRegistry.STYLUS_SELECTION.get());
        helper.assertTrue(golem.getUUID().equals(selected), "A creative player's right-click should select the golem on the held stylus, got " + selected);

        helper.succeed();
    }

    /**
     * Right-click on golems with the imprint tablet goes through the player's interaction, as in game: copy from the first golem,
     * paste onto the second, and crouch-right-click clears the clipboard without touching the golem.
     */
    public static void imprintTabletCopiesPastesAndClears(GameTestHelper helper) {
        GolemEntity source = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(2, 2, 2));
        GolemEntity target = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(6, 2, 2));
        Player player = helper.makeMockServerPlayerInLevel();
        source.setOwnerUUID(player.getUUID());
        target.setOwnerUUID(player.getUUID());

        WorkFootprint footprint = WorkFootprint.ofBox(helper.absolutePos(new BlockPos(1, 1, 1)), helper.absolutePos(new BlockPos(3, 1, 3)), helper.getLevel().dimension());
        source.updateWorkplace(current -> Workplace.EMPTY.withFootprints(List.of(footprint)).markInitialized());

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.IMPRINT_TABLET.get()));

        player.setShiftKeyDown(false);
        InteractionResult copied = player.interactOn(source, InteractionHand.MAIN_HAND, Vec3.ZERO);
        ItemStack tablet = player.getItemInHand(InteractionHand.MAIN_HAND);
        helper.assertTrue(copied.consumesAction(), "Right-click on an owned golem should be consumed by the tablet, got " + copied);
        helper.assertTrue(tablet.get(DataComponentRegistry.IMPRINT_CLIPBOARD.get()) != null, "Right-click on a golem should copy its workplace");

        InteractionResult pasted = player.interactOn(target, InteractionHand.MAIN_HAND, Vec3.ZERO);
        helper.assertTrue(pasted.consumesAction(), "Right-click on a second golem should be consumed by the tablet, got " + pasted);
        List<WorkFootprint> targetFootprints = target.getWorkplace(helper.getLevel()).orElseThrow().footprints();
        helper.assertTrue(targetFootprints.size() == 1, "Right-click on a golem with a clipboard should paste it, got " + targetFootprints);

        player.setShiftKeyDown(true);
        player.interactOn(target, InteractionHand.MAIN_HAND, Vec3.ZERO);
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).get(DataComponentRegistry.IMPRINT_CLIPBOARD.get()) == null,
                "Crouch-right-click should clear the clipboard");
        helper.assertTrue(target.getWorkplace(helper.getLevel()).orElseThrow().footprints().size() == 1,
                "Crouch-right-click should not paste onto the golem");

        helper.succeed();
    }

    public static void workAreaDimensionPreventsCrossDimensionWeirdness(GameTestHelper helper) {
        WorkFootprint overworldBox = WorkFootprint.ofBox(new BlockPos(10, 64, 10), new BlockPos(14, 65, 14), Level.OVERWORLD);
        Tag encoded = WorkFootprint.CODEC.encodeStart(NbtOps.INSTANCE, overworldBox).getOrThrow();
        WorkFootprint decoded = WorkFootprint.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        helper.assertTrue(decoded.equals(overworldBox) && decoded.dimension().equals(Optional.of(Level.OVERWORLD)),
                "WorkFootprint dimension should round-trip via codec");

        Workplace source = Workplace.EMPTY
                .withFootprints(List.of(overworldBox))
                .withLinks(List.of(new ContainerLink(new BlockPos(9, 64, 10), Direction.UP, ContainerRole.BOTH)))
                .markInitialized();
        helper.assertTrue(source.footprintsIn(Level.OVERWORLD).size() == 1, "Overworld should see Overworld footprint");
        helper.assertTrue(source.footprintsIn(Level.NETHER).isEmpty(), "Nether must ignore Overworld footprint");
        helper.assertTrue(source.zones(Level.NETHER, 0, 3).isEmpty(), "Nether must get no work zones from Overworld footprint");

        Workplace relative = StylusActions.toRelative(source, new BlockPos(10, 64, 10), Level.OVERWORLD);

        Workplace releasedInOverworld = StylusActions.toAbsoluteForDimension(relative, new BlockPos(20, 70, 20), Level.OVERWORLD);
        helper.assertTrue(releasedInOverworld.footprints().size() == 1 && releasedInOverworld.links().size() == 1,
                "Releasing in same dimension should restore footprints and links");

        Workplace releasedInNether = StylusActions.toAbsoluteForDimension(relative, new BlockPos(20, 70, 20), Level.NETHER);
        helper.assertTrue(releasedInNether.footprints().isEmpty() && releasedInNether.links().isEmpty() && !releasedInNether.initialized(),
                "Releasing in a different dimension must drop old dimension footprints and links");

        helper.succeed();
    }
}
