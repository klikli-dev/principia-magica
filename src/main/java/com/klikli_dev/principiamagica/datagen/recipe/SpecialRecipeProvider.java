// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.recipe;

import com.google.gson.JsonObject;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

/**
 * Special (code-defined) crafting recipes. They have no ingredients in the data, only their serializer type.
 */
public class SpecialRecipeProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public SpecialRecipeProvider(PackOutput packOutput) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public String getName() {
        return "Special Recipes/" + PrincipiaMagica.MODID;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.allOf(
                this.special(output, PrincipiaMagica.loc("repair_cracked_golem"), "principiamagica:repair_cracked_golem"));
    }

    private CompletableFuture<?> special(CachedOutput output, Identifier id, String type) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        return DataProvider.saveStable(output, json, this.pathProvider.json(id));
    }
}
