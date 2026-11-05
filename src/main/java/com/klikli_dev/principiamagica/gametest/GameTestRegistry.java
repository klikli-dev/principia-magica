// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public class GameTestRegistry {

    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, PrincipiaMagica.MODID);

    // --- Golem ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GOLEM_SPAWNS_WITH_BRAIN =
            TEST_FUNCTIONS.register("golem_spawns_with_brain", () -> GolemGameTests::spawnsWithBrain);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORK_ZONE_CONTAINS_AND_ITERATES =
            TEST_FUNCTIONS.register("work_zone_contains_and_iterates", () -> GolemGameTests::workZoneContainsAndIterates);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONTAINER_ROLE_CYCLES =
            TEST_FUNCTIONS.register("container_role_cycles", () -> GolemGameTests::containerRoleCycles);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORKPLACE_CODEC_ROUND_TRIPS =
            TEST_FUNCTIONS.register("workplace_codec_round_trips", () -> GolemGameTests::workplaceCodecRoundTrips);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORK_RESERVATIONS_ARE_EXCLUSIVE =
            TEST_FUNCTIONS.register("work_reservations_are_exclusive", () -> GolemGameTests::workReservationsAreExclusive);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORKPLACE_DEFAULTS_LINK_ADJACENT_CHEST =
            TEST_FUNCTIONS.register("workplace_defaults_link_adjacent_chest", () -> GolemGameTests::workplaceDefaultsLinkAdjacentChest);

    // --- Golem stylus ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONTAINER_LINK_CYCLES =
            TEST_FUNCTIONS.register("container_link_cycles", () -> StylusGameTests::containerLinkCycles);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ZONE_ADD_AND_REMOVE =
            TEST_FUNCTIONS.register("zone_add_and_remove", () -> StylusGameTests::zoneAddAndRemove);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COPY_AND_PASTE_KEEPS_RELATIVE_LAYOUT =
            TEST_FUNCTIONS.register("copy_and_paste_keeps_relative_layout", () -> StylusGameTests::copyAndPasteKeepsRelativeLayout);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COPPER_STYLUS_RETURNED_AS_CRAFT_REMAINDER =
            TEST_FUNCTIONS.register("copper_stylus_returned_as_craft_remainder", () -> StylusGameTests::copperStylusReturnedAsCraftRemainder);

    // --- Golem menu ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> JOB_OPTIONS_ONLY_ACCEPT_DECLARED_KEYS =
            TEST_FUNCTIONS.register("job_options_only_accept_declared_keys", () -> GolemMenuGameTests::jobOptionsOnlyAcceptDeclaredKeys);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ZONE_AND_LINK_INDICES_ARE_VALIDATED =
            TEST_FUNCTIONS.register("zone_and_link_indices_are_validated", () -> GolemMenuGameTests::zoneAndLinkIndicesAreValidated);

    // --- Farming ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_HARVEST_AND_REPLANT =
            TEST_FUNCTIONS.register("farming_harvest_and_replant", () -> FarmingGameTests::harvestAndReplant);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_DEPOSIT_FULL_INVENTORY =
            TEST_FUNCTIONS.register("farming_deposit_full_inventory", () -> FarmingGameTests::depositFullInventory);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_RESTOCK_AND_PLANT =
            TEST_FUNCTIONS.register("farming_restock_and_plant", () -> FarmingGameTests::restockAndPlant);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_STACKED_PLANT_KEEPS_BASE =
            TEST_FUNCTIONS.register("farming_stacked_plant_keeps_base", () -> FarmingGameTests::stackedPlantKeepsBase);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_GOURD_HARVEST_KEEPS_STEM =
            TEST_FUNCTIONS.register("farming_gourd_harvest_keeps_stem", () -> FarmingGameTests::gourdHarvestKeepsStem);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_BERRY_BUSH_RESETS =
            TEST_FUNCTIONS.register("farming_berry_bush_resets", () -> FarmingGameTests::berryBushResets);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_TILLS_DIRT_WITH_HOE =
            TEST_FUNCTIONS.register("farming_tills_dirt_with_hoe", () -> FarmingGameTests::tillsDirtWithHoe);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_TILL_WITHOUT_HOE_REPORTS_MISSING_TOOL =
            TEST_FUNCTIONS.register("farming_till_without_hoe_reports_missing_tool", () -> FarmingGameTests::tillWithoutHoeReportsMissingTool);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_PLANTING_WITHOUT_SEEDS_REPORTS_NEEDS_SUPPLIES =
            TEST_FUNCTIONS.register("farming_planting_without_seeds_reports_needs_supplies", () -> FarmingGameTests::plantingWithoutSeedsReportsNeedsSupplies);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_BONEMEAL_GROWS_CROPS =
            TEST_FUNCTIONS.register("farming_bonemeal_grows_crops", () -> FarmingGameTests::bonemealGrowsCrops);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_TWO_GOLEMS_SHARE_FIELD =
            TEST_FUNCTIONS.register("farming_two_golems_share_field", () -> FarmingGameTests::twoGolemsShareField);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_CORE_ROUND_TRIPS =
            TEST_FUNCTIONS.register("farming_core_round_trips", () -> FarmingGameTests::farmingCoreRoundTrips);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_FIELD_MEMORY_PRUNES_STALE_SPOTS =
            TEST_FUNCTIONS.register("farming_field_memory_prunes_stale_spots", () -> FarmingGameTests::fieldMemoryPrunesStaleSpots);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_CLEARS_OBSTRUCTIONS_FOR_PLANTING =
            TEST_FUNCTIONS.register("farming_clears_obstructions_for_planting", () -> FarmingGameTests::clearsObstructionsForPlanting);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_OBSTRUCTIONS_STAY_WHEN_OPTION_OFF =
            TEST_FUNCTIONS.register("farming_obstructions_stay_when_option_off", () -> FarmingGameTests::obstructionsStayWhenOptionOff);

    // --- Lumberjack ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_FELLS_NATURAL_TREE =
            TEST_FUNCTIONS.register("lumberjack_fells_natural_tree", () -> LumberjackGameTests::fellsNaturalTree);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_REPLANTS_SAPLING =
            TEST_FUNCTIONS.register("lumberjack_replants_sapling", () -> LumberjackGameTests::replantsSapling);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_IGNORES_PLAYER_LEAVES =
            TEST_FUNCTIONS.register("lumberjack_ignores_player_leaves", () -> LumberjackGameTests::ignoresPlayerLeaves);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_IGNORES_BARE_LOG_PILLAR =
            TEST_FUNCTIONS.register("lumberjack_ignores_bare_log_pillar", () -> LumberjackGameTests::ignoresBareLogPillar);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_IGNORES_WALL_TOUCHING_NATURAL_LEAVES =
            TEST_FUNCTIONS.register("lumberjack_ignores_wall_touching_natural_leaves", () -> LumberjackGameTests::ignoresWallTouchingNaturalLeaves);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_FELLS_2X2_AND_REPLANTS_FOUR =
            TEST_FUNCTIONS.register("lumberjack_fells_2x2_and_replants_four", () -> LumberjackGameTests::fells2x2AndReplantsFour);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_FELLS_TREE_ENCLOSED_BY_LOW_LEAVES =
            TEST_FUNCTIONS.register("lumberjack_fells_tree_enclosed_by_low_leaves", () -> LumberjackGameTests::fellsTreeEnclosedByLowLeaves);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_PERSISTENT_LEAVES_ARE_NOT_CLEARED =
            TEST_FUNCTIONS.register("lumberjack_persistent_leaves_are_not_cleared", () -> LumberjackGameTests::persistentLeavesAreNotCleared);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_CLEAR_LEAVES_OFF_KEEPS_LEAVES =
            TEST_FUNCTIONS.register("lumberjack_clear_leaves_off_keeps_leaves", () -> LumberjackGameTests::clearLeavesOffKeepsLeaves);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_FELLS_BUSH_FROM_FAR_SIDE_THROUGH_GAP =
            TEST_FUNCTIONS.register("lumberjack_fells_bush_from_far_side_through_gap", () -> LumberjackGameTests::fellsBushFromFarSideThroughGap);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_FELLS_TREE_AROUND_UNREACHABLE_OPENING =
            TEST_FUNCTIONS.register("lumberjack_fells_tree_around_unreachable_opening", () -> LumberjackGameTests::fellsTreeAroundUnreachableOpening);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_GOLEM_ON_LEAVES_COMES_DOWN =
            TEST_FUNCTIONS.register("lumberjack_golem_on_leaves_comes_down", () -> LumberjackGameTests::golemOnLeavesComesDown);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_FELLS_JUNGLE_BUSH =
            TEST_FUNCTIONS.register("lumberjack_fells_jungle_bush", () -> LumberjackGameTests::fellsJungleBush);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_FELLS_GROWN_OAK =
            TEST_FUNCTIONS.register("lumberjack_fells_grown_oak", () -> LumberjackGameTests::fellsGrownOak);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_FELLS_GROWN_FANCY_OAK =
            TEST_FUNCTIONS.register("lumberjack_fells_grown_fancy_oak", () -> LumberjackGameTests::fellsGrownFancyOak);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_FELLS_GROWN_SPRUCE =
            TEST_FUNCTIONS.register("lumberjack_fells_grown_spruce", () -> LumberjackGameTests::fellsGrownSpruce);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_CARVES_ON_OWN_SIDE =
            TEST_FUNCTIONS.register("lumberjack_carves_on_own_side_not_around_gap", () -> LumberjackGameTests::carvesOnOwnSideNotAroundGap);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_TALLER_THAN_ZONE =
            TEST_FUNCTIONS.register("lumberjack_taller_than_zone", () -> LumberjackGameTests::tallerThanZone);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_MISSING_AXE_REPORTS_STATUS =
            TEST_FUNCTIONS.register("lumberjack_missing_axe_reports_status", () -> LumberjackGameTests::missingAxeReportsStatus);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_RESTOCKS_SAPLINGS_FROM_SUPPLY =
            TEST_FUNCTIONS.register("lumberjack_restocks_saplings_from_supply", () -> LumberjackGameTests::restocksSaplingsFromSupply);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_DETECTOR_FINDS_DIAGONAL_BRANCHES =
            TEST_FUNCTIONS.register("lumberjack_detector_finds_diagonal_branches", () -> LumberjackGameTests::detectorFindsDiagonalBranches);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_CORE_ROUND_TRIPS =
            TEST_FUNCTIONS.register("lumberjack_core_round_trips", () -> LumberjackGameTests::lumberjackCoreRoundTrips);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_BREAK_TICKS_FOLLOW_THE_TOOL =
            TEST_FUNCTIONS.register("lumberjack_break_ticks_follow_the_tool", () -> LumberjackGameTests::breakTicksFollowTheTool);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_BONEMEAL_GROWS_SAPLING =
            TEST_FUNCTIONS.register("lumberjack_bonemeal_grows_sapling", () -> LumberjackGameTests::bonemealGrowsSapling);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_BONEMEAL_OFF_LEAVES_SAPLING =
            TEST_FUNCTIONS.register("lumberjack_bonemeal_off_leaves_sapling", () -> LumberjackGameTests::bonemealOffLeavesSapling);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_RESTOCKS_BONEMEAL_FROM_SUPPLY =
            TEST_FUNCTIONS.register("lumberjack_restocks_bonemeal_from_supply", () -> LumberjackGameTests::restocksBonemealFromSupply);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_STALLED_SAPLING_STOPS_BONEMEAL =
            TEST_FUNCTIONS.register("lumberjack_stalled_sapling_stops_bonemeal", () -> LumberjackGameTests::stalledSaplingStopsTakingBonemeal);

    // --- Upgrades ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UPGRADE_SWIFTNESS_INCREASES_MOVEMENT_SPEED =
            TEST_FUNCTIONS.register("upgrade_swiftness_increases_movement_speed", () -> UpgradeGameTests::swiftnessIncreasesMovementSpeed);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UPGRADE_CAPACITY_ADDS_INVENTORY_SLOTS =
            TEST_FUNCTIONS.register("upgrade_capacity_adds_inventory_slots", () -> UpgradeGameTests::capacityAddsInventorySlots);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UPGRADE_CAPACITY_REMOVAL_MOVES_ITEMS =
            TEST_FUNCTIONS.register("upgrade_capacity_removal_moves_items", () -> UpgradeGameTests::capacityRemovalMovesItemsIntoFreeSlots);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UPGRADE_CAPACITY_REMOVAL_DROPS_OVERFLOW =
            TEST_FUNCTIONS.register("upgrade_capacity_removal_drops_overflow", () -> UpgradeGameTests::capacityRemovalDropsOverflow);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UPGRADE_DUPLICATE_IS_REJECTED =
            TEST_FUNCTIONS.register("upgrade_duplicate_is_rejected", () -> UpgradeGameTests::duplicateUpgradeIsRejected);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UPGRADE_SLOT_LIMIT_IS_RESPECTED =
            TEST_FUNCTIONS.register("upgrade_slot_limit_is_respected", () -> UpgradeGameTests::upgradeSlotLimitIsRespected);

    // --- Persistence ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PERSISTENCE_SAVE_AND_LOAD_KEEPS_EVERYTHING =
            TEST_FUNCTIONS.register("persistence_save_and_load_keeps_everything", () -> PersistenceGameTests::saveAndLoadKeepsEverything);

    // --- Progression ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CLAY_ADVANCEMENT_AND_PRINCIPIA_RECIPE_EXIST =
            TEST_FUNCTIONS.register("clay_advancement_and_principia_recipe_exist", () -> ProgressionGameTests::clayAdvancementAndPrincipiaRecipeExist);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PRINCIPIA_BOOK_AND_FIRST_ENTRY_LOAD =
            TEST_FUNCTIONS.register("principia_book_and_first_entry_load", () -> ProgressionGameTests::principiaBookAndFirstEntryLoad);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WEDGE_OF_COPPER_ADVANCEMENT_AND_STYLUS_RECIPE_EXIST =
            TEST_FUNCTIONS.register("wedge_of_copper_advancement_and_stylus_recipe_exist", () -> ProgressionGameTests::wedgeOfCopperAdvancementAndStylusRecipeExist);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STYLUS_ENTRY_LOADS =
            TEST_FUNCTIONS.register("stylus_entry_loads", () -> ProgressionGameTests::stylusEntryLoads);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WET_TABLET_ADVANCEMENT_RECIPE_AND_ENTRY_EXIST =
            TEST_FUNCTIONS.register("wet_tablet_advancement_recipe_and_entry_exist", () -> ProgressionGameTests::wetTabletAdvancementRecipeAndEntryExist);

    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        registerGolemTests(event);
        registerProgressionTests(event);
    }

    private static void registerProgressionTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(PrincipiaMagica.loc("progression"));
        var structure = PrincipiaMagica.loc("golem_test");

        registerTest(event, CLAY_ADVANCEMENT_AND_PRINCIPIA_RECIPE_EXIST, environment, structure, 40, 0);
        registerTest(event, PRINCIPIA_BOOK_AND_FIRST_ENTRY_LOAD, environment, structure, 40, 0);
        registerTest(event, WEDGE_OF_COPPER_ADVANCEMENT_AND_STYLUS_RECIPE_EXIST, environment, structure, 40, 0);
        registerTest(event, STYLUS_ENTRY_LOADS, environment, structure, 40, 0);
        registerTest(event, WET_TABLET_ADVANCEMENT_RECIPE_AND_ENTRY_EXIST, environment, structure, 40, 0);
    }

    private static void registerGolemTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(PrincipiaMagica.loc("golem"));
        var structure = PrincipiaMagica.loc("golem_test");

        registerTest(event, GOLEM_SPAWNS_WITH_BRAIN, environment, structure, 40, 0);
        registerTest(event, WORK_ZONE_CONTAINS_AND_ITERATES, environment, structure, 40, 0);
        registerTest(event, CONTAINER_ROLE_CYCLES, environment, structure, 40, 0);
        registerTest(event, WORKPLACE_CODEC_ROUND_TRIPS, environment, structure, 40, 0);
        registerTest(event, WORK_RESERVATIONS_ARE_EXCLUSIVE, environment, structure, 40, 0);
        registerTest(event, WORKPLACE_DEFAULTS_LINK_ADJACENT_CHEST, environment, structure, 40, 0);
        registerTest(event, CONTAINER_LINK_CYCLES, environment, structure, 40, 0);
        registerTest(event, ZONE_ADD_AND_REMOVE, environment, structure, 40, 0);
        registerTest(event, COPY_AND_PASTE_KEEPS_RELATIVE_LAYOUT, environment, structure, 40, 0);
        registerTest(event, COPPER_STYLUS_RETURNED_AS_CRAFT_REMAINDER, environment, structure, 40, 0);
        registerTest(event, JOB_OPTIONS_ONLY_ACCEPT_DECLARED_KEYS, environment, structure, 40, 0);
        registerTest(event, ZONE_AND_LINK_INDICES_ARE_VALIDATED, environment, structure, 40, 0);

        registerTest(event, FARMING_HARVEST_AND_REPLANT, environment, structure, 800, 0);
        registerTest(event, FARMING_DEPOSIT_FULL_INVENTORY, environment, structure, 800, 0);
        registerTest(event, FARMING_RESTOCK_AND_PLANT, environment, structure, 800, 0);
        registerTest(event, FARMING_STACKED_PLANT_KEEPS_BASE, environment, structure, 800, 0);
        registerTest(event, FARMING_GOURD_HARVEST_KEEPS_STEM, environment, structure, 800, 0);
        registerTest(event, FARMING_BERRY_BUSH_RESETS, environment, structure, 800, 0);
        registerTest(event, FARMING_TILLS_DIRT_WITH_HOE, environment, structure, 800, 0);
        registerTest(event, FARMING_TILL_WITHOUT_HOE_REPORTS_MISSING_TOOL, environment, structure, 800, 0);
        registerTest(event, FARMING_PLANTING_WITHOUT_SEEDS_REPORTS_NEEDS_SUPPLIES, environment, structure, 800, 0);
        registerTest(event, FARMING_BONEMEAL_GROWS_CROPS, environment, structure, 800, 0);
        registerTest(event, FARMING_TWO_GOLEMS_SHARE_FIELD, environment, structure, 800, 0);
        registerTest(event, FARMING_CORE_ROUND_TRIPS, environment, structure, 40, 0);
        registerTest(event, FARMING_FIELD_MEMORY_PRUNES_STALE_SPOTS, environment, structure, 800, 0);
        registerTest(event, FARMING_CLEARS_OBSTRUCTIONS_FOR_PLANTING, environment, structure, 800, 0);
        registerTest(event, FARMING_OBSTRUCTIONS_STAY_WHEN_OPTION_OFF, environment, structure, 400, 0);

        registerTest(event, LUMBERJACK_FELLS_NATURAL_TREE, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_REPLANTS_SAPLING, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_IGNORES_PLAYER_LEAVES, environment, structure, 400, 0);
        registerTest(event, LUMBERJACK_IGNORES_BARE_LOG_PILLAR, environment, structure, 400, 0);
        registerTest(event, LUMBERJACK_IGNORES_WALL_TOUCHING_NATURAL_LEAVES, environment, structure, 400, 0);
        registerTest(event, LUMBERJACK_FELLS_2X2_AND_REPLANTS_FOUR, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_TALLER_THAN_ZONE, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_FELLS_TREE_ENCLOSED_BY_LOW_LEAVES, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_PERSISTENT_LEAVES_ARE_NOT_CLEARED, environment, structure, 400, 0);
        registerTest(event, LUMBERJACK_CLEAR_LEAVES_OFF_KEEPS_LEAVES, environment, structure, 400, 0);
        registerTest(event, LUMBERJACK_FELLS_BUSH_FROM_FAR_SIDE_THROUGH_GAP, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_FELLS_TREE_AROUND_UNREACHABLE_OPENING, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_GOLEM_ON_LEAVES_COMES_DOWN, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_FELLS_JUNGLE_BUSH, environment, structure, 1200, 0);
        registerTest(event, LUMBERJACK_FELLS_GROWN_OAK, environment, structure, 1200, 0);
        registerTest(event, LUMBERJACK_FELLS_GROWN_FANCY_OAK, environment, structure, 1200, 0);
        registerTest(event, LUMBERJACK_FELLS_GROWN_SPRUCE, environment, structure, 1200, 0);
        registerTest(event, LUMBERJACK_CARVES_ON_OWN_SIDE, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_MISSING_AXE_REPORTS_STATUS, environment, structure, 400, 0);
        registerTest(event, LUMBERJACK_RESTOCKS_SAPLINGS_FROM_SUPPLY, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_DETECTOR_FINDS_DIAGONAL_BRANCHES, environment, structure, 40, 0);
        registerTest(event, LUMBERJACK_CORE_ROUND_TRIPS, environment, structure, 40, 0);
        registerTest(event, LUMBERJACK_BREAK_TICKS_FOLLOW_THE_TOOL, environment, structure, 40, 0);
        registerTest(event, LUMBERJACK_BONEMEAL_GROWS_SAPLING, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_BONEMEAL_OFF_LEAVES_SAPLING, environment, structure, 400, 0);
        registerTest(event, LUMBERJACK_RESTOCKS_BONEMEAL_FROM_SUPPLY, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_STALLED_SAPLING_STOPS_BONEMEAL, environment, structure, 1000, 0);

        registerTest(event, UPGRADE_SWIFTNESS_INCREASES_MOVEMENT_SPEED, environment, structure, 40, 0);
        registerTest(event, UPGRADE_CAPACITY_ADDS_INVENTORY_SLOTS, environment, structure, 40, 0);
        registerTest(event, UPGRADE_CAPACITY_REMOVAL_MOVES_ITEMS, environment, structure, 40, 0);
        registerTest(event, UPGRADE_CAPACITY_REMOVAL_DROPS_OVERFLOW, environment, structure, 40, 0);
        registerTest(event, UPGRADE_DUPLICATE_IS_REJECTED, environment, structure, 40, 0);
        registerTest(event, UPGRADE_SLOT_LIMIT_IS_RESPECTED, environment, structure, 40, 0);

        registerTest(event, PERSISTENCE_SAVE_AND_LOAD_KEEPS_EVERYTHING, environment, structure, 40, 0);
    }

    private static void registerTest(
            RegisterGameTestsEvent event,
            DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test,
            Holder<TestEnvironmentDefinition<?>> environment,
            Identifier structure,
            int maxTicks,
            int setupTicks
    ) {
        var testData = new TestData<>(environment, structure, maxTicks, setupTicks, true);
        event.registerTest(test.getId(), new FunctionGameTestInstance(test.getKey(), testData));
    }
}
