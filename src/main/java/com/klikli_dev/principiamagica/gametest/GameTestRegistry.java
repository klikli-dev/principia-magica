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

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FLAT_AND_VOLUME_ZONES =
            TEST_FUNCTIONS.register("flat_and_volume_zones", () -> GolemGameTests::flatAndVolumeZones);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BREAKING_GLYPH_KEEPS_FOOTPRINT =
            TEST_FUNCTIONS.register("breaking_glyph_keeps_footprint", () -> GolemGameTests::breakingGlyphKeepsFootprint);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONTAINER_ROLE_CYCLES =
            TEST_FUNCTIONS.register("container_role_cycles", () -> GolemGameTests::containerRoleCycles);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORKPLACE_CODEC_ROUND_TRIPS =
            TEST_FUNCTIONS.register("workplace_codec_round_trips", () -> GolemGameTests::workplaceCodecRoundTrips);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORK_RESERVATIONS_ARE_EXCLUSIVE =
            TEST_FUNCTIONS.register("work_reservations_are_exclusive", () -> GolemGameTests::workReservationsAreExclusive);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORKPLACE_DEFAULTS_LINK_ADJACENT_CHEST =
            TEST_FUNCTIONS.register("workplace_defaults_link_adjacent_chest", () -> GolemGameTests::workplaceDefaultsLinkAdjacentChest);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PURPOSELESS_GOLEM_STAYS_AND_CANNOT_WORK =
            TEST_FUNCTIONS.register("purposeless_golem_stays_and_cannot_work", () -> GolemGameTests::purposelessGolemStaysAndCannotWork);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_ENABLES_WORK_AND_REMOVAL_RETURNS_TO_STAY =
            TEST_FUNCTIONS.register("axiom_enables_work_and_removal_returns_to_stay", () -> GolemGameTests::axiomEnablesWorkAndRemovalReturnsToStay);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PLACED_GOLEM_WITHOUT_AXIOM_IS_PURPOSELESS =
            TEST_FUNCTIONS.register("placed_golem_without_axiom_is_purposeless", () -> GolemGameTests::placedGolemWithoutAxiomIsPurposeless);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEFAULTS_KEEP_CONFIGURED_WORKPLACE =
            TEST_FUNCTIONS.register("defaults_keep_configured_workplace", () -> GolemGameTests::defaultsKeepConfiguredWorkplace);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REMOVING_AXIOM_KEEPS_FOLLOW =
            TEST_FUNCTIONS.register("removing_axiom_keeps_follow", () -> GolemGameTests::removingAxiomKeepsFollow);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_GIVEN_STARTS_WORK_IN_DEFAULT_AREA =
            TEST_FUNCTIONS.register("axiom_given_starts_work_in_default_area", () -> GolemGameTests::axiomGivenStartsWorkInDefaultArea);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_GIVEN_KEEPS_CONFIGURED_WORKPLACE =
            TEST_FUNCTIONS.register("axiom_given_keeps_configured_workplace", () -> GolemGameTests::axiomGivenKeepsConfiguredWorkplace);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NON_OWNER_CANNOT_GIVE_AXIOM =
            TEST_FUNCTIONS.register("non_owner_cannot_give_axiom", () -> GolemGameTests::nonOwnerCannotGiveAxiom);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_GIVEN_HARVESTS_FIELD =
            TEST_FUNCTIONS.register("axiom_given_harvests_field", () -> GolemGameTests::axiomGivenHarvestsField);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> IN_WORLD_TOOL_EQUIP_SWAPS_TOOLS =
            TEST_FUNCTIONS.register("in_world_tool_equip_swaps_tools", () -> GolemGameTests::inWorldToolEquipSwapsTools);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CARRY_IS_ONLY_NON_TOOL_ITEMS =
            TEST_FUNCTIONS.register("carry_is_only_non_tool_items", () -> GolemGameTests::carryIsOnlyNonToolItems);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_GIVEN_LUMBERJACK_WITHOUT_AXE_REPORTS_MISSING_TOOL =
            TEST_FUNCTIONS.register("axiom_given_lumberjack_without_axe_reports_missing_tool", () -> GolemGameTests::axiomGivenLumberjackWithoutAxeReportsMissingTool);

    // --- Golem removal ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEATH_DROPS_CONTENTS_EQUIPMENT_AND_AXIOM =
            TEST_FUNCTIONS.register("death_drops_contents_equipment_and_axiom", () -> GolemRemovalGameTests::deathDropsContentsEquipmentAndAxiom);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEATH_WITHOUT_AXIOM_DROPS_NO_AXIOM =
            TEST_FUNCTIONS.register("death_without_axiom_drops_no_axiom", () -> GolemRemovalGameTests::deathWithoutAxiomDropsNoAxiom);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DISCARDED_GOLEM_DROPS_NOTHING =
            TEST_FUNCTIONS.register("discarded_golem_drops_nothing", () -> GolemRemovalGameTests::discardedGolemDropsNothing);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STYLUS_PICKUP_KEEPS_INVENTORY_ON_GOLEM_ITEM =
            TEST_FUNCTIONS.register("stylus_pickup_keeps_inventory_on_golem_item", () -> GolemRemovalGameTests::stylusPickupKeepsInventoryOnGolemItem);

    // --- Golem stylus ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONTAINER_LINK_CYCLES =
            TEST_FUNCTIONS.register("container_link_cycles", () -> StylusGameTests::containerLinkCycles);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REMOVE_LINK_KEEPS_FOOTPRINT =
            TEST_FUNCTIONS.register("remove_link_keeps_footprint", () -> StylusGameTests::removeLinkKeepsFootprint);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CARVING_TWO_GLYPHS_CREATES_FOOTPRINT =
            TEST_FUNCTIONS.register("carving_two_glyphs_creates_footprint", () -> StylusGameTests::carvingTwoGlyphsCreatesFootprint);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> OVERSIZED_FOOTPRINT_IS_REJECTED =
            TEST_FUNCTIONS.register("oversized_footprint_is_rejected", () -> StylusGameTests::oversizedFootprintIsRejected);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHIFT_CARVE_ERASES_FOOTPRINT =
            TEST_FUNCTIONS.register("shift_carve_erases_footprint", () -> StylusGameTests::shiftCarveErasesFootprint);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GLYPH_TARGET_HITS_EVERY_FACE_FROM_AFAR =
            TEST_FUNCTIONS.register("glyph_target_hits_every_face_from_afar", () -> StylusGameTests::glyphTargetHitsEveryFaceFromAfar);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GLYPH_TARGET_PREFERS_NEAREST_GLYPH =
            TEST_FUNCTIONS.register("glyph_target_prefers_nearest_glyph", () -> StylusGameTests::glyphTargetPrefersNearestGlyph);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GLYPH_TARGET_MISSES_BESIDE_AND_YIELDS_TO_BLOCKS =
            TEST_FUNCTIONS.register("glyph_target_misses_beside_and_yields_to_blocks", () -> StylusGameTests::glyphTargetMissesBesideAndYieldsToBlocks);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REMOVE_FOOTPRINT_WITH_GLYPH_MATCHES_BLOCK_AND_FACE =
            TEST_FUNCTIONS.register("remove_footprint_with_glyph_matches_block_and_face", () -> StylusGameTests::removeFootprintWithGlyphMatchesBlockAndFace);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COPY_AND_PASTE_KEEPS_RELATIVE_LAYOUT =
            TEST_FUNCTIONS.register("copy_and_paste_keeps_relative_layout", () -> StylusGameTests::copyAndPasteKeepsRelativeLayout);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> 
CORNER_LIFT_KEEPS_PLUMB_LINE_AND_CLAMPS =
            TEST_FUNCTIONS.register("corner_lift_keeps_plumb_line_and_clamps", () -> StylusGameTests::cornerLiftKeepsPlumbLineAndClamps);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> 
WHOLE_MOVE_CLAMPS_CORNERS =
            TEST_FUNCTIONS.register("whole_move_clamps_corners", () -> StylusGameTests::wholeMoveClampsCorners);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> 
LEGACY_FOOTPRINT_LOADS_FLAT =
            TEST_FUNCTIONS.register("legacy_footprint_loads_flat", () -> StylusGameTests::legacyFootprintLoadsFlat);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> 
CORNER_COLLISION_BLOCKED_ON_PLACEMENT =
            TEST_FUNCTIONS.register("corner_collision_blocked_on_placement", () -> StylusGameTests::cornerCollisionBlockedOnPlacement);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> 
CORNER_COLLISION_BLOCKED_ON_LIFT_AND_SHIFT =
            TEST_FUNCTIONS.register("corner_collision_blocked_on_lift_and_shift", () -> StylusGameTests::cornerCollisionBlockedOnLiftAndShift);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> 
ZONE_BOX_TARGETING_PICKS_NEAREST_AND_FLOATING_BOXES =
            TEST_FUNCTIONS.register("zone_box_targeting_picks_nearest_and_floating_boxes", () -> StylusGameTests::zoneBoxTargetingPicksNearestAndFloatingBoxes);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> 
RIGHT_CLICK_BLOCK_SUPPRESSES_BLOCK_USE_WHEN_GOLEM_SELECTED =
            TEST_FUNCTIONS.register("right_click_block_suppresses_block_use_when_golem_selected", () -> StylusGameTests::rightClickBlockSuppressesBlockUseWhenGolemSelected);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COPPER_STYLUS_RETURNED_BY_CRAFTING_RECIPES =
            TEST_FUNCTIONS.register("copper_stylus_returned_by_crafting_recipes", () -> StylusGameTests::copperStylusReturnedByCraftingRecipes);

    // --- Golem menu ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> JOB_OPTIONS_ONLY_ACCEPT_DECLARED_KEYS =
            TEST_FUNCTIONS.register("job_options_only_accept_declared_keys", () -> GolemMenuGameTests::jobOptionsOnlyAcceptDeclaredKeys);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ZONE_AND_LINK_INDICES_ARE_VALIDATED =
            TEST_FUNCTIONS.register("zone_and_link_indices_are_validated", () -> GolemMenuGameTests::zoneAndLinkIndicesAreValidated);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_SLOT_INSTALLS_JOB =
            TEST_FUNCTIONS.register("axiom_slot_installs_job", () -> GolemMenuGameTests::axiomSlotInstallsJob);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_SLOT_PICKUP_RETURNS_AXIOM_ONCE =
            TEST_FUNCTIONS.register("axiom_slot_pickup_returns_axiom_once", () -> GolemMenuGameTests::axiomSlotPickupReturnsAxiomOnce);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_SLOT_SWAP_RETURNS_PREVIOUS_AXIOM =
            TEST_FUNCTIONS.register("axiom_slot_swap_returns_previous_axiom", () -> GolemMenuGameTests::axiomSlotSwapReturnsPreviousAxiom);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_SHIFT_CLICK_INSTALLS_JOB =
            TEST_FUNCTIONS.register("axiom_shift_click_installs_job", () -> GolemMenuGameTests::axiomShiftClickInstallsJob);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOOL_SLOT_ACCEPTS_TOOL_WITHOUT_JOB =
            TEST_FUNCTIONS.register("tool_slot_accepts_tool_without_job", () -> GolemMenuGameTests::toolSlotAcceptsToolWithoutJob);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GOLEM_SIDE_SLOTS_INACTIVE_ON_JOB_TAB =
            TEST_FUNCTIONS.register("golem_side_slots_inactive_on_job_tab", () -> GolemMenuGameTests::golemSideSlotsInactiveOnJobTab);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHIFT_CLICK_ON_JOB_TAB_DOES_NOT_ENTER_GOLEM_INVENTORY =
            TEST_FUNCTIONS.register("shift_click_on_job_tab_does_not_enter_golem_inventory", () -> GolemMenuGameTests::shiftClickOnJobTabDoesNotEnterGolemInventory);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> JOB_TAB_NEEDS_AXIOM =
            TEST_FUNCTIONS.register("job_tab_needs_axiom", () -> GolemMenuGameTests::jobTabNeedsAxiom);

    // --- Farming ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_HARVEST_AND_REPLANT =
            TEST_FUNCTIONS.register("farming_harvest_and_replant", () -> FarmingGameTests::harvestAndReplant);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> 
STACKED_FARM_VOLUME_COVERS_EVERY_LAYER =
            TEST_FUNCTIONS.register("stacked_farm_volume_covers_every_layer", () -> FarmingGameTests::stackedFarmVolumeCoversEveryLayer);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_DEPOSIT_FULL_INVENTORY =
            TEST_FUNCTIONS.register("farming_deposit_full_inventory", () -> FarmingGameTests::depositFullInventory);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_DEPOSIT_SKIPS_FULL_CHEST =
            TEST_FUNCTIONS.register("farming_deposit_skips_full_chest", () -> FarmingGameTests::depositSkipsFullChest);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_RESTOCK_AND_PLANT =
            TEST_FUNCTIONS.register("farming_restock_and_plant", () -> FarmingGameTests::restockAndPlant);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_RESTOCK_AND_PLANT_WITH_HOE =
            TEST_FUNCTIONS.register("farming_restock_and_plant_with_hoe", () -> FarmingGameTests::restockAndPlantWithHoe);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_TILLS_THEN_RESTOCKS_AND_PLANTS =
            TEST_FUNCTIONS.register("farming_tills_then_restocks_and_plants", () -> FarmingGameTests::tillsThenRestocksAndPlants);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_CLEARS_GRASS_THEN_TILLS_AND_PLANTS =
            TEST_FUNCTIONS.register("farming_clears_grass_then_tills_and_plants", () -> FarmingGameTests::clearsGrassThenTillsAndPlants);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_ENABLING_TILL_AFTER_IDLE_STARTS_TILLING =
            TEST_FUNCTIONS.register("farming_enabling_till_after_idle_starts_tilling", () -> FarmingGameTests::enablingTillAfterIdleStartsTilling);

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

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_TASK_SHOWS_HARVEST =
            TEST_FUNCTIONS.register("farming_task_shows_harvest", () -> FarmingGameTests::taskShowsHarvestWhileHarvesting);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_AXIOM_ROUND_TRIPS =
            TEST_FUNCTIONS.register("farming_axiom_round_trips", () -> FarmingGameTests::farmingAxiomRoundTrips);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_FIELD_MEMORY_PRUNES_STALE_SPOTS =
            TEST_FUNCTIONS.register("farming_field_memory_prunes_stale_spots", () -> FarmingGameTests::fieldMemoryPrunesStaleSpots);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_CLEARS_OBSTRUCTIONS_FOR_PLANTING =
            TEST_FUNCTIONS.register("farming_clears_obstructions_for_planting", () -> FarmingGameTests::clearsObstructionsForPlanting);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FARMING_OBSTRUCTIONS_STAY_WHEN_OPTION_OFF =
            TEST_FUNCTIONS.register("farming_obstructions_stay_when_option_off", () -> FarmingGameTests::obstructionsStayWhenOptionOff);

    // --- Lumberjack ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_FELLS_NATURAL_TREE =
            TEST_FUNCTIONS.register("lumberjack_fells_natural_tree", () -> LumberjackGameTests::fellsNaturalTree);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_AXE_BREAKING_STOPS_FELLING =
            TEST_FUNCTIONS.register("lumberjack_axe_breaking_stops_felling", () -> LumberjackGameTests::axeBreakingStopsFelling);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_FELLING_CHARGES_ONE_USE_PER_LOG =
            TEST_FUNCTIONS.register("lumberjack_felling_charges_one_use_per_log", () -> LumberjackGameTests::fellingChargesOneUsePerLog);

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

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_IGNORES_STUMP_OUTSIDE_FOOTPRINT =
            TEST_FUNCTIONS.register("lumberjack_ignores_stump_outside_footprint", () -> LumberjackGameTests::ignoresStumpOutsideFootprint);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_MISSING_AXE_REPORTS_STATUS =
            TEST_FUNCTIONS.register("lumberjack_missing_axe_reports_status", () -> LumberjackGameTests::missingAxeReportsStatus);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_RESTOCKS_SAPLINGS_FROM_SUPPLY =
            TEST_FUNCTIONS.register("lumberjack_restocks_saplings_from_supply", () -> LumberjackGameTests::restocksSaplingsFromSupply);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_BACK_SLOT_HOLDS_TOOL =
            TEST_FUNCTIONS.register("lumberjack_back_slot_holds_tool", () -> LumberjackGameTests::backSlotHoldsToolWhileCarrying);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_DETECTOR_FINDS_DIAGONAL_BRANCHES =
            TEST_FUNCTIONS.register("lumberjack_detector_finds_diagonal_branches", () -> LumberjackGameTests::detectorFindsDiagonalBranches);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_AXIOM_ROUND_TRIPS =
            TEST_FUNCTIONS.register("lumberjack_axiom_round_trips", () -> LumberjackGameTests::lumberjackAxiomRoundTrips);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_BREAK_TICKS_FOLLOW_THE_TOOL =
            TEST_FUNCTIONS.register("lumberjack_break_ticks_follow_the_tool", () -> LumberjackGameTests::breakTicksFollowTheTool);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_BONEMEAL_GROWS_SAPLING =
            TEST_FUNCTIONS.register("lumberjack_bonemeal_grows_sapling", () -> LumberjackGameTests::bonemealGrowsSapling);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_BONEMEAL_OFF_LEAVES_SAPLING =
            TEST_FUNCTIONS.register("lumberjack_bonemeal_off_leaves_sapling", () -> LumberjackGameTests::bonemealOffLeavesSapling);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_RESTOCKS_BONEMEAL_FROM_SUPPLY =
            TEST_FUNCTIONS.register("lumberjack_restocks_bonemeal_from_supply", () -> LumberjackGameTests::restocksBonemealFromSupply);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_RESTOCKS_BONEMEAL_WITH_AXE =
            TEST_FUNCTIONS.register("lumberjack_restocks_bonemeal_with_axe", () -> LumberjackGameTests::restocksBonemealWithAxe);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUMBERJACK_STALLED_SAPLING_STOPS_BONEMEAL =
            TEST_FUNCTIONS.register("lumberjack_stalled_sapling_stops_bonemeal", () -> LumberjackGameTests::stalledSaplingStopsTakingBonemeal);

    // --- Libation ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_SPOUT_OPENS_HOLE_IN_SLAB =
            TEST_FUNCTIONS.register("libation_spout_opens_hole_in_slab", () -> LibationGameTests::spoutOpensHoleInSlab);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_SPOUT_POPS_OFF_WHEN_HOST_IS_REMOVED =
            TEST_FUNCTIONS.register("libation_spout_pops_off_when_host_is_removed", () -> LibationGameTests::spoutPopsOffWhenHostIsRemoved);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_SPOUT_OPENS_FROM_REDSTONE =
            TEST_FUNCTIONS.register("libation_spout_opens_from_redstone", () -> LibationGameTests::spoutOpensFromRedstone);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_SLAB_DISSOLVES_WITHOUT_SPOUT =
            TEST_FUNCTIONS.register("libation_slab_dissolves_without_spout", () -> LibationGameTests::slabDissolvesWithoutSpout);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_SLAB_DRAINS_ONLY_THROUGH_OPEN_SPOUT =
            TEST_FUNCTIONS.register("libation_slab_drains_only_through_open_spout", () -> LibationGameTests::slabDrainsOnlyThroughOpenSpout);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_DRAIN_STOPS_WHEN_STORAGE_IS_EMPTY =
            TEST_FUNCTIONS.register("libation_drain_stops_when_storage_is_empty", () -> LibationGameTests::drainStopsWhenStorageIsEmpty);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_SYNC_THROTTLE_FLUSHES_AT_THE_INTERVAL =
            TEST_FUNCTIONS.register("libation_sync_throttle_flushes_at_the_interval", () -> LibationGameTests::syncThrottleFlushesRequestsAtTheInterval);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_DISPLAYED_AMOUNT_EASES_TOWARDS_SYNCED_AMOUNT =
            TEST_FUNCTIONS.register("libation_displayed_amount_eases_towards_synced_amount", () -> LibationGameTests::displayedAmountEasesTowardsSyncedAmount);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_STEEPING_DOES_NOT_COMPLETE_INSTANTLY =
            TEST_FUNCTIONS.register("libation_steeping_does_not_complete_instantly", () -> LibationGameTests::steepingDoesNotCompleteInstantly);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_STEEPING_MAKES_QUICKENED_CLAY =
            TEST_FUNCTIONS.register("libation_steeping_makes_quickened_clay", () -> LibationGameTests::steepingClayMakesQuickenedClay);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_STEEPING_COMPLETES_AFTER_RECIPE_TIME =
            TEST_FUNCTIONS.register("libation_steeping_completes_after_recipe_time", () -> LibationGameTests::steepingCompletesAfterRecipeTime);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_STEEPING_WAITS_FOR_PRINCIPLES =
            TEST_FUNCTIONS.register("libation_steeping_waits_for_principles", () -> LibationGameTests::steepingWaitsForPrinciples);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_CURING_SURVIVES_RELOAD =
            TEST_FUNCTIONS.register("libation_curing_survives_reload", () -> LibationGameTests::curingSurvivesReload);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_STEEPING_SURVIVES_RELOAD =
            TEST_FUNCTIONS.register("libation_steeping_survives_reload", () -> LibationGameTests::steepingSurvivesReload);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_STEEPING_RESETS_WHEN_ITEM_IS_REMOVED =
            TEST_FUNCTIONS.register("libation_steeping_resets_when_item_is_removed", () -> LibationGameTests::steepingResetsWhenItemIsRemoved);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_DRYING_SLAB_DRIES_INTO_OUTPUT =
            TEST_FUNCTIONS.register("libation_drying_slab_dries_into_output", () -> LibationGameTests::dryingSlabDriesIntoOutput);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_DRYING_SLAB_STALLS_WHEN_OUTPUT_IS_FULL =
            TEST_FUNCTIONS.register("libation_drying_slab_stalls_when_output_is_full", () -> LibationGameTests::dryingSlabStallsWhenOutputIsFull);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LIBATION_DRYING_SLAB_HOPPER_INSERTS_AND_EXTRACTS =
            TEST_FUNCTIONS.register("libation_drying_slab_hopper_inserts_and_extracts", () -> LibationGameTests::dryingSlabHopperInsertsInputAndExtractsOutput);

    // --- Persistence ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PERSISTENCE_SAVE_AND_LOAD_KEEPS_EVERYTHING =
            TEST_FUNCTIONS.register("persistence_save_and_load_keeps_everything", () -> PersistenceGameTests::saveAndLoadKeepsEverything);

    // --- Progression ---

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PRINCIPIA_RECIPE_EXISTS =
            TEST_FUNCTIONS.register("principia_recipe_exists", () -> ProgressionGameTests::principiaRecipeExists);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PRINCIPIA_BOOK_AND_FIRST_ENTRY_LOAD =
            TEST_FUNCTIONS.register("principia_book_and_first_entry_load", () -> ProgressionGameTests::principiaBookAndFirstEntryLoad);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WEDGE_OF_COPPER_STYLUS_RECIPE_EXISTS =
            TEST_FUNCTIONS.register("wedge_of_copper_stylus_recipe_exists", () -> ProgressionGameTests::wedgeOfCopperStylusRecipeExists);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STYLUS_ENTRY_LOADS =
            TEST_FUNCTIONS.register("stylus_entry_loads", () -> ProgressionGameTests::stylusEntryLoads);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_TABLET_RECIPE_AND_ENTRY_EXIST =
            TEST_FUNCTIONS.register("axiom_tablet_recipe_and_entry_exist", () -> ProgressionGameTests::axiomTabletRecipeAndEntryExist);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_RECIPES_AND_ENTRY_EXIST =
            TEST_FUNCTIONS.register("axiom_recipes_and_entry_exist", () -> ProgressionGameTests::axiomRecipesAndEntryExist);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> QUICKENED_RECIPE_AND_ENTRY_EXIST =
            TEST_FUNCTIONS.register("quickened_recipe_and_entry_exist", () -> ProgressionGameTests::quickenedRecipeAndEntryExist);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GOLEM_BODY_RECIPE_AND_ENTRY_EXIST =
            TEST_FUNCTIONS.register("golem_body_recipe_and_entry_exist", () -> ProgressionGameTests::golemBodyRecipeAndEntryExist);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PURPOSE_ENTRY_EXISTS =
            TEST_FUNCTIONS.register("purpose_entry_exists", () -> ProgressionGameTests::purposeEntryExists);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GOLEM_PLACEMENT_GRANTS_RESEARCH_FACT =
            TEST_FUNCTIONS.register("golem_placement_grants_research_fact", () -> ProgressionGameTests::golemPlacementGrantsResearchFact);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AXIOM_GIVEN_GRANTS_RESEARCH_FACT =
            TEST_FUNCTIONS.register("axiom_given_grants_research_fact", () -> ProgressionGameTests::axiomGivenGrantsResearchFact);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> KUDURRU_ENTRY_EXISTS =
            TEST_FUNCTIONS.register("kudurru_entry_exists", () -> ProgressionGameTests::kudurruEntryExists);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GOLEM_MILESTONES_ARE_HIDDEN_ROOTS =
            TEST_FUNCTIONS.register("golem_milestones_are_hidden_roots", () -> ProgressionGameTests::golemMilestonesAreHiddenRoots);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VISIBLE_ADVANCEMENTS_ARE_REMOVED =
            TEST_FUNCTIONS.register("visible_advancements_are_removed", () -> ProgressionGameTests::visibleAdvancementsAreRemoved);


    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        registerGolemTests(event);
        registerProgressionTests(event);
    }

    private static void registerProgressionTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(PrincipiaMagica.loc("progression"));
        var structure = PrincipiaMagica.loc("golem_test");

        registerTest(event, PRINCIPIA_RECIPE_EXISTS, environment, structure, 40, 0);
        registerTest(event, PRINCIPIA_BOOK_AND_FIRST_ENTRY_LOAD, environment, structure, 40, 0);
        registerTest(event, WEDGE_OF_COPPER_STYLUS_RECIPE_EXISTS, environment, structure, 40, 0);
        registerTest(event, STYLUS_ENTRY_LOADS, environment, structure, 40, 0);
        registerTest(event, AXIOM_TABLET_RECIPE_AND_ENTRY_EXIST, environment, structure, 40, 0);
        registerTest(event, AXIOM_RECIPES_AND_ENTRY_EXIST, environment, structure, 40, 0);
        registerTest(event, QUICKENED_RECIPE_AND_ENTRY_EXIST, environment, structure, 40, 0);
        registerTest(event, GOLEM_BODY_RECIPE_AND_ENTRY_EXIST, environment, structure, 40, 0);
        registerTest(event, PURPOSE_ENTRY_EXISTS, environment, structure, 40, 0);
        registerTest(event, GOLEM_PLACEMENT_GRANTS_RESEARCH_FACT, environment, structure, 40, 0);
        registerTest(event, AXIOM_GIVEN_GRANTS_RESEARCH_FACT, environment, structure, 40, 0);
        registerTest(event, KUDURRU_ENTRY_EXISTS, environment, structure, 40, 0);
        registerTest(event, GOLEM_MILESTONES_ARE_HIDDEN_ROOTS, environment, structure, 40, 0);
        registerTest(event, VISIBLE_ADVANCEMENTS_ARE_REMOVED, environment, structure, 40, 0);
    }

    private static void registerGolemTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(PrincipiaMagica.loc("golem"));
        var structure = PrincipiaMagica.loc("golem_test");

        registerGolemCoreTests(event, environment, structure);
        registerStylusTests(event, environment, structure);
        registerMenuTests(event, environment, structure);
        registerAxiomTests(event, environment, structure);
        registerRemovalTests(event, environment, structure);
        registerFarmingTests(event, environment, structure);
        registerLumberjackTests(event, environment, structure);
        registerLibationTests(event, environment, structure);
    }

    private static void registerGolemCoreTests(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, Identifier structure) {
        registerTest(event, GOLEM_SPAWNS_WITH_BRAIN, environment, structure, 40, 0);
        registerTest(event, WORK_ZONE_CONTAINS_AND_ITERATES, environment, structure, 40, 0);
        registerTest(event, FLAT_AND_VOLUME_ZONES, environment, structure, 40, 0);
        registerTest(event, BREAKING_GLYPH_KEEPS_FOOTPRINT, environment, structure, 40, 0);
        registerTest(event, CONTAINER_ROLE_CYCLES, environment, structure, 40, 0);
        registerTest(event, WORKPLACE_CODEC_ROUND_TRIPS, environment, structure, 40, 0);
        registerTest(event, WORK_RESERVATIONS_ARE_EXCLUSIVE, environment, structure, 40, 0);
        registerTest(event, WORKPLACE_DEFAULTS_LINK_ADJACENT_CHEST, environment, structure, 40, 0);
        registerTest(event, PERSISTENCE_SAVE_AND_LOAD_KEEPS_EVERYTHING, environment, structure, 40, 0);
    }

    private static void registerStylusTests(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, Identifier structure) {
        registerTest(event, CONTAINER_LINK_CYCLES, environment, structure, 40, 0);
        registerTest(event, REMOVE_LINK_KEEPS_FOOTPRINT, environment, structure, 40, 0);
        registerTest(event, CARVING_TWO_GLYPHS_CREATES_FOOTPRINT, environment, structure, 40, 0);
        registerTest(event, OVERSIZED_FOOTPRINT_IS_REJECTED, environment, structure, 40, 0);
        registerTest(event, SHIFT_CARVE_ERASES_FOOTPRINT, environment, structure, 40, 0);
        registerTest(event, GLYPH_TARGET_HITS_EVERY_FACE_FROM_AFAR, environment, structure, 40, 0);
        registerTest(event, GLYPH_TARGET_PREFERS_NEAREST_GLYPH, environment, structure, 40, 0);
        registerTest(event, GLYPH_TARGET_MISSES_BESIDE_AND_YIELDS_TO_BLOCKS, environment, structure, 40, 0);
        registerTest(event, REMOVE_FOOTPRINT_WITH_GLYPH_MATCHES_BLOCK_AND_FACE, environment, structure, 40, 0);
        registerTest(event, COPY_AND_PASTE_KEEPS_RELATIVE_LAYOUT, environment, structure, 40, 0);
        registerTest(event, CORNER_LIFT_KEEPS_PLUMB_LINE_AND_CLAMPS, environment, structure, 40, 0);
        registerTest(event, WHOLE_MOVE_CLAMPS_CORNERS, environment, structure, 40, 0);
        registerTest(event, LEGACY_FOOTPRINT_LOADS_FLAT, environment, structure, 40, 0);
        registerTest(event, CORNER_COLLISION_BLOCKED_ON_PLACEMENT, environment, structure, 40, 0);
        registerTest(event, CORNER_COLLISION_BLOCKED_ON_LIFT_AND_SHIFT, environment, structure, 40, 0);
        registerTest(event, ZONE_BOX_TARGETING_PICKS_NEAREST_AND_FLOATING_BOXES, environment, structure, 40, 0);
        registerTest(event, RIGHT_CLICK_BLOCK_SUPPRESSES_BLOCK_USE_WHEN_GOLEM_SELECTED, environment, structure, 40, 0);
        registerTest(event, COPPER_STYLUS_RETURNED_BY_CRAFTING_RECIPES, environment, structure, 40, 0);
    }

    private static void registerMenuTests(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, Identifier structure) {
        registerTest(event, JOB_OPTIONS_ONLY_ACCEPT_DECLARED_KEYS, environment, structure, 40, 0);
        registerTest(event, ZONE_AND_LINK_INDICES_ARE_VALIDATED, environment, structure, 40, 0);
        registerTest(event, AXIOM_SLOT_INSTALLS_JOB, environment, structure, 40, 0);
        registerTest(event, AXIOM_SLOT_PICKUP_RETURNS_AXIOM_ONCE, environment, structure, 40, 0);
        registerTest(event, AXIOM_SLOT_SWAP_RETURNS_PREVIOUS_AXIOM, environment, structure, 40, 0);
        registerTest(event, AXIOM_SHIFT_CLICK_INSTALLS_JOB, environment, structure, 40, 0);
        registerTest(event, TOOL_SLOT_ACCEPTS_TOOL_WITHOUT_JOB, environment, structure, 40, 0);
        registerTest(event, GOLEM_SIDE_SLOTS_INACTIVE_ON_JOB_TAB, environment, structure, 40, 0);
        registerTest(event, SHIFT_CLICK_ON_JOB_TAB_DOES_NOT_ENTER_GOLEM_INVENTORY, environment, structure, 40, 0);
        registerTest(event, JOB_TAB_NEEDS_AXIOM, environment, structure, 40, 0);
    }

    private static void registerAxiomTests(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, Identifier structure) {
        registerTest(event, PURPOSELESS_GOLEM_STAYS_AND_CANNOT_WORK, environment, structure, 80, 0);
        registerTest(event, AXIOM_ENABLES_WORK_AND_REMOVAL_RETURNS_TO_STAY, environment, structure, 40, 0);
        registerTest(event, PLACED_GOLEM_WITHOUT_AXIOM_IS_PURPOSELESS, environment, structure, 40, 0);
        registerTest(event, DEFAULTS_KEEP_CONFIGURED_WORKPLACE, environment, structure, 40, 0);
        registerTest(event, REMOVING_AXIOM_KEEPS_FOLLOW, environment, structure, 40, 0);
        registerTest(event, AXIOM_GIVEN_STARTS_WORK_IN_DEFAULT_AREA, environment, structure, 40, 0);
        registerTest(event, AXIOM_GIVEN_KEEPS_CONFIGURED_WORKPLACE, environment, structure, 40, 0);
        registerTest(event, NON_OWNER_CANNOT_GIVE_AXIOM, environment, structure, 40, 0);
        registerTest(event, AXIOM_GIVEN_HARVESTS_FIELD, environment, structure, 2400, 0);
        registerTest(event, IN_WORLD_TOOL_EQUIP_SWAPS_TOOLS, environment, structure, 40, 0);
        registerTest(event, CARRY_IS_ONLY_NON_TOOL_ITEMS, environment, structure, 40, 0);
        registerTest(event, AXIOM_GIVEN_LUMBERJACK_WITHOUT_AXE_REPORTS_MISSING_TOOL, environment, structure, 80, 0);
    }

    private static void registerRemovalTests(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, Identifier structure) {
        registerTest(event, DEATH_DROPS_CONTENTS_EQUIPMENT_AND_AXIOM, environment, structure, 40, 0);
        registerTest(event, DEATH_WITHOUT_AXIOM_DROPS_NO_AXIOM, environment, structure, 40, 0);
        registerTest(event, DISCARDED_GOLEM_DROPS_NOTHING, environment, structure, 40, 0);
        registerTest(event, STYLUS_PICKUP_KEEPS_INVENTORY_ON_GOLEM_ITEM, environment, structure, 40, 0);
    }

    private static void registerFarmingTests(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, Identifier structure) {
        registerTest(event, FARMING_HARVEST_AND_REPLANT, environment, structure, 4800, 0);
        registerTest(event, STACKED_FARM_VOLUME_COVERS_EVERY_LAYER, environment, structure, 40, 0);
        registerTest(event, FARMING_DEPOSIT_FULL_INVENTORY, environment, structure, 800, 0);
        registerTest(event, FARMING_DEPOSIT_SKIPS_FULL_CHEST, environment, structure, 800, 0);
        registerTest(event, FARMING_RESTOCK_AND_PLANT, environment, structure, 800, 0);
        registerTest(event, FARMING_RESTOCK_AND_PLANT_WITH_HOE, environment, structure, 800, 0);
        registerTest(event, FARMING_TILLS_THEN_RESTOCKS_AND_PLANTS, environment, structure, 1200, 0);
        registerTest(event, FARMING_CLEARS_GRASS_THEN_TILLS_AND_PLANTS, environment, structure, 1200, 0);
        registerTest(event, FARMING_ENABLING_TILL_AFTER_IDLE_STARTS_TILLING, environment, structure, 1200, 0);
        registerTest(event, FARMING_STACKED_PLANT_KEEPS_BASE, environment, structure, 800, 0);
        registerTest(event, FARMING_GOURD_HARVEST_KEEPS_STEM, environment, structure, 800, 0);
        registerTest(event, FARMING_BERRY_BUSH_RESETS, environment, structure, 800, 0);
        registerTest(event, FARMING_TILLS_DIRT_WITH_HOE, environment, structure, 800, 0);
        registerTest(event, FARMING_TILL_WITHOUT_HOE_REPORTS_MISSING_TOOL, environment, structure, 800, 0);
        registerTest(event, FARMING_PLANTING_WITHOUT_SEEDS_REPORTS_NEEDS_SUPPLIES, environment, structure, 800, 0);
        registerTest(event, FARMING_BONEMEAL_GROWS_CROPS, environment, structure, 800, 0);
        registerTest(event, FARMING_TWO_GOLEMS_SHARE_FIELD, environment, structure, 2400, 0);
        registerTest(event, FARMING_TASK_SHOWS_HARVEST, environment, structure, 800, 0);
        registerTest(event, FARMING_AXIOM_ROUND_TRIPS, environment, structure, 40, 0);
        registerTest(event, FARMING_FIELD_MEMORY_PRUNES_STALE_SPOTS, environment, structure, 800, 0);
        registerTest(event, FARMING_CLEARS_OBSTRUCTIONS_FOR_PLANTING, environment, structure, 800, 0);
        registerTest(event, FARMING_OBSTRUCTIONS_STAY_WHEN_OPTION_OFF, environment, structure, 400, 0);
    }

    private static void registerLumberjackTests(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, Identifier structure) {
        registerTest(event, LUMBERJACK_FELLS_NATURAL_TREE, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_AXE_BREAKING_STOPS_FELLING, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_FELLING_CHARGES_ONE_USE_PER_LOG, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_REPLANTS_SAPLING, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_IGNORES_PLAYER_LEAVES, environment, structure, 400, 0);
        registerTest(event, LUMBERJACK_IGNORES_BARE_LOG_PILLAR, environment, structure, 400, 0);
        registerTest(event, LUMBERJACK_IGNORES_WALL_TOUCHING_NATURAL_LEAVES, environment, structure, 400, 0);
        registerTest(event, LUMBERJACK_FELLS_2X2_AND_REPLANTS_FOUR, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_TALLER_THAN_ZONE, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_IGNORES_STUMP_OUTSIDE_FOOTPRINT, environment, structure, 800, 0);
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
        registerTest(event, LUMBERJACK_BACK_SLOT_HOLDS_TOOL, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_DETECTOR_FINDS_DIAGONAL_BRANCHES, environment, structure, 40, 0);
        registerTest(event, LUMBERJACK_AXIOM_ROUND_TRIPS, environment, structure, 40, 0);
        registerTest(event, LUMBERJACK_BREAK_TICKS_FOLLOW_THE_TOOL, environment, structure, 40, 0);
        registerTest(event, LUMBERJACK_BONEMEAL_GROWS_SAPLING, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_BONEMEAL_OFF_LEAVES_SAPLING, environment, structure, 400, 0);
        registerTest(event, LUMBERJACK_RESTOCKS_BONEMEAL_FROM_SUPPLY, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_RESTOCKS_BONEMEAL_WITH_AXE, environment, structure, 800, 0);
        registerTest(event, LUMBERJACK_STALLED_SAPLING_STOPS_BONEMEAL, environment, structure, 1000, 0);
    }

    private static void registerLibationTests(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, Identifier structure) {
        registerTest(event, LIBATION_SPOUT_OPENS_HOLE_IN_SLAB, environment, structure, 40, 0);
        registerTest(event, LIBATION_SPOUT_POPS_OFF_WHEN_HOST_IS_REMOVED, environment, structure, 40, 0);
        registerTest(event, LIBATION_SPOUT_OPENS_FROM_REDSTONE, environment, structure, 40, 0);
        registerTest(event, LIBATION_SLAB_DISSOLVES_WITHOUT_SPOUT, environment, structure, 300, 0);
        registerTest(event, LIBATION_SLAB_DRAINS_ONLY_THROUGH_OPEN_SPOUT, environment, structure, 600, 0);
        registerTest(event, LIBATION_DRAIN_STOPS_WHEN_STORAGE_IS_EMPTY, environment, structure, 600, 0);
        registerTest(event, LIBATION_SYNC_THROTTLE_FLUSHES_AT_THE_INTERVAL, environment, structure, 40, 0);
        registerTest(event, LIBATION_DISPLAYED_AMOUNT_EASES_TOWARDS_SYNCED_AMOUNT, environment, structure, 40, 0);
        registerTest(event, LIBATION_STEEPING_DOES_NOT_COMPLETE_INSTANTLY, environment, structure, 40, 0);
        registerTest(event, LIBATION_STEEPING_MAKES_QUICKENED_CLAY, environment, structure, 300, 0);
        registerTest(event, LIBATION_STEEPING_COMPLETES_AFTER_RECIPE_TIME, environment, structure, 300, 0);
        registerTest(event, LIBATION_STEEPING_WAITS_FOR_PRINCIPLES, environment, structure, 400, 0);
        registerTest(event, LIBATION_CURING_SURVIVES_RELOAD, environment, structure, 300, 0);
        registerTest(event, LIBATION_STEEPING_SURVIVES_RELOAD, environment, structure, 300, 0);
        registerTest(event, LIBATION_STEEPING_RESETS_WHEN_ITEM_IS_REMOVED, environment, structure, 300, 0);
        registerTest(event, LIBATION_DRYING_SLAB_DRIES_INTO_OUTPUT, environment, structure, 600, 0);
        registerTest(event, LIBATION_DRYING_SLAB_STALLS_WHEN_OUTPUT_IS_FULL, environment, structure, 1000, 0);
        registerTest(event, LIBATION_DRYING_SLAB_HOPPER_INSERTS_AND_EXTRACTS, environment, structure, 600, 0);
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
