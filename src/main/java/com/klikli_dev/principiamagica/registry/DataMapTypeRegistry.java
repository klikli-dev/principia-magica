// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class DataMapTypeRegistry {
    /**
     * The sapling that grows from a leaf block, used by the lumberjack job to replant felled trees.
     */
    public static final DataMapType<Block, Item> LEAF_SAPLINGS = DataMapType.builder(PrincipiaMagica.loc("leaf_saplings"), Registries.BLOCK, BuiltInRegistries.ITEM.byNameCodec()).build();

    public static void onRegisterDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(LEAF_SAPLINGS);
    }
}
