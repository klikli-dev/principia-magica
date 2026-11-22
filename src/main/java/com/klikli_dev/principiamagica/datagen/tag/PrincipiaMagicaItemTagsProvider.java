// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.tag;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class PrincipiaMagicaItemTagsProvider extends ItemTagsProvider {
    public PrincipiaMagicaItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, PrincipiaMagica.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(AxiomItem.AXIOMS)
                .add(ItemRegistry.AXIOM_OF_HARVEST.get())
                .add(ItemRegistry.AXIOM_OF_FELLING.get());
    }
}
