// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.khemeia;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class DryingSlabEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "drying_slab";

    public DryingSlabEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The clay dries in the sun, and the law dries with it. Left in the dark, the clay stays soft, and the law is not yet bound.");

        this.notebookPage("notebook",
                "Put a steeped axiom into the input and take the finished axiom from the output. Drying runs on the light at the block above the slab, so a lamp or the open sky dries it. In the dark it does not dry at all.");

        this.recipeTab("recipe", "Drying Slab", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("drying_slab")));

        this.infoPage("drying", "Drying", """
                Only steeped unfired axioms can go in the input. Each tick the drying moves forward by the light level divided by 15. At full light a finished axiom takes 400 ticks (20 seconds). Drying pauses while the output has no room.
                \\
                \\
                An empty hand takes the output first. Taking the input back out restarts its drying.
                """);

        this.recipePage("harvest", "Drying the Axiom of Harvest", PrincipiaMagica.loc("drying/axiom_of_harvest"));
        this.recipePage("felling", "Drying the Axiom of Felling", PrincipiaMagica.loc("drying/axiom_of_felling"));
    }

    @Override
    protected String entryName() {
        return "Drying Slab";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.DRYING_SLAB.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
