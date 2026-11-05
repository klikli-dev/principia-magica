// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.datagen.research.PrincipiaResearch;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class FleshAndClayEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "flesh_and_clay";

    public FleshAndClayEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "They slew We-ila, the god who had reason. In his flesh and blood they mixed the clay. From his flesh came the ghost, and the ghost remembered. So man was made, to bear the basket in the gods' place.");

        this.notebookPage("notebook",
                "Clay holds a law, but law alone does not move. The old makers mixed in flesh: a god's, if the tablet is to be believed. I have only what the dead leave behind. It will have to do. I would rather not think about what the clay remembers of it.");

        this.page("recipe", () -> BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("quickened_clay")));
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return entry.withCondition(this.condition().researchNodeUnlocked(PrincipiaResearch.FLESH_AND_CLAY));
    }

    @Override
    protected String entryName() {
        return "Flesh and Clay";
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
