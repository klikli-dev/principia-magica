// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.datamap;

import com.klikli_dev.principiamagica.registry.DataMapTypeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public class PrincipiaMagicaDataMapProvider extends DataMapProvider {
    public PrincipiaMagicaDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        this.builder(DataMapTypeRegistry.LEAF_SAPLINGS)
                .add(Blocks.OAK_LEAVES.builtInRegistryHolder(), Items.OAK_SAPLING, false)
                .add(Blocks.SPRUCE_LEAVES.builtInRegistryHolder(), Items.SPRUCE_SAPLING, false)
                .add(Blocks.BIRCH_LEAVES.builtInRegistryHolder(), Items.BIRCH_SAPLING, false)
                .add(Blocks.JUNGLE_LEAVES.builtInRegistryHolder(), Items.JUNGLE_SAPLING, false)
                .add(Blocks.ACACIA_LEAVES.builtInRegistryHolder(), Items.ACACIA_SAPLING, false)
                .add(Blocks.DARK_OAK_LEAVES.builtInRegistryHolder(), Items.DARK_OAK_SAPLING, false)
                .add(Blocks.PALE_OAK_LEAVES.builtInRegistryHolder(), Items.PALE_OAK_SAPLING, false)
                .add(Blocks.CHERRY_LEAVES.builtInRegistryHolder(), Items.CHERRY_SAPLING, false)
                .add(Blocks.AZALEA_LEAVES.builtInRegistryHolder(), Items.AZALEA, false)
                .add(Blocks.FLOWERING_AZALEA_LEAVES.builtInRegistryHolder(), Items.FLOWERING_AZALEA, false)
                .add(Blocks.MANGROVE_LEAVES.builtInRegistryHolder(), Items.MANGROVE_PROPAGULE, false);
    }
}
