// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.item.CrackedGolemItem;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.item.GolemItem;
import com.klikli_dev.principiamagica.content.golem.item.GolemUpgradeItem;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingJob;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.LumberjackJob;
import com.klikli_dev.principiamagica.content.item.book.PrincipiaItem;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PrincipiaMagica.MODID);

    public static final DeferredItem<PrincipiaItem> PRINCIPIA = ITEMS.registerItem("principia", p -> new PrincipiaItem(p.stacksTo(1)));

    public static final DeferredItem<GolemItem> GOLEM = ITEMS.registerItem("golem", p -> new GolemItem(p.stacksTo(16)));

    public static final DeferredItem<CrackedGolemItem> CRACKED_GOLEM = ITEMS.registerItem("cracked_golem", p -> new CrackedGolemItem(p.stacksTo(1)));

    public static final DeferredItem<StylusItem> COPPER_STYLUS = ITEMS.registerItem("copper_stylus",
            p -> new StylusItem(p.stacksTo(1).craftRemainder(selfTemplate("copper_stylus"))));

    public static final DeferredItem<Item> WET_TABLET = ITEMS.registerItem("wet_tablet", p -> new Item(p.stacksTo(64)));

    public static final DeferredItem<Item> QUICKENED_CLAY = ITEMS.registerItem("quickened_clay", p -> new Item(p.stacksTo(64)));

    public static final DeferredItem<Item> UNFIRED_AXIOM_OF_HARVEST = ITEMS.registerItem("unfired_axiom_of_harvest", p -> new Item(p.stacksTo(16)));

    public static final DeferredItem<Item> UNFIRED_AXIOM_OF_FELLING = ITEMS.registerItem("unfired_axiom_of_felling", p -> new Item(p.stacksTo(16)));

    public static final DeferredItem<AxiomItem> AXIOM_OF_HARVEST = ITEMS.registerItem("axiom_of_harvest", p -> new AxiomItem(p.stacksTo(1), FarmingJob.KEY));

    public static final DeferredItem<AxiomItem> AXIOM_OF_FELLING = ITEMS.registerItem("axiom_of_felling", p -> new AxiomItem(p.stacksTo(1), LumberjackJob.KEY));

    public static final DeferredItem<GolemUpgradeItem> SWIFTNESS_UPGRADE = ITEMS.registerItem("swiftness_upgrade",
            p -> new GolemUpgradeItem(p.stacksTo(16), GolemUpgradeRegistry.SWIFTNESS.getKey()));

    public static final DeferredItem<GolemUpgradeItem> CAPACITY_UPGRADE = ITEMS.registerItem("capacity_upgrade",
            p -> new GolemUpgradeItem(p.stacksTo(16), GolemUpgradeRegistry.CAPACITY.getKey()));

    /**
     * Crafting remainder that returns the item itself. The item is not bound yet while its properties are built, so the holder is resolved lazily.
     */
    private static ItemStackTemplate selfTemplate(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, PrincipiaMagica.loc(name));
        return new ItemStackTemplate(DeferredHolder.create(key), 1, DataComponentPatch.EMPTY);
    }
}
