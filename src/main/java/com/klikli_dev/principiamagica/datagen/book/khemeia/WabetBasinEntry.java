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

public class WabetBasinEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "wabet_basin";

    public WabetBasinEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The pure place is an open court, and the offerings are laid out where the sun can reach them. The tablets say the sun binds what the hand cannot.");

        this.notebookPage("notebook",
                "Libation spouts drain into the basin and the Primals mix in it. Pairs cure into light compounds, but only in light. The brighter it is, the faster they cure. When the compounds are ready I open the tap, and they run into the trough below.");

        this.recipeTab("recipe", "Wabet Basin", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("wabet_basin")));

        this.stage("quickened_clay", s -> s
                .title("Quickened clay")
                .obtain(ItemRegistry.QUICKENED_CLAY.get()));

        this.infoPage("curing", "Curing in Light", """
                The basin needs a light level of at least 9 at its own block. Below that, nothing cures and the Primals wait in the water.
                \\
                \\
                At full light a step takes 100 ticks (5 seconds). The dimmer the light, the longer it takes. Each step uses 25 mB of each Primal in a pair and gives 25 mB of the compound.
                \\
                \\
                The basin holds 1000 mB, and the Vessel Tap drains it into the block below.
                """);
    }

    @Override
    protected String entryName() {
        return "Wabet Basin";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.WABET_BASIN.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
