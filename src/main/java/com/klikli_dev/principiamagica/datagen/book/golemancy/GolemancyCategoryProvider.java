// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.golemancy;

import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaCategoryProvider;
import net.minecraft.world.item.Items;

/**
 * Golemancy: the bodies of golems, the laws given to them, and the work they do. Launched from the hub.
 */
public class GolemancyCategoryProvider extends PrincipiaCategoryProvider {

    public static final String CATEGORY_ID = "golemancy";

    public GolemancyCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        // Entries sit on even grid coordinates, so every entry has at least one empty field between it and its neighbours.
        var introEntry = this.add(new GolemancyIntroEntry(this).generate());
        this.layout().entry(introEntry).at(0, 0);

        var theBodyEntry = this.add(new TheBodyEntry(this).generate());
        this.layout().entry(theBodyEntry).at(2, 0);
        theBodyEntry.withParent(introEntry).hideWhileLocked(true);

        var purposeEntry = this.add(new PurposeEntry(this).generate());
        this.layout().entry(purposeEntry).at(4, 0);
        purposeEntry.withParent(theBodyEntry).hideWhileLocked(true);

        var kudurruEntry = this.add(new KudurruEntry(this).generate());
        this.layout().entry(kudurruEntry).at(6, 0);
        kudurruEntry.withParent(purposeEntry).hideWhileLocked(true);

        var careOfGolemsEntry = this.add(new CareOfGolemsEntry(this).generate());
        this.layout().entry(careOfGolemsEntry).at(2, 2);
        careOfGolemsEntry.withParent(theBodyEntry).hideWhileLocked(true);

        var axiomEntry = this.add(new AxiomEntry(this).generate());
        this.layout().entry(axiomEntry).at(0, 2);
        axiomEntry.withParent(introEntry).hideWhileLocked(true);

        var jobsHarvestEntry = this.add(new JobsHarvestEntry(this).generate());
        this.layout().entry(jobsHarvestEntry).at(0, 4);
        jobsHarvestEntry.withParent(axiomEntry).hideWhileLocked(true);

        var jobsFellingEntry = this.add(new JobsFellingEntry(this).generate());
        this.layout().entry(jobsFellingEntry).at(2, 4);
        jobsFellingEntry.withParent(axiomEntry).hideWhileLocked(true);
    }

    @Override
    protected String categoryName() {
        return "Golemancy";
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
