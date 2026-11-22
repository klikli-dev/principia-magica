// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.loot;

import com.klikli_dev.principiamagica.registry.BlockRegistry;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;

public class PrincipiaMagicaBlockLootSubProvider extends BlockLootSubProvider {

    public PrincipiaMagicaBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Override
    protected void generate() {
        this.dropSelf(BlockRegistry.LIBATION_SLAB.get());
        this.dropSelf(BlockRegistry.LIBATION_SPOUT.get());
        this.dropSelf(BlockRegistry.VESSEL_TAP.get());
        this.dropSelf(BlockRegistry.DRYING_SLAB.get());
        this.add(BlockRegistry.CATCH_TROUGH.get(), this.createPrincipleContainerTable(BlockRegistry.CATCH_TROUGH.get()));
        this.add(BlockRegistry.WABET_BASIN.get(), this.createPrincipleContainerTable(BlockRegistry.WABET_BASIN.get()));
        this.add(BlockRegistry.STEEPING_JAR.get(), this.createPrincipleContainerTable(BlockRegistry.STEEPING_JAR.get()));
    }

    /**
     * Drops the block with the principles it holds, stored in {@link DataComponentRegistry#STORED_PRINCIPLES}.
     */
    private LootTable.Builder createPrincipleContainerTable(Block block) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block)
                                .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                        .include(DataComponentRegistry.STORED_PRINCIPLES.get()))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(
                BlockRegistry.LIBATION_SLAB.get(),
                BlockRegistry.LIBATION_SPOUT.get(),
                BlockRegistry.VESSEL_TAP.get(),
                BlockRegistry.CATCH_TROUGH.get(),
                BlockRegistry.WABET_BASIN.get(),
                BlockRegistry.STEEPING_JAR.get(),
                BlockRegistry.DRYING_SLAB.get()
        );
    }
}
