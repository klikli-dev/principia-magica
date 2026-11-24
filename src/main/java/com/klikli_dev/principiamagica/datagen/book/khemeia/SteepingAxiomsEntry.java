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
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class SteepingAxiomsEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "steeping_axioms";

    public SteepingAxiomsEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Each law wants its matter. For the harvest, the loam. For the felling, the shard. Press the one into the other while both are still soft.");

        this.notebookPage("notebook",
                "An unfired axiom goes into the Catch Trough with 100 mB of loam for the harvest, or shard for the felling. After 100 ticks it is a steeped unfired axiom, and it is ready for the Drying Slab. The trough must hold the Principles before the steeping can start.");

        this.recipePage("harvest", "Steeping the Axiom of Harvest", PrincipiaMagica.loc("steeping/axiom_of_harvest_unfired"));
        this.recipePage("felling", "Steeping the Axiom of Felling", PrincipiaMagica.loc("steeping/axiom_of_felling_unfired"));
    }

    @Override
    protected String entryName() {
        return "Steeping Axioms";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
