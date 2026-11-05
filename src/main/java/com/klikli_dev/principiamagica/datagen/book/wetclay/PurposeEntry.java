// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.datagen.research.PrincipiaResearch;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class PurposeEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "purpose";

    public PurposeEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Let him bear the toil of the gods. Let him bear the basket.");

        this.notebookPage("notebook",
                "I pressed the axiom against its chest and the clay took it like a mouth. It went to the field at once. It has not looked at me since.");

        this.page("giving", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Giving an Axiom");
        this.pageText("""
                Hold a fired axiom and right-click your golem. The axiom sinks into the clay and is spent. A golem that holds an axiom is no longer purposeless: it sets to work at once.
                """
        );

        this.page("modes", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Work, Follow, Stay");
        this.pageText("""
                **Work** makes the golem do its job inside its work area.
                \\
                \\
                **Follow** makes it walk after you.
                \\
                \\
                **Stay** keeps it in place. A golem without an axiom can only follow or stay.
                """
        );

        this.page("screen", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("The Golem Screen");
        this.pageText("""
                Right-click your golem with an empty hand to open its screen. There you set its mode, its work area and its links to containers.
                """
        );

        this.page("removing", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Taking an Axiom Back");
        this.pageText("""
                Shift-right-click your golem with an empty hand, or remove the axiom in its screen. The axiom returns to your hands and the golem is purposeless again. It stays where it is, unless it was set to follow you.
                """
        );

        this.page("work_area", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("The Default Work Area");
        this.pageText("""
                Without a work area of its own, a golem given an axiom marks a field of nine by nine blocks around the spot where it stands, one block down and two up. Containers touching the golem are linked to it and serve as both supply and deposit.
                \\
                \\
                A field and a chest beside the golem are enough. A work area you have already set up is kept.
                """
        );
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return entry.withCondition(this.condition().researchNodeUnlocked(PrincipiaResearch.CLAY_THAT_STANDS));
    }

    @Override
    protected String entryName() {
        return "Purpose";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.AXIOM_OF_HARVEST.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
