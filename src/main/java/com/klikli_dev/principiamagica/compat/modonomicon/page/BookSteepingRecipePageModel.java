// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.book.page.BookRecipePage;

public class BookSteepingRecipePageModel extends BookRecipePageModel<BookSteepingRecipePageModel> {
    protected BookSteepingRecipePageModel() {
        super(BookSteepingRecipePage.ID);
    }

    public static BookSteepingRecipePageModel create() {
        return new BookSteepingRecipePageModel();
    }

    @Override
    protected BookPage createPage(BookRecipePage.JsonDataHolder common) {
        return new BookSteepingRecipePage(common);
    }
}
