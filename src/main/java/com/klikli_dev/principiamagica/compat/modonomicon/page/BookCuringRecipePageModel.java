// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.book.page.BookRecipePage;

public class BookCuringRecipePageModel extends BookRecipePageModel<BookCuringRecipePageModel> {
    protected BookCuringRecipePageModel() {
        super(BookCuringRecipePage.ID);
    }

    public static BookCuringRecipePageModel create() {
        return new BookCuringRecipePageModel();
    }

    @Override
    protected BookPage createPage(BookRecipePage.JsonDataHolder common) {
        return new BookCuringRecipePage(common);
    }
}
