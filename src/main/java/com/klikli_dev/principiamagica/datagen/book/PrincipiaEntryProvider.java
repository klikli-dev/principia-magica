// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryTabModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookCuringRecipePageModel;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookDryingRecipePageModel;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookSteepingRecipePageModel;
import net.minecraft.resources.Identifier;

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

    /**
     * Adds a plain text page with the given id, title and text, e.g. for practical notes on a mechanic.
     */
    protected void infoPage(String pageId, String title, String text) {
        this.textPage(pageId, title, text);
    }

    /**
     * Adds a page that shows a single principle recipe (curing, steeping or drying) with the given title.
     * The page is drawn by the book itself, so it does not need a recipe viewer.
     */
    protected void recipePage(String pageId, String title, Identifier recipeId) {
        this.page(pageId, () -> principleRecipePage(recipeId)
                .withTitle1(this.context().pageTitle())
                .withRecipeId1(recipeId));
        this.pageTitle(title);
    }

    /**
     * @return the page model for the recipe, chosen by the recipe folder, e.g. {@code curing/light/loam}.
     */
    private static BookRecipePageModel<?> principleRecipePage(Identifier recipeId) {
        return switch (recipeId.getPath().split("/")[0]) {
            case "curing" -> BookCuringRecipePageModel.create();
            case "steeping" -> BookSteepingRecipePageModel.create();
            case "drying" -> BookDryingRecipePageModel.create();
            default -> throw new IllegalArgumentException("No principle recipe page for " + recipeId);
        };
    }

    private void textPage(String pageId, String title, String text) {
        this.page(pageId, () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle(title);
        this.pageText(text);
    }

    /**
     * Adds a tab for the entry that opens the given recipe page over the book, on the right edge.
     * The tab icon is derived from the recipe output.
     */
    protected void recipeTab(String tabId, String title, BookPageModel<?> page) {
        this.tab(tabId, () -> BookEntryTabModel.create()
                .withPage(page)
                .withTitle(this.tabTitleKey())
        );
        this.tabTitle(title);
    }
}
