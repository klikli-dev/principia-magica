// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen;

import com.klikli_dev.principiamagica.datagen.lang.ENUSProvider;
import com.klikli_dev.principiamagica.datagen.model.PrincipiaMagicaModelProvider;
import com.klikli_dev.principiamagica.datagen.recipe.ShapedRecipeProvider;
import com.klikli_dev.principiamagica.datagen.tag.PrincipiaMagicaBlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class PrincipiaMagicaDataGenerators {

    public static void onGatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();

        generator.addProvider(true, new PrincipiaMagicaBlockTagsProvider(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(true, new ENUSProvider(generator.getPackOutput()));
        generator.addProvider(true, new PrincipiaMagicaModelProvider(generator.getPackOutput()));
        generator.addProvider(true, new ShapedRecipeProvider(generator.getPackOutput(), event.getLookupProvider()));
    }
}
