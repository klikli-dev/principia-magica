// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.scribespath;

import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaCategoryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

/**
 * The hub of the book. Always open. It holds the root, the stylus, the libation pages, and links to the alchemy
 * and golem categories.
 */
public class ScribesPathCategoryProvider extends PrincipiaCategoryProvider {

    public static final String CATEGORY_ID = "scribes_path";

    public ScribesPathCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        var principiaEntry = this.add(new PrincipiaEntry(this).generate());
        this.layout().entry(principiaEntry).at(0, 0);

        var stylusEntry = this.add(new StylusEntry(this).generate());
        this.layout().entry(stylusEntry).at(1, 0);
        stylusEntry.withParent(principiaEntry).hideWhileLocked(true);

        var clayRemembersEntry = this.add(new ClayRemembersEntry(this).generate());
        this.layout().entry(clayRemembersEntry).at(2, 0);
        clayRemembersEntry.withParent(stylusEntry).hideWhileLocked(true);

        var somethingRemainsEntry = this.add(new SomethingRemainsEntry(this).generate());
        this.layout().entry(somethingRemainsEntry).at(0, 1);
        somethingRemainsEntry.withParent(principiaEntry).hideWhileLocked(true);

        var libationSlabEntry = this.add(new LibationSlabEntry(this).generate());
        this.layout().entry(libationSlabEntry).at(1, 1);
        libationSlabEntry.withParent(principiaEntry).hideWhileLocked(true);

        var spoutAndTapEntry = this.add(new SpoutAndTapEntry(this).generate());
        this.layout().entry(spoutAndTapEntry).at(2, 1);
        spoutAndTapEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var catchTroughEntry = this.add(new CatchTroughEntry(this).generate());
        this.layout().entry(catchTroughEntry).at(3, 1);
        catchTroughEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var siltEntry = this.add(PrimalEntry.silt(this).generate());
        this.layout().entry(siltEntry).at(1, 2);
        siltEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var gleamEntry = this.add(PrimalEntry.gleam(this).generate());
        this.layout().entry(gleamEntry).at(2, 2);
        gleamEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var sapEntry = this.add(PrimalEntry.sap(this).generate());
        this.layout().entry(sapEntry).at(3, 2);
        sapEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var ashEntry = this.add(PrimalEntry.ash(this).generate());
        this.layout().entry(ashEntry).at(4, 2);
        ashEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var oppositesEntry = this.add(new OppositesEntry(this).generate());
        this.layout().entry(oppositesEntry).at(1, 3);
        oppositesEntry.withParent(libationSlabEntry).hideWhileLocked(true);

        var clayPhialsEntry = this.add(new ClayPhialsEntry(this).generate());
        this.layout().entry(clayPhialsEntry).at(4, 1);
        clayPhialsEntry.withParent(catchTroughEntry).hideWhileLocked(true);

        var natronSpongeEntry = this.add(new NatronSpongeEntry(this).generate());
        this.layout().entry(natronSpongeEntry).at(5, 1);
        natronSpongeEntry.withParent(catchTroughEntry).hideWhileLocked(true);

        // The alchemy link is always visible: the Khemeia category is open from the start.
        var khemeiaLinkEntry = this.add(new KhemeiaLinkEntry(this).generate());
        this.layout().entry(khemeiaLinkEntry).at(6, 1);
        khemeiaLinkEntry.withParent(principiaEntry);

        var wetClayLinkEntry = this.add(new WetClayLinkEntry(this).generate());
        this.layout().entry(wetClayLinkEntry).at(7, 1);
        wetClayLinkEntry.withParent(khemeiaLinkEntry).hideWhileLocked(true);
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
