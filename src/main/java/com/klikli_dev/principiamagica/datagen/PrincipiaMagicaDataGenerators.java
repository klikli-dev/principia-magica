// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen;

import com.klikli_dev.modonomicon.api.datagen.LanguageProviderCache;
import com.klikli_dev.modonomicon.api.datagen.NeoBookProvider;
import com.klikli_dev.modonomicon.api.datagen.NeoResearchProvider;
import com.klikli_dev.modonomicon.api.datagen.research.ResearchCache;
import com.klikli_dev.principiamagica.datagen.PrincipiaMagicaDataGenerators;
import com.klikli_dev.principiamagica.datagen.advancement.PrincipiaMagicaAdvancementSubProvider;
import com.klikli_dev.principiamagica.datagen.book.PrincipiaBookProvider;
import com.klikli_dev.principiamagica.datagen.datamap.PrincipiaMagicaDataMapProvider;
import com.klikli_dev.principiamagica.datagen.lang.ENUSProvider;
import com.klikli_dev.principiamagica.datagen.model.PrincipiaMagicaModelProvider;
import com.klikli_dev.principiamagica.datagen.research.PrincipiaResearch;
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
        var langCache = new LanguageProviderCache("en_us");
        var researchCache = new ResearchCache();

        generator.addProvider(true, NeoBookProvider.of(event, langCache, researchCache,
                new PrincipiaBookProvider()
        ));
        generator.addProvider(true, NeoResearchProvider.of(event, langCache, researchCache,
                new PrincipiaResearch()
        ));

        //Important: the lang provider needs to be added after the book provider to process the texts added by the book provider
        generator.addProvider(true, new ENUSProvider(generator.getPackOutput(), langCache));
        generator.addProvider(true, new PrincipiaMagicaModelProvider(generator.getPackOutput()));
        generator.addProvider(true, new ShapedRecipeProvider(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(true, new ShapelessRecipeProvider(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(true, new PrincipiaMagicaDataMapProvider(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(true, new AdvancementProvider(generator.getPackOutput(), event.getLookupProvider(), List.of(
                new PrincipiaMagicaAdvancementSubProvider()
        )));
    }
}
