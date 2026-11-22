// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.libation.drying.DryingSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlock;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.slab.SpoutSide;
import com.klikli_dev.principiamagica.content.libation.trough.CatchTroughBlockEntity;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import com.klikli_dev.principiamagica.registry.BlockRegistry;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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
            be.useItemOn(new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get()), player, InteractionHand.MAIN_HAND);

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
            be.useItemOn(new ItemStack(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get()), helper.makeMockServerPlayerInLevel(), InteractionHand.MAIN_HAND);

            helper.runAfterDelay(time + 10, () -> {
                helper.assertTrue(be.getInput().getCount() == 1, "The second item should not be consumed while the output is full, input is " + be.getInput());
                helper.assertTrue(be.getOutput().getCount() == 1, "The output should stay at one axiom, output is " + be.getOutput());
                helper.succeed();
            });
        });
    }

    /**
     * A hopper inserts a drying input into the empty input of the drying slab, and only extracts the dried output. Once the output
     * holds the result, the input cannot be extracted and a second input is refused while the first one dries.
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

        int secondInput;
        try (Transaction transaction = Transaction.open(null)) {
            secondInput = handler.insert(ItemResource.of(steeped), 1, transaction);
            transaction.commit();
        }
        helper.assertTrue(secondInput == 0, "The slab should refuse a second input while the input is not empty, accepted " + secondInput);

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
        trough.useItemOn(item, helper.makeMockServerPlayerInLevel(), InteractionHand.MAIN_HAND);
        return trough;
    }

    /**
     * Places a drying slab with light above it, the glowstone gives full light, and puts the input into it.
     */
    private static DryingSlabBlockEntity dryingSlabWithInput(GameTestHelper helper, BlockPos pos, ItemStack input) {
        helper.setBlock(pos, BlockRegistry.DRYING_SLAB.get().defaultBlockState());
        helper.setBlock(pos.above(), Blocks.GLOWSTONE);
        DryingSlabBlockEntity slab = (DryingSlabBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        slab.useItemOn(input, helper.makeMockServerPlayerInLevel(), InteractionHand.MAIN_HAND);
        return slab;
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
        slab.useItemOn(new ItemStack(Items.BONE), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(slab.getWater() == LibationSlabBlockEntity.WATER_CAPACITY, "A water bucket should fill the slab, got " + slab.getWater());
        return slab;
    }
}
