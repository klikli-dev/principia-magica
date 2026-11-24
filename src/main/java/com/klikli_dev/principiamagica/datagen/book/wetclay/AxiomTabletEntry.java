// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class AxiomTabletEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "axiom_tablet";

    public AxiomTabletEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The tablet is pressed flat and left wet. Its law is not yet written, and the clay will keep whatever is carved into it.");

        this.notebookPage("notebook",
                "A blank tablet, pressed from quickened clay and held together with a stylus. The scribe treats wet clay as something that *listens*.");

        this.stage("quickened_clay", s -> s
                .title("Quickened clay")
                .obtain(ItemRegistry.QUICKENED_CLAY.get()));

        this.recipeTab("recipe", "Axiom Tablet", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("axiom_tablet")));
    }

    @Override
    protected String entryName() {
        return "Axiom Tablet";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.AXIOM_TABLET.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
