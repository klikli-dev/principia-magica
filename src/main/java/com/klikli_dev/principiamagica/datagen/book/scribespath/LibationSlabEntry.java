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
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class LibationSlabEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "libation_slab";

    public LibationSlabEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.notebookPage("notebook",
                "An offering table with a carved spout, like the ones in the temples. I set things on it and pour water over them, and what the water takes from them is what I am after.");

        this.stage("slab", s -> s
                .title("A libation slab")
                .obtain(ItemRegistry.LIBATION_SLAB.get()));

        this.recipeTab("recipe", "Libation Slab", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("libation_slab")));

        this.scribePage("scribe",
                "They set the bread and the beer on the stone, and the priest poured the water after them. The gods took the essence of the offering, and the stone kept the rest.");

        this.infoPage("washing", "Washing Offerings", """
                Right-click the slab with an item that can be washed to put it on the slab, as much as fits. An empty hand takes the stack back, unless an item is already dissolving.
                \\
                \\
                Pour water in with a bucket or any water container, up to 1000 mB. Each item dissolves one at a time, in 40 ticks, and only while the water covers its full cost. Its Principles go into the slab's storage.
                \\
                \\
                The storage holds 1000 mB. An open Libation Spout drains it into the block below. Breaking the slab drops the items on it; the water and the stored Principles are lost.
                """);
    }

    @Override
    protected String entryName() {
        return "Libation Slab";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.LIBATION_SLAB.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
