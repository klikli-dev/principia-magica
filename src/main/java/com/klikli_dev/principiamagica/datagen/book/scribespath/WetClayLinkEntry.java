// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.scribespath;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.datagen.book.wetclay.WetClayCategoryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

/**
 * Link from the hub to the golem category. It unlocks once quickened clay is obtained.
 */
public class WetClayLinkEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "wet_clay_link";

    public WetClayLinkEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.stage("quickened_clay", s -> s
                .title("Quickened clay")
                .obtain(ItemRegistry.QUICKENED_CLAY.get()));

        this.notebookPage("notebook",
                "Clay that has drunk the flesh can be given a purpose. The pages on the golems are gathered in "
                        + this.categoryLink("The Wet Clay", WetClayCategoryProvider.CATEGORY_ID) + ".");
    }

    @Override
    protected String entryName() {
        return "The Wet Clay";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.QUICKENED_CLAY.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
