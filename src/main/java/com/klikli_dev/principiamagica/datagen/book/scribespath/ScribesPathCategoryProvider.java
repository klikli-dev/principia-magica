// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.scribespath;

import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaCategoryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

/**
 * The hub of the book. Always open. It holds the root, the stylus, the libation pages, the quickened clay group, and
 * links to the alchemy and golemancy categories.
 */
public class ScribesPathCategoryProvider extends PrincipiaCategoryProvider {

    public static final String CATEGORY_ID = "scribes_path";

    public ScribesPathCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        // Entries sit on even grid coordinates, so every entry has at least one empty field between it and its neighbours.
        // Libation Slab -> Catch Trough -> Khemeia link -> Golemancy link runs along the middle row.
        var principiaEntry = this.add(new PrincipiaEntry(this).generate());
        this.layout().entry(principiaEntry).at(0, 0);

        var stylusEntry = this.add(new StylusEntry(this).generate());
        this.layout().entry(stylusEntry).at(2, 0);
        stylusEntry.withParent(principiaEntry).hideWhileLocked(true);

        var clayRemembersEntry = this.add(new ClayRemembersEntry(this).generate());
        this.layout().entry(clayRemembersEntry).at(4, 0);
        clayRemembersEntry.withParent(stylusEntry).hideWhileLocked(true);

        var somethingRemainsEntry = this.add(new SomethingRemainsEntry(this).generate());
        this.layout().entry(somethingRemainsEntry).at(0, 2);
        somethingRemainsEntry.withParent(principiaEntry).hideWhileLocked(true);

        var libationSlabEntry = this.add(new LibationSlabEntry(this).generate());
        this.layout().entry(libationSlabEntry).at(2, 2);
        libationSlabEntry.withParent(principiaEntry).hideWhileLocked(true);

        var spoutAndTapEntry = this.add(new SpoutAndTapEntry(this).generate());
        this.layout().entry(spoutAndTapEntry).at(4, 2);
        spoutAndTapEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var catchTroughEntry = this.add(new CatchTroughEntry(this).generate());
        this.layout().entry(catchTroughEntry).at(6, 2);
        catchTroughEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var quickenedClayEntry = this.add(new QuickenedClayEntry(this).generate());
        this.layout().entry(quickenedClayEntry).at(6, 0);
        quickenedClayEntry.withParent(catchTroughEntry).hideWhileLocked(true);

        var khemeiaLinkEntry = this.add(new KhemeiaLinkEntry(this).generate());
        this.layout().entry(khemeiaLinkEntry).at(8, 2);
        khemeiaLinkEntry.withParent(catchTroughEntry).hideWhileLocked(true);

        var golemancyLinkEntry = this.add(new GolemancyLinkEntry(this).generate());
        this.layout().entry(golemancyLinkEntry).at(10, 2);
        golemancyLinkEntry.withParent(khemeiaLinkEntry).hideWhileLocked(true);

        var oppositesEntry = this.add(new OppositesEntry(this).generate());
        this.layout().entry(oppositesEntry).at(0, 4);
        oppositesEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var siltEntry = this.add(PrimalEntry.silt(this).generate());
        this.layout().entry(siltEntry).at(2, 4);
        siltEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var gleamEntry = this.add(PrimalEntry.gleam(this).generate());
        this.layout().entry(gleamEntry).at(4, 4);
        gleamEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var sapEntry = this.add(PrimalEntry.sap(this).generate());
        this.layout().entry(sapEntry).at(6, 4);
        sapEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var ashEntry = this.add(PrimalEntry.ash(this).generate());
        this.layout().entry(ashEntry).at(8, 4);
        ashEntry.withParent(libationSlabEntry).hideWhileLocked(true);
    }

    @Override
    protected String categoryName() {
        return "The Scribe's Path";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(ItemRegistry.PRINCIPIA.get());
    }

    @Override
    public String categoryId() {
        return CATEGORY_ID;
    }
}
