// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.job.CommonJobOptions;
import com.klikli_dev.principiamagica.content.golem.job.JobOption;
import com.klikli_dev.principiamagica.content.golem.menu.GolemCommands;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.List;
import java.util.Optional;

public class GolemMenuGameTests {

    public static void jobOptionsOnlyAcceptDeclaredKeys(GameTestHelper helper) {
        List<JobOption> options = List.of(CommonJobOptions.PICKUP_ITEMS);

        helper.assertTrue(GolemCommands.setJobOption(Workplace.EMPTY, options, "not_an_option", false).isEmpty(), "Undeclared option keys should be rejected");

        Optional<Workplace> updated = GolemCommands.setJobOption(Workplace.EMPTY, options, CommonJobOptions.PICKUP_ITEMS.key(), false);
        helper.assertTrue(updated.isPresent() && !updated.get().options().get(CommonJobOptions.PICKUP_ITEMS), "A declared option should be set");

        helper.succeed();
    }

    public static void zoneAndLinkIndicesAreValidated(GameTestHelper helper) {
        Workplace workplace = Workplace.EMPTY
                .withFootprints(List.of(WorkFootprint.centered(BlockPos.ZERO, 1, 0)))
                .withLinks(List.of(new ContainerLink(new BlockPos(1, 1, 1), Direction.NORTH, ContainerRole.SUPPLY)));

        helper.assertTrue(GolemCommands.removeFootprint(workplace, 1).isEmpty(), "Footprint index past the end should be rejected");
        helper.assertTrue(GolemCommands.removeFootprint(workplace, -1).isEmpty(), "Negative footprint index should be rejected");
        helper.assertTrue(GolemCommands.removeFootprint(workplace, 0).orElseThrow().footprints().isEmpty(), "A valid footprint index should remove the footprint");

        helper.assertTrue(GolemCommands.shiftFootprint(workplace, 1, 1).isEmpty(), "Footprint index past the end should be rejected when raising");
        helper.assertTrue(GolemCommands.shiftFootprint(workplace, 0, 3).orElseThrow().footprints().get(0).raise() == 3, "Raising should move the footprint up");
        helper.assertTrue(GolemCommands.shiftFootprint(workplace, 0, -2).orElseThrow().footprints().get(0).raise() == -2, "Lowering should move the footprint down");
        helper.assertTrue(GolemCommands.shiftFootprint(workplace, 0, 100).orElseThrow().footprints().get(0).raise() == WorkFootprint.MAX_RAISE,
                "Raising should be clamped to the maximum");
        helper.assertTrue(GolemCommands.shiftFootprint(workplace, 0, -100).orElseThrow().footprints().get(0).raise() == WorkFootprint.MIN_RAISE,
                "Lowering should be clamped to the minimum");

        helper.assertTrue(GolemCommands.setLinkRole(workplace, 1, ContainerRole.DEPOSIT).isEmpty(), "Link index past the end should be rejected");
        helper.assertTrue(GolemCommands.setLinkRole(workplace, 0, ContainerRole.DEPOSIT).orElseThrow().links().get(0).role() == ContainerRole.DEPOSIT, "Setting a role should replace the link role");
        helper.assertTrue(GolemCommands.setLinkRole(workplace, 0, ContainerRole.SUPPLY).orElseThrow().links().get(0).role() == ContainerRole.SUPPLY, "Setting the current role should keep it");

        helper.assertTrue(GolemCommands.removeLink(workplace, 1).isEmpty(), "Link index past the end should be rejected on removal");
        helper.assertTrue(GolemCommands.removeLink(workplace, 0).orElseThrow().links().isEmpty(), "A valid link index should remove the link");

        helper.succeed();
    }
}