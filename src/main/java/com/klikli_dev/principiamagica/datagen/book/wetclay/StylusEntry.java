// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.world.item.Items;

public class StylusEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "the_stylus";

    public StylusEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.notebookPage("notebook",
                "A cut reed would do, but copper holds its edge. With this I can carve the clay, and, if the tablets are right, command what the clay holds.");

        this.stage("rotten_flesh", s -> s
                .title("Rotten flesh")
                .obtain(Items.ROTTEN_FLESH));

        this.scribePage("scribe",
                "The reed is cut, the clay is wet, the word goes in. What is pressed in the clay does not leave it.");

        this.page("uses", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Two Uses");
        this.pageText("""
                As a crafting tool, the stylus is never consumed: it returns to your grid after every recipe that uses it.
                \\
                \\
                As a configuration tool, it selects a golem, carves, lifts and moves its work boundaries, and links its containers.
                """
        );
    }

    @Override
    protected String entryName() {
        return "The Stylus";
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
