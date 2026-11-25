// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.golemancy;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

/**
 * The whole pipeline of an axiom: craft the unfired axiom, steep it in the catch trough, dry it on the drying slab,
 * and give it to a golem.
 */
public class AxiomEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "axioms";

    public AxiomEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Enki kept the decrees in his house in Eridu: the decree of the plough, the decree of the axe, the decree of kingship, the decree of the destruction of cities. Each could be held in the hand. Each could be given away.");

        this.notebookPage("notebook",
                "The scribe calls them *me*. I call them axioms: laws that need no proof, only obedience. Carve the purpose while the clay is wet, with something of that purpose pressed in: grain for the harvest, a sapling for the felling. Then steep it in its matter and dry it in the light. Wet clay can still be persuaded. Dried clay keeps its law.");

        this.stage("axiom_tablet", s -> s
                .title("An axiom tablet")
                .obtain(ItemRegistry.AXIOM_TABLET.get()));

        this.recipeTab("recipe_harvest", "Unfired Axiom of Harvest", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("axiom_of_harvest_unfired")));
        this.recipeTab("recipe_felling", "Unfired Axiom of Felling", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("axiom_of_felling_unfired")));

        this.recipePage("steeping_harvest", "Steeping the Axiom of Harvest", PrincipiaMagica.loc("steeping/axiom_of_harvest_unfired"));
        this.recipePage("steeping_felling", "Steeping the Axiom of Felling", PrincipiaMagica.loc("steeping/axiom_of_felling_unfired"));
        this.recipePage("drying_harvest", "Drying the Axiom of Harvest", PrincipiaMagica.loc("drying/axiom_of_harvest"));
        this.recipePage("drying_felling", "Drying the Axiom of Felling", PrincipiaMagica.loc("drying/axiom_of_felling"));

        this.infoPage("giving", "Giving the Axiom", "Once dried, an axiom is given to a golem with a right-click. See "
                + this.entryLink("Purpose", GolemancyCategoryProvider.CATEGORY_ID, PurposeEntry.ENTRY_ID) + ".");
    }

    @Override
    protected String entryName() {
        return "Processing Axioms";
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
