// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.ai.behaviour.ContainerAccess;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.job.GolemBreaking;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.JobOptionValues;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.LumberjackJob;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.LumberjackTargets;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.TreeDetector;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LumberjackGameTests {
    private static final BlockPos GOLEM_POS = new BlockPos(0, 2, 0);
    private static final BlockPos FAR_POS = new BlockPos(0, 2, 3);
    private static final BlockPos ZONE_MIN = new BlockPos(0, 1, 0);
    private static final BlockPos ZONE_MAX = new BlockPos(7, 4, 7);
    /**
     * The opposite corner of the footprint, carved on the grass: the footprint's ground is the grass level.
     */
    private static final BlockPos GROUND_MAX = new BlockPos(7, 1, 7);
    private static final BlockPos CHEST_POS = new BlockPos(0, 2, 6);
    private static final BlockPos TREE = new BlockPos(3, 2, 3);
    private static final int BASE_Y = 2;
    private static final int LOW_RING_CELLS = 48;

    /**
     * Stone at y=0 and grass at y=1: saplings can only be planted on grass or dirt, so the stumps stand on grass.
     */
    private static void buildGround(GameTestHelper helper) {
        for (int x = 0; x <= ZONE_MAX.getX(); x++) {
            for (int z = 0; z <= ZONE_MAX.getZ(); z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                helper.setBlock(new BlockPos(x, 1, z), Blocks.GRASS_BLOCK);
            }
        }
    }

    private static BlockState naturalLeaves(Block leaves) {
        return leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT, false).setValue(LeavesBlock.DISTANCE, 1);
    }

    private static BlockState playerLeaves(Block leaves) {
        return leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.DISTANCE, 1);
    }

    /**
     * Walls the stump in with a 5x5 ring of leaves on the two lowest layers, so the golem cannot stand next to the trunk.
     */
    private static void buildLowLeafRing(GameTestHelper helper, BlockState leaves) {
        for (int dy = 0; dy <= 1; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }

                    helper.setBlock(new BlockPos(TREE.getX() + dx, BASE_Y + dy, TREE.getZ() + dz), leaves);
                }
            }
        }
    }

    private static boolean isTrunk(List<BlockPos> columns, int x, int z) {
        return columns.stream().anyMatch(column -> column.getX() == x && column.getZ() == z);
    }

    /**
     * Builds a trunk of the given height on every column, with a canopy of leaves around the top when leaves are given.
     */
    private static void buildTree(GameTestHelper helper, List<BlockPos> columns, int height, Block log, @Nullable BlockState leaves) {
        for (BlockPos column : columns) {
            for (int i = 0; i < height; i++) {
                helper.setBlock(new BlockPos(column.getX(), BASE_Y + i, column.getZ()), log);
            }
        }

        if (leaves == null) {
            return;
        }

        for (BlockPos column : columns) {
            for (int dy = height - 2; dy <= height; dy++) {
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        int x = column.getX() + dx;
                        int z = column.getZ() + dz;
                        if (dy < height && isTrunk(columns, x, z)) {
                            continue;
                        }

                        helper.setBlock(new BlockPos(x, BASE_Y + dy, z), leaves);
                    }
                }
            }
        }
    }

    private static boolean logsGone(GameTestHelper helper, int height) {
        for (int i = 0; i < height; i++) {
            if (helper.getBlockState(TREE.above(i)).is(BlockTags.LOGS)) {
                return false;
            }
        }

        return true;
    }

    private static boolean logsPresent(GameTestHelper helper, int height) {
        return !logsGone(helper, height);
    }

    private static ContainerLink link(GameTestHelper helper, BlockPos rel, ContainerRole role) {
        return new ContainerLink(helper.absolutePos(rel), Direction.UP, role);
    }

    private static GolemEntity spawnLumberjack(GameTestHelper helper, List<ContainerLink> links, boolean withAxe) {
        return spawnLumberjack(helper, links, withAxe, JobOptionValues.EMPTY);
    }

    private static GolemEntity spawnLumberjack(GameTestHelper helper, List<ContainerLink> links, boolean withAxe, JobOptionValues options) {
        return spawnLumberjackAt(helper, GOLEM_POS, links, withAxe, options);
    }

    private static GolemEntity spawnLumberjackAt(GameTestHelper helper, BlockPos spawnPos, List<ContainerLink> links, boolean withAxe, JobOptionValues options) {
        ServerLevel level = helper.getLevel();
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), spawnPos);
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(PrincipiaMagicaRegistries.GOLEM_JOB.get(LumberjackJob.KEY).orElseThrow()));
        golem.setOwnerUUID(UUID.randomUUID());
        golem.setAnchor(GlobalPos.of(level.dimension(), helper.absolutePos(GOLEM_POS)));

        WorkFootprint footprint = WorkFootprint.of(
                new CarvedGlyph(helper.absolutePos(ZONE_MIN), Direction.UP),
                new CarvedGlyph(helper.absolutePos(GROUND_MAX), Direction.UP));
        golem.updateWorkplace(workplace -> workplace
                .withFootprints(List.of(footprint))
                .withLinks(links)
                .withOptions(options)
                .markInitialized());

        if (withAxe) {
            golem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        }

        return golem;
    }

    private static void fillChest(GameTestHelper helper, BlockPos rel, ItemStack stack) {
        helper.setBlock(rel, Blocks.CHEST);
        Container chest = ContainerAccess.containerAt(helper.getLevel(), helper.absolutePos(rel));
        chest.setItem(0, stack);
    }

    public static void fellsNaturalTree(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        GolemEntity golem = spawnLumberjack(helper, List.of(), true);

        helper.succeedWhen(() -> {
            helper.assertTrue(logsGone(helper, 5), "All logs of the natural tree should be felled");
            helper.assertTrue(golem.getInventory().countItem(Items.OAK_LOG) >= 5, "Golem should carry the logs, has " + golem.getInventory().countItem(Items.OAK_LOG));
            helper.assertTrue(golem.getItemBySlot(EquipmentSlot.MAINHAND).getDamageValue() > 0, "The axe should lose durability while felling");
        });
    }

    public static void fellsTreeEnclosedByLowLeaves(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        buildLowLeafRing(helper, naturalLeaves(Blocks.OAK_LEAVES));
        GolemEntity golem = spawnLumberjack(helper, List.of(), true);

        helper.succeedWhen(() -> {
            helper.assertTrue(logsGone(helper, 5), "The tree walled in by natural leaves should be felled, leaves left at BASE_Y: " + countLowLeaves(helper, Blocks.OAK_LEAVES));
            helper.assertTrue(golem.getInventory().countItem(Items.OAK_LOG) >= 5, "Golem should carry the logs, has " + golem.getInventory().countItem(Items.OAK_LOG));
        });
    }

    public static void persistentLeavesAreNotCleared(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        buildLowLeafRing(helper, playerLeaves(Blocks.OAK_LEAVES));
        spawnLumberjack(helper, List.of(), true);

        helper.runAfterDelay(300, () -> {
            helper.assertTrue(countLowLeaves(helper, Blocks.OAK_LEAVES) == LOW_RING_CELLS, "Player-placed leaves around the trunk should never be broken, left " + countLowLeaves(helper, Blocks.OAK_LEAVES));
            helper.succeed();
        });
    }

    public static void clearLeavesOffKeepsLeaves(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        buildLowLeafRing(helper, naturalLeaves(Blocks.OAK_LEAVES));
        spawnLumberjack(helper, List.of(), true, JobOptionValues.EMPTY.with(LumberjackTargets.CLEAR_LEAVES, false));

        helper.runAfterDelay(300, () -> {
            helper.assertTrue(countLowLeaves(helper, Blocks.OAK_LEAVES) == LOW_RING_CELLS, "Natural leaves should stay while the clear leaves option is off, left " + countLowLeaves(helper, Blocks.OAK_LEAVES));
            helper.succeed();
        });
    }

    /**
     * Counts the leaves of the ring on the two lowest layers, which is LOW_RING_CELLS cells around the trunk when all are present.
     */
    private static int countLowLeaves(GameTestHelper helper, Block leaves) {
        int count = 0;
        for (int dy = 0; dy <= 1; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if ((dx != 0 || dz != 0) && helper.getBlockState(new BlockPos(TREE.getX() + dx, BASE_Y + dy, TREE.getZ() + dz)).is(leaves)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    /**
     * A bush-like mound: a single layer of natural leaves fills the 5x5 around the trunk at stump height. The golem can
     * step up onto it, because the air above it is free.
     */
    private static void buildBushMound(GameTestHelper helper, BlockState leaves) {
        for (int dy = 0; dy <= 0; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }

                    helper.setBlock(new BlockPos(TREE.getX() + dx, BASE_Y + dy, TREE.getZ() + dz), leaves);
                }
            }
        }
    }

    /**
     * A gap already exists on the east side, the golem stands on the west side. Walking to the stump from there can
     * route over the leaves instead of around them, so the golem must neither climb onto the bush nor get stuck.
     */
    public static void fellsBushFromFarSideThroughGap(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        buildBushMound(helper, naturalLeaves(Blocks.OAK_LEAVES));
        helper.setBlock(new BlockPos(TREE.getX() + 1, BASE_Y, TREE.getZ()), Blocks.AIR);
        GolemEntity golem = spawnLumberjackAt(helper, FAR_POS, List.of(), true, JobOptionValues.EMPTY);

        helper.succeedWhen(() -> {
            BlockPos feet = helper.relativePos(golem.blockPosition());
            helper.assertTrue(feet.getY() <= BASE_Y, "The golem should never stand on the bush, it is at " + feet);
            helper.assertTrue(logsGone(helper, 5), "The tree behind the bush should be felled, golem at " + feet);
        });
    }

    /**
     * The golem starts on top of the bush. It must find its way down to the ground and fell the tree from there.
     */
    public static void golemOnLeavesComesDown(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        buildBushMound(helper, naturalLeaves(Blocks.OAK_LEAVES));
        GolemEntity golem = spawnLumberjackAt(helper, new BlockPos(TREE.getX() - 2, BASE_Y + 1, TREE.getZ()), List.of(), true, JobOptionValues.EMPTY);

        helper.succeedWhen(() -> {
            BlockPos feet = helper.relativePos(golem.blockPosition());
            helper.assertTrue(feet.getY() <= BASE_Y, "The golem should come down from the bush, it is at " + feet);
            helper.assertTrue(logsGone(helper, 5), "The tree should be felled after the golem came down, golem at " + feet);
        });
    }

    /**
     * The only open cell next to the stump is walled in by leaves, so the golem cannot reach it. It must give up on that
     * cell and open a corridor from its own side.
     */
    public static void fellsTreeAroundUnreachableOpening(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        buildBushMound(helper, naturalLeaves(Blocks.OAK_LEAVES));
        helper.setBlock(new BlockPos(TREE.getX() + 1, BASE_Y, TREE.getZ()), Blocks.AIR);
        GolemEntity golem = spawnLumberjackAt(helper, FAR_POS, List.of(), true, JobOptionValues.EMPTY);

        helper.succeedWhen(() -> {
            BlockPos feet = helper.relativePos(golem.blockPosition());
            helper.assertTrue(feet.getY() <= BASE_Y, "The golem should never stand on the bush, it is at " + feet);
            helper.assertTrue(logsGone(helper, 5), "The tree should be felled after the walled-in opening was given up, golem at " + feet);
        });
    }

    /**
     * Grows a vanilla tree feature at the stump position, with a fixed seed so the layout is the same every run.
     */
    private static void growFeature(GameTestHelper helper, ResourceKey<ConfiguredFeature<?, ?>> feature, long seed) {
        ServerLevel level = helper.getLevel();
        ConfiguredFeature<?, ?> configured = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(feature).value();
        configured.place(level, level.getChunkSource().getGenerator(), RandomSource.create(seed), helper.absolutePos(TREE));
    }

    /**
     * True when no log is left in the work zone and the space above it, where vanilla trees grow.
     */
    private static boolean noLogsInZone(GameTestHelper helper) {
        for (int x = ZONE_MIN.getX(); x <= ZONE_MAX.getX(); x++) {
            for (int y = BASE_Y; y <= BASE_Y + 14; y++) {
                for (int z = ZONE_MIN.getZ(); z <= ZONE_MAX.getZ(); z++) {
                    if (helper.getBlockState(new BlockPos(x, y, z)).is(BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static void fellsGrownTree(GameTestHelper helper, ResourceKey<ConfiguredFeature<?, ?>> feature, long seed) {
        buildGround(helper);
        growFeature(helper, feature, seed);
        GolemEntity golem = spawnLumberjack(helper, List.of(), true);

        helper.succeedWhen(() -> {
            BlockPos feet = helper.relativePos(golem.blockPosition());
            helper.assertTrue(!onLeaves(helper, feet), "The golem should never stand on leaves, it is at " + feet);
            helper.assertTrue(noLogsInZone(helper), "The grown tree should be felled, golem at " + feet);
        });
    }

    /**
     * True if the golem stands on a leaf or is above the ground layer of the bush.
     */
    private static boolean onLeaves(GameTestHelper helper, BlockPos feet) {
        return feet.getY() > BASE_Y || helper.getBlockState(feet.below()).is(BlockTags.LEAVES);
    }

    public static void fellsJungleBush(GameTestHelper helper) {
        fellsGrownTree(helper, TreeFeatures.JUNGLE_BUSH, 1L);
    }

    public static void fellsGrownOak(GameTestHelper helper) {
        fellsGrownTree(helper, TreeFeatures.OAK, 2L);
    }

    public static void fellsGrownFancyOak(GameTestHelper helper) {
        fellsGrownTree(helper, TreeFeatures.FANCY_OAK, 3L);
    }

    public static void fellsGrownSpruce(GameTestHelper helper) {
        fellsGrownTree(helper, TreeFeatures.SPRUCE, 4L);
    }

    /**
     * A gap exists on the far (east) side, the golem stands on the near (west) side. It must carve through the leaves
     * on its own side instead of walking around to the gap.
     */
    public static void carvesOnOwnSideNotAroundGap(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        buildBushMound(helper, naturalLeaves(Blocks.OAK_LEAVES));
        helper.setBlock(new BlockPos(TREE.getX() + 1, BASE_Y, TREE.getZ()), Blocks.AIR);
        GolemEntity golem = spawnLumberjackAt(helper, FAR_POS, List.of(), true, JobOptionValues.EMPTY);

        helper.succeedWhen(() -> {
            BlockPos feet = helper.relativePos(golem.blockPosition());
            helper.assertTrue(!onLeaves(helper, feet), "The golem should never stand on the bush, it is at " + feet);
            helper.assertTrue(logsGone(helper, 5), "The tree should be felled, golem at " + feet);
            helper.assertTrue(!helper.getBlockState(new BlockPos(TREE.getX() - 1, BASE_Y, TREE.getZ())).is(Blocks.OAK_LEAVES), "The leaf on the golem's side should be carved, not walked around");
        });
    }

    public static void replantsSapling(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        GolemEntity golem = spawnLumberjack(helper, List.of(), true);
        golem.getInventory().insert(new ItemStack(Items.OAK_SAPLING, 4));

        helper.succeedWhen(() -> helper.assertTrue(helper.getBlockState(TREE).is(Blocks.OAK_SAPLING),
                "A sapling should be planted on the stump, got " + helper.getBlockState(TREE)));
    }

    public static void ignoresPlayerLeaves(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, playerLeaves(Blocks.OAK_LEAVES));
        spawnLumberjack(helper, List.of(), true);

        helper.runAfterDelay(200, () -> {
            helper.assertTrue(logsPresent(helper, 5), "Logs with only player-placed leaves should not be felled");
            helper.succeed();
        });
    }

    public static void ignoresBareLogPillar(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, null);
        spawnLumberjack(helper, List.of(), true);

        helper.runAfterDelay(200, () -> {
            helper.assertTrue(logsPresent(helper, 5), "A log pillar without leaves should not be felled");
            helper.succeed();
        });
    }

    public static void ignoresWallTouchingNaturalLeaves(GameTestHelper helper) {
        buildGround(helper);
        for (int x = 0; x <= 3; x++) {
            for (int y = BASE_Y; y <= BASE_Y + 2; y++) {
                helper.setBlock(new BlockPos(x, y, 5), Blocks.OAK_LOG);
            }
        }

        helper.setBlock(new BlockPos(0, BASE_Y + 1, 6), naturalLeaves(Blocks.OAK_LEAVES));
        spawnLumberjack(helper, List.of(), true);

        helper.runAfterDelay(200, () -> {
            for (int x = 0; x <= 3; x++) {
                helper.assertTrue(helper.getBlockState(new BlockPos(x, BASE_Y, 5)).is(Blocks.OAK_LOG), "A wall of logs should not be felled");
            }

            helper.succeed();
        });
    }

    public static void fells2x2AndReplantsFour(GameTestHelper helper) {
        buildGround(helper);
        List<BlockPos> trunk = List.of(new BlockPos(3, 0, 3), new BlockPos(4, 0, 3), new BlockPos(3, 0, 4), new BlockPos(4, 0, 4));
        buildTree(helper, trunk, 5, Blocks.DARK_OAK_LOG, naturalLeaves(Blocks.DARK_OAK_LEAVES));
        GolemEntity golem = spawnLumberjack(helper, List.of(), true);
        golem.getInventory().insert(new ItemStack(Items.DARK_OAK_SAPLING, 4));

        helper.succeedWhen(() -> {
            for (BlockPos stump : trunk) {
                BlockPos rel = new BlockPos(stump.getX(), BASE_Y, stump.getZ());
                helper.assertTrue(helper.getBlockState(rel).is(Blocks.DARK_OAK_SAPLING), "Each stump of the 2x2 tree should get a dark oak sapling, got " + helper.getBlockState(rel) + " at " + rel);
            }
        });
    }

    public static void tallerThanZone(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 10, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        spawnLumberjack(helper, List.of(), true);

        helper.succeedWhen(() -> helper.assertTrue(logsGone(helper, 10), "The whole tree should be felled, even above the work zone"));
    }

    /**
     * The footprint only defines the trunk area: a tree whose stump is outside of it is left standing.
     */
    public static void ignoresStumpOutsideFootprint(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        GolemEntity golem = spawnLumberjack(helper, List.of(), true);
        WorkFootprint beside = WorkFootprint.of(
                new CarvedGlyph(helper.absolutePos(new BlockPos(0, 1, 0)), Direction.UP),
                new CarvedGlyph(helper.absolutePos(new BlockPos(1, 1, 1)), Direction.UP));
        golem.updateWorkplace(workplace -> workplace.withFootprints(List.of(beside)));

        helper.runAfterDelay(300, () -> {
            helper.assertTrue(logsPresent(helper, 5), "A tree outside the footprint should not be felled");
            helper.succeed();
        });
    }

    public static void missingAxeReportsStatus(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        GolemEntity golem = spawnLumberjack(helper, List.of(), false);

        helper.runAfterDelay(200, () -> {
            helper.assertTrue(golem.getStatus() == GolemStatus.MISSING_TOOL, "Golem without an axe should report a missing tool, got " + golem.getStatus());
            helper.assertTrue(logsPresent(helper, 5), "Without an axe the tree should not be felled");
            helper.succeed();
        });
    }

    public static void restocksSaplingsFromSupply(GameTestHelper helper) {
        buildGround(helper);
        buildTree(helper, List.of(TREE), 5, Blocks.OAK_LOG, naturalLeaves(Blocks.OAK_LEAVES));
        ContainerLink supply = link(helper, CHEST_POS, ContainerRole.SUPPLY);
        fillChest(helper, CHEST_POS, new ItemStack(Items.OAK_SAPLING, 4));
        GolemEntity golem = spawnLumberjack(helper, List.of(supply), true);

        helper.succeedWhen(() -> helper.assertTrue(helper.getBlockState(TREE).is(Blocks.OAK_SAPLING),
                "A sapling restocked from the supply chest should be planted on the stump, got " + helper.getBlockState(TREE)));
    }

    public static void bonemealGrowsSapling(GameTestHelper helper) {
        buildGround(helper);
        helper.setBlock(TREE, Blocks.OAK_SAPLING);
        GolemEntity golem = spawnLumberjack(helper, List.of(), true, bonemealOn());
        golem.getInventory().insert(new ItemStack(Items.BONE_MEAL, 16));

        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getBlockState(TREE).getValue(SaplingBlock.STAGE) > 0, "Bone meal should advance the sapling, got " + helper.getBlockState(TREE));
            helper.assertTrue(golem.getInventory().countItem(Items.BONE_MEAL) < 16, "Bone meal should be used on the sapling");
        });
    }

    public static void bonemealOffLeavesSapling(GameTestHelper helper) {
        buildGround(helper);
        helper.setBlock(TREE, Blocks.OAK_SAPLING);
        GolemEntity golem = spawnLumberjack(helper, List.of(), true, JobOptionValues.EMPTY.with(LumberjackTargets.REPLANT, false));
        golem.getInventory().insert(new ItemStack(Items.BONE_MEAL, 16));

        helper.runAfterDelay(200, () -> {
            helper.assertTrue(helper.getBlockState(TREE).is(Blocks.OAK_SAPLING), "The sapling should not be touched while the option is off, got " + helper.getBlockState(TREE));
            helper.assertTrue(golem.getInventory().countItem(Items.BONE_MEAL) == 16, "No bone meal should be used while the option is off");
            helper.succeed();
        });
    }

    public static void restocksBonemealFromSupply(GameTestHelper helper) {
        buildGround(helper);
        helper.setBlock(TREE, Blocks.OAK_SAPLING);
        ContainerLink supply = link(helper, CHEST_POS, ContainerRole.SUPPLY);
        fillChest(helper, CHEST_POS, new ItemStack(Items.BONE_MEAL, 16));
        spawnLumberjack(helper, List.of(supply), true, bonemealOn());

        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getBlockState(TREE).getValue(SaplingBlock.STAGE) > 0, "A sapling should be advanced with bone meal restocked from the supply chest, got " + helper.getBlockState(TREE));
            helper.assertTrue(ContainerAccess.containerAt(helper.getLevel(), helper.absolutePos(CHEST_POS)).getItem(0).getCount() < 16, "The supply chest should have given up bone meal");
        });
    }

    public static void stalledSaplingStopsTakingBonemeal(GameTestHelper helper) {
        buildGround(helper);
        helper.setBlock(TREE, Blocks.OAK_SAPLING);
        helper.setBlock(TREE.above(), Blocks.STONE);
        GolemEntity golem = spawnLumberjack(helper, List.of(), true, bonemealOn());
        golem.getInventory().insert(new ItemStack(Items.BONE_MEAL, 16));

        helper.runAfterDelay(900, () -> {
            helper.assertTrue(helper.getBlockState(TREE).is(Blocks.OAK_SAPLING), "A sapling without room should stay a sapling, got " + helper.getBlockState(TREE));
            helper.assertTrue(golem.getInventory().countItem(Items.BONE_MEAL) == 4, "A sapling without room should take 12 bone meal and then be skipped, left " + golem.getInventory().countItem(Items.BONE_MEAL));
            helper.succeed();
        });
    }

    private static JobOptionValues bonemealOn() {
        return JobOptionValues.EMPTY.with(LumberjackTargets.BONEMEAL, true).with(LumberjackTargets.REPLANT, false);
    }

    public static void detectorFindsDiagonalBranches(GameTestHelper helper) {
        buildGround(helper);
        for (int y = BASE_Y; y <= BASE_Y + 2; y++) {
            helper.setBlock(new BlockPos(TREE.getX(), y, TREE.getZ()), Blocks.ACACIA_LOG);
        }

        helper.setBlock(new BlockPos(TREE.getX(), BASE_Y + 3, TREE.getZ()), Blocks.ACACIA_LOG);
        helper.setBlock(new BlockPos(TREE.getX() + 1, BASE_Y + 3, TREE.getZ() + 1), Blocks.ACACIA_LOG);
        helper.setBlock(new BlockPos(TREE.getX() + 1, BASE_Y + 3, TREE.getZ() + 2), naturalLeaves(Blocks.ACACIA_LEAVES));

        Optional<TreeDetector.Tree> tree = TreeDetector.scan(helper.getLevel(), helper.absolutePos(TREE));
        helper.assertTrue(tree.isPresent(), "A diagonal acacia branch should be detected as a natural tree");
        tree.ifPresent(found -> {
            helper.assertTrue(found.logs().size() == 5, "All logs of the diagonal tree should be part of it, got " + found.logs().size());
            helper.assertTrue(found.stumps().equals(List.of(helper.absolutePos(TREE))), "The tree should have one stump, got " + found.stumps());
            helper.assertTrue(found.leaves() == Blocks.ACACIA_LEAVES, "The dominant leaves should be acacia, got " + found.leaves());
        });

        helper.succeed();
    }

    public static void breakTicksFollowTheTool(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos logPos = helper.absolutePos(TREE);
        helper.setBlock(TREE, Blocks.OAK_LOG);
        GolemEntity golem = spawnLumberjack(helper, List.of(), true);

        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        int withAxe = GolemBreaking.breakTicks(level, golem, logPos, log);
        golem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        int bareHanded = GolemBreaking.breakTicks(level, golem, logPos, log);
        int wheat = GolemBreaking.breakTicks(level, golem, logPos, Blocks.WHEAT.defaultBlockState());

        helper.assertTrue(withAxe == 10, "An iron axe should break an oak log in 10 ticks, got " + withAxe);
        helper.assertTrue(bareHanded == 60, "Bare hands should break an oak log in 60 ticks, got " + bareHanded);
        helper.assertTrue(wheat == 0, "Wheat should break instantly, got " + wheat);
        helper.succeed();
    }

    public static void lumberjackAxiomRoundTrips(GameTestHelper helper) {
        Holder<GolemJob> job = PrincipiaMagicaRegistries.GOLEM_JOB.get(LumberjackJob.KEY).orElseThrow();
        ItemStack axiom = AxiomItem.createStack(job);
        helper.assertTrue(axiom.is(ItemRegistry.AXIOM_OF_FELLING.get()), "Lumberjack job should map to the axiom of felling, got " + axiom);

        GolemEntity golem = spawnLumberjack(helper, List.of(), false);
        ItemStack taken = golem.takeJob();
        helper.assertTrue(taken.is(ItemRegistry.AXIOM_OF_FELLING.get()), "Removing the job should return the axiom of felling, got " + taken);
        helper.assertTrue(golem.getSpec().job().isEmpty(), "Golem should have no job after the axiom is removed");
        helper.succeed();
    }
}
