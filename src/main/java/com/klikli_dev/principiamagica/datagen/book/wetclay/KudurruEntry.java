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

public class KudurruEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "kudurru";

    public KudurruEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "This is the boundary of the field. Whoever moves this stone, whoever says \"the field is not bounded\", may the gods of the land curse him, may his name be torn out.");

        this.notebookPage("notebook",
                "The old landholders carved their boundaries in stone and cursed whoever moved them. My golem needs only the marks. Carved corners tell it where its field begins and ends; it decides how high to reach unless I lift the boundary myself.");

        this.page("select", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Selecting a Golem");
        this.pageText("""
                Right-click your golem with the stylus to select it. Right-click it again to deselect it. Only the selected golem shows its boundaries while you hold the stylus.
                """
        );

        this.page("carve", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Carving the Boundary");
        this.pageText("""
                Right-click a block face with the stylus to carve the first corner. Right-click a second face to carve the opposite corner. Together the two glyphs mark a rectangle on the ground, no wider and no deeper than 64 blocks.
                """
        );

        this.page("raise", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Raising and Lowering");
        this.pageText("""
                Hold shift and scroll over a boundary with the stylus to lift it. The golem works at the lifted level, and the axiom decides how far above and below that level it reaches. Lift it up to sixteen blocks, or sink it down to eight blocks below the ground.
                """
        );

        this.page("erase", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Erasing a Boundary");
        this.pageText("""
                Shift-right-click a carved glyph with the stylus to erase its boundary. Breaking the block a glyph is carved into erases it as well. Either way, the field it belonged to is gone.
                """
        );

        this.page("link", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Linking Containers");
        this.pageText("""
                With your golem selected, right-click a chest with the stylus to link it. Each further right-click changes the link: supply, then deposit, then both, then unlinked again. A supply chest gives the golem seeds, saplings and bone meal. A deposit chest takes the harvest. Shift-right-click a linked chest to remove its link at once, and the boundary around it stays as it is.
                """
        );

        this.page("link_auto", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Linked by Default");
        this.pageText("""
                The chests touching the golem when it is given an axiom are linked both ways, so a golem placed beside its field and a chest works right away.
                """
        );

        this.notebookPage("link_notebook",
                "A chest is only a chest until the golem knows what to do with it. Let it fetch from one, and let it fill another. I do not need to touch either myself.");
    }

    @Override
    protected BookEntryModel additionalSetup(BookEntryModel entry) {
        return entry.withCondition(this.condition().researchNodeUnlocked(PrincipiaResearch.PURPOSE));
    }

    @Override
    protected String entryName() {
        return "Kudurru";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.COPPER_STYLUS.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
