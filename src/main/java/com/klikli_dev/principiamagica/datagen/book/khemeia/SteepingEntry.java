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
import com.klikli_dev.principiamagica.datagen.book.golemancy.AxiomEntry;
import com.klikli_dev.principiamagica.datagen.book.golemancy.GolemancyCategoryProvider;
import com.klikli_dev.principiamagica.datagen.book.scribespath.QuickenedClayEntry;
import com.klikli_dev.principiamagica.datagen.book.scribespath.ScribesPathCategoryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

/**
 * The general steeping process of the Catch Trough. The recipes that use it are shown in the hub and in golemancy.
 */
public class SteepingEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "steeping";

    public SteepingEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The tablets say the black water changes what it touches. A thing set in the trough, and given the water it asks for, comes out as something else.");

        this.notebookPage("notebook",
                "An item in the Catch Trough and the Principles its recipe asks for. After 100 ticks the item is replaced, and exactly those Principles are used up. The rest stays in the trough. If the Principles fall short, or the item stops matching, the progress starts again.");

        this.recipePage("quickened", "Steeping Quickened Clay", PrincipiaMagica.loc("steeping/quickened_clay"));

        this.infoPage("uses", "Where Steeping Leads", "Steeped clay becomes "
                + this.entryLink("Quickened Clay", ScribesPathCategoryProvider.CATEGORY_ID, QuickenedClayEntry.ENTRY_ID)
                + ". A steeped unfired axiom is dried on the Drying Slab, see "
                + this.entryLink("Processing Axioms", GolemancyCategoryProvider.CATEGORY_ID, AxiomEntry.ENTRY_ID) + ".");
    }

    @Override
    protected String entryName() {
        return "Steeping";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.CATCH_TROUGH.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
