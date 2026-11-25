// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.golemancy;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.datagen.research.PrincipiaResearch;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class CareOfGolemsEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "care_of_golems";

    public CareOfGolemsEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Flesh that is torn must be mended with flesh. The clay that holds the body will take the mending, and the body will stand again.");

        this.notebookPage("notebook",
                "A hurt golem can be mended with quickened clay. When it has to move, I can pick it up with the stylus and set it down elsewhere, and it keeps everything it was given.");

        this.stage("clay_that_stands", s -> s
                .fact(PrincipiaResearch.GOLEM_PLACED)
                .title("Clay that stands")
                .icon(ItemRegistry.GOLEM.get()));

        this.recipeTab("imprint_recipe", "Imprint Tablet", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("imprint_tablet")));

        this.infoPage("copying", "Copying a Workplace", """
                Right-click one of your golems with the imprint tablet to copy its work zones and container links. Right-click another of your golems to paste them there.
                \\
                \\
                The copy is stored relative to the golem's anchor, so it lands in the same place around the new golem. Crouch-right-click the tablet to clear the copy.
                """);

        this.infoPage("healing", "Healing a Golem", """
                Right-click a hurt golem with quickened clay. Each clay heals one health. A golem at full health is not affected, so its other interactions still work.
                \\
                \\
                Creative players keep their clay.
                """);

        this.infoPage("picking_up", "Picking Up a Golem", """
                Crouch-left-click a golem with the stylus to pick it up. It becomes a golem item again, and it keeps its work zone, its inventory and its equipment. Nothing drops. Placing the item restores all of it.
                """);

        this.infoPage("carrying", "What a Golem Carries", """
                A golem carries two items: one in its hand and one on its back. Both are kept when the golem is picked up with the stylus.
                """);
    }

    @Override
    protected String entryName() {
        return "Care of Golems";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.QUICKENED_CLAY.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
