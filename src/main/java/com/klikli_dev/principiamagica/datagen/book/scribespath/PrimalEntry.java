// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.scribespath;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.List;

/**
 * One entry per Primal. Its washing sources are shown as libation recipe pages, two to a page, and its opposite follows.
 */
public class PrimalEntry extends PrincipiaEntryProvider {

    private final String entryId;
    private final String entryName;
    private final ItemLike icon;
    private final String scribeText;
    private final String notebookText;
    private final String introText;
    private final List<String> washingRecipes;
    private final String oppositeText;

    private PrimalEntry(CategoryProvider parent, String entryId, String entryName, ItemLike icon,
                        String scribeText, String notebookText, String introText, List<String> washingRecipes, String oppositeText) {
        super(parent);
        this.entryId = entryId;
        this.entryName = entryName;
        this.icon = icon;
        this.scribeText = scribeText;
        this.notebookText = notebookText;
        this.introText = introText;
        this.washingRecipes = washingRecipes;
        this.oppositeText = oppositeText;
    }

    public static PrimalEntry silt(CategoryProvider parent) {
        return new PrimalEntry(parent, "primal_silt", "Primal: Silt", Items.DIRT,
                "The first thing the gods dug from the river was silt, and they set it under everything else. Whatever is laid in the mud is laid in silt.",
                "Silt washes out of dirt, clay balls, sand and cobblestone. It is the first Primal I can wash, and loam and mire are made from it.",
                "The libation slab washes Silt out of these offerings.",
                List.of("dirt", "clay_ball", "sand", "cobblestone"),
                "Opposed to **Gleam**. Silt and gleam never cure together.");
    }

    public static PrimalEntry gleam(CategoryProvider parent) {
        return new PrimalEntry(parent, "primal_gleam", "Primal: Gleam", Items.GLOWSTONE_DUST,
                "Gleam is the light the tablets ask for, the brightness that is put into a thing and does not fade.",
                "Gleam washes out of glass, glowstone dust, gold nuggets and feathers. It goes into myrrh and glaze in the light, and into unguent and kohl in the dark.",
                "The libation slab washes Gleam out of these offerings.",
                List.of("glass", "gold_nugget", "glowstone_dust", "feather"),
                "Opposed to **Silt**. Gleam and silt never cure together.");
    }

    public static PrimalEntry sap(CategoryProvider parent) {
        return new PrimalEntry(parent, "primal_sap", "Primal: Sap", Items.WHEAT,
                "Sap is the green of the field, and the field does not grow without the labour of the gods.",
                "Sap is the Primal of growing things. It washes out of wheat, seeds, saplings and leaves, and it goes into loam and myrrh.",
                "The libation slab washes Sap out of these offerings. Rotten flesh gives Sap along with Ash.",
                List.of("wheat", "wheat_seeds", "saplings", "leaves", "rotten_flesh"),
                "Opposed to **Ash**. Sap and ash never cure together.");
    }

    public static PrimalEntry ash(CategoryProvider parent) {
        return new PrimalEntry(parent, "primal_ash", "Primal: Ash", Items.COAL,
                "What the fire leaves behind is ash, and the tablets say it is the remainder of everything that was burned.",
                "Ash washes out of coal, charcoal, flint and bone. It is the Primal of what is burned, and shard and bitumen are made from it.",
                "The libation slab washes Ash out of these offerings. Rotten flesh gives Ash along with Sap.",
                List.of("coal", "charcoal", "flint", "bone", "rotten_flesh"),
                "Opposed to **Sap**. Ash and sap never cure together.");
    }

    @Override
    protected void generatePages() {
        this.scribePage("scribe", this.scribeText);

        this.notebookPage("notebook", this.notebookText);

        // Every Primal unlocks with the libation slab for now. Discovering each Primal the first time it is produced or
        // identified is future work, which needs its own research fact.
        this.stage("primal", s -> s
                .title("A libation slab")
                .obtain(ItemRegistry.LIBATION_SLAB.get()));

        this.infoPage("washing", "Washed From", this.introText);

        for (int i = 0; i < this.washingRecipes.size(); i += 2) {
            var recipe1 = PrincipiaMagica.loc("libation/" + this.washingRecipes.get(i));
            var recipe2 = i + 1 < this.washingRecipes.size() ? PrincipiaMagica.loc("libation/" + this.washingRecipes.get(i + 1)) : null;
            this.recipesPage("washing_" + (i / 2), recipe1, recipe2);
        }

        this.infoPage("opposite", "Opposite", this.oppositeText);
    }

    @Override
    protected String entryName() {
        return this.entryName;
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(this.icon);
    }

    @Override
    protected String entryId() {
        return this.entryId;
    }
}
