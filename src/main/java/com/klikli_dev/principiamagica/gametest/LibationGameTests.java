// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.libation.AmountSmoother;
import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.content.libation.PrincipleSelection;
import com.klikli_dev.principiamagica.content.libation.RecipeCache;
import com.klikli_dev.principiamagica.content.libation.SyncThrottle;
import com.klikli_dev.principiamagica.content.libation.drying.DryingSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.item.ClayPhialItem;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlock;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.slab.SpoutSide;
import com.klikli_dev.principiamagica.content.libation.trough.CatchTroughBlockEntity;
import com.klikli_dev.principiamagica.content.libation.vessel.CuringVesselBlockEntity;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import com.klikli_dev.principiamagica.registry.BlockRegistry;
import com.klikli_dev.principiamagica.registry.FluidRegistry;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LibationGameTests {

    /**
     * The principles that the steeping recipe of the unfired axiom of harvest needs.
     */
    private static final int STEEPING_PRINCIPLES = 100;

    /**
     * A spout placed on the north side of a slab opens the north rim, and removing the spout closes it again.
     */
    public static void spoutOpensHoleInSlab(GameTestHelper helper) {
        BlockPos slab = new BlockPos(3, 2, 3);
        BlockPos spout = new BlockPos(3, 2, 2);
        helper.setBlock(slab, BlockRegistry.LIBATION_SLAB.get().defaultBlockState());
        helper.assertBlockProperty(slab, LibationSlabBlock.SPOUT, SpoutSide.NONE);

        helper.setBlock(spout, BlockRegistry.LIBATION_SPOUT.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        helper.runAfterDelay(1, () -> {
            helper.assertBlockProperty(slab, LibationSlabBlock.SPOUT, SpoutSide.NORTH);

            helper.destroyBlock(spout);
            helper.runAfterDelay(1, () -> {
                helper.assertBlockProperty(slab, LibationSlabBlock.SPOUT, SpoutSide.NONE);
                helper.succeed();
            });
        });
    }

    /**
     * A spout drops off when the slab it is attached to is removed.
     */
    public static void spoutPopsOffWhenHostIsRemoved(GameTestHelper helper) {
        BlockPos slab = new BlockPos(3, 2, 3);
        BlockPos spout = new BlockPos(3, 2, 2);
        helper.setBlock(slab, BlockRegistry.LIBATION_SLAB.get().defaultBlockState());
        helper.setBlock(spout, BlockRegistry.LIBATION_SPOUT.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));

        helper.destroyBlock(slab);
        helper.runAfterDelay(1, () -> {
            helper.assertBlockState(spout, state -> state.isAir(), state -> Component.literal("The spout should be removed with its host"));
            helper.succeed();
        });
    }

    /**
     * A redstone signal at the spout opens it, the same way it opened the vessel taps.
     */
    public static void spoutOpensFromRedstone(GameTestHelper helper) {
        BlockPos slab = new BlockPos(3, 2, 3);
        BlockPos spout = new BlockPos(3, 2, 2);
        BlockPos power = new BlockPos(3, 2, 1);
        helper.setBlock(slab, BlockRegistry.LIBATION_SLAB.get().defaultBlockState());
        helper.setBlock(spout, BlockRegistry.LIBATION_SPOUT.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        helper.setBlock(power, Blocks.REDSTONE_BLOCK);

        helper.runAfterDelay(2, () -> {
            helper.assertBlockProperty(spout, BlockStateProperties.OPEN, true);
            helper.succeed();
        });
    }

    /**
     * Without any spout the slab still dissolves a washed item: its water covers the principles and they are kept in its storage.
     */
    public static void slabDissolvesWithoutSpout(GameTestHelper helper) {
        BlockPos slab = new BlockPos(3, 2, 3);
        helper.setBlock(slab, BlockRegistry.LIBATION_SLAB.get().defaultBlockState());
        LibationSlabBlockEntity be = stockSlab(helper, slab);

        helper.runAfterDelay(120, () -> {
            helper.assertTrue(be.getStored().total() == 50, "The slab should store the 50 mB of ash without a spout, stored " + be.getStored().total());
            helper.succeed();
        });
    }

    /**
     * Stored principles wait while the spout is closed, and drain into the trough below once the spout is open.
     */
    public static void slabDrainsOnlyThroughOpenSpout(GameTestHelper helper) {
        BlockPos slab = new BlockPos(3, 2, 3);
        BlockPos spout = new BlockPos(3, 2, 2);
        BlockPos trough = new BlockPos(3, 1, 2);
        helper.setBlock(slab, BlockRegistry.LIBATION_SLAB.get().defaultBlockState());
        helper.setBlock(spout, BlockRegistry.LIBATION_SPOUT.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        helper.setBlock(trough, BlockRegistry.CATCH_TROUGH.get().defaultBlockState());
        LibationSlabBlockEntity be = stockSlab(helper, slab);
        CatchTroughBlockEntity troughBe = (CatchTroughBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(trough));

        helper.runAfterDelay(120, () -> {
            helper.assertTrue(be.getStored().total() == 50, "A closed spout should keep the principles in storage, stored " + be.getStored().total());
            helper.assertTrue(troughBe.getPrinciples().total() == 0, "A closed spout should not drain, trough holds " + troughBe.getPrinciples().total());

            helper.setBlock(spout, BlockRegistry.LIBATION_SPOUT.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                    .setValue(BlockStateProperties.OPEN, true));
            helper.runAfterDelay(120, () -> {
                helper.assertTrue(be.getStored().total() == 0, "An open spout should drain the storage, stored " + be.getStored().total());
                helper.assertTrue(troughBe.getPrinciples().total() == 50, "The trough should receive the 50 mB, holds " + troughBe.getPrinciples().total());
                helper.succeed();
            });
        });
    }

    /**
     * Steeping takes time: right after the principles and the item are in the trough, the item is still unfired.
     */
    public static void steepingDoesNotCompleteInstantly(GameTestHelper helper) {
        BlockPos trough = new BlockPos(3, 1, 3);
        CatchTroughBlockEntity be = troughWithUnfiredAxiom(helper, trough, STEEPING_PRINCIPLES);

        helper.runAfterDelay(2, () -> {
            helper.assertTrue(be.getItem().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get()), "The axiom should still be unfired right after steeping starts, got " + be.getItem());
            helper.assertTrue(be.getPrinciples().total() == STEEPING_PRINCIPLES, "The principles should not be consumed before the steeping completes, holds " + be.getPrinciples().total());
            helper.succeed();
        });
    }

    /**
     * Clay steeps with unguent into quickened clay, which the axiom tablet is crafted from.
     */
    public static void steepingClayMakesQuickenedClay(GameTestHelper helper) {
        int time = steepingTime(helper, "steeping/quickened_clay");
        BlockPos trough = new BlockPos(3, 1, 3);
        CatchTroughBlockEntity be = troughWith(helper, trough, new ItemStack(Items.CLAY_BALL), new PrincipleStack(PrincipleRegistry.UNGUENT, 25));

        helper.runAfterDelay(time + 5, () -> {
            helper.assertTrue(be.getItem().is(ItemRegistry.QUICKENED_CLAY.get()) && be.getItem().getCount() == 2, "The clay should steep into two quickened clay, got " + be.getItem());
            helper.assertTrue(be.getPrinciples().total() == 0, "The 25 mB of unguent should be consumed, holds " + be.getPrinciples().total());
            helper.succeed();
        });
    }

    /**
     * With sufficient principles the item steeps once the recipe time has passed, and exactly the recipe principles are consumed.
     */
    public static void steepingCompletesAfterRecipeTime(GameTestHelper helper) {
        int time = steepingTime(helper, "steeping/axiom_of_harvest_unfired");
        BlockPos trough = new BlockPos(3, 1, 3);
        CatchTroughBlockEntity be = troughWithUnfiredAxiom(helper, trough, STEEPING_PRINCIPLES + 20);

        helper.runAfterDelay(time / 2, () -> {
            helper.assertTrue(be.getItem().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get()), "The axiom should still be unfired halfway through the steeping, got " + be.getItem());
            helper.runAfterDelay(time - time / 2 + 5, () -> {
                helper.assertTrue(be.getItem().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()), "The axiom should be steeped after the recipe time, got " + be.getItem());
                helper.assertTrue(be.getPrinciples().total() == 20, "Only the 100 mB of the recipe should be consumed, holds " + be.getPrinciples().total());
                helper.succeed();
            });
        });
    }

    /**
     * Without sufficient principles the item does not steep, and steeping starts from zero once the principles are sufficient.
     */
    public static void steepingWaitsForPrinciples(GameTestHelper helper) {
        int time = steepingTime(helper, "steeping/axiom_of_harvest_unfired");
        BlockPos trough = new BlockPos(3, 1, 3);
        CatchTroughBlockEntity be = troughWithUnfiredAxiom(helper, trough, STEEPING_PRINCIPLES / 2);

        helper.runAfterDelay(time + 10, () -> {
            helper.assertTrue(be.getItem().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get()), "The axiom should stay unfired with insufficient principles, got " + be.getItem());
            be.insert(PrincipleContents.of(new PrincipleStack(PrincipleRegistry.LOAM, STEEPING_PRINCIPLES / 2)), false);
            helper.runAfterDelay(time / 2, () -> {
                helper.assertTrue(be.getItem().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get()), "Steeping should start from zero once the principles are sufficient, got " + be.getItem());
                helper.runAfterDelay(time / 2 + 5, () -> {
                    helper.assertTrue(be.getItem().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()), "The axiom should be steeped after the recipe time with sufficient principles, got " + be.getItem());
                    helper.succeed();
                });
            });
        });
    }

    /**
     * A steeping that yields several items leaves them as they are in the trough. Only a single item steeps, so principles put
     * in afterwards are not consumed by a recipe that would match the stack.
     */
    public static void steepingMultiItemResultWaitsForExtraction(GameTestHelper helper) {
        int time = steepingTime(helper, "steeping/quickened_clay");
        BlockPos trough = new BlockPos(3, 1, 3);
        CatchTroughBlockEntity be = troughWith(helper, trough, new ItemStack(Items.CLAY_BALL), new PrincipleStack(PrincipleRegistry.UNGUENT, 25));

        helper.runAfterDelay(time + 5, () -> {
            helper.assertTrue(be.getItem().is(ItemRegistry.QUICKENED_CLAY.get()) && be.getItem().getCount() == 2, "The clay should steep into two quickened clay, got " + be.getItem());
            be.insert(PrincipleContents.of(new PrincipleStack(PrincipleRegistry.UNGUENT, 25)), false);
            helper.runAfterDelay(time + 5, () -> {
                helper.assertTrue(be.getItem().is(ItemRegistry.QUICKENED_CLAY.get()) && be.getItem().getCount() == 2,
                        "The two quickened clay should stay in the trough, got " + be.getItem());
                helper.assertTrue(be.getPrinciples().get(PrincipleRegistry.UNGUENT) == 25,
                        "No recipe should consume the principles of a stack of two, holds " + be.getPrinciples().get(PrincipleRegistry.UNGUENT));
                helper.succeed();
            });
        });
    }

    /**
     * Removing the item resets the steeping, so the item needs the full recipe time once it is put back.
     */
    public static void steepingResetsWhenItemIsRemoved(GameTestHelper helper) {
        int time = steepingTime(helper, "steeping/axiom_of_harvest_unfired");
        BlockPos trough = new BlockPos(3, 1, 3);
        CatchTroughBlockEntity be = troughWithUnfiredAxiom(helper, trough, STEEPING_PRINCIPLES);
        Player player = helper.makeMockServerPlayerInLevel();

        helper.runAfterDelay(time / 2, () -> {
            be.useWithoutItem(player);
            helper.assertTrue(be.getItem().isEmpty(), "Taking the item out should empty the trough, holds " + be.getItem());
            player.getInventory().clearContent();
            holdAndUse(player, new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get()), be);

            // Without the reset the first steeping would have completed at the full recipe time.
            helper.runAfterDelay(time / 2 + 10, () -> {
                helper.assertTrue(be.getItem().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get()), "Steeping should restart from zero when the item is put back, got " + be.getItem());
                helper.runAfterDelay(time / 2, () -> {
                    helper.assertTrue(be.getItem().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()), "The axiom should be steeped after the full recipe time, got " + be.getItem());
                    helper.succeed();
                });
            });
        });
    }

    /**
     * A Wabet Basin that reloads while curing keeps its progress. Curing must not restart from zero because the recipe
     * was not remembered across the reload.
     */
    public static void curingSurvivesReload(GameTestHelper helper) {
        BlockPos basin = new BlockPos(3, 2, 3);
        helper.setBlock(basin.above(), Blocks.GLOWSTONE);
        helper.setBlock(basin, BlockRegistry.WABET_BASIN.get().defaultBlockState());
        CuringVesselBlockEntity be = (CuringVesselBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(basin));
        be.insert(PrincipleContents.of(new PrincipleStack(PrincipleRegistry.SILT, 25), new PrincipleStack(PrincipleRegistry.SAP, 25)), false);

        helper.runAfterDelay(50, () -> {
            int progress = be.getProgress();
            helper.assertTrue(progress >= 40, "The basin should be curing before the reload, progress is " + progress);

            CuringVesselBlockEntity reloaded = (CuringVesselBlockEntity) reloadedCopy(helper, be);
            helper.assertTrue(reloaded.getProgress() == progress, "The reloaded basin should load its progress, got " + reloaded.getProgress());
            for (int tick = 0; tick < 5; tick++) {
                CuringVesselBlockEntity.tick(helper.getLevel(), be.getBlockPos(), be.getBlockState(), reloaded);
            }
            helper.assertTrue(reloaded.getProgress() == progress + 5, "The reloaded basin should continue curing, progress is " + reloaded.getProgress() + " after " + progress);
            helper.succeed();
        });
    }

    /**
     * A catch trough that reloads while steeping keeps the steeping progress, so the item completes at the recipe time.
     */
    public static void steepingSurvivesReload(GameTestHelper helper) {
        int time = steepingTime(helper, "steeping/axiom_of_harvest_unfired");
        BlockPos trough = new BlockPos(3, 1, 3);
        CatchTroughBlockEntity be = troughWithUnfiredAxiom(helper, trough, STEEPING_PRINCIPLES);

        helper.runAfterDelay(time / 2, () -> {
            CatchTroughBlockEntity reloaded = (CatchTroughBlockEntity) reloadedCopy(helper, be);
            for (int tick = 0; tick < time - time / 2 + 5; tick++) {
                CatchTroughBlockEntity.tick(helper.getLevel(), be.getBlockPos(), be.getBlockState(), reloaded);
            }
            helper.assertTrue(reloaded.getItem().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()),
                    "The reloaded trough should finish steeping at the recipe time, got " + reloaded.getItem());
            helper.succeed();
        });
    }

    /**
     * The drying slab dries its input into the output, which needs light above the slab.
     */
    public static void dryingSlabDriesIntoOutput(GameTestHelper helper) {
        int time = dryingTime(helper);
        BlockPos slab = new BlockPos(3, 2, 3);
        DryingSlabBlockEntity be = dryingSlabWithInput(helper, slab, new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()));

        helper.runAfterDelay(time + 10, () -> {
            helper.assertTrue(be.getOutput().is(ItemRegistry.AXIOM_OF_HARVEST.get()), "The slab should dry the item into the axiom, output is " + be.getOutput());
            helper.assertTrue(be.getInput().isEmpty(), "The dried item should leave the input, holds " + be.getInput());
            helper.succeed();
        });
    }

    /**
     * Axioms stack to one, so once the output holds an axiom the next dried item cannot merge into it. The slab then stalls
     * and keeps the next item in its input.
     */
    public static void dryingSlabStallsWhenOutputIsFull(GameTestHelper helper) {
        int time = dryingTime(helper);
        BlockPos slab = new BlockPos(3, 2, 3);
        DryingSlabBlockEntity be = dryingSlabWithInput(helper, slab, new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()));

        helper.runAfterDelay(time + 10, () -> {
            helper.assertTrue(be.getOutput().is(ItemRegistry.AXIOM_OF_HARVEST.get()) && be.getOutput().getCount() == 1, "The first item should be dried into one axiom, output is " + be.getOutput());
            holdAndUse(helper.makeMockServerPlayerInLevel(), new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()), be);

            helper.runAfterDelay(time + 10, () -> {
                helper.assertTrue(be.getInput().getCount() == 1, "The second item should not be consumed while the output is full, input is " + be.getInput());
                helper.assertTrue(be.getOutput().getCount() == 1, "The output should stay at one axiom, output is " + be.getOutput());
                helper.succeed();
            });
        });
    }

    /**
     * A hopper inserts a drying input into the empty input of the drying slab, and only extracts the dried output. The input
     * cannot be extracted from outside, and a second input of the same item stacks onto the input while the first one dries.
     */
    public static void dryingSlabHopperInsertsInputAndExtractsOutput(GameTestHelper helper) {
        int time = dryingTime(helper);
        BlockPos slab = new BlockPos(3, 2, 3);
        helper.setBlock(slab, BlockRegistry.DRYING_SLAB.get().defaultBlockState());
        helper.setBlock(slab.above(), Blocks.GLOWSTONE);
        DryingSlabBlockEntity be = (DryingSlabBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(slab));
        ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(slab), Direction.UP);
        helper.assertTrue(handler != null, "The drying slab should expose an item capability");
        ItemStack steeped = new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get());

        int refused;
        int accepted;
        int extractedInput;
        try (Transaction transaction = Transaction.open(null)) {
            refused = handler.insert(ItemResource.of(new ItemStack(Items.DIRT)), 1, transaction);
            accepted = handler.insert(ItemResource.of(steeped), 1, transaction);
            transaction.commit();
        }
        try (Transaction transaction = Transaction.open(null)) {
            extractedInput = handler.extract(ItemResource.of(steeped), 1, transaction);
            transaction.commit();
        }
        helper.assertTrue(refused == 0, "The slab should refuse items that do not dry, accepted " + refused);
        helper.assertTrue(accepted == 1, "The slab should accept a drying input into its empty input, accepted " + accepted);
        helper.assertTrue(extractedInput == 0, "The input should not be extractable from outside, extracted " + extractedInput);
        helper.assertTrue(be.getInput().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()), "The input should hold the inserted item, got " + be.getInput());

        int topUp;
        try (Transaction transaction = Transaction.open(null)) {
            topUp = handler.insert(ItemResource.of(steeped), 1, transaction);
            transaction.commit();
        }
        helper.assertTrue(topUp == 1 && be.getInput().getCount() == 2,
                "The slab should stack the same input on the one that dries, accepted " + topUp + ", input is " + be.getInput());

        helper.runAfterDelay(time + 10, () -> {
            helper.assertTrue(be.getOutput().is(ItemRegistry.AXIOM_OF_HARVEST.get()), "The input should dry into the output, got " + be.getOutput());
            int extractedOutput;
            try (Transaction transaction = Transaction.open(null)) {
                extractedOutput = handler.extract(ItemResource.of(new ItemStack(ItemRegistry.AXIOM_OF_HARVEST.get())), 1, transaction);
                transaction.commit();
            }
            helper.assertTrue(extractedOutput == 1, "A hopper should extract the finished axiom, extracted " + extractedOutput);
            helper.assertTrue(be.getOutput().isEmpty(), "The output should be empty after the extraction, got " + be.getOutput());
            helper.succeed();
        });
    }

    /**
     * Topping up a drying input with the same item, by a player and by a hopper, keeps the progress of the item that dries:
     * the first item still finishes at the drying time, and the stacked items wait behind it.
     */
    public static void dryingSlabTopUpKeepsProgress(GameTestHelper helper) {
        int time = dryingTime(helper);
        BlockPos slab = new BlockPos(3, 2, 3);
        helper.setBlock(slab, BlockRegistry.DRYING_SLAB.get().defaultBlockState());
        helper.setBlock(slab.above(), Blocks.GLOWSTONE);
        DryingSlabBlockEntity be = (DryingSlabBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(slab));
        Player player = helper.makeMockServerPlayerInLevel();
        ItemResource steeped = ItemResource.of(new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()));
        ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(slab), Direction.UP);
        helper.assertTrue(handler != null, "The drying slab should expose an item capability");
        holdAndUse(player, new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()), be);

        helper.runAfterDelay(time / 2, () -> {
            float before = be.getProgress();
            helper.assertTrue(before > 0, "The first item should be drying, progress is " + before);

            holdAndUse(player, new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get(), 2), be);
            helper.assertTrue(be.getInput().getCount() == 3, "A player should top up the input, input is " + be.getInput());
            helper.assertTrue(be.getProgress() >= before, "Topping up should not reset the progress, was " + before + " is " + be.getProgress());

            int hopperTopUp;
            try (Transaction transaction = Transaction.open(null)) {
                hopperTopUp = handler.insert(steeped, 1, transaction);
                transaction.commit();
            }
            helper.assertTrue(hopperTopUp == 1 && be.getInput().getCount() == 4,
                    "A hopper should top up the input, accepted " + hopperTopUp + ", input is " + be.getInput());
            helper.assertTrue(be.getProgress() >= before, "A hopper top-up should not reset the progress, is " + be.getProgress());

            helper.runAfterDelay(time / 2 + 10, () -> {
                // The first item finishes at the drying time as if nothing was stacked, the others wait behind it.
                helper.assertTrue(be.getOutput().is(ItemRegistry.AXIOM_OF_HARVEST.get()) && be.getOutput().getCount() == 1,
                        "The first item should dry at its own time, output is " + be.getOutput());
                helper.assertTrue(be.getInput().getCount() == 3, "Three items should wait in the input, input is " + be.getInput());
                helper.succeed();
            });
        });
    }

    /**
     * Once the storage of a slab is drained through an open spout, the drain stops: the trough receives all 50 mB and nothing
     * moves on afterwards.
     */
    public static void drainStopsWhenStorageIsEmpty(GameTestHelper helper) {
        BlockPos slab = new BlockPos(3, 2, 3);
        BlockPos spout = new BlockPos(3, 2, 2);
        BlockPos trough = new BlockPos(3, 1, 2);
        helper.setBlock(slab, BlockRegistry.LIBATION_SLAB.get().defaultBlockState());
        helper.setBlock(spout, BlockRegistry.LIBATION_SPOUT.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        helper.setBlock(trough, BlockRegistry.CATCH_TROUGH.get().defaultBlockState());
        LibationSlabBlockEntity be = stockSlab(helper, slab);
        CatchTroughBlockEntity troughBe = (CatchTroughBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(trough));

        // The spout opens once the storage is full, as in slabDrainsOnlyThroughOpenSpout: neighbour updates close an unpowered spout.
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(be.getStored().total() == 50, "The slab should store the 50 mB of ash while the spout is closed, stored " + be.getStored().total());
            helper.setBlock(spout, BlockRegistry.LIBATION_SPOUT.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                    .setValue(BlockStateProperties.OPEN, true));
            helper.runAfterDelay(120, () -> {
                helper.assertBlockProperty(spout, BlockStateProperties.OPEN, true);
                helper.assertTrue(be.getStored().isEmpty(), "The drain should empty the storage, stored " + be.getStored().total());
                helper.assertTrue(troughBe.getPrinciples().total() == 50, "The trough should receive all 50 mB, holds " + troughBe.getPrinciples().total());
                helper.runAfterDelay(60, () -> {
                    helper.assertTrue(be.getStored().isEmpty(), "A stopped drain should not refill the storage, stored " + be.getStored().total());
                    helper.assertTrue(troughBe.getPrinciples().total() == 50, "A stopped drain should not move principles, trough holds " + troughBe.getPrinciples().total());
                    helper.succeed();
                });
            });
        });
    }

    /**
     * A requested sync waits until the sync interval has passed since the last sync, and is flushed on the tick that ends it.
     */
    public static void syncThrottleFlushesRequestsAtTheInterval(GameTestHelper helper) {
        BlockPos trough = new BlockPos(3, 1, 3);
        helper.setBlock(trough, BlockRegistry.CATCH_TROUGH.get().defaultBlockState());
        CatchTroughBlockEntity be = (CatchTroughBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(trough));
        SyncThrottle throttle = new SyncThrottle(be);

        throttle.syncNow();
        throttle.requestSync();
        helper.assertTrue(throttle.pending(), "A requested sync should be pending");
        for (int tick = 1; tick < SyncThrottle.SYNC_INTERVAL; tick++) {
            throttle.tick();
        }
        helper.assertTrue(throttle.pending(), "A requested sync should wait for the sync interval");
        throttle.tick();
        helper.assertTrue(!throttle.pending(), "A requested sync should be flushed once the sync interval passed");
        helper.succeed();
    }

    /**
     * The displayed amount of a liquid snaps when it first shows and when it empties or refills, and otherwise eases towards the
     * synced amount.
     */
    public static void displayedAmountEasesTowardsSyncedAmount(GameTestHelper helper) {
        AmountSmoother smoother = new AmountSmoother();
        helper.assertTrue(smoother.update(100, 0, 0.0F) == 100.0F, "The first amount should snap to the synced amount");
        float eased = smoother.update(200, 10, 0.0F);
        helper.assertTrue(eased > 100.0F && eased < 200.0F, "The amount should ease towards the synced amount, got " + eased);
        helper.assertTrue(smoother.update(0, 11, 0.0F) == 0.0F, "An amount that empties should snap to zero");
        helper.assertTrue(smoother.update(50, 12, 0.0F) == 50.0F, "An amount that refills should snap, got " + smoother.update(50, 12, 0.0F));
        helper.succeed();
    }

    /**
     * A hopper fills the catch trough through its block capabilities, with principles and a steepable item and no player. The
     * trough steeps the item, and a hopper then takes the finished item out of it.
     */
    public static void catchTroughHopperSteepsAndReleasesItemWithoutPlayer(GameTestHelper helper) {
        int time = steepingTime(helper, "steeping/axiom_of_harvest_unfired");
        BlockPos trough = new BlockPos(3, 1, 3);
        helper.setBlock(trough, BlockRegistry.CATCH_TROUGH.get().defaultBlockState());
        CatchTroughBlockEntity be = (CatchTroughBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(trough));
        ResourceHandler<ItemResource> items = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(trough), Direction.UP);
        ResourceHandler<FluidResource> fluids = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(trough), Direction.UP);
        helper.assertTrue(items != null && fluids != null, "The trough should expose item and fluid capabilities");

        try (Transaction tx = Transaction.open(null)) {
            int inserted = fluids.insert(FluidRegistry.resourceFor(PrincipleRegistry.LOAM), STEEPING_PRINCIPLES, tx);
            helper.assertTrue(inserted == STEEPING_PRINCIPLES, "The trough should accept the principles from a pipe, accepted " + inserted);
            tx.commit();
        }
        try (Transaction tx = Transaction.open(null)) {
            int inserted = items.insert(ItemResource.of(new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get())), 1, tx);
            helper.assertTrue(inserted == 1, "The trough should accept a steepable item from a hopper, accepted " + inserted);
            tx.commit();
        }

        helper.runAfterDelay(time + 5, () -> {
            helper.assertTrue(be.getItem().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()),
                    "The trough should steep the item without a player, got " + be.getItem());
            helper.assertTrue(be.getPrinciples().total() == 0, "The recipe principles should be consumed, holds " + be.getPrinciples().total());

            int extracted;
            try (Transaction tx = Transaction.open(null)) {
                extracted = items.extract(ItemResource.of(new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get())), 1, tx);
                tx.commit();
            }
            helper.assertTrue(extracted == 1, "A hopper should take the steeped item out, extracted " + extracted);
            helper.assertTrue(be.getItem().isEmpty(), "The trough should be empty after the extraction, holds " + be.getItem());
            helper.succeed();
        });
    }

    /**
     * A player and a hopper follow the same rules for the catch trough: an unsteepable item is refused by both, and a steepable
     * item fits only once, while the trough holds one.
     */
    public static void catchTroughRefusesUnsteepableAndHoldsOneForPlayerAndHopper(GameTestHelper helper) {
        BlockPos trough = new BlockPos(3, 1, 3);
        helper.setBlock(trough, BlockRegistry.CATCH_TROUGH.get().defaultBlockState());
        CatchTroughBlockEntity be = (CatchTroughBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(trough));
        ResourceHandler<ItemResource> items = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(trough), Direction.UP);
        helper.assertTrue(items != null, "The trough should expose an item capability");
        Player player = helper.makeMockServerPlayerInLevel();

        int refused;
        try (Transaction tx = Transaction.open(null)) {
            refused = items.insert(ItemResource.of(new ItemStack(Items.DIAMOND)), 1, tx);
        }
        helper.assertTrue(refused == 0, "A hopper should be refused an unsteepable item, accepted " + refused);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
        InteractionResult refusedByHand = be.useItemOn(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(refusedByHand == InteractionResult.PASS && player.getMainHandItem().is(Items.DIAMOND),
                "A player should be refused an unsteepable item and keep it, got " + refusedByHand);
        helper.assertTrue(be.getItem().isEmpty(), "The trough should stay empty, holds " + be.getItem());

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get(), 2));
        InteractionResult accepted = be.useItemOn(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(accepted.consumesAction() && be.getItem().is(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get()) && be.getItem().getCount() == 1,
                "A player should put one steepable item into the trough, holds " + be.getItem());
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "The rest of the stack should stay in hand, holds " + player.getMainHandItem());

        InteractionResult full = be.useItemOn(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(full == InteractionResult.PASS, "The trough should refuse a second item while it holds one, got " + full);
        helper.succeed();
    }

    /**
     * The libation slab takes washable items from pipes but never gives them back to them. Players take the stack back, and
     * the slab refuses items that are not washable.
     */
    public static void libationSlabHopperInsertsWashableItemOnly(GameTestHelper helper) {
        BlockPos slab = new BlockPos(3, 2, 3);
        helper.setBlock(slab, BlockRegistry.LIBATION_SLAB.get().defaultBlockState());
        LibationSlabBlockEntity be = (LibationSlabBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(slab));
        ResourceHandler<ItemResource> items = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(slab), Direction.UP);
        helper.assertTrue(items != null, "The slab should expose an item capability");

        int refused;
        try (Transaction tx = Transaction.open(null)) {
            refused = items.insert(ItemResource.of(new ItemStack(Items.DIAMOND)), 1, tx);
        }
        helper.assertTrue(refused == 0, "The slab should refuse an item that is not washable, accepted " + refused);

        int accepted;
        try (Transaction tx = Transaction.open(null)) {
            accepted = items.insert(ItemResource.of(new ItemStack(Items.BONE)), 1, tx);
            tx.commit();
        }
        helper.assertTrue(accepted == 1, "A hopper should insert a washable item into the slab, accepted " + accepted);

        int extracted;
        try (Transaction tx = Transaction.open(null)) {
            extracted = items.extract(ItemResource.of(new ItemStack(Items.BONE)), 1, tx);
        }
        helper.assertTrue(extracted == 0, "A hopper should not take the washable item back out, extracted " + extracted);

        Player player = helper.makeMockServerPlayerInLevel();
        InteractionResult taken = be.useWithoutItem(player);
        helper.assertTrue(taken.consumesAction() && be.getStack().isEmpty(), "A player should take the stack back out of the slab");
        helper.assertTrue(player.getInventory().countItem(Items.BONE) == 1, "The taken stack should land in the player's inventory");
        helper.succeed();
    }

    /**
     * Places a catch trough with the given amount of loam and an unfired axiom of harvest, which steeps with the loam.
     */
    private static CatchTroughBlockEntity troughWithUnfiredAxiom(GameTestHelper helper, BlockPos pos, int loam) {
        return troughWith(helper, pos, new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get()), new PrincipleStack(PrincipleRegistry.LOAM, loam));
    }

    /**
     * Places a catch trough with the principle, then puts the item into it.
     */
    private static CatchTroughBlockEntity troughWith(GameTestHelper helper, BlockPos pos, ItemStack item, PrincipleStack principle) {
        helper.setBlock(pos, BlockRegistry.CATCH_TROUGH.get().defaultBlockState());
        CatchTroughBlockEntity trough = (CatchTroughBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        trough.insert(PrincipleContents.of(principle), false);
        holdAndUse(helper.makeMockServerPlayerInLevel(), item, trough);
        return trough;
    }

    /**
     * Puts the stack into the main hand of the player and uses the trough with it, the way a click does.
     */
    private static void holdAndUse(Player player, ItemStack stack, CatchTroughBlockEntity trough) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
        trough.useItemOn(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
    }

    /**
     * Places a drying slab with light above it, the glowstone gives full light, and puts the input into it.
     */
    private static DryingSlabBlockEntity dryingSlabWithInput(GameTestHelper helper, BlockPos pos, ItemStack input) {
        helper.setBlock(pos, BlockRegistry.DRYING_SLAB.get().defaultBlockState());
        helper.setBlock(pos.above(), Blocks.GLOWSTONE);
        DryingSlabBlockEntity slab = (DryingSlabBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        holdAndUse(helper.makeMockServerPlayerInLevel(), input, slab);
        return slab;
    }

    /**
     * Puts the stack into the main hand of the player and uses the drying slab with it, the way a click does.
     */
    private static void holdAndUse(Player player, ItemStack stack, DryingSlabBlockEntity slab) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
        slab.useItemOn(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
    }

    /**
     * Simulates a reload: the block entity is saved and loaded into a new instance, the way a chunk load does it. The copy
     * is not placed in the world, the test ticks it.
     */
    private static BlockEntity reloadedCopy(GameTestHelper helper, BlockEntity be) {
        ServerLevel level = helper.getLevel();
        CompoundTag saved = be.saveWithFullMetadata(level.registryAccess());
        BlockEntity copy = BlockEntity.loadStatic(be.getBlockPos(), be.getBlockState(), saved, level.registryAccess());
        helper.assertTrue(copy != null, "The saved block entity should load again");
        copy.setLevel(level);
        return copy;
    }

    /**
     * @param path the recipe path, e.g. steeping/axiom_of_harvest_unfired.
     */
    private static int steepingTime(GameTestHelper helper, String path) {
        SteepingRecipe recipe = (SteepingRecipe) helper.getLevel().getServer().getRecipeManager()
                .byKey(ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc(path))).orElseThrow().value();
        return recipe.time();
    }

    private static int dryingTime(GameTestHelper helper) {
        DryingRecipe recipe = (DryingRecipe) helper.getLevel().getServer().getRecipeManager()
                .byKey(ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc("drying/axiom_of_harvest"))).orElseThrow().value();
        return recipe.time();
    }

    /**
     * Fills the slab with water from a bucket and puts a bone in it, which dissolves into 50 mB of ash.
     */
    private static LibationSlabBlockEntity stockSlab(GameTestHelper helper, BlockPos pos) {
        LibationSlabBlockEntity slab = (LibationSlabBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        Player player = helper.makeMockServerPlayerInLevel();
        ItemStack bucket = new ItemStack(Items.WATER_BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
        slab.useItemOn(bucket, player, InteractionHand.MAIN_HAND);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
        slab.useItemOn(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(slab.getWater() == LibationSlabBlockEntity.WATER_CAPACITY, "A water bucket should fill the slab, got " + slab.getWater());
        return slab;
    }

    /**
     * All principle blocks expose {@link Capabilities.Fluid#BLOCK}: vessels and troughs support transactional
     * principle fluid insert/extract with abort rollback (and reject water), while the libation slab accepts water
     * and exposes principles as extract-only.
     */
    public static void principleBlockCapabilitiesInsertAndExtract(GameTestHelper helper) {
        BlockPos basinPos = new BlockPos(2, 2, 2);
        BlockPos jarPos = new BlockPos(3, 2, 2);
        BlockPos troughPos = new BlockPos(4, 2, 2);
        BlockPos slabPos = new BlockPos(5, 2, 2);

        helper.setBlock(basinPos, BlockRegistry.WABET_BASIN.get().defaultBlockState());
        helper.setBlock(jarPos, BlockRegistry.STEEPING_JAR.get().defaultBlockState());
        helper.setBlock(troughPos, BlockRegistry.CATCH_TROUGH.get().defaultBlockState());
        helper.setBlock(slabPos, BlockRegistry.LIBATION_SLAB.get().defaultBlockState());

        FluidResource loam = FluidRegistry.resourceFor(PrincipleRegistry.LOAM);
        FluidResource water = FluidResource.of(Fluids.WATER);
        for (BlockPos rel : List.of(basinPos, jarPos, troughPos)) {
            ResourceHandler<FluidResource> handler = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(rel), Direction.UP);
            helper.assertTrue(handler != null, "Expected fluid capability at " + rel);

            // Water must be rejected by vessels and troughs.
            try (Transaction tx = Transaction.open(null)) {
                int waterInserted = handler.insert(water, 100, tx);
                helper.assertTrue(waterInserted == 0, "Vessels and troughs should reject water, got " + waterInserted + " at " + rel);
            }

            // Aborted transaction must leave state untouched.
            try (Transaction tx = Transaction.open(null)) {
                int inserted = handler.insert(loam, 100, tx);
                helper.assertTrue(inserted == 100, "Expected 100 mB inserted in simulation at " + rel);
            }
            try (Transaction tx = Transaction.open(null)) {
                int extracted = handler.extract(loam, 100, tx);
                helper.assertTrue(extracted == 0, "Aborted insert should have rolled back at " + rel);
            }

            try (Transaction tx = Transaction.open(null)) {
                int inserted = handler.insert(loam, 150, tx);
                helper.assertTrue(inserted == 150, "Expected 150 mB committed at " + rel);
                tx.commit();
            }
            try (Transaction tx = Transaction.open(null)) {
                int extracted = handler.extract(loam, 50, tx);
                helper.assertTrue(extracted == 50, "Expected 50 mB extracted at " + rel);
                tx.commit();
            }
        }

        // Libation slab accepts water insertion, refuses principle insertion, and allows principle extraction.
        ResourceHandler<FluidResource> slabHandler = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(slabPos), Direction.UP);
        helper.assertTrue(slabHandler != null, "Expected fluid capability on libation slab");
        try (Transaction tx = Transaction.open(null)) {
            int inserted = slabHandler.insert(loam, 50, tx);
            helper.assertTrue(inserted == 0, "Libation slab should refuse principle insertion, got " + inserted);
        }
        LibationSlabBlockEntity slabBe = stockSlab(helper, slabPos);
        helper.runAfterDelay(120, () -> {
            helper.assertTrue(slabBe.getStored().total() == 50, "Expected 50 mB ash in slab");
            try (Transaction tx = Transaction.open(null)) {
                int extractedWater = slabHandler.extract(water, 100, tx);
                helper.assertTrue(extractedWater == 0, "Libation slab water should be insert-only, extracted " + extractedWater);
                int extracted = slabHandler.extract(FluidRegistry.resourceFor(PrincipleRegistry.ASH), 50, tx);
                helper.assertTrue(extracted == 50, "Expected 50 mB ash extracted from slab, got " + extracted);
                tx.commit();
            }
            helper.assertTrue(slabBe.getStored().isEmpty(), "Slab should be empty after extraction");
            helper.succeed();
        });
    }

    /**
     * The principle slots of a principle block share one capacity: a second principle only fits into what the first one leaves,
     * a rolled back insert leaves nothing behind, and extracting inside a transaction frees room for the same transaction.
     */
    public static void principleStorageSharesCapacityBetweenPrinciples(GameTestHelper helper) {
        BlockPos troughPos = new BlockPos(3, 2, 3);
        helper.setBlock(troughPos, BlockRegistry.CATCH_TROUGH.get().defaultBlockState());
        CatchTroughBlockEntity trough = (CatchTroughBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(troughPos));
        ResourceHandler<FluidResource> handler = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(troughPos), Direction.UP);
        helper.assertTrue(handler != null, "The trough should expose a fluid capability");
        FluidResource loam = FluidRegistry.resourceFor(PrincipleRegistry.LOAM);
        FluidResource silt = FluidRegistry.resourceFor(PrincipleRegistry.SILT);

        try (Transaction tx = Transaction.open(null)) {
            helper.assertTrue(handler.insert(loam, 400, tx) == 400, "The trough should take 400 mB of loam");
            tx.commit();
        }
        try (Transaction tx = Transaction.open(null)) {
            int accepted = handler.insert(silt, 200, tx);
            helper.assertTrue(accepted == 100, "Silt should only fit into the 100 mB the loam leaves, accepted " + accepted);
            tx.commit();
        }
        helper.assertTrue(trough.getPrinciples().total() == CatchTroughBlockEntity.CAPACITY, "The trough should be full, holds " + trough.getPrinciples().total());

        try (Transaction tx = Transaction.open(null)) {
            helper.assertTrue(handler.insert(silt, 100, tx) == 0, "A full trough should refuse more principles");
            // Not committed: the insert must leave nothing behind.
        }
        helper.assertTrue(trough.getPrinciples().total() == CatchTroughBlockEntity.CAPACITY, "A rolled back insert should not change the trough, holds " + trough.getPrinciples().total());

        try (Transaction tx = Transaction.open(null)) {
            helper.assertTrue(handler.extract(loam, 100, tx) == 100, "Loam should come out of the trough");
            int accepted = handler.insert(silt, 150, tx);
            helper.assertTrue(accepted == 100, "The room extracted in the same transaction should be usable, accepted " + accepted);
            tx.commit();
        }
        helper.assertTrue(trough.getPrinciples().get(PrincipleRegistry.LOAM) == 300 && trough.getPrinciples().get(PrincipleRegistry.SILT) == 200,
                "The trough should hold 300 mB loam and 200 mB silt, holds " + trough.getPrinciples());
        helper.succeed();
    }


    /**
     * Clay phials hold up to 250 mB, fill/pour like buckets, stack when holding the exact same principle, and top up on crouch-click.
     */
    public static void clayPhialFillsPoursAndStacks(GameTestHelper helper) {
        BlockPos basinPos = new BlockPos(3, 2, 3);
        BlockPos troughPos = new BlockPos(4, 2, 3);
        helper.setBlock(basinPos, BlockRegistry.WABET_BASIN.get().defaultBlockState());
        helper.setBlock(troughPos, BlockRegistry.CATCH_TROUGH.get().defaultBlockState());

        CuringVesselBlockEntity basin = (CuringVesselBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(basinPos));
        CatchTroughBlockEntity trough = (CatchTroughBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(troughPos));
        basin.insert(PrincipleContents.of(new PrincipleStack(PrincipleRegistry.LOAM, 600)), false);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.CLAY_PHIAL.get(), 4));

        // Draw twice from the basin (250 mB each) -> 2 empty phials remain in hand, 2 full Loam phials stacked in inventory.
        InteractionResult firstDraw = useHeldOn(helper, player, basinPos);
        InteractionResult secondDraw = useHeldOn(helper, player, basinPos);
        helper.assertTrue(firstDraw.consumesAction() && secondDraw.consumesAction(), "Drawing from basin should succeed");
        helper.assertTrue(basin.getPrinciples().get(PrincipleRegistry.LOAM) == 100, "Basin should have 100 mB Loam left, got " + basin.getPrinciples().get(PrincipleRegistry.LOAM));
        helper.assertTrue(player.getMainHandItem().getCount() == 2, "Main hand should have 2 empty phials left, got " + player.getMainHandItem().getCount());

        ItemStack fullStack = findPhialStack(player, stack -> !ClayPhialItem.isEmpty(stack));
        helper.assertTrue(!fullStack.isEmpty() && fullStack.getCount() == 2, "Full Loam phials should stack together (expected count 2, got " + fullStack.getCount() + ")");

        // Pour one full phial into the trough using the stacked full phials.
        player.setItemInHand(InteractionHand.MAIN_HAND, fullStack.copy());
        InteractionResult pourResult = useHeldOn(helper, player, troughPos);
        helper.assertTrue(pourResult.consumesAction(), "Pouring into trough should succeed");
        helper.assertTrue(trough.getPrinciples().get(PrincipleRegistry.LOAM) == 250, "Trough should hold 250 mB Loam");
        helper.assertTrue(player.getMainHandItem().getCount() == 1 && !ClayPhialItem.isEmpty(player.getMainHandItem()), "One full phial should remain in main hand");

        // Partial phial (100 mB) tops up from basin on crouch-click.
        player.setItemInHand(InteractionHand.MAIN_HAND, ClayPhialItem.createFilled(PrincipleRegistry.LOAM, 100));
        player.setShiftKeyDown(true);
        InteractionResult topUp = useHeldOn(helper, player, basinPos);
        player.setShiftKeyDown(false);
        helper.assertTrue(topUp.consumesAction(), "Crouch-click top up should succeed");
        PrincipleStack stored = ClayPhialItem.getStored(player.getMainHandItem());
        helper.assertTrue(stored != null && stored.amount() == 200, "Partial phial should top up to 200 mB, got " + stored);
        helper.assertTrue(basin.getPrinciples().isEmpty(), "Basin should be empty after top up");
        helper.succeed();
    }

    /**
     * An empty clay phial draws only the HUD-selected principle from a mixed container.
     */
    public static void clayPhialSeparatesMixedPrinciples(GameTestHelper helper) {
        BlockPos troughPos = new BlockPos(3, 2, 3);
        helper.setBlock(troughPos, BlockRegistry.CATCH_TROUGH.get().defaultBlockState());
        CatchTroughBlockEntity trough = (CatchTroughBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(troughPos));
        trough.insert(PrincipleContents.of(
                new PrincipleStack(PrincipleRegistry.SILT, 100),
                new PrincipleStack(PrincipleRegistry.LOAM, 100)), false);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.CLAY_PHIAL.get()));
        PrincipleSelection.set(player, PrincipleRegistry.LOAM);

        InteractionResult result = useHeldOn(helper, player, troughPos);
        PrincipleSelection.set(player, null);

        helper.assertTrue(result.consumesAction(), "Phial draw should succeed");
        PrincipleStack drawn = ClayPhialItem.getStored(player.getMainHandItem());
        helper.assertTrue(drawn != null && drawn.principle().value().equals(PrincipleRegistry.LOAM.get()) && drawn.amount() == 100,
                "Phial should hold 100 mB Loam, got " + drawn);
        helper.assertTrue(trough.getPrinciples().get(PrincipleRegistry.SILT) == 100, "Trough should still hold 100 mB Silt");
        helper.assertTrue(trough.getPrinciples().get(PrincipleRegistry.LOAM) == 0, "Trough should have 0 mB Loam left");
        helper.succeed();
    }

    /**
     * Natron sponge blots only the selected principle on normal right-click, and purges all principles on crouch-right-click.
     */
    public static void natronSpongeBlotsSelectedAndPurgesAll(GameTestHelper helper) {
        BlockPos troughPos = new BlockPos(3, 2, 3);
        helper.setBlock(troughPos, BlockRegistry.CATCH_TROUGH.get().defaultBlockState());
        CatchTroughBlockEntity trough = (CatchTroughBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(troughPos));
        trough.insert(PrincipleContents.of(
                new PrincipleStack(PrincipleRegistry.SILT, 100),
                new PrincipleStack(PrincipleRegistry.LOAM, 100)), false);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.NATRON_SPONGE.get()));
        PrincipleSelection.set(player, PrincipleRegistry.SILT);

        InteractionResult blotResult = useHeldOn(helper, player, troughPos);
        PrincipleSelection.set(player, null);
        helper.assertTrue(blotResult.consumesAction(), "Blotting should succeed");
        helper.assertTrue(trough.getPrinciples().get(PrincipleRegistry.SILT) == 0, "Silt should be blotted out");
        helper.assertTrue(trough.getPrinciples().get(PrincipleRegistry.LOAM) == 100, "Loam should remain untouched");

        trough.insert(PrincipleContents.of(new PrincipleStack(PrincipleRegistry.ASH, 50)), false);
        player.setShiftKeyDown(true);
        InteractionResult purgeResult = useHeldOn(helper, player, troughPos);
        player.setShiftKeyDown(false);
        helper.assertTrue(purgeResult.consumesAction(), "Crouch-click purge should succeed");
        helper.assertTrue(trough.getPrinciples().isEmpty(), "Trough should be completely empty after purge, holds " + trough.getPrinciples());
        helper.succeed();
    }

    /**
     * Crafting three clay balls around quickened clay yields eight clay phials.
     */
    public static void clayPhialRecipeProducesEight(GameTestHelper helper) {
        CraftingInput input = CraftingInput.of(3, 2, List.of(
                new ItemStack(Items.CLAY_BALL),
                new ItemStack(ItemRegistry.QUICKENED_CLAY.get()),
                new ItemStack(Items.CLAY_BALL),
                ItemStack.EMPTY,
                new ItemStack(Items.CLAY_BALL),
                ItemStack.EMPTY
        ));
        var recipe = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        helper.assertTrue(recipe.isPresent(), "Clay phial crafting recipe should match 3 clay balls + 1 quickened clay");
        ItemStack output = recipe.get().value().assemble(input);
        helper.assertTrue(output.is(ItemRegistry.CLAY_PHIAL.get()) && output.getCount() == 8,
                "Expected 8 clay phials, got " + output);
        helper.succeed();
    }

    /**
     * Crafting one quickened clay with three sand yields eight natron sponges.
     */
    public static void natronSpongeRecipeProducesEight(GameTestHelper helper) {
        CraftingInput input = CraftingInput.of(2, 2, List.of(
                new ItemStack(ItemRegistry.QUICKENED_CLAY.get()),
                new ItemStack(Items.SAND),
                new ItemStack(Items.SAND),
                new ItemStack(Items.SAND)
        ));
        var recipe = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        helper.assertTrue(recipe.isPresent(), "Natron sponge crafting recipe should match 1 quickened clay + 3 sand");
        ItemStack output = recipe.get().value().assemble(input);
        helper.assertTrue(output.is(ItemRegistry.NATRON_SPONGE.get()) && output.getCount() == 8,
                "Expected 8 natron sponges, got " + output);
        helper.succeed();
    }

    /**
     * The slab leaves clay phials to their own use, so an empty phial draws the principles stored in the slab. The slab itself
     * does not consume the click.
     */
    public static void libationSlabLetsPhialsDrawStoredPrinciples(GameTestHelper helper) {
        BlockPos slab = new BlockPos(3, 2, 3);
        helper.setBlock(slab, BlockRegistry.LIBATION_SLAB.get().defaultBlockState());
        LibationSlabBlockEntity be = stockSlab(helper, slab);

        helper.runAfterDelay(120, () -> {
            helper.assertTrue(be.getStored().total() == 50, "The slab should store 50 mB of ash, stored " + be.getStored().total());
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.CLAY_PHIAL.get()));

            InteractionResult slabResult = be.useItemOn(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(slabResult == InteractionResult.PASS, "The slab should leave the phial to its own use, got " + slabResult);

            InteractionResult drawn = useHeldOn(helper, player, slab);
            PrincipleStack stored = ClayPhialItem.getStored(player.getMainHandItem());
            helper.assertTrue(drawn.consumesAction() && stored != null && stored.amount() == 50,
                    "An empty phial should draw the 50 mB of ash from the slab, holds " + stored);
            helper.assertTrue(be.getStored().isEmpty(), "The slab should give up the drawn principles, stored " + be.getStored().total());
            helper.succeed();
        });
    }

    /**
     * A recipe cache reuses its recipe for the recipe manager it was looked up with, and looks it up again for another one, as
     * a datapack reload brings. Missing recipes are cached the same way.
     */
    public static void recipeCacheLooksUpAgainAfterRecipeReload(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RecipeManager current = level.getServer().getRecipeManager();
        RecipeManager reloaded = new RecipeManager(level.registryAccess());
        ItemStack stack = new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get());
        PrincipleContents principles = PrincipleContents.of(new PrincipleStack(PrincipleRegistry.LOAM, STEEPING_PRINCIPLES));
        int[] lookups = {0};
        Supplier<Optional<RecipeHolder<SteepingRecipe>>> lookup = () -> {
            lookups[0]++;
            return LibationUtils.steeping(level, stack, principles);
        };

        RecipeCache<SteepingRecipe> cache = new RecipeCache<>();
        helper.assertTrue(cache.get(current, lookup) != null, "The steeping recipe should be found");
        helper.assertTrue(lookups[0] == 1, "The first lookup should ask the recipe manager once, asked " + lookups[0]);
        cache.get(current, lookup);
        helper.assertTrue(lookups[0] == 1, "The same recipe manager should reuse the recipe, asked " + lookups[0]);
        cache.get(reloaded, lookup);
        helper.assertTrue(lookups[0] == 2, "A reloaded recipe manager should look the recipe up again, asked " + lookups[0]);
        cache.invalidate();
        cache.get(reloaded, lookup);
        helper.assertTrue(lookups[0] == 3, "Invalidated contents should look the recipe up again, asked " + lookups[0]);

        RecipeCache<SteepingRecipe> missing = new RecipeCache<>();
        helper.assertTrue(missing.get(current, Optional::empty) == null, "A missing recipe should be reported as missing");
        helper.assertTrue(missing.get(reloaded, lookup) != null && lookups[0] == 4,
                "A missing recipe should be looked up again after a reload, asked " + lookups[0]);
        helper.succeed();
    }

    private static InteractionResult useHeldOn(GameTestHelper helper, Player player, BlockPos relPos) {
        BlockPos absPos = helper.absolutePos(relPos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absPos), Direction.UP, absPos, false);
        return player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    private static ItemStack findPhialStack(Player player, Predicate<ItemStack> predicate) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ItemRegistry.CLAY_PHIAL.get()) && predicate.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
