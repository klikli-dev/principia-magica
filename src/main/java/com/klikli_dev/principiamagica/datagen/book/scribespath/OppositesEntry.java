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

/**
 * Lore-only teaser. Opposed Primals have no recipe, but their effect is not implemented yet, so the text promises none.
 */
public class OppositesEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "opposites";

    public OppositesEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Silt against gleam, sap against ash. The tablets set them in pairs and say that the pairs strain against each other. The strain has a name, isfet, and the scribe does not write down what it does.");

        this.notebookPage("notebook",
                "In a vessel, an opposed pair never cures. The two Primals simply stay in the water together. I have not yet found what that does, but I will note it when I do.");
    }

    @Override
    protected String entryName() {
        return "Opposites";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.COMPASS);
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
