// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.modonomicon.data.BookDataManager;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.item.book.PrincipiaItem;
import com.klikli_dev.principiamagica.datagen.advancement.PrincipiaMagicaAdvancementSubProvider;
import com.klikli_dev.principiamagica.datagen.book.wetclay.PrincipiaEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.WetClayCategoryProvider;
import net.minecraft.gametest.framework.GameTestHelper;

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
}
