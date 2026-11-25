// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.scribespath;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.datagen.book.khemeia.KhemeiaCategoryProvider;
import com.klikli_dev.principiamagica.datagen.book.khemeia.SteepingEntry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class CatchTroughEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "catch_trough";

    public CatchTroughEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The trough takes the run-off, and in the run-off the clay listens. Let it hold one thing at a time, and let that thing be changed.");

        this.notebookPage("notebook",
                "A clay trough set in front of the spout, where the run-off collects. It is where the water's Principles meet what I put in it.");

        this.stage("trough", s -> s
                .title("A catch trough")
                .obtain(ItemRegistry.CATCH_TROUGH.get()));

        this.recipeTab("recipe", "Catch Trough", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("catch_trough")));

        this.infoPage("holding", "Holding",
                "The trough holds one item and up to 500 mB of Principles. It takes only items that a steeping recipe uses, and an empty hand takes the item back out. "
                        + "Breaking it keeps its Principles on the dropped block. How the trough steeps is described in "
                        + this.entryLink("Steeping", KhemeiaCategoryProvider.CATEGORY_ID, SteepingEntry.ENTRY_ID) + ".");
    }

    @Override
    protected String entryName() {
        return "Catch Trough";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.CATCH_TROUGH.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
