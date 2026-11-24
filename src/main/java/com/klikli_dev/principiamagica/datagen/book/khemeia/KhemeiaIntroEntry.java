// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.khemeia;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class KhemeiaIntroEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "khemeia_intro";

    public KhemeiaIntroEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The flood leaves black silt on the banks. The scribes of the Black Land learned to ask the silt for more than it gave, and they named the craft after the earth itself: khemeia.");

        this.notebookPage("notebook",
                "Every change I make in these vessels is a process, and the copied tablets call them all khemeia. Libation washes the Primals out of things. The basin and the jar cure pairs of them into compounds. The trough steeps. The slab dries. I will set down each one as I learn it.");

        this.infoPage("processes", "The Processes", """
                **Libation**: washing offerings with water on the Libation Slab.
                \\
                \\
                **Curing**: pairs of Primals cure into compounds in the Wabet Basin (light) and the Steeping Jar (dark).
                \\
                \\
                **Steeping**: items take Principles in the Catch Trough and become something new.
                \\
                \\
                **Drying**: steeped items dry on the Drying Slab into finished work.
                """);
    }

    @Override
    protected String entryName() {
        return "Khemeia";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.CATEGORY_START;
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
