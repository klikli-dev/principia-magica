// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.khemeia;

import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaCategoryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

/**
 * The alchemy category. It groups all alchemical processes: libation, curing, steeping and drying, and later the
 * processes still to come.
 */
public class KhemeiaCategoryProvider extends PrincipiaCategoryProvider {

    public static final String CATEGORY_ID = "khemeia";

    public KhemeiaCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        // Entries sit on even grid coordinates, so every entry has at least one empty field between it and its neighbours.
        var introEntry = this.add(new KhemeiaIntroEntry(this).generate());
        this.layout().entry(introEntry).at(0, 0);

        var steepingJarEntry = this.add(new SteepingJarEntry(this).generate());
        this.layout().entry(steepingJarEntry).at(2, 0);
        steepingJarEntry.withParent(introEntry).hideWhileLocked(true);

        var darkCompoundsEntry = this.add(new DarkCompoundsEntry(this).generate());
        this.layout().entry(darkCompoundsEntry).at(4, 0);
        darkCompoundsEntry.withParent(steepingJarEntry).hideWhileLocked(true);

        var steepingEntry = this.add(new SteepingEntry(this).generate());
        this.layout().entry(steepingEntry).at(6, 0);
        steepingEntry.withParent(darkCompoundsEntry).hideWhileLocked(true);

        var wabetBasinEntry = this.add(new WabetBasinEntry(this).generate());
        this.layout().entry(wabetBasinEntry).at(6, 2);
        wabetBasinEntry.withParent(steepingEntry).hideWhileLocked(true);

        var lightCompoundsEntry = this.add(new LightCompoundsEntry(this).generate());
        this.layout().entry(lightCompoundsEntry).at(8, 2);
        lightCompoundsEntry.withParent(wabetBasinEntry).hideWhileLocked(true);

        var dryingSlabEntry = this.add(new DryingSlabEntry(this).generate());
        this.layout().entry(dryingSlabEntry).at(10, 2);
        dryingSlabEntry.withParent(lightCompoundsEntry).hideWhileLocked(true);

        var lustralWatersEntry = this.add(new LustralWatersEntry(this).generate());
        this.layout().entry(lustralWatersEntry).at(10, 4);
        lustralWatersEntry.withParent(dryingSlabEntry).hideWhileLocked(true);
    }

    @Override
    protected String categoryName() {
        return "Khemeia";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(ItemRegistry.STEEPING_JAR.get());
    }

    @Override
    public String categoryId() {
        return CATEGORY_ID;
    }
}
