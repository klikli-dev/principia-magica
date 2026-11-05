// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

/**
 * Base for entries of the Principia book. Every entry consists of a notebook passage (the player's own observations)
 * and a scribe passage (translated tablet text, rendered in italics).
 */
public abstract class PrincipiaEntryProvider extends EntryProvider {

    public static final String NOTEBOOK_TITLE = "Notebook";
    public static final String SCRIBE_TITLE = "Scribe";

    public PrincipiaEntryProvider(CategoryProvider parent) {
        super(parent);
    }

    /**
     * Adds a notebook page with the given id and text.
     */
    protected void notebookPage(String pageId, String text) {
        this.textPage(pageId, NOTEBOOK_TITLE, text);
    }

    /**
     * Adds a scribe page with the given id and text. The text is rendered in italics.
     */
    protected void scribePage(String pageId, String text) {
        this.textPage(pageId, SCRIBE_TITLE, "*" + text + "*");
    }

    private void textPage(String pageId, String title, String text) {
        this.page(pageId, () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle(title);
        this.pageText(text);
    }
}
