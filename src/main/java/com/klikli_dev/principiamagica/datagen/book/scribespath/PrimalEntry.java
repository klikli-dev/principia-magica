// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book.scribespath;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaEntryProvider;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Items;

/**
 * One entry per Primal. Each lists what the libation slab washes out of its offerings, and its opposite.
 */
public class PrimalEntry extends PrincipiaEntryProvider {

    private final String entryId;
    private final String entryName;
    private final ItemLike icon;
    private final String scribeText;
    private final String notebookText;
    private final String washingText;
    private final String oppositeText;

    private PrimalEntry(CategoryProvider parent, String entryId, String entryName, ItemLike icon,
                        String scribeText, String notebookText, String washingText, String oppositeText) {
        super(parent);
        this.entryId = entryId;
        this.entryName = entryName;
        this.icon = icon;
        this.scribeText = scribeText;
        this.notebookText = notebookText;
        this.washingText = washingText;
        this.oppositeText = oppositeText;
    }

    public static PrimalEntry silt(CategoryProvider parent) {
        return new PrimalEntry(parent, "primal_silt", "Primal: Silt", Items.DIRT,
                "The first thing the gods dug from the river was silt, and they set it under everything else. Whatever is laid in the mud is laid in silt.",
                "Silt washes out of dirt, clay balls, sand and cobblestone. It is the first Primal I can wash, and loam and mire are made from it.",
                """
                        - Dirt: 50 mB
                        - Clay ball: 50 mB
                        - Sand: 50 mB
                        - Cobblestone: 50 mB
                        """,
                "Opposed to **Gleam**. Silt and gleam never cure together.");
    }

    public static PrimalEntry gleam(CategoryProvider parent) {
        return new PrimalEntry(parent, "primal_gleam", "Primal: Gleam", Items.GLOWSTONE_DUST,
                "Gleam is the light the tablets ask for, the brightness that is put into a thing and does not fade.",
                "Gleam washes out of glass, glowstone dust, gold nuggets and feathers. It goes into myrrh and glaze in the light, and into unguent and kohl in the dark.",
                """
                        - Glass: 50 mB
                        - Glowstone dust: 50 mB
                        - Gold nugget: 25 mB
                        - Feather: 25 mB
                        """,
                "Opposed to **Silt**. Gleam and silt never cure together.");
    }

    public static PrimalEntry sap(CategoryProvider parent) {
        return new PrimalEntry(parent, "primal_sap", "Primal: Sap", Items.WHEAT,
                "Sap is the green of the field, and the field does not grow without the labour of the gods.",
                "Sap is the Primal of growing things. It washes out of wheat, seeds, saplings and leaves, and rotten flesh gives it along with ash. It goes into loam and myrrh.",
                """
                        - Wheat: 50 mB
                        - Saplings (any): 50 mB
                        - Wheat seeds: 25 mB
                        - Leaves (any): 25 mB
                        - Rotten flesh: 25 mB, with 25 mB of ash
                        """,
                "Opposed to **Ash**. Sap and ash never cure together.");
    }

    public static PrimalEntry ash(CategoryProvider parent) {
        return new PrimalEntry(parent, "primal_ash", "Primal: Ash", Items.COAL,
                "What the fire leaves behind is ash, and the tablets say it is the remainder of everything that was burned.",
                "Ash washes out of coal, charcoal, flint and bone. It is the Primal of what is burned, and shard and bitumen are made from it.",
                """
                        - Coal: 50 mB
                        - Charcoal: 50 mB
                        - Flint: 50 mB
                        - Bone: 50 mB
                        - Rotten flesh: 25 mB, with 25 mB of sap
                        """,
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

        this.infoPage("washing", "Washed From", this.washingText);

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
