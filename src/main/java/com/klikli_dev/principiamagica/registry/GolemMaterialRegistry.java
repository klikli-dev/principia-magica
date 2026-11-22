// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.spec.GolemMaterial;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class GolemMaterialRegistry {
    public static final DeferredRegister<GolemMaterial> GOLEM_MATERIALS = DeferredRegister.create(PrincipiaMagicaRegistries.Keys.GOLEM_MATERIAL, PrincipiaMagica.MODID);

    public static final DeferredHolder<GolemMaterial, GolemMaterial> WOOD = GOLEM_MATERIALS.register("wood", () -> new GolemMaterial(
            PrincipiaMagica.loc("textures/entity/golem/wooden_golem.png"),
            1,
            Ingredient.of(ItemRegistry.QUICKENED_CLAY.get()),
            false,
            List.of(
                    GolemMaterial.modifier(Attributes.MAX_HEALTH, -2.0, AttributeModifier.Operation.ADD_VALUE),
                    GolemMaterial.modifier(Attributes.MOVEMENT_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    GolemMaterial.modifier(AttributeRegistry.GOLEM_WORK_SPEED, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )));

    public static final DeferredHolder<GolemMaterial, GolemMaterial> STONE = GOLEM_MATERIALS.register("stone", () -> new GolemMaterial(
            PrincipiaMagica.loc("textures/entity/golem/stone_golem.png"),
            2,
            Ingredient.of(Items.COBBLESTONE),
            false,
            List.of(
                    GolemMaterial.modifier(Attributes.MAX_HEALTH, 4.0, AttributeModifier.Operation.ADD_VALUE),
                    GolemMaterial.modifier(Attributes.ARMOR, 4.0, AttributeModifier.Operation.ADD_VALUE),
                    GolemMaterial.modifier(Attributes.MOVEMENT_SPEED, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    GolemMaterial.modifier(AttributeRegistry.GOLEM_WORK_SPEED, -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )));

    public static final DeferredHolder<GolemMaterial, GolemMaterial> METAL = GOLEM_MATERIALS.register("metal", () -> new GolemMaterial(
            PrincipiaMagica.loc("textures/entity/golem/metal_golem.png"),
            3,
            Ingredient.of(Items.IRON_INGOT),
            true,
            List.of(
                    GolemMaterial.modifier(Attributes.MAX_HEALTH, 10.0, AttributeModifier.Operation.ADD_VALUE),
                    GolemMaterial.modifier(Attributes.ARMOR, 8.0, AttributeModifier.Operation.ADD_VALUE),
                    GolemMaterial.modifier(Attributes.MOVEMENT_SPEED, -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )));
}
