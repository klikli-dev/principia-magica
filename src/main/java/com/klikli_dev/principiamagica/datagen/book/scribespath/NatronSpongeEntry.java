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

public class NatronSpongeEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "natron_sponge";

    public NatronSpongeEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.notebookPage("notebook",
                "Natron draws the wet out of things. I can soak up one Principle at a time from a vessel, or clear a vessel of everything it holds. It is reusable, so one sponge serves many vessels.");

        this.stage("quickened_clay", s -> s
                .title("Quickened clay")
                .obtain(ItemRegistry.QUICKENED_CLAY.get()));

        this.recipeTab("recipe", "Natron Sponge", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("natron_sponge")));

        this.scribePage("scribe",
                "The salt of the valley is dried out of the ground. Press the sponge to the water and the water gives up what it carries. The tablets give no more instruction than that.");

        this.infoPage("using", "Using the Sponge", """
                Right-click a Principle container to blot out the Principle selected on the HUD. Crouch-right-click it to purge every Principle it holds.
                \\
                \\
                Select the Principle with crouch-scroll.
                """);
    }

    @Override
    protected String entryName() {
        return "Natron Sponge";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.NATRON_SPONGE.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
