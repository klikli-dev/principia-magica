// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.advancement;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PrincipiaMagicaAdvancementSubProvider implements AdvancementSubProvider {

    public static final Identifier THE_CLAY_REMEMBERS = PrincipiaMagica.loc("the_clay_remembers");

    public static final ResourceKey<Recipe<?>> PRINCIPIA_RECIPE = ResourceKey.create(Registries.RECIPE, PrincipiaMagica.loc("principia"));

    @Override
    public void generate(HolderLookup.@NotNull Provider registries, @NotNull Consumer<AdvancementHolder> writer) {
        var theClayRemembers = Advancement.Builder.advancement()
                .display(
                        Items.CLAY_BALL,
                        Component.translatable("advancements.principiamagica.the_clay_remembers.title"),
                        Component.translatable("advancements.principiamagica.the_clay_remembers.description"),
                        Identifier.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
                        AdvancementType.TASK,
                        true,
                        false,
                        false)
                .addCriterion("has_clay_ball", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CLAY_BALL))
                .rewards(AdvancementRewards.Builder.recipe(PRINCIPIA_RECIPE))
                .build(THE_CLAY_REMEMBERS);

        writer.accept(theClayRemembers);
    }
}
