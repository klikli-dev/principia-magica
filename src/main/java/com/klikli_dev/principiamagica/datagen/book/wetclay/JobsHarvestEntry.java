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

public class JobsHarvestEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "jobs_harvest";

    public JobsHarvestEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Let the grain stand where the law says, and let it be cut when it is ripe. Let the field be sown again before the sun has moved.");

        this.notebookPage("notebook",
                "The Axiom of Harvest sets a golem to the field. It cuts ripe crops, replants them from the seeds it carries, and fetches seeds from supply chests when it runs short.");

        this.stage("axiom_of_harvest", s -> s
                .title("An Axiom of Harvest")
                .obtain(ItemRegistry.AXIOM_OF_HARVEST.get()));

        this.infoPage("options", "Harvest Options", """
                **Replant crops**: sow the field again after the harvest.
                \\
                \\
                **Clear obstructions**: remove grass, flowers and similar from the work zone.
                \\
                \\
                **Till soil near water**: turn the soil with a hoe, if the golem carries one.
                \\
                \\
                **Apply bone meal**: help crops grow.
                \\
                \\
                **Pick up stray items**: collect what lies in the field.
                """);
    }

    @Override
    protected String entryName() {
        return "Jobs: Harvest";
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
