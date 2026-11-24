// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.khemeia;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class QuickenedClayEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "quickened_clay";

    public QuickenedClayEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The clay of the first makers lay dead until it drank. What it drinks is the flesh of the god, and the ghost in that flesh remembers the word. Let the clay drink, and it will hear.");

        this.notebookPage("notebook",
                "Set a clay ball in the Catch Trough and give it 25 mB of unguent. Unguent comes from the Steeping Jar's tap, or from a clay phial poured into the trough. For every 25 mB the clay gives back two quickened clay.");

        this.recipePage("steeping", "Steeping Quickened Clay", PrincipiaMagica.loc("steeping/quickened_clay"));

        this.infoPage("uses", "What It Is For", """
                Quickened clay goes into the Axiom Tablet, the Wooden Golem, the Unfired Clay Phial and the Natron Sponge.
                \\
                \\
                A golem that is hurt takes it too: right-click the golem with quickened clay to heal one health for each clay.
                """);
    }

    @Override
    protected String entryName() {
        return "Quickened Clay";
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
