// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.khemeia;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import net.minecraft.world.item.Items;

/**
 * Lore-only skeleton for the second tier of Principles. The eight tier 2 compounds are registered but have no recipes
 * yet, so this entry promises no mechanics.
 */
public class LustralWatersEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "lustral_waters";

    public LustralWatersEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Beyond the black and the light the tablets name further waters, eight of them, mixed from what the Primals and the compounds already give. The scribe wrote their names and left the rest of the recipe blank.");

        this.notebookPage("notebook",
                "I cannot make them yet, and I do not know what they are for. I will write down whatever I learn of them here, and nothing I have not yet seen.");

        this.stage("finished_axiom", s -> s
                .title("A finished axiom")
                .obtain(AxiomItem.AXIOMS));
    }

    @Override
    protected String entryName() {
        return "The Lustral Waters";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.WATER_BUCKET);
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
