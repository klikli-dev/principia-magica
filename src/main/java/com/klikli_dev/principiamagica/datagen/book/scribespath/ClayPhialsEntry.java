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
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class ClayPhialsEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "clay_phials";

    public ClayPhialsEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Carry the law in a small vessel, sealed in clay. The tablets call it the unguentarium, and the makers filled it with what they had pressed out of the earth.");

        this.notebookPage("notebook",
                "A phial holds 250 mB of one Principle. Empty phials, and phials holding exactly the same Principle and amount, stack up to 16. Unfired phials are made from clay balls and quickened clay, and they are fired in a furnace or on a campfire.");

        this.stage("quickened_clay", s -> s
                .title("Quickened clay")
                .obtain(ItemRegistry.QUICKENED_CLAY.get()));

        this.recipeTab("recipe", "Unfired Clay Phial", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("unfired_clay_phial")));

        this.page("fire", () -> BookSmeltingRecipePageModel.create()
                .withTitle1(this.context().pageTitle())
                .withRecipeId1(PrincipiaMagica.loc("smelting/clay_phial")));
        this.pageTitle("Firing the Phial");

        this.infoPage("using", "Using a Phial", """
                Select a Principle with crouch-scroll while the phial is empty. Right-click a Principle container to pour the phial into it.
                \\
                \\
                Crouch-right-click a container to draw its Principles into the phial. A phial takes one Principle at a time and holds no more than 250 mB.
                """);
    }

    @Override
    protected String entryName() {
        return "Clay Phials";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.CLAY_PHIAL.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
