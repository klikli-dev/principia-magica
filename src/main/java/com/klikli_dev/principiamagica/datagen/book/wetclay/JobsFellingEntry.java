// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.wetclay;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;

public class JobsFellingEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "jobs_felling";

    public JobsFellingEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "The tree was grown to be cut, and the gods allow it to be cut. Let the axe find the trees that are no longer needed, and leave the wood that was built by hands where it stands.");

        this.notebookPage("notebook",
                "The Axiom of Felling sets a golem to the trees. It fells from the stump upward, chops each log with its axe, and replants a sapling on every stump it has one for.");

        this.stage("axiom_of_felling", s -> s
                .title("An Axiom of Felling")
                .obtain(ItemRegistry.AXIOM_OF_FELLING.get()));

        this.infoPage("felled", "What It Fells", """
                Only natural trees are felled. A tree counts as natural when its logs touch leaves that are decaying. Player-built wood is never touched, and trees spreading more than 10 blocks sideways or taller than 48 blocks are left alone.
                \\
                \\
                A better axe chops faster.
                """);

        this.infoPage("options", "Felling Options", """
                **Replant saplings**: plant a sapling on each felled stump.
                \\
                \\
                **Clear blocking leaves**: break natural leaves that wall in a trunk at stump height. Leaves that a player placed are never broken.
                \\
                \\
                **Apply bone meal to saplings**: help the saplings in the zone grow.
                \\
                \\
                **Pick up stray items**: collect the logs, saplings and sticks that lie in the zone.
                """);
    }

    @Override
    protected String entryName() {
        return "Jobs: Felling";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(ItemRegistry.AXIOM_OF_FELLING.get());
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
