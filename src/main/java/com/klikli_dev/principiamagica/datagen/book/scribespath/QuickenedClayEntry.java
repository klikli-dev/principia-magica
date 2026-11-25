// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.scribespath;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSmeltingRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.datagen.book.golemancy.AxiomEntry;
import com.klikli_dev.principiamagica.datagen.book.golemancy.GolemancyCategoryProvider;
import com.klikli_dev.principiamagica.datagen.book.golemancy.TheBodyEntry;
import com.klikli_dev.principiamagica.datagen.book.khemeia.KhemeiaCategoryProvider;
import com.klikli_dev.principiamagica.datagen.book.khemeia.SteepingEntry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

/**
 * The hub group of quickened clay. Quickened clay is the material of the tablets, the phials, the sponge and the golem
 * body, so its crafts are tabs on this entry. Its steeping process is described in Khemeia.
 */
public class QuickenedClayEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "quickened_clay";

    public QuickenedClayEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The clay of the first makers lay dead until it drank. What it drinks is the flesh of the god, and the ghost in that flesh remembers the word. Let the clay drink, and it will hear.");

        this.notebookPage("notebook",
                "Clay holds a law, but law alone does not move. The old makers mixed in flesh, and I have only what the dead leave behind. Steep a clay ball with 25 mB of unguent in the Catch Trough, and for every 25 mB it gives back two quickened clay. See "
                        + this.entryLink("Steeping", KhemeiaCategoryProvider.CATEGORY_ID, SteepingEntry.ENTRY_ID) + ".");

        this.stage("quickened", s -> s
                .title("Quickened clay")
                .obtain(ItemRegistry.QUICKENED_CLAY.get()));

        this.recipeTab("tablet_recipe", "Axiom Tablet", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("axiom_tablet")));

        this.recipeTab("phial_recipe", "Unfired Clay Phial", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("unfired_clay_phial")));

        this.recipeTab("sponge_recipe", "Natron Sponge", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("natron_sponge")));

        this.recipeTab("golem_recipe", "Wooden Golem", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("golem/wood")));

        this.infoPage("tablet", "The Axiom Tablet", "Every axiom is carved into a tablet first. The tablet is a stylus with two quickened clay. See "
                + this.entryLink("Processing Axioms", GolemancyCategoryProvider.CATEGORY_ID,
                AxiomEntry.ENTRY_ID) + ".");

        this.infoPage("phial", "Clay Phials", """
                A clay phial holds 250 mB of one Principle. Empty phials, and phials holding exactly the same Principle and amount, stack up to 16. Select a Principle with crouch-scroll while the phial is empty.
                \\
                \\
                Right-click a Principle container to pour the phial into it. Crouch-right-click a container to draw its Principles into the phial.
                """);

        this.page("firing", () -> BookSmeltingRecipePageModel.create()
                .withTitle1(this.context().pageTitle())
                .withRecipeId1(PrincipiaMagica.loc("smelting/clay_phial")));
        this.pageTitle("Firing the Phial");

        this.infoPage("sponge", "The Natron Sponge", """
                Right-click a Principle container to blot out the Principle selected on the HUD. Crouch-right-click it to purge every Principle it holds.
                \\
                \\
                The sponge is reusable, so one sponge serves many vessels.
                """);

        this.infoPage("golem", "The Golem Body", "Wood for the bones, quickened clay for the sinews. The body is made from the recipe above, and a golem is given its purpose once it stands. See "
                + this.entryLink("The Body", GolemancyCategoryProvider.CATEGORY_ID,
                TheBodyEntry.ENTRY_ID) + ".");
    }

    @Override
    protected String entryName() {
        return "Quickened Clay";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.QUICKENED_CLAY.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
