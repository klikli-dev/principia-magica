// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.modonomicon.data.BookDataManager;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.item.book.PrincipiaItem;
import com.klikli_dev.principiamagica.datagen.advancement.PrincipiaMagicaAdvancementSubProvider;
import com.klikli_dev.principiamagica.datagen.book.wetclay.AxiomEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.FleshAndClayEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.PrincipiaEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.StylusEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.PurposeEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.TheBodyEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.WetClayCategoryProvider;
import com.klikli_dev.principiamagica.datagen.book.wetclay.WetTabletEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;

import java.util.List;

public class ProgressionGameTests {

    public static void principiaBookAndFirstEntryLoad(GameTestHelper helper) {
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category the_wet_clay is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID + "/" + PrincipiaEntry.ENTRY_ID)) != null,
                "Entry principia is missing from category the_wet_clay");
        helper.succeed();
    }

    public static void clayAdvancementAndPrincipiaRecipeExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getAdvancements().get(PrincipiaMagicaAdvancementSubProvider.THE_CLAY_REMEMBERS) != null,
                "Advancement the_clay_remembers is missing");
        helper.assertTrue(server.getRecipeManager().byKey(PrincipiaMagicaAdvancementSubProvider.PRINCIPIA_RECIPE).isPresent(),
                "Recipe principia is missing");
        helper.succeed();
    }

    public static void wedgeOfCopperAdvancementAndStylusRecipeExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getAdvancements().get(PrincipiaMagicaAdvancementSubProvider.A_WEDGE_OF_COPPER) != null,
                "Advancement a_wedge_of_copper is missing");
        helper.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc("copper_stylus"))).isPresent(),
                "Recipe copper_stylus is missing");
        helper.succeed();
    }

    public static void stylusEntryLoads(GameTestHelper helper) {
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category the_wet_clay is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID + "/" + StylusEntry.ENTRY_ID)) != null,
                "Entry the_stylus is missing from category the_wet_clay");
        helper.succeed();
    }

    public static void wetTabletAdvancementRecipeAndEntryExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getAdvancements().get(PrincipiaMagicaAdvancementSubProvider.WET_CLAY) != null,
                "Advancement wet_clay is missing");
        helper.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc("wet_tablet"))).isPresent(),
                "Recipe wet_tablet is missing");
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category the_wet_clay is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID + "/" + WetTabletEntry.ENTRY_ID)) != null,
                "Entry wet_clay is missing from category the_wet_clay");
        helper.succeed();
    }

    public static void axiomAdvancementRecipesAndEntryExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getAdvancements().get(PrincipiaMagicaAdvancementSubProvider.A_LAW_MADE_FAST) != null,
                "Advancement a_law_made_fast is missing");
        for (var recipe : List.of("unfired_axiom_of_harvest", "unfired_axiom_of_felling", "axiom_of_harvest", "axiom_of_felling")) {
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

    public static void quickenedAdvancementsRecipeAndEntryExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getAdvancements().get(PrincipiaMagicaAdvancementSubProvider.ROTTEN_FLESH) != null,
                "Advancement rotten_flesh is missing");
        helper.assertTrue(server.getAdvancements().get(PrincipiaMagicaAdvancementSubProvider.QUICKENED) != null,
                "Advancement quickened is missing");
        helper.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc("quickened_clay"))).isPresent(),
                "Recipe quickened_clay is missing");
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category the_wet_clay is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID + "/" + FleshAndClayEntry.ENTRY_ID)) != null,
                "Entry flesh_and_clay is missing from category the_wet_clay");
        helper.succeed();
    }

    public static void golemBodyRecipeAdvancementAndEntryExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getAdvancements().get(PrincipiaMagicaAdvancementSubProvider.CLAY_THAT_STANDS) != null,
                "Advancement clay_that_stands is missing");
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

    public static void purposeAdvancementAndEntryExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        var purpose = server.getAdvancements().get(PrincipiaMagicaAdvancementSubProvider.PURPOSE);
        helper.assertTrue(purpose != null, "Advancement purpose is missing");
        helper.assertTrue(purpose.value().criteria().containsKey("given_axiom"), "Purpose should be triggered by given_axiom");
        var book = BookDataManager.get().getBook(PrincipiaItem.PRINCIPIA);
        helper.assertTrue(book != null, "Modonomicon book principia is missing");
        var category = book.getCategory(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID));
        helper.assertTrue(category != null, "Category the_wet_clay is missing");
        helper.assertTrue(category.getEntry(PrincipiaMagica.loc(WetClayCategoryProvider.CATEGORY_ID + "/" + PurposeEntry.ENTRY_ID)) != null,
                "Entry purpose is missing from category the_wet_clay");
        helper.succeed();
    }
}
