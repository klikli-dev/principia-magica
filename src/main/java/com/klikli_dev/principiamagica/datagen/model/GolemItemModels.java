// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.model;

import com.geckolib.renderer.internal.GeckolibItemSpecialRenderer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.item.GolemItem;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.resources.Identifier;

/**
 * Item model of the golem item: the GeckoLib golem model as a special model, on top of a base model.
 * The base model only provides the display transforms, so the golem is held, dropped and shown in the GUI like a block.
 */
public final class GolemItemModels {

    private static final Identifier BASE_MODEL = PrincipiaMagica.loc("item/golem");
    private static final Identifier PARTICLE = Identifier.withDefaultNamespace("item/stick");

    private GolemItemModels() {
    }

    public static void register(ItemModelGenerators itemModels) {
        itemModels.itemModelOutput.accept(ItemRegistry.GOLEM.get(),
                ItemModelUtils.specialModel(BASE_MODEL, new GeckolibItemSpecialRenderer.Unbaked<GolemItem>()));
        itemModels.modelOutput.accept(BASE_MODEL, GolemItemModels::baseModel);
    }

    private static JsonObject baseModel() {
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", PARTICLE.toString());

        JsonObject display = new JsonObject();
        // Mirrors the transforms of a full block, scaled to the height of the golem (about 1.2 blocks).
        display.add("gui", transform(30, 225, 0, 0, 0, 0, 0.6F));
        display.add("ground", transform(0, 0, 0, 0, 3, 0, 0.4F));
        display.add("fixed", transform(0, 0, 0, 0, 0, 0, 0.5F));
        display.add("thirdperson_righthand", transform(75, 45, 0, 0, 2.5F, 0, 0.45F));
        display.add("thirdperson_lefthand", transform(75, 225, 0, 0, 2.5F, 0, 0.45F));
        display.add("firstperson_righthand", transform(0, 45, 0, 0, 0, 0, 0.4F));
        display.add("firstperson_lefthand", transform(0, 225, 0, 0, 0, 0, 0.4F));

        JsonObject root = new JsonObject();
        root.add("textures", textures);
        root.add("display", display);
        return root;
    }

    private static JsonObject transform(float rotX, float rotY, float rotZ, float transX, float transY, float transZ, float scale) {
        JsonObject transform = new JsonObject();
        transform.add("rotation", vector(rotX, rotY, rotZ));
        transform.add("translation", vector(transX, transY, transZ));
        transform.add("scale", vector(scale, scale, scale));
        return transform;
    }

    private static JsonElement vector(float x, float y, float z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }
}
