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

    // --- Golem baton ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONTAINER_LINK_CYCLES =
            TEST_FUNCTIONS.register("container_link_cycles", () -> BatonGameTests::containerLinkCycles);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ZONE_ADD_AND_REMOVE =
            TEST_FUNCTIONS.register("zone_add_and_remove", () -> BatonGameTests::zoneAddAndRemove);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COPY_AND_PASTE_KEEPS_RELATIVE_LAYOUT =
            TEST_FUNCTIONS.register("copy_and_paste_keeps_relative_layout", () -> BatonGameTests::copyAndPasteKeepsRelativeLayout);

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

    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        registerGolemTests(event);
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
