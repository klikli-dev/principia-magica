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
import com.klikli_dev.principiamagica.datagen.book.golemancy.AxiomEntry;
import com.klikli_dev.principiamagica.datagen.book.golemancy.GolemancyCategoryProvider;
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

        this.infoPage("holding", "Holding and Steeping",
                "The trough holds one item and up to 500 mB of Principles. It takes only items that a steeping recipe uses, and an empty hand takes the item back out.\n"
                        + "\\\n\\\n"
                        + "While the item and the Principles match a steeping recipe, the trough works on it for 100 ticks. Then the item is replaced and exactly the recipe's Principles are used up. "
                        + "A clay ball with unguent becomes quickened clay, see "
                        + this.entryLink("Quickened Clay", KhemeiaCategoryProvider.CATEGORY_ID, "quickened_clay")
                        + ". Axioms are steeped in loam or shard, see "
                        + this.entryLink("Processing Axioms", GolemancyCategoryProvider.CATEGORY_ID, AxiomEntry.ENTRY_ID) + ".");
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
