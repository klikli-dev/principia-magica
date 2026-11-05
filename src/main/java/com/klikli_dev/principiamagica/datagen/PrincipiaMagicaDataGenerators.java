// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen;

import com.klikli_dev.principiamagica.datagen.PrincipiaMagicaDataGenerators;
import com.klikli_dev.principiamagica.datagen.advancement.PrincipiaMagicaAdvancementSubProvider;
import com.klikli_dev.principiamagica.datagen.datamap.PrincipiaMagicaDataMapProvider;
import com.klikli_dev.principiamagica.datagen.lang.ENUSProvider;
import com.klikli_dev.principiamagica.datagen.model.PrincipiaMagicaModelProvider;
import com.klikli_dev.principiamagica.datagen.recipe.ShapedRecipeProvider;
import com.klikli_dev.principiamagica.datagen.recipe.ShapelessRecipeProvider;
import com.klikli_dev.principiamagica.datagen.tag.PrincipiaMagicaBlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;

public class PrincipiaMagicaDataGenerators {

    public static void onGatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();

        generator.addProvider(true, new PrincipiaMagicaBlockTagsProvider(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(true, new ENUSProvider(generator.getPackOutput()));
        generator.addProvider(true, new PrincipiaMagicaModelProvider(generator.getPackOutput()));
        generator.addProvider(true, new ShapedRecipeProvider(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(true, new ShapelessRecipeProvider(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(true, new PrincipiaMagicaDataMapProvider(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(true, new AdvancementProvider(generator.getPackOutput(), event.getLookupProvider(), List.of(
                new PrincipiaMagicaAdvancementSubProvider()
        )));
    }
}
