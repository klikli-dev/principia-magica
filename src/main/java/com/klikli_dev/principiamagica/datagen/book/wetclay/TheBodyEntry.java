// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.datagen.research.PrincipiaResearch;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class TheBodyEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "the_body";

    public TheBodyEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Fourteen pieces of clay she pinched off. Seven on the right, seven on the left. Between them she set the brick.");

        this.notebookPage("notebook",
                "Wood for the bones, quickened clay for the sinews. When I set it down it turned its head toward me. Nothing more. It does not know what it is for. It only knows that I am here.");

        this.page("recipe", () -> BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("golem/wood")));

        this.page("placing", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Placing a Body");
        this.pageText("""
                Place the golem on a block face and it stands there for good. It never wanders off and it never fades.
                \\
                \\
                Without an axiom it has no purpose. It stays where it is put, or follows you if you ask it to. It cannot work until it is given an axiom.
                """
        );
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return entry.withCondition(this.condition().researchNodeUnlocked(PrincipiaResearch.QUICKENED));
    }

    @Override
    protected String entryName() {
        return "The Body";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.GOLEM.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
