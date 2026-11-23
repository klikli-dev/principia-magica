// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.item.GolemItem;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeModeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PrincipiaMagica.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PRINCIPIA_MAGICA = CREATIVE_MODE_TABS.register(PrincipiaMagica.MODID, () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> GolemItem.createStack(GolemSpec.of(GolemMaterialRegistry.WOOD)))
            .title(Component.translatable("itemGroup." + PrincipiaMagica.MODID))
            .displayItems((parameters, output) -> {
                output.accept(GolemItem.createStack(GolemSpec.of(GolemMaterialRegistry.WOOD)));
                output.accept(ItemRegistry.PRINCIPIA.get());
                output.accept(ItemRegistry.LIBATION_SLAB.get());
                output.accept(ItemRegistry.LIBATION_SPOUT.get());
                output.accept(ItemRegistry.CATCH_TROUGH.get());
                output.accept(ItemRegistry.WABET_BASIN.get());
                output.accept(ItemRegistry.STEEPING_JAR.get());
                output.accept(ItemRegistry.VESSEL_TAP.get());
                output.accept(ItemRegistry.DRYING_SLAB.get());
                output.accept(ItemRegistry.CLAY_PHIAL.get());
                output.accept(ItemRegistry.NATRON_SPONGE.get());
                output.accept(ItemRegistry.COPPER_STYLUS.get());
                output.accept(ItemRegistry.IMPRINT_TABLET.get());
                output.accept(ItemRegistry.AXIOM_TABLET.get());
                output.accept(ItemRegistry.QUICKENED_CLAY.get());
                output.accept(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get());
                output.accept(ItemRegistry.AXIOM_OF_FELLING_UNFIRED.get());
                output.accept(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get());
                output.accept(ItemRegistry.AXIOM_OF_FELLING_UNFIRED_STEEPED.get());
                output.accept(ItemRegistry.AXIOM_OF_HARVEST.get());
                output.accept(ItemRegistry.AXIOM_OF_FELLING.get());
            }).build());
}
