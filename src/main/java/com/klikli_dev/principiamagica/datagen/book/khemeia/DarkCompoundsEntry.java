// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.khemeia;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import net.minecraft.world.item.Items;

public class DarkCompoundsEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "dark_compounds";

    public DarkCompoundsEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Mire, bitumen, unguent, kohl. The tablets call them the blacks of the dead. Each is two Primals made one, and each is made only in the dark.");

        this.notebookPage("notebook",
                "A curing step takes 25 mB of each Primal in a pair and gives back 25 mB of the compound. The Steeping Jar does this on its own. Unguent is the one I need for quickened clay.");

        this.recipePage("mire", "Mire", PrincipiaMagica.loc("curing/dark/mire"));
        this.recipePage("bitumen", "Bitumen", PrincipiaMagica.loc("curing/dark/bitumen"));
        this.recipePage("unguent", "Unguent", PrincipiaMagica.loc("curing/dark/unguent"));
        this.recipePage("kohl", "Kohl", PrincipiaMagica.loc("curing/dark/kohl"));
    }

    @Override
    protected String entryName() {
        return "Dark Compounds";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.BLACK_DYE);
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
