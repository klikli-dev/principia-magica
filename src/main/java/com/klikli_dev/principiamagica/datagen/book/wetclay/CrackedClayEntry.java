// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.datagen.research.PrincipiaResearch;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class CrackedClayEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "cracked_clay";

    public CrackedClayEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "What is fired may break. What is broken remembers.");

        this.notebookPage("notebook",
                "It fell apart in the field. The axiom inside was whole. A little more quickened clay and it stood again, and went back to work as if nothing had happened. Perhaps for it nothing did.");

        this.page("death", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("When a Golem Falls");
        this.pageText("""
                A golem that dies collapses into a cracked golem. Its axiom, its seals and its workplace stay with it, so the work you carved and the law you gave it are not lost.
                \\
                \\
                Its inventory and whatever it held in its hands are dropped where it fell. Pick them up quickly, dropped things do not last forever.
                """
        );

        this.page("repair", () -> BookSpotlightPageModel.create()
                .withItem(ItemRegistry.CRACKED_GOLEM.get())
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Repairing the Body");
        this.pageText("""
                Craft a copper stylus, the cracked golem and a quickened clay in any slots of the crafting grid. The stylus is returned to you, the clay is used up.
                \\
                \\
                The result is the golem again, with its axiom, seals and workplace. Repairing it takes flesh again, so keep rotten flesh at hand.
                """
        );
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return entry.withCondition(this.condition().researchNodeUnlocked(PrincipiaResearch.PURPOSE));
    }

    @Override
    protected String entryName() {
        return "Cracked Clay";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.CRACKED_GOLEM.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
