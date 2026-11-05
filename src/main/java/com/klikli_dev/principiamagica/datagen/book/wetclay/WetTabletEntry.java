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

public class WetTabletEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "wet_clay";

    public WetTabletEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.notebookPage("notebook",
                "A blank tablet. The scribe treats wet clay as something that *listens*.");

        this.scribePage("scribe",
                "Wet clay is open. It takes the word and the thumbprint and the fly that lands on it. Do not let it hear what you do not mean.");

        this.page("recipe", () -> BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("wet_tablet")));
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return entry.withCondition(this.condition().researchNodeUnlocked(PrincipiaResearch.A_WEDGE_OF_COPPER));
    }

    @Override
    protected String entryName() {
        return "Wet Clay";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.WET_TABLET.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
