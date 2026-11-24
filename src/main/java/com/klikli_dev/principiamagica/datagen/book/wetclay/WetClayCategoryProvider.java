// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaCategoryProvider;
import net.minecraft.world.item.Items;

public class WetClayCategoryProvider extends PrincipiaCategoryProvider {

    public static final String CATEGORY_ID = "the_wet_clay";

    public WetClayCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        var rootEntry = this.add(new WetClayRootEntry(this).generate());
        this.layout().entry(rootEntry).at(0, 0);

        var fleshAndClayEntry = this.add(new FleshAndClayEntry(this).generate());
        this.layout().entry(fleshAndClayEntry).at(1, 0);
        fleshAndClayEntry.withParent(rootEntry).hideWhileLocked(true);

        var axiomTabletEntry = this.add(new AxiomTabletEntry(this).generate());
        this.layout().entry(axiomTabletEntry).at(1, 1);
        axiomTabletEntry.withParent(rootEntry).hideWhileLocked(true);

        var axiomEntry = this.add(new AxiomEntry(this).generate());
        this.layout().entry(axiomEntry).at(2, 1);
        axiomEntry.withParent(axiomTabletEntry).hideWhileLocked(true);

        var theBodyEntry = this.add(new TheBodyEntry(this).generate());
        this.layout().entry(theBodyEntry).at(2, 0);
        theBodyEntry.withParent(fleshAndClayEntry).hideWhileLocked(true);

        var purposeEntry = this.add(new PurposeEntry(this).generate());
        this.layout().entry(purposeEntry).at(3, 0);
        purposeEntry.withParent(theBodyEntry).hideWhileLocked(true);

        var kudurruEntry = this.add(new KudurruEntry(this).generate());
        this.layout().entry(kudurruEntry).at(4, 0);
        kudurruEntry.withParent(purposeEntry).hideWhileLocked(true);
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
