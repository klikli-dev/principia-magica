// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.item.GolemItem;
import com.klikli_dev.principiamagica.content.golem.item.ImprintTabletItem;
import com.klikli_dev.principiamagica.content.golem.item.QuickenedClayItem;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingJob;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.LumberjackJob;
import com.klikli_dev.principiamagica.content.item.book.PrincipiaItem;
import com.klikli_dev.principiamagica.content.libation.item.ClayPhialItem;
import com.klikli_dev.principiamagica.content.libation.item.NatronSpongeItem;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PrincipiaMagica.MODID);

    private static final int UNFIRED_AXIOM_STACK_SIZE = 16;
    private static final int MATERIAL_STACK_SIZE = 64;

    public static final DeferredItem<PrincipiaItem> PRINCIPIA = ITEMS.registerItem("principia", p -> new PrincipiaItem(p.stacksTo(1)));

    public static final DeferredItem<GolemItem> GOLEM = ITEMS.registerItem("golem", p -> new GolemItem(p.stacksTo(16)));

    public static final DeferredItem<StylusItem> COPPER_STYLUS = ITEMS.registerItem("copper_stylus",
            p -> new StylusItem(p.stacksTo(1).craftRemainder(selfTemplate("copper_stylus"))));

    public static final DeferredItem<ImprintTabletItem> IMPRINT_TABLET = ITEMS.registerItem("imprint_tablet",
            p -> new ImprintTabletItem(p.stacksTo(1)));

    public static final DeferredItem<BlockItem> LIBATION_SLAB = ITEMS.registerSimpleBlockItem("libation_slab", BlockRegistry.LIBATION_SLAB);

    public static final DeferredItem<BlockItem> LIBATION_SPOUT = ITEMS.registerSimpleBlockItem("libation_spout", BlockRegistry.LIBATION_SPOUT);

    public static final DeferredItem<BlockItem> VESSEL_TAP = ITEMS.registerSimpleBlockItem("vessel_tap", BlockRegistry.VESSEL_TAP);

    public static final DeferredItem<BlockItem> CATCH_TROUGH = ITEMS.registerSimpleBlockItem("catch_trough", BlockRegistry.CATCH_TROUGH);

    public static final DeferredItem<BlockItem> WABET_BASIN = ITEMS.registerSimpleBlockItem("wabet_basin", BlockRegistry.WABET_BASIN);

    public static final DeferredItem<BlockItem> STEEPING_JAR = ITEMS.registerSimpleBlockItem("steeping_jar", BlockRegistry.STEEPING_JAR);

    public static final DeferredItem<BlockItem> DRYING_SLAB = ITEMS.registerSimpleBlockItem("drying_slab", BlockRegistry.DRYING_SLAB);

    public static final DeferredItem<ClayPhialItem> CLAY_PHIAL = ITEMS.registerItem("clay_phial",
            p -> new ClayPhialItem(p.stacksTo(ClayPhialItem.STACK_SIZE)));

    public static final DeferredItem<Item> UNFIRED_CLAY_PHIAL = registerStackable("unfired_clay_phial", ClayPhialItem.STACK_SIZE);

    /**
     * Dummy items that only exist to be drawn by the clay phial renderer. They are not in any creative tab.
     */
    public static final DeferredItem<Item> CLAY_PHIAL_OPEN_ICON = ITEMS.registerItem("clay_phial_open_icon", Item::new);

    public static final DeferredItem<Item> CLAY_PHIAL_CLOSED_ICON = ITEMS.registerItem("clay_phial_closed_icon", Item::new);

    public static final DeferredItem<Item> PHIAL_LABEL_ICON = ITEMS.registerItem("phial_label_icon", Item::new);

    public static final DeferredItem<NatronSpongeItem> NATRON_SPONGE = ITEMS.registerItem("natron_sponge",
            p -> new NatronSpongeItem(p.stacksTo(16)));

    public static final DeferredItem<Item> AXIOM_TABLET = registerStackable("axiom_tablet", MATERIAL_STACK_SIZE);

    public static final DeferredItem<QuickenedClayItem> QUICKENED_CLAY = ITEMS.registerItem("quickened_clay", p -> new QuickenedClayItem(p.stacksTo(MATERIAL_STACK_SIZE)));

    public static final DeferredItem<Item> AXIOM_OF_HARVEST_UNFIRED = registerStackable("axiom_of_harvest_unfired", UNFIRED_AXIOM_STACK_SIZE);

    public static final DeferredItem<Item> AXIOM_OF_FELLING_UNFIRED = registerStackable("axiom_of_felling_unfired", UNFIRED_AXIOM_STACK_SIZE);

    public static final DeferredItem<Item> AXIOM_OF_HARVEST_UNFIRED_STEEPED = registerStackable("axiom_of_harvest_unfired_steeped", UNFIRED_AXIOM_STACK_SIZE);

    public static final DeferredItem<Item> AXIOM_OF_FELLING_UNFIRED_STEEPED = registerStackable("axiom_of_felling_unfired_steeped", UNFIRED_AXIOM_STACK_SIZE);

    public static final DeferredItem<AxiomItem> AXIOM_OF_HARVEST = ITEMS.registerItem("axiom_of_harvest", p -> new AxiomItem(p.stacksTo(1), FarmingJob.KEY));

    public static final DeferredItem<AxiomItem> AXIOM_OF_FELLING = ITEMS.registerItem("axiom_of_felling", p -> new AxiomItem(p.stacksTo(1), LumberjackJob.KEY));

    /**
     * A plain item with the given maximum stack size.
     */
    private static DeferredItem<Item> registerStackable(String name, int maxStackSize) {
        return ITEMS.registerItem(name, p -> new Item(p.stacksTo(maxStackSize)));
    }

    /**
     * Crafting remainder that returns the item itself. The item is not bound yet while its properties are built, so the holder is resolved lazily.
     */
    private static ItemStackTemplate selfTemplate(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, PrincipiaMagica.loc(name));
        return new ItemStackTemplate(DeferredHolder.create(key), 1, DataComponentPatch.EMPTY);
    }
}
