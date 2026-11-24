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

public class SpoutAndTapEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "spout_and_tap";

    public SpoutAndTapEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The water runs down the carved lip and into the cup below. Whatever the water took from the offering, it carries on to the vessel.");

        this.notebookPage("notebook",
                "A spout carries the water off the slab, and a tap lets a vessel drain. Both are small blocks that sit beside the block they serve, and they both open and close with an empty hand.");

        this.stage("spout", s -> s
                .title("A libation spout")
                .obtain(ItemRegistry.LIBATION_SPOUT.get()));

        this.recipeTab("spout_recipe", "Libation Spout", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("libation_spout")));

        this.recipeTab("tap_recipe", "Vessel Tap", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("vessel_tap")));

        this.infoPage("spout", "The Libation Spout", """
                Place the spout on a side of the slab. It faces away from the slab. An empty-hand click opens or closes it, and so does a redstone signal.
                \\
                \\
                An open spout drains 25 mB every 10 ticks into the block directly below it. That block must hold Principles: a Catch Trough, a Wabet Basin or a Steeping Jar. A comparator reads the slab's water.
                """);

        this.infoPage("tap", "The Vessel Tap", """
                The tap goes on a side of a Wabet Basin or a Steeping Jar, one tap to each vessel. It works like the spout: an empty-hand click or a redstone signal opens and closes it.
                \\
                \\
                An open tap drains the vessel 25 mB every 10 ticks into the block below. A full basin empties in about 20 seconds. A comparator reads the vessel's fill.
                """);
    }

    @Override
    protected String entryName() {
        return "Spout & Vessel Tap";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.LIBATION_SPOUT.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
