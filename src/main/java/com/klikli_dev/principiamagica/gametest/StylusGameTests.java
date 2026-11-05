// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class StylusGameTests {

    public static void copperStylusReturnedAsCraftRemainder(GameTestHelper helper) {
        var stylus = ItemRegistry.COPPER_STYLUS.get();
        var stack = new ItemStack(stylus);

        helper.assertTrue(!stack.isDamageableItem(), "Copper stylus must not have durability");
        helper.assertTrue(stack.getMaxStackSize() == 1, "Copper stylus should not stack");

        var remainder = stylus.getCraftingRemainder(stack);
        helper.assertTrue(remainder != null, "Copper stylus must leave a crafting remainder");
        helper.assertTrue(remainder.item().value() == stylus, "Crafting remainder of the copper stylus must be the stylus itself");
        helper.succeed();
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
        helper.assertTrue(footprint.groundY() == first.getY(), "Ground should be the lower glyph block, got " + footprint.groundY());
        helper.assertTrue(footprint.raise() == 0, "A carved footprint should start flat, raise " + footprint.raise());
        helper.assertTrue(footprint.glyphs().equals(List.of(new CarvedGlyph(first, Direction.UP), new CarvedGlyph(second, Direction.NORTH))),
                "The footprint should keep both carved glyphs");
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

    public static void copyAndPasteKeepsRelativeLayout(GameTestHelper helper) {
        BlockPos sourceAnchor = new BlockPos(10, 64, 10);
        Workplace source = Workplace.EMPTY
                .withFootprints(List.of(new WorkFootprint(11, 11, 13, 12, 64, 0, List.of())))
                .withLinks(List.of(new ContainerLink(new BlockPos(12, 64, 9), Direction.NORTH, ContainerRole.BOTH)));

        Workplace clipboard = StylusActions.toRelative(source, sourceAnchor);
        helper.assertTrue(clipboard.footprints().get(0).minX() == 1 && clipboard.footprints().get(0).minZ() == 1,
                "Clipboard footprints should be relative to the anchor");
        helper.assertTrue(clipboard.footprints().get(0).groundY() == 0, "Clipboard ground should be relative to the anchor");

        BlockPos targetAnchor = new BlockPos(20, 70, 30);
        Workplace target = StylusActions.paste(Workplace.EMPTY, clipboard, targetAnchor);
        WorkFootprint pasted = target.footprints().get(0);
        helper.assertTrue(pasted.minX() == 21 && pasted.minZ() == 31, "Pasted footprint should be placed at the target anchor");
        helper.assertTrue(pasted.groundY() == 70, "Pasted footprint ground should follow the target anchor height, got " + pasted.groundY());
        helper.assertTrue(target.links().get(0).pos().equals(new BlockPos(22, 70, 29)), "Pasted link should be placed at the target anchor");
        helper.assertTrue(target.links().get(0).role() == ContainerRole.BOTH, "Pasted link should keep its role");

        helper.succeed();
    }
}
