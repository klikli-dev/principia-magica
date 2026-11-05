// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.lumberjack;

import com.klikli_dev.principiamagica.registry.DataMapTypeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public final class Saplings {
    private static final String LEAVES_SUFFIX = "_leaves";
    private static final String SAPLING_SUFFIX = "_sapling";

    private Saplings() {
    }

    /**
     * Resolves the sapling that grows from the given leaves. The leaf data map is checked first, then the
     * {@code <name>_leaves} to {@code <name>_sapling} naming convention in the leaf's namespace.
     */
    public static Optional<Item> forLeaves(Block leaves) {
        Item mapped = leaves.builtInRegistryHolder().getData(DataMapTypeRegistry.LEAF_SAPLINGS);
        if (mapped != null) {
            return Optional.of(mapped);
        }

        Identifier id = BuiltInRegistries.BLOCK.getKey(leaves);
        if (!id.getPath().endsWith(LEAVES_SUFFIX)) {
            return Optional.empty();
        }

        String base = id.getPath().substring(0, id.getPath().length() - LEAVES_SUFFIX.length());
        Identifier saplingId = Identifier.fromNamespaceAndPath(id.getNamespace(), base + SAPLING_SUFFIX);
        return BuiltInRegistries.ITEM.getOptional(saplingId).filter(Saplings::isSapling);
    }

    public static boolean isSapling(Item item) {
        return item.builtInRegistryHolder().is(ItemTags.SAPLINGS);
    }
}
