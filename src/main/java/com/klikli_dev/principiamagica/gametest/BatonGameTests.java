// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.item.BatonActions;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.List;
import java.util.Optional;

public class BatonGameTests {

    public static void containerLinkCycles(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);

        Workplace workplace = BatonActions.cycleLink(Workplace.EMPTY, pos, Direction.UP);
        helper.assertTrue(workplace.links().size() == 1, "First cycle should add a link");
        helper.assertTrue(workplace.links().get(0).role() == ContainerRole.SUPPLY, "New link should start as SUPPLY");

        workplace = BatonActions.cycleLink(workplace, pos, Direction.NORTH);
        helper.assertTrue(workplace.links().get(0).role() == ContainerRole.DEPOSIT, "Second cycle should be DEPOSIT");

        workplace = BatonActions.cycleLink(workplace, pos, Direction.UP);
        helper.assertTrue(workplace.links().get(0).role() == ContainerRole.BOTH, "Third cycle should be BOTH");

        workplace = BatonActions.cycleLink(workplace, pos, Direction.UP);
        helper.assertTrue(workplace.links().isEmpty(), "Fourth cycle should remove the link");
        helper.assertTrue(workplace.initialized(), "Link edits should mark the workplace initialized");

        helper.succeed();
    }

    public static void zoneAddAndRemove(GameTestHelper helper) {
        Optional<Workplace> added = BatonActions.addZone(Workplace.EMPTY, WorkZone.of(new BlockPos(0, 0, 0), new BlockPos(4, 2, 4)));
        helper.assertTrue(added.isPresent() && added.get().zones().size() == 1, "A small zone should be added");

        Optional<Workplace> tooLarge = BatonActions.addZone(Workplace.EMPTY, WorkZone.of(new BlockPos(0, 0, 0), new BlockPos(BatonActions.MAX_ZONE_WIDTH, 0, 0)));
        helper.assertTrue(tooLarge.isEmpty(), "A zone wider than the maximum should be rejected");

        Workplace nested = added.get()
                .withZones(List.of(
                        WorkZone.of(new BlockPos(0, 0, 0), new BlockPos(10, 5, 10)),
                        WorkZone.of(new BlockPos(2, 0, 2), new BlockPos(3, 0, 3))
                ));
        Workplace afterRemove = BatonActions.removeZoneAt(nested, new BlockPos(2, 0, 2));
        helper.assertTrue(afterRemove.zones().size() == 1, "Removing inside nested zones should remove exactly one zone");
        helper.assertTrue(afterRemove.zones().get(0).volume() > 4, "The smallest containing zone should be the one removed");

        Workplace unchanged = BatonActions.removeZoneAt(nested, new BlockPos(50, 50, 50));
        helper.assertTrue(unchanged.zones().size() == 2, "Removing outside every zone should change nothing");

        helper.succeed();
    }

    public static void copyAndPasteKeepsRelativeLayout(GameTestHelper helper) {
        BlockPos sourceAnchor = new BlockPos(10, 64, 10);
        Workplace source = Workplace.EMPTY
                .withZones(List.of(WorkZone.of(new BlockPos(11, 64, 11), new BlockPos(13, 65, 12))))
                .withLinks(List.of(new ContainerLink(new BlockPos(12, 64, 9), Direction.NORTH, ContainerRole.BOTH)));

        Workplace clipboard = BatonActions.toRelative(source, sourceAnchor);
        helper.assertTrue(clipboard.zones().get(0).min().equals(new BlockPos(1, 0, 1)), "Clipboard zones should be relative to the anchor");

        BlockPos targetAnchor = new BlockPos(20, 70, 30);
        Workplace target = BatonActions.paste(Workplace.EMPTY, clipboard, targetAnchor);
        helper.assertTrue(target.zones().get(0).min().equals(new BlockPos(21, 70, 31)), "Pasted zone should be placed at the target anchor");
        helper.assertTrue(target.links().get(0).pos().equals(new BlockPos(22, 70, 29)), "Pasted link should be placed at the target anchor");
        helper.assertTrue(target.links().get(0).role() == ContainerRole.BOTH, "Pasted link should keep its role");

        helper.succeed();
    }
}
