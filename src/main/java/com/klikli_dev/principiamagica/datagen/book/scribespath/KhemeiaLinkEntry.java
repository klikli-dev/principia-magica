// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.scribespath;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.datagen.book.khemeia.KhemeiaCategoryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

/**
 * Link from the hub to the alchemy category. Always visible, as the Khemeia category is always open.
 */
public class KhemeiaLinkEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "khemeia_link";

    public KhemeiaLinkEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.notebookPage("notebook",
                "Everything the vessels do, the copied tablets call khemeia. The processes are gathered in a separate set of pages: "
                        + this.categoryLink("Khemeia", KhemeiaCategoryProvider.CATEGORY_ID) + ".");
    }

    @Override
    protected String entryName() {
        return "Khemeia";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.STEEPING_JAR.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
