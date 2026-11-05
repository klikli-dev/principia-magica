// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class PrincipiaEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "principia";

    public PrincipiaEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.notebookPage("notebook",
                "The clay by the river bears marks: wedges, rows of them, too regular to be chance. I have started copying them here. If they are words, someone meant them to last.");

        this.scribePage("scribe",
                "In the beginning the gods laboured, and the labour was heavy. They dug the rivers. They bore the baskets. And they wept beneath the weight.");
    }

    @Override
    protected String entryName() {
        return "Principia";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.CATEGORY_START;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.PRINCIPIA.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
