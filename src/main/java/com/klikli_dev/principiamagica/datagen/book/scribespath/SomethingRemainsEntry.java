// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.scribespath;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import net.minecraft.world.item.Items;

/**
 * The first hint of dark curing. It only points toward the flesh, it does not describe a recipe.
 */
public class SomethingRemainsEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "something_remains";

    public SomethingRemainsEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Something of life is left in the flesh of the dead. The tablets do not say it is good. They say only that it remembers, and that the clay will want it.");

        this.notebookPage("notebook",
                "Rotten flesh is no food. But the dead keep something in it, and the tablets speak of flesh and clay in the same breath. I have been wondering whether one needs the other. I will look for it where the light does not reach.");

        this.stage("rotten_flesh", s -> s
                .title("Rotten flesh")
                .obtain(Items.ROTTEN_FLESH));
    }

    @Override
    protected String entryName() {
        return "Something Remains";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.ROTTEN_FLESH);
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
