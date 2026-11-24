// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import net.minecraft.world.item.Items;

/**
 * Root of the golemancy category. Golems are bodies of clay and flesh that are given a law to keep.
 */
public class WetClayRootEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "wet_clay";

    public WetClayRootEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Wet clay is open. It takes the word and the thumbprint and the fly that lands on it. Do not let it hear what you do not mean.");

        this.notebookPage("notebook",
                "Golems are clay that has been given flesh and then a purpose. The first pages are about the body, the rest about the law that moves it.");
    }

    @Override
    protected String entryName() {
        return "The Wet Clay";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.CATEGORY_START;
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
