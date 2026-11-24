// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookCategoryModel;

/**
 * Base for categories of the Principia book. All categories share the clay background.
 */
public abstract class PrincipiaCategoryProvider extends CategoryProvider {

    public PrincipiaCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected BookCategoryModel additionalSetup(BookCategoryModel category) {
        // The node screen background is not part of the book theme, so it is set here.
        return super.additionalSetup(category)
                .withBackground(this.modLoc("textures/gui/sprites/modonomicon/themes/clay/background.png"))
                .withBackgroundSize(140, 140);
    }
}
