// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.ai.behaviour.ContainerAccess;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.item.GolemCoreItem;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.JobOptionValues;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingJob;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingTargets;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FarmingGameTests {
    private static final BlockPos GOLEM_POS = new BlockPos(0, 2, 0);
    private static final BlockPos ZONE_MIN = new BlockPos(0, 1, 0);
    private static final BlockPos ZONE_MAX = new BlockPos(7, 4, 7);
    private static final BlockPos CHEST_POS = new BlockPos(0, 2, 6);
    private static final BlockPos WATER_POS = new BlockPos(5, 1, 3);

    private static List<BlockPos> farmSpots() {
        return List.of(
                new BlockPos(2, 2, 2), new BlockPos(3, 2, 2), new BlockPos(4, 2, 2),
                new BlockPos(2, 2, 3), new BlockPos(3, 2, 3), new BlockPos(4, 2, 3),
                new BlockPos(2, 2, 4), new BlockPos(3, 2, 4), new BlockPos(4, 2, 4)
        );
    }

    /**
     * Solid stone at y=0 and y=1 so golems walk on y=2 and water placed on the floor level cannot flow away.
     */
    private static void buildFloor(GameTestHelper helper) {
        for (int x = 0; x <= ZONE_MAX.getX(); x++) {
            for (int z = 0; z <= ZONE_MAX.getZ(); z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
    }

    private static void buildFarmland(GameTestHelper helper) {
        buildFloor(helper);
        for (BlockPos spot : farmSpots()) {
            helper.setBlock(spot.below(), Blocks.FARMLAND);
        }

        helper.setBlock(WATER_POS, Blocks.WATER);
    }

    private static void buildMatureWheat(GameTestHelper helper) {
        buildFarmland(helper);
        for (BlockPos spot : farmSpots()) {
            helper.setBlock(spot, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
        }
    }

    private static ContainerLink link(GameTestHelper helper, BlockPos rel, ContainerRole role) {
        return new ContainerLink(helper.absolutePos(rel), Direction.UP, role);
    }

    private static GolemEntity spawnFarmer(GameTestHelper helper, BlockPos pos, List<ContainerLink> links, JobOptionValues options) {
        ServerLevel level = helper.getLevel();
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), pos);
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(PrincipiaMagicaRegistries.GOLEM_JOB.get(FarmingJob.KEY).orElseThrow()));
        golem.setOwnerUUID(UUID.randomUUID());
        golem.setAnchor(GlobalPos.of(level.dimension(), helper.absolutePos(GOLEM_POS)));

        WorkZone zone = WorkZone.of(helper.absolutePos(ZONE_MIN), helper.absolutePos(ZONE_MAX));
        golem.updateWorkplace(workplace -> workplace
                .withZones(List.of(zone))
                .withLinks(links)
                .withOptions(options)
                .markInitialized());
        return golem;
    }

    private static int countInChest(GameTestHelper helper, BlockPos rel, Item item) {
        Container chest = ContainerAccess.containerAt(helper.getLevel(), helper.absolutePos(rel));
        int count = 0;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            ItemStack stack = chest.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void fillChest(GameTestHelper helper, BlockPos rel, ItemStack stack) {
        helper.setBlock(rel, Blocks.CHEST);
        Container chest = ContainerAccess.containerAt(helper.getLevel(), helper.absolutePos(rel));
        chest.setItem(0, stack);
    }

    public static void harvestAndReplant(GameTestHelper helper) {
        buildMatureWheat(helper);
        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY);
        golem.getInventory().insert(new ItemStack(Items.WHEAT_SEEDS, 9));

        helper.succeedWhen(() -> {
            for (BlockPos spot : farmSpots()) {
                BlockState state = helper.getBlockState(spot);
                helper.assertTrue(state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) < 7, "Crop at " + spot + " should be harvested and replanted, got " + state);
            }
            helper.assertTrue(golem.getInventory().countItem(Items.WHEAT) >= 9, "Golem should carry the harvested wheat, has " + golem.getInventory().countItem(Items.WHEAT));
        });
    }

    public static void depositFullInventory(GameTestHelper helper) {
        buildFarmland(helper);
        ContainerLink deposit = link(helper, CHEST_POS, ContainerRole.DEPOSIT);
        helper.setBlock(CHEST_POS, Blocks.CHEST);
        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(deposit), JobOptionValues.EMPTY);
        for (int slot = 0; slot < golem.getInventory().getUsableSlots(); slot++) {
            golem.getInventory().setItem(slot, new ItemStack(Items.WHEAT, 64));
        }

        helper.succeedWhen(() -> helper.assertTrue(countInChest(helper, CHEST_POS, Items.WHEAT) >= 64,
                "Full golem should deposit wheat into the deposit chest, chest has " + countInChest(helper, CHEST_POS, Items.WHEAT)));
    }

    public static void restockAndPlant(GameTestHelper helper) {
        buildFarmland(helper);
        ContainerLink supply = link(helper, CHEST_POS, ContainerRole.SUPPLY);
        fillChest(helper, CHEST_POS, new ItemStack(Items.WHEAT_SEEDS, 16));
        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(supply), JobOptionValues.EMPTY);

        helper.succeedWhen(() -> {
            long planted = farmSpots().stream().filter(spot -> helper.getBlockState(spot).is(Blocks.WHEAT)).count();
            helper.assertTrue(planted >= 9, "All farmland spots should be planted from supplied seeds, planted " + planted + ", golem has " + golem.getInventory().countItem(Items.WHEAT_SEEDS));
        });
    }

    public static void stackedPlantKeepsBase(GameTestHelper helper) {
        buildFloor(helper);
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.SAND);
        helper.setBlock(new BlockPos(3, 1, 2), Blocks.WATER);
        helper.setBlock(new BlockPos(2, 2, 2), Blocks.SUGAR_CANE);
        helper.setBlock(new BlockPos(2, 3, 2), Blocks.SUGAR_CANE);
        helper.setBlock(new BlockPos(2, 4, 2), Blocks.SUGAR_CANE);
        spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY);

        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.SUGAR_CANE, new BlockPos(2, 2, 2));
            helper.assertTrue(helper.getBlockState(new BlockPos(2, 3, 2)).isAir(), "Middle sugar cane should be harvested");
            helper.assertTrue(helper.getBlockState(new BlockPos(2, 4, 2)).isAir(), "Top sugar cane should be harvested");
        });
    }

    public static void gourdHarvestKeepsStem(GameTestHelper helper) {
        buildFloor(helper);
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(3, 1, 2), Blocks.WATER);
        helper.setBlock(new BlockPos(2, 2, 2), Blocks.ATTACHED_PUMPKIN_STEM.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        helper.setBlock(new BlockPos(3, 2, 2), Blocks.PUMPKIN);
        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY);

        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getBlockState(new BlockPos(3, 2, 2)).isAir(), "Ripe pumpkin should be harvested");
            Block stem = helper.getBlockState(new BlockPos(2, 2, 2)).getBlock();
            helper.assertTrue(stem instanceof StemBlock || stem instanceof AttachedStemBlock, "Stem should stay, got " + stem);
            helper.assertTrue(golem.getInventory().countItem(Items.PUMPKIN) >= 1, "Golem should carry the pumpkin");
        });
    }

    public static void berryBushResets(GameTestHelper helper) {
        buildFloor(helper);
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.DIRT);
        helper.setBlock(new BlockPos(2, 2, 2), Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(BlockStateProperties.AGE_3, 3));
        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY);

        helper.succeedWhen(() -> {
            BlockState bush = helper.getBlockState(new BlockPos(2, 2, 2));
            helper.assertTrue(bush.is(Blocks.SWEET_BERRY_BUSH) && bush.getValue(BlockStateProperties.AGE_3) == 1, "Berry bush should reset to age 1, got " + bush);
            helper.assertTrue(golem.getInventory().countItem(Items.SWEET_BERRIES) >= 1, "Golem should carry berries");
        });
    }

    public static void tillsDirtWithHoe(GameTestHelper helper) {
        buildFloor(helper);
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.DIRT);
        helper.setBlock(new BlockPos(4, 1, 2), Blocks.WATER);
        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY.with(FarmingTargets.TILL, true));
        golem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_HOE));

        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getBlockState(new BlockPos(2, 1, 2)).is(Blocks.FARMLAND), "Dirt near water should be tilled");
            helper.assertTrue(golem.getMainHandItem().getDamageValue() > 0, "Tilling should damage the hoe");
        });
    }

    public static void tillWithoutHoeReportsMissingTool(GameTestHelper helper) {
        buildFloor(helper);
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.DIRT);
        helper.setBlock(new BlockPos(4, 1, 2), Blocks.WATER);
        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY.with(FarmingTargets.TILL, true));

        helper.succeedWhen(() -> {
            helper.assertTrue(golem.getStatus() == GolemStatus.MISSING_TOOL, "Golem should report a missing hoe, got " + golem.getStatus());
            helper.assertTrue(helper.getBlockState(new BlockPos(2, 1, 2)).is(Blocks.DIRT), "Dirt should stay untilled without a hoe");
        });
    }

    public static void plantingWithoutSeedsReportsNeedsSupplies(GameTestHelper helper) {
        buildFarmland(helper);
        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY);

        helper.succeedWhen(() -> helper.assertTrue(golem.getStatus() == GolemStatus.NEEDS_SUPPLIES,
                "Golem without seeds and no supply should report needs supplies, got " + golem.getStatus()));
    }

    public static void bonemealGrowsCrops(GameTestHelper helper) {
        buildFarmland(helper);
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.WHEAT);
        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY.with(FarmingTargets.BONEMEAL, true));
        golem.getInventory().insert(new ItemStack(Items.BONE_MEAL, 8));

        helper.succeedWhen(() -> {
            BlockState crop = helper.getBlockState(new BlockPos(3, 2, 3));
            helper.assertTrue(crop.is(Blocks.WHEAT) && crop.getValue(CropBlock.AGE) >= 2, "Bone meal should grow the wheat, got " + crop);
            helper.assertTrue(golem.getInventory().countItem(Items.BONE_MEAL) < 8, "Bone meal should be used up");
        });
    }

    public static void twoGolemsShareField(GameTestHelper helper) {
        buildMatureWheat(helper);
        GolemEntity first = spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY);
        first.getInventory().insert(new ItemStack(Items.WHEAT_SEEDS, 9));
        GolemEntity second = spawnFarmer(helper, new BlockPos(6, 2, 0), List.of(), JobOptionValues.EMPTY);
        second.getInventory().insert(new ItemStack(Items.WHEAT_SEEDS, 9));

        helper.succeedWhen(() -> {
            for (BlockPos spot : farmSpots()) {
                BlockState state = helper.getBlockState(spot);
                helper.assertTrue(state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) < 7, "Crop at " + spot + " should be harvested once, got " + state);
            }
        });
    }

    public static void fieldMemoryPrunesStaleSpots(GameTestHelper helper) {
        buildFarmland(helper);
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.DIRT);
        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY);
        BlockPos valid = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos degraded = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos outside = helper.absolutePos(new BlockPos(12, 2, 12));
        Holder<Item> seed = BuiltInRegistries.ITEM.wrapAsHolder(Items.WHEAT_SEEDS);
        golem.updateWorkplace(workplace -> workplace.withItemMemory(Map.of(valid, seed, degraded, seed, outside, seed)));

        helper.succeedWhen(() -> {
            Map<BlockPos, Holder<Item>> memory = golem.getWorkplace(helper.getLevel()).orElseThrow().itemMemory();
            helper.assertTrue(memory.containsKey(valid), "Memory for plantable farmland should be kept");
            helper.assertTrue(!memory.containsKey(degraded), "Memory for farmland that became dirt should be pruned");
            helper.assertTrue(!memory.containsKey(outside), "Memory outside all zones should be pruned");
        });
    }

    public static void clearsObstructionsForPlanting(GameTestHelper helper) {
        buildFarmland(helper);
        for (BlockPos spot : farmSpots()) {
            helper.setBlock(spot, Blocks.SHORT_GRASS);
        }
        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY);
        golem.getInventory().insert(new ItemStack(Items.WHEAT_SEEDS, 9));

        helper.succeedWhen(() -> {
            long planted = farmSpots().stream().filter(spot -> helper.getBlockState(spot).is(Blocks.WHEAT)).count();
            helper.assertTrue(planted >= 9, "Grass above the farmland should be cleared and planted, planted " + planted);
        });
    }

    public static void obstructionsStayWhenOptionOff(GameTestHelper helper) {
        buildFarmland(helper);
        for (BlockPos spot : farmSpots()) {
            helper.setBlock(spot, Blocks.SHORT_GRASS);
        }
        spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY.with(FarmingTargets.CLEAR_OBSTRUCTIONS, false));

        helper.runAfterDelay(200, () -> {
            for (BlockPos spot : farmSpots()) {
                helper.assertTrue(helper.getBlockState(spot).is(Blocks.SHORT_GRASS), "Grass at " + spot + " should stay with the option off");
            }

            helper.succeed();
        });
    }

    public static void farmingCoreRoundTrips(GameTestHelper helper) {
        Holder<GolemJob> job = PrincipiaMagicaRegistries.GOLEM_JOB.get(FarmingJob.KEY).orElseThrow();
        ItemStack core = GolemCoreItem.createStack(job);
        helper.assertTrue(core.is(ItemRegistry.FARMING_CORE.get()), "Farming job should map to the farming core, got " + core);

        GolemEntity golem = spawnFarmer(helper, GOLEM_POS, List.of(), JobOptionValues.EMPTY);
        ItemStack taken = golem.takeJob();
        helper.assertTrue(taken.is(ItemRegistry.FARMING_CORE.get()), "Removing the job should return the farming core, got " + taken);
        helper.assertTrue(golem.getSpec().job().isEmpty(), "Golem should have no job after the core is removed");

        helper.succeed();
    }
}
