// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.khemeia;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.world.item.Items;

public class SteepingJarEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "steeping_jar";

    public SteepingJarEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The jar is closed with its lid, and the tablets say why. What is cured in the dark must not be seen while it works. Keep it shut, and let the dark do its labour.");

        this.notebookPage("notebook",
                "A clay jar with a brick belly and a lid that stays down while it works. Fill it from the libation spouts and it takes the Primals. Dark compounds form inside, and I do not need to tend it.");

        this.recipeTab("recipe", "Steeping Jar", BookCraftingRecipePageModel.create()
                .withRecipeId1(PrincipiaMagica.loc("steeping_jar")));

        this.stage("rotten_flesh", s -> s
                .title("Rotten flesh")
                .obtain(Items.ROTTEN_FLESH));

        this.infoPage("using", "Using the Jar", """
                The jar holds 1000 mB of Principles and takes them whenever it has room. It is filled the same way as the Wabet Basin: from libation spouts, or by pouring a clay phial into it.
                \\
                \\
                Each pair of Primals cures into its dark compound, 25 mB and 25 mB into 25 mB, and each step takes 160 ticks. Open the Vessel Tap to drain the jar into the block below it. Curing continues while it drains.
                """);
    }

    @Override
    protected String entryName() {
        return "Steeping Jar";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.STEEPING_JAR.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
