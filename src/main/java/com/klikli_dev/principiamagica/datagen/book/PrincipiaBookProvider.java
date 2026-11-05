// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book;

import com.klikli_dev.modonomicon.api.datagen.SingleBookSubProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookModel;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.wetclay.WetClayCategoryProvider;
import com.klikli_dev.principiamagica.registry.CreativeModeTabRegistry;

public class PrincipiaBookProvider extends SingleBookSubProvider {

    public static final String BOOK_ID = "principia";

    public PrincipiaBookProvider() {
        super(BOOK_ID, PrincipiaMagica.MODID);
    }

    @Override
    protected void registerDefaultMacros() {
    }

    @Override
    protected void generateCategories() {
        this.add(new WetClayCategoryProvider(this).generate());
    }

    @Override
    protected String bookName() {
        return "Principia";
    }

    @Override
    protected String bookTooltip() {
        return "A notebook of copied tablets.\n\u00A7o(In-Game Guide for Principia Magica)\u00A7r";
    }

    @Override
    protected BookModel additionalSetup(BookModel book) {
        return super.additionalSetup(book)
                .withGenerateBookItem(false)
                .withCustomBookItem(this.modLoc(BOOK_ID))
                .withCreativeTab(CreativeModeTabRegistry.PRINCIPIA_MAGICA.getId())
                .withGenerateEntryHierarchyResearch(true);
    }
}
