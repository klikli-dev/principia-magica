// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.modonomicon.data.BookDataManager;
import com.klikli_dev.modonomicon.research.ResearchServices;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.GolemItem;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.item.book.PrincipiaItem;
import com.klikli_dev.principiamagica.datagen.advancement.PrincipiaMagicaAdvancementSubProvider;
import com.klikli_dev.principiamagica.datagen.book.scribespath.PrincipiaEntry;
import com.klikli_dev.principiamagica.datagen.book.scribespath.ScribesPathCategoryProvider;
import com.klikli_dev.principiamagica.datagen.book.scribespath.StylusEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.AxiomEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.FleshAndClayEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.KudurruEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.PurposeEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.TheBodyEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.WetClayCategoryProvider;
import com.klikli_dev.principiamagica.datagen.book.wetclay.AxiomTabletEntry;
import com.klikli_dev.principiamagica.datagen.research.PrincipiaResearch;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ProgressionGameTests {

    /**
     * The advancements that were visible in the advancement screen before they were replaced by the hidden milestones.
     */
    private static final List<String> REMOVED_ADVANCEMENTS = List.of(
            "the_clay_remembers", "a_wedge_of_copper", "wet_clay", "a_law_made_fast",
            "rotten_flesh", "quickened", "clay_that_stands", "purpose", "recipes/principia");

    public static void principiaBookAndFirstEntryLoad(GameTestHelper helper) {
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(ScribesPathCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category scribes_path is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(ScribesPathCategoryProvider.CATEGORY_ID + "/" + PrincipiaEntry.ENTRY_ID)) != null,
                "Entry principia is missing from category scribes_path");
        helper.succeed();
    }

    public static void principiaRecipeExists(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc("principia"))).isPresent(),
                "Recipe principia is missing");
        helper.succeed();
    }

    public static void wedgeOfCopperStylusRecipeExists(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc("copper_stylus"))).isPresent(),
                "Recipe copper_stylus is missing");
        helper.succeed();
    }

    public static void stylusEntryLoads(GameTestHelper helper) {
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(ScribesPathCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category scribes_path is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(ScribesPathCategoryProvider.CATEGORY_ID + "/" + StylusEntry.ENTRY_ID)) != null,
                "Entry the_stylus is missing from category scribes_path");
        helper.succeed();
    }

    public static void axiomTabletRecipeAndEntryExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc("axiom_tablet"))).isPresent(),
                "Recipe axiom_tablet is missing");
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category the_wet_clay is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID + "/" + AxiomTabletEntry.ENTRY_ID)) != null,
                "Entry wet_clay is missing from category the_wet_clay");
        helper.succeed();
    }

    public static void axiomRecipesAndEntryExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        for (var recipe : List.of("axiom_of_harvest_unfired", "axiom_of_felling_unfired", "drying/axiom_of_harvest", "drying/axiom_of_felling")) {
            helper.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc(recipe))).isPresent(),
                    "Recipe " + recipe + " is missing");
        }
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category the_wet_clay is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID + "/" + AxiomEntry.ENTRY_ID)) != null,
                "Entry axioms is missing from category the_wet_clay");
        helper.succeed();
    }

    public static void quickenedRecipeAndEntryExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc("steeping/quickened_clay"))).isPresent(),
                "Recipe steeping/quickened_clay is missing");
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category the_wet_clay is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID + "/" + FleshAndClayEntry.ENTRY_ID)) != null,
                "Entry flesh_and_clay is missing from category the_wet_clay");
        helper.succeed();
    }

    public static void golemBodyRecipeAndEntryExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc("golem/wood"))).isPresent(),
                "Recipe golem/wood is missing");
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category the_wet_clay is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID + "/" + TheBodyEntry.ENTRY_ID)) != null,
                "Entry the_body is missing from category the_wet_clay");
        helper.succeed();
    }

    public static void purposeEntryExists(GameTestHelper helper) {
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category the_wet_clay is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID + "/" + PurposeEntry.ENTRY_ID)) != null,
                "Entry purpose is missing from category the_wet_clay");
        helper.succeed();
    }

    /**
     * The golem milestones are hidden roots without a display, and they are triggered by the golem criteria.
     */
    public static void golemMilestonesAreHiddenRoots(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        for (var entry : List.of(
                new Milestone(PrincipiaMagicaAdvancementSubProvider.GOLEM_PLACED, "golem_placed"),
                new Milestone(PrincipiaMagicaAdvancementSubProvider.GOLEM_GIVEN_AXIOM, "golem_given_axiom"))) {
            var holder = server.getAdvancements().get(entry.id());
            helper.assertTrue(holder != null, "Advancement " + entry.id() + " is missing");
            helper.assertTrue(holder.value().display().isEmpty(), "Advancement " + entry.id() + " should have no display");
            helper.assertTrue(holder.value().parent().isEmpty(), "Advancement " + entry.id() + " should be a root");
            helper.assertTrue(holder.value().criteria().containsKey(entry.criterion()),
                    "Advancement " + entry.id() + " should be triggered by " + entry.criterion());
        }
        helper.succeed();
    }

    /**
     * The visible advancements and the principia recipe advancement are gone, the principia recipe itself stays.
     */
    public static void visibleAdvancementsAreRemoved(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        for (var path : REMOVED_ADVANCEMENTS) {
            helper.assertTrue(server.getAdvancements().get(PrincipiaMagica.loc(path)) == null,
                    "Advancement " + path + " should be removed");
        }
        helper.succeed();
    }

    public static void golemPlacementGrantsResearchFact(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos clicked = helper.absolutePos(new BlockPos(1, 1, 1));
        ItemStack stack = GolemItem.createStack(GolemSpec.of(GolemMaterialRegistry.WOOD));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(clicked), Direction.UP, clicked, false)));

        helper.assertTrue(ResearchServices.state().getStateFor(player).hasFact(PrincipiaResearch.GOLEM_PLACED.id()),
                "Placing a golem should grant the golem_placed fact");
        helper.succeed();
    }

    public static void axiomGivenGrantsResearchFact(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        golem.setOwnerUUID(player.getUUID());
        ItemStack axiom = ItemRegistry.AXIOM_OF_HARVEST.get().getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, axiom);

        axiom.interactLivingEntity(player, golem, InteractionHand.MAIN_HAND);

        helper.assertTrue(ResearchServices.state().getStateFor(player).hasFact(PrincipiaResearch.GOLEM_GIVEN_AXIOM.id()),
                "Giving an axiom should grant the golem_given_axiom fact");
        helper.succeed();
    }

    public static void kudurruEntryExists(GameTestHelper helper) {
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category the_wet_clay is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID + "/" + KudurruEntry.ENTRY_ID)) != null,
                "Entry kudurru is missing from category the_wet_clay");
        helper.succeed();
    }

    private record Milestone(Identifier id, String criterion) {
    }
}
