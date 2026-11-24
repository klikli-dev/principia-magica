// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class ImprintTabletEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "imprint_tablet";

    public ImprintTabletEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Copy the law from one tablet onto another, and the second will be the same as the first. Do not copy the law of a man who is not yours.");

        this.notebookPage("notebook",
                "A tablet that copies the work of one golem and pastes it onto another. I have wanted this since I first had two golems to tend.");

        this.stage("imprint_tablet", s -> s
                .title("An imprint tablet")
                .obtain(ItemRegistry.IMPRINT_TABLET.get()));

        this.recipeTab("recipe", "Imprint Tablet", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("imprint_tablet")));

        this.infoPage("copying", "Copying and Pasting", """
                Right-click one of your golems with the imprint tablet to copy its work zones and container links. Right-click another of your golems to paste them there.
                \\
                \\
                The copy is stored relative to the golem's anchor, so it lands in the same place around the new golem. Crouch-right-click the tablet to clear the copy.
                """);
    }

    @Override
    protected String entryName() {
        return "Imprint Tablet";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.IMPRINT_TABLET.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
