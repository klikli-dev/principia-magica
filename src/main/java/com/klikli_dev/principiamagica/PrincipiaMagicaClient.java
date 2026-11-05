// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica;

import com.klikli_dev.principiamagica.content.golem.client.GolemRenderer;
import com.klikli_dev.principiamagica.content.golem.client.GolemScreen;
import com.klikli_dev.principiamagica.content.golem.client.WorkplaceOverlayRenderer;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.MenuRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = PrincipiaMagica.MODID, dist = Dist.CLIENT)
public class PrincipiaMagicaClient {
    public PrincipiaMagicaClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(PrincipiaMagicaClient::onRegisterEntityRenderers);
        modEventBus.addListener(PrincipiaMagicaClient::onRegisterMenuScreens);
        NeoForge.EVENT_BUS.addListener(WorkplaceOverlayRenderer::onRenderLevelStage);
    }

    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityTypeRegistry.GOLEM.get(), GolemRenderer::new);
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.GOLEM.get(), GolemScreen::new);
    }
}
