// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import net.minecraft.world.item.Items;

public class WetClayCategoryProvider extends CategoryProvider {

    public static final String CATEGORY_ID = "the_wet_clay";

    public WetClayCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        var principiaEntry = this.add(new PrincipiaEntry(this).generate());
        this.layout().entry(principiaEntry).at(0, 0);
    }

    @Override
    protected String categoryName() {
        return "The Wet Clay";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Items.CLAY_BALL);
    }

    @Override
    public String categoryId() {
        return CATEGORY_ID;
    }
}
