// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.book.page.BookRecipePage;

public class BookLibationRecipePageModel extends BookRecipePageModel<BookLibationRecipePageModel> {
    protected BookLibationRecipePageModel() {
        super(BookLibationRecipePage.ID);
    }

    public static BookLibationRecipePageModel create() {
        return new BookLibationRecipePageModel();
    }

    @Override
    protected BookPage createPage(BookRecipePage.JsonDataHolder common) {
        return new BookLibationRecipePage(common);
    }
}
