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
import net.minecraft.world.item.Items;

public class LightCompoundsEntry extends PrincipiaEntryProvider {

    public static final String ENTRY_ID = "light_compounds";

    public LightCompoundsEntry(CategoryProvider parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe",
                "Loam is the earth the harvest grows in, and shard is the stone the axe is set in. The sun-court made them both. The law is given its purpose through them.");

        this.notebookPage("notebook",
                "Loam and shard are what the axes of the harvest and the felling need. Myrrh and glaze I have not yet found a use for, but I will write them down all the same.");

        this.recipePage("loam", "Loam", PrincipiaMagica.loc("curing/light/loam"));
        this.recipePage("shard", "Shard", PrincipiaMagica.loc("curing/light/shard"));
        this.recipePage("myrrh", "Myrrh", PrincipiaMagica.loc("curing/light/myrrh"));
        this.recipePage("glaze", "Glaze", PrincipiaMagica.loc("curing/light/glaze"));
    }

    @Override
    protected String entryName() {
        return "Light Compounds";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.GLOWSTONE_DUST);
    }

    @Override
    protected String entryId() {
        return ENTRY_ID;
    }
}
