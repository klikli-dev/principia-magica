// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.advancement;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.advancement.GolemGivenAxiomTrigger;
import com.klikli_dev.principiamagica.content.golem.advancement.GolemPlacedTrigger;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
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
    public static final Identifier A_WEDGE_OF_COPPER = PrincipiaMagica.loc("a_wedge_of_copper");
    public static final Identifier WET_CLAY = PrincipiaMagica.loc("wet_clay");
    public static final Identifier A_LAW_MADE_FAST = PrincipiaMagica.loc("a_law_made_fast");
    public static final Identifier ROTTEN_FLESH = PrincipiaMagica.loc("rotten_flesh");
    public static final Identifier QUICKENED = PrincipiaMagica.loc("quickened");
    public static final Identifier CLAY_THAT_STANDS = PrincipiaMagica.loc("clay_that_stands");
    public static final Identifier PURPOSE = PrincipiaMagica.loc("purpose");

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

        var aWedgeOfCopper = Advancement.Builder.advancement()
                .parent(theClayRemembers)
                .display(
                        ItemRegistry.COPPER_STYLUS.get(),
                        Component.translatable("advancements.principiamagica.a_wedge_of_copper.title"),
                        Component.translatable("advancements.principiamagica.a_wedge_of_copper.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false)
                .addCriterion("has_copper_stylus", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.COPPER_STYLUS.get()))
                .build(A_WEDGE_OF_COPPER);

        writer.accept(aWedgeOfCopper);

        var wetClay = Advancement.Builder.advancement()
                .parent(aWedgeOfCopper)
                .display(
                        ItemRegistry.WET_TABLET.get(),
                        Component.translatable("advancements.principiamagica.wet_clay.title"),
                        Component.translatable("advancements.principiamagica.wet_clay.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false)
                .addCriterion("has_wet_tablet", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.WET_TABLET.get()))
                .build(WET_CLAY);

        writer.accept(wetClay);

        var aLawMadeFast = Advancement.Builder.advancement()
                .parent(wetClay)
                .display(
                        ItemRegistry.AXIOM_OF_HARVEST.get(),
                        Component.translatable("advancements.principiamagica.a_law_made_fast.title"),
                        Component.translatable("advancements.principiamagica.a_law_made_fast.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false)
                .addCriterion("has_fired_axiom", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), AxiomItem.AXIOMS)))
                .build(A_LAW_MADE_FAST);

        writer.accept(aLawMadeFast);

        var rottenFlesh = Advancement.Builder.advancement()
                .parent(theClayRemembers)
                .display(
                        Items.ROTTEN_FLESH,
                        Component.translatable("advancements.principiamagica.rotten_flesh.title"),
                        Component.translatable("advancements.principiamagica.rotten_flesh.description"),
                        null,
                        AdvancementType.TASK,
                        false,
                        false,
                        true)
                .addCriterion("has_rotten_flesh", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ROTTEN_FLESH))
                .build(ROTTEN_FLESH);

        writer.accept(rottenFlesh);

        var quickened = Advancement.Builder.advancement()
                .parent(aWedgeOfCopper)
                .display(
                        ItemRegistry.QUICKENED_CLAY.get(),
                        Component.translatable("advancements.principiamagica.quickened.title"),
                        Component.translatable("advancements.principiamagica.quickened.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false)
                .addCriterion("has_quickened_clay", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.QUICKENED_CLAY.get()))
                .build(QUICKENED);

        writer.accept(quickened);

        var clayThatStands = Advancement.Builder.advancement()
                .parent(quickened)
                .display(
                        ItemRegistry.GOLEM.get(),
                        Component.translatable("advancements.principiamagica.clay_that_stands.title"),
                        Component.translatable("advancements.principiamagica.clay_that_stands.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false)
                .addCriterion("golem_placed", GolemPlacedTrigger.placed())
                .build(CLAY_THAT_STANDS);

        writer.accept(clayThatStands);

        var purpose = Advancement.Builder.advancement()
                .parent(clayThatStands)
                .display(
                        ItemRegistry.AXIOM_OF_HARVEST.get(),
                        Component.translatable("advancements.principiamagica.purpose.title"),
                        Component.translatable("advancements.principiamagica.purpose.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false)
                .addCriterion("given_axiom", GolemGivenAxiomTrigger.given())
                .build(PURPOSE);

        writer.accept(purpose);
    }
}
