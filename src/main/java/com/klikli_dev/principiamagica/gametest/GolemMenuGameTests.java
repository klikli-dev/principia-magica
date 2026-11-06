// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.job.CommonJobOptions;
import com.klikli_dev.principiamagica.content.golem.job.JobOption;
import com.klikli_dev.principiamagica.content.golem.menu.GolemCommands;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenu;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenuTab;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

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

    public static void axiomSlotInstallsJob(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        helper.setBlock(new BlockPos(2, 2, 1), Blocks.CHEST);
        Player player = ownedPlayer(helper, golem);
        GolemMenu menu = new GolemMenu(0, player.getInventory(), golem.getId());

        ItemStack axiom = ItemRegistry.AXIOM_OF_HARVEST.get().getDefaultInstance();
        helper.assertTrue(menu.axiomSlot().mayPlace(axiom), "An owned golem should accept an axiom in the axiom slot");
        menu.axiomSlot().set(axiom);

        helper.assertTrue(golem.getSpec().job().isPresent(), "Setting the axiom slot should install the job");
        helper.assertTrue(golem.getMode() == GolemMode.WORK, "Installing a job should switch to Work, got " + golem.getMode());
        helper.assertTrue(golem.getWorkplace(helper.getLevel()).orElseThrow().footprints().size() == 1,
                "Installing a job into a purposeless golem should create the default footprint");
        helper.assertTrue(menu.axiomSlot().getItem().is(ItemRegistry.AXIOM_OF_HARVEST.get()), "The slot should show the installed axiom");

        helper.succeed();
    }

    public static void axiomSlotPickupReturnsAxiomOnce(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(AxiomItem.jobOf(harvestAxiom()).orElseThrow()));
        golem.setMode(GolemMode.WORK);
        Player player = ownedPlayer(helper, golem);
        GolemMenu menu = new GolemMenu(0, player.getInventory(), golem.getId());

        menu.clicked(indexOf(menu, menu.axiomSlot()), 0, ContainerInput.PICKUP, player);

        ItemStack carried = menu.getCarried();
        helper.assertTrue(carried.is(ItemRegistry.AXIOM_OF_HARVEST.get()) && carried.getCount() == 1, "Taking the axiom should put it on the cursor, got " + carried);
        helper.assertTrue(golem.getSpec().job().isEmpty(), "Taking the axiom should remove the job");
        helper.assertTrue(golem.getMode() == GolemMode.STAY, "Taking the axiom should return the golem to Stay, got " + golem.getMode());
        helper.assertTrue(!player.getInventory().contains(stack -> stack.is(ItemRegistry.AXIOM_OF_HARVEST.get())),
                "The axiom should exist only on the cursor");

        helper.succeed();
    }

    public static void axiomSlotSwapReturnsPreviousAxiom(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(AxiomItem.jobOf(harvestAxiom()).orElseThrow()));
        golem.setMode(GolemMode.WORK);
        Player player = ownedPlayer(helper, golem);
        GolemMenu menu = new GolemMenu(0, player.getInventory(), golem.getId());

        ItemStack felling = ItemRegistry.AXIOM_OF_FELLING.get().getDefaultInstance();
        menu.setCarried(felling);
        menu.clicked(indexOf(menu, menu.axiomSlot()), 0, ContainerInput.PICKUP, player);

        helper.assertTrue(golem.getSpec().job().equals(AxiomItem.jobOf(felling)), "Swapping should install the felling job");
        ItemStack carried = menu.getCarried();
        helper.assertTrue(carried.is(ItemRegistry.AXIOM_OF_HARVEST.get()) && carried.getCount() == 1,
                "Swapping should put the previous axiom on the cursor, got " + carried);
        helper.assertTrue(!player.getInventory().contains(stack -> stack.is(ItemRegistry.AXIOM_OF_HARVEST.get()) || stack.is(ItemRegistry.AXIOM_OF_FELLING.get())),
                "Swapping should not duplicate or drop an axiom into the inventory");
        helper.assertTrue(menu.axiomSlot().getItem().is(ItemRegistry.AXIOM_OF_FELLING.get()), "The slot should show the new axiom");

        helper.succeed();
    }

    public static void axiomShiftClickInstallsJob(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        Player player = ownedPlayer(helper, golem);
        GolemMenu menu = new GolemMenu(0, player.getInventory(), golem.getId());

        ItemStack axiom = ItemRegistry.AXIOM_OF_HARVEST.get().getDefaultInstance();
        player.getInventory().setItem(0, axiom);
        Slot hotbar = menu.playerSlots().stream().filter(slot -> slot.getItem() == axiom).findFirst().orElseThrow();
        menu.clicked(indexOf(menu, hotbar), 0, ContainerInput.QUICK_MOVE, player);

        helper.assertTrue(golem.getSpec().job().isPresent(), "Shift-clicking an axiom should install the job");
        helper.assertTrue(!player.getInventory().contains(stack -> stack.is(ItemRegistry.AXIOM_OF_HARVEST.get())),
                "Shift-clicking should move the axiom out of the inventory");

        helper.succeed();
    }

    public static void toolSlotRejectsWithoutJob(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        Player player = ownedPlayer(helper, golem);
        GolemMenu menu = new GolemMenu(0, player.getInventory(), golem.getId());

        helper.assertTrue(!menu.toolSlot().mayPlace(new ItemStack(Items.IRON_AXE)), "A golem without a job should not accept a tool");

        helper.succeed();
    }

    public static void golemSideSlotsInactiveOnJobTabAndSeals(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(AxiomItem.jobOf(harvestAxiom()).orElseThrow()));
        golem.setMode(GolemMode.WORK);
        Player player = ownedPlayer(helper, golem);
        GolemMenu menu = new GolemMenu(0, player.getInventory(), golem.getId());
        Slot playerSlot = menu.playerSlots().getFirst();

        helper.assertTrue(menu.axiomSlot().isActive() && menu.toolSlot().isActive(), "Golem-side slots should be active on the golem tab by default");

        helper.assertTrue(menu.clickMenuButton(player, GolemMenuTab.JOB.ordinal()), "The job tab button should be accepted");
        helper.assertTrue(!menu.axiomSlot().isActive() && !menu.toolSlot().isActive(), "Golem-side slots should be inactive on the job tab");
        helper.assertTrue(playerSlot.isActive(), "Player slots should stay active on the job tab");

        menu.clicked(indexOf(menu, menu.axiomSlot()), 0, ContainerInput.PICKUP, player);
        helper.assertTrue(golem.getSpec().job().isPresent(), "Clicks on inactive slots should be ignored");

        helper.assertTrue(menu.clickMenuButton(player, GolemMenuTab.GOLEM.ordinal()), "The golem tab button should be accepted");
        helper.assertTrue(menu.clickMenuButton(player, GolemMenu.BUTTON_SEAL_DETAIL_OPEN), "Opening the seal detail should be accepted");
        helper.assertTrue(!menu.axiomSlot().isActive() && !menu.toolSlot().isActive(), "Golem-side slots should be inactive while a seal detail is open");
        helper.assertTrue(playerSlot.isActive(), "Player slots should stay active while a seal detail is open");

        helper.assertTrue(menu.clickMenuButton(player, GolemMenu.BUTTON_SEAL_DETAIL_CLOSE), "Closing the seal detail should be accepted");
        helper.assertTrue(menu.axiomSlot().isActive(), "Golem-side slots should be active again after closing the seal detail");

        helper.succeed();
    }

    public static void shiftClickOnJobTabDoesNotEnterGolemInventory(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(AxiomItem.jobOf(harvestAxiom()).orElseThrow()));
        golem.setMode(GolemMode.WORK);
        Player player = ownedPlayer(helper, golem);
        GolemMenu menu = new GolemMenu(0, player.getInventory(), golem.getId());

        golem.getInventory().setItem(0, new ItemStack(Items.COBBLESTONE));
        player.getInventory().setItem(0, new ItemStack(Items.COBBLESTONE));
        Slot hotbar = menu.playerSlots().stream().filter(slot -> slot.getItem().is(Items.COBBLESTONE) && slot.getItem().getCount() == 1 && slot.getContainerSlot() == 0)
                .findFirst().orElseThrow();
        helper.assertTrue(menu.clickMenuButton(player, GolemMenuTab.JOB.ordinal()), "The job tab button should be accepted");

        menu.clicked(indexOf(menu, hotbar), 0, ContainerInput.QUICK_MOVE, player);

        helper.assertTrue(golem.getInventory().getItem(0).getCount() == 1,
                "Shift-clicking on the job tab should not merge into the golem inventory, got " + golem.getInventory().getItem(0).getCount());

        helper.succeed();
    }

    private static Player ownedPlayer(GameTestHelper helper, GolemEntity golem) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        golem.setOwnerUUID(player.getUUID());
        player.setPos(golem.getX(), golem.getY(), golem.getZ());
        return player;
    }

    private static ItemStack harvestAxiom() {
        return ItemRegistry.AXIOM_OF_HARVEST.get().getDefaultInstance();
    }

    private static int indexOf(GolemMenu menu, Slot slot) {
        return menu.slots.indexOf(slot);
    }
}