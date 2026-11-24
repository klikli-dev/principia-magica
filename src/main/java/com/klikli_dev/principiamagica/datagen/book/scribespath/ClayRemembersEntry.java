// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.scribespath;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import net.minecraft.world.item.Items;

public class ClayRemembersEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "the_clay_remembers";

    public ClayRemembersEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The reed is cut, the clay is wet, the word goes in. What is pressed in the clay does not leave it. The clay remembers.");

        this.notebookPage("notebook",
                "The copied tablets say the clay remembers every mark pressed into it. I believe them now. Everything in this book is a mark I have seen and made, and I will keep to what I have actually observed.");
    }

    @Override
    protected String entryName() {
        return "The Clay Remembers";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.CLAY_BALL);
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
