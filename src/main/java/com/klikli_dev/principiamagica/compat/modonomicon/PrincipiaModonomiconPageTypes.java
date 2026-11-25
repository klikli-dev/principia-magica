// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon;

import com.klikli_dev.modonomicon.data.BookPageType;
import com.klikli_dev.modonomicon.registry.BookPageTypeRegistry;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookCuringRecipePage;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookDryingRecipePage;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookLibationRecipePage;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookSteepingRecipePage;

public final class PrincipiaModonomiconPageTypes {

    public static final BookPageType<BookCuringRecipePage> CURING_RECIPE = BookPageTypeRegistry.register(BookCuringRecipePage.ID, BookCuringRecipePage.CODEC, BookCuringRecipePage.STREAM_CODEC);
    public static final BookPageType<BookSteepingRecipePage> STEEPING_RECIPE = BookPageTypeRegistry.register(BookSteepingRecipePage.ID, BookSteepingRecipePage.CODEC, BookSteepingRecipePage.STREAM_CODEC);
    public static final BookPageType<BookDryingRecipePage> DRYING_RECIPE = BookPageTypeRegistry.register(BookDryingRecipePage.ID, BookDryingRecipePage.CODEC, BookDryingRecipePage.STREAM_CODEC);
    public static final BookPageType<BookLibationRecipePage> LIBATION_RECIPE = BookPageTypeRegistry.register(BookLibationRecipePage.ID, BookLibationRecipePage.CODEC, BookLibationRecipePage.STREAM_CODEC);

    private PrincipiaModonomiconPageTypes() {
    }

    /**
     * Registers the page types. Must run before the book is loaded.
     */
    public static void bootstrap() {
    }
}
