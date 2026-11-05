// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica;

import com.klikli_dev.principiamagica.datagen.PrincipiaMagicaDataGenerators;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.gametest.GameTestRegistry;
import com.klikli_dev.principiamagica.registry.ActivityRegistry;
import com.klikli_dev.principiamagica.registry.AttributeRegistry;
import com.klikli_dev.principiamagica.registry.CreativeModeTabRegistry;
import com.klikli_dev.principiamagica.registry.CropHandlerRegistry;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.DataMapTypeRegistry;
import com.klikli_dev.principiamagica.registry.EntityDataSerializerRegistry;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemJobRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.GolemUpgradeRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import com.klikli_dev.principiamagica.registry.MenuRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import com.klikli_dev.principiamagica.registry.SensorTypeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@Mod(PrincipiaMagica.MODID)
public class PrincipiaMagica {
    public static final String MODID = "principiamagica";

    public PrincipiaMagica(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ItemRegistry.ITEMS.register(modEventBus);
        EntityTypeRegistry.ENTITY_TYPES.register(modEventBus);
        DataComponentRegistry.DATA_COMPONENTS.register(modEventBus);
        AttributeRegistry.ATTRIBUTES.register(modEventBus);
        CreativeModeTabRegistry.CREATIVE_MODE_TABS.register(modEventBus);
        EntityDataSerializerRegistry.ENTITY_DATA_SERIALIZERS.register(modEventBus);
        MenuRegistry.MENU_TYPES.register(modEventBus);
        MemoryModuleTypeRegistry.MEMORY_MODULE_TYPES.register(modEventBus);
        SensorTypeRegistry.SENSOR_TYPES.register(modEventBus);
        ActivityRegistry.ACTIVITIES.register(modEventBus);
        GameTestRegistry.TEST_FUNCTIONS.register(modEventBus);

        GolemMaterialRegistry.GOLEM_MATERIALS.register(modEventBus);
        GolemJobRegistry.GOLEM_JOBS.register(modEventBus);
        GolemUpgradeRegistry.GOLEM_UPGRADES.register(modEventBus);
        CropHandlerRegistry.CROP_HANDLERS.register(modEventBus);

        modEventBus.addListener(PrincipiaMagicaRegistries::onRegisterRegistries);
        modEventBus.addListener(DataMapTypeRegistry::onRegisterDataMapTypes);
        modEventBus.addListener(GolemEntity::onEntityAttributeCreation);
        modEventBus.addListener(Networking::register);
        NeoForge.EVENT_BUS.addListener(PrincipiaMagica::onPlayerTick);
        modEventBus.addListener(GameTestRegistry::onRegisterGameTests);
        modEventBus.addListener(PrincipiaMagicaDataGenerators::onGatherData);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            StylusItem.onPlayerTick(serverPlayer);
        }
    }

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
