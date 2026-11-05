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
                output.accept(GolemItem.createStack(GolemSpec.of(GolemMaterialRegistry.STONE)));
                output.accept(GolemItem.createStack(GolemSpec.of(GolemMaterialRegistry.METAL)));
                output.accept(ItemRegistry.PRINCIPIA.get());
                output.accept(ItemRegistry.GOLEM_BATON.get());
                output.accept(ItemRegistry.FARMING_CORE.get());
                output.accept(ItemRegistry.LUMBERJACK_CORE.get());
                output.accept(ItemRegistry.SWIFTNESS_UPGRADE.get());
                output.accept(ItemRegistry.CAPACITY_UPGRADE.get());
            }).build());
}
