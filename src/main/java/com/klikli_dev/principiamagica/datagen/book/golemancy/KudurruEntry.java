// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.golemancy;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
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
                Right-click a block face with the stylus to carve the first corner. Right-click a second face to carve the opposite corner. Together the two glyphs mark a rectangle, no wider, deeper, or taller than 64 blocks. You can carve multiple overlapping boundaries, as long as no two corners occupy the same block.
                """
        );

        this.page("raise", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Lifting a Corner");
        this.pageText("""
                Hold shift and scroll over a corner glyph with the stylus to lift or lower that corner along its plumb line, up to the 64-block boundary height limit. When the corners sit at different heights, the boundary is a box, and the golem works the whole box, reaching as far as its axiom allows.
                """
        );

        this.page("move", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Moving a Boundary");
        this.pageText("""
                Aim at a boundary box and hold shift while scrolling to move all of it up or down at once. Its corners keep their blocks and move with it, as long as they do not collide with another corner. A flat boundary stays flat, and the golem decides how far it reaches from its level.
                """
        );

        this.page("erase", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Erasing a Boundary");
        this.pageText("""
                Shift-right-click a carved glyph or anywhere on a boundary box with the stylus to erase it, and the field it belonged to is gone. You can do this from a distance: aim at a glyph or box and it highlights, then shift-right-click to erase. Breaking the block a glyph is carved into does not erase it.
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
