// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.scribespath;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.datagen.book.wetclay.KudurruEntry;
import com.klikli_dev.principiamagica.datagen.book.wetclay.WetClayCategoryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class StylusEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "the_stylus";

    public StylusEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The reed is cut, the clay is wet, the word goes in. What is pressed in the clay does not leave it.");

        this.notebookPage("notebook",
                "A cut reed would do, but copper holds its edge. With this I can carve the clay, and, if the tablets are right, command what the clay holds.");

        this.recipeTab("recipe", "Copper Stylus", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("copper_stylus")));

        this.stage("wedge", s -> s
                .title("A wedge of copper")
                .craft(ItemRegistry.COPPER_STYLUS.get()));

        this.page("uses", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Two Uses");
        this.pageText("As a crafting tool, the stylus is never consumed: it returns to your grid after every recipe that uses it.\n"
                + "\\\n\\\n"
                + "As a configuration tool, it selects a golem, carves, lifts and moves its work boundaries, and links its containers. See "
                + this.entryLink("Kudurru", WetClayCategoryProvider.CATEGORY_ID, KudurruEntry.ENTRY_ID) + ".");
    }

    @Override
    protected String entryName() {
        return "The Stylus";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.COPPER_STYLUS.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
