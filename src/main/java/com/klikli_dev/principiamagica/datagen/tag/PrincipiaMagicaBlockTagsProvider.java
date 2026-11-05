// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.tag;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingTargets;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.GourdHandler;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.StackedPlantHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class PrincipiaMagicaBlockTagsProvider extends BlockTagsProvider {
    public PrincipiaMagicaBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, PrincipiaMagica.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(GourdHandler.HARVESTABLE)
                .add(Blocks.PUMPKIN)
                .add(Blocks.MELON);

        this.tag(StackedPlantHandler.HARVESTABLE)
                .add(Blocks.SUGAR_CANE)
                .add(Blocks.CACTUS)
                .add(Blocks.BAMBOO);

        this.tag(FarmingTargets.CLEARABLE)
                .addTag(BlockTags.SMALL_FLOWERS)
                .add(Blocks.SHORT_GRASS)
                .add(Blocks.TALL_GRASS)
                .add(Blocks.FERN)
                .add(Blocks.LARGE_FERN)
                .add(Blocks.DEAD_BUSH)
                .add(Blocks.BUSH)
                .add(Blocks.FIREFLY_BUSH)
                .add(Blocks.SHORT_DRY_GRASS)
                .add(Blocks.TALL_DRY_GRASS)
                .add(Blocks.LEAF_LITTER)
                .add(Blocks.SUNFLOWER)
                .add(Blocks.LILAC)
                .add(Blocks.ROSE_BUSH)
                .add(Blocks.PEONY);
    }
}
