// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSmeltingRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.datagen.research.PrincipiaResearch;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class AxiomEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "axioms";

    public AxiomEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.notebookPage("notebook",
                "The scribe calls them *me*. I call them axioms: laws that need no proof, only obedience. Carve the purpose while the clay is wet, with something of that purpose pressed in: grain for the harvest, a sapling for the felling. Then fire it. Wet clay can still be persuaded. Fired clay cannot.");

        this.scribePage("scribe",
                "Enki kept the decrees in his house in Eridu: the decree of the plough, the decree of the axe, the decree of kingship, the decree of the destruction of cities. Each could be held in the hand. Each could be given away.");

        this.page("recipe_harvest", () -> BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("unfired_axiom_of_harvest")));

        this.page("recipe_felling", () -> BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("unfired_axiom_of_felling")));

        this.page("firing", () -> BookSmeltingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("axiom_of_harvest"))
                .withRecipeId2(PrincipiaMagica.loc("axiom_of_felling")));
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return entry.withCondition(this.condition().researchNodeUnlocked(PrincipiaResearch.WET_CLAY));
    }

    @Override
    protected String entryName() {
        return "Axioms";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.AXIOM_OF_HARVEST.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
