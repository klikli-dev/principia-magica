// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.modonomicon.api.datagen.book.page.BookRecipePageModel;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.book.page.BookRecipePage;

public class BookDryingRecipePageModel extends BookRecipePageModel<BookDryingRecipePageModel> {
    protected BookDryingRecipePageModel() {
        super(BookDryingRecipePage.ID);
    }

    public static BookDryingRecipePageModel create() {
        return new BookDryingRecipePageModel();
    }

    @Override
    protected BookPage createPage(BookRecipePage.JsonDataHolder common) {
        return new BookDryingRecipePage(common);
    }
}
