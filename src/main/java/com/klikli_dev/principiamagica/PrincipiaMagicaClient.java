// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica;

import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.client.GolemRenderer;
import com.klikli_dev.principiamagica.content.golem.client.gui.GolemScreen;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.StylusHud;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceInput;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceOverlayRenderer;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceRenderTypes;
import com.klikli_dev.principiamagica.content.libation.LibationClientEffects;
import com.klikli_dev.principiamagica.content.libation.client.CatchTroughRenderer;
import com.klikli_dev.principiamagica.content.libation.client.CuringVesselRenderer;
import com.klikli_dev.principiamagica.content.libation.client.DryingSlabRenderer;
import com.klikli_dev.principiamagica.content.libation.client.LibationEffects;
import com.klikli_dev.principiamagica.content.libation.client.LibationHud;
import com.klikli_dev.principiamagica.content.libation.client.LibationSlabRenderer;
import com.klikli_dev.principiamagica.content.libation.client.LibationSpoutRenderer;
import com.klikli_dev.principiamagica.content.libation.client.PrincipleDripParticle;
import com.klikli_dev.principiamagica.content.libation.client.PrincipleSplashParticle;
import com.klikli_dev.principiamagica.client.principle.PrincipleTooltips;
import com.klikli_dev.principiamagica.client.recipe.ClientRecipes;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.MenuRegistry;
import com.klikli_dev.principiamagica.registry.ParticleTypeRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.klikli_dev.principiamagica.client.gui.GuiStyles;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = PrincipiaMagica.MODID, dist = Dist.CLIENT)
public class PrincipiaMagicaClient {
    public PrincipiaMagicaClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        GuiStyles.register();

        modEventBus.addListener(PrincipiaMagicaClient::onRegisterEntityRenderers);
        modEventBus.addListener(PrincipiaMagicaClient::onRegisterMenuScreens);
        modEventBus.addListener(PrincipiaMagicaClient::onRegisterParticleProviders);
        modEventBus.addListener(WorkplaceRenderTypes::onRegisterRenderPipelines);
        LibationClientEffects.install(new LibationEffects());
        WorkplaceOverlayRenderer workplaceOverlay = new WorkplaceOverlayRenderer();
        StylusHud stylusHud = new StylusHud();
        modEventBus.addListener(stylusHud::onRegisterGuiLayers);
        LibationHud libationHud = new LibationHud();
        modEventBus.addListener(libationHud::onRegisterGuiLayers);
        NeoForge.EVENT_BUS.addListener(workplaceOverlay::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(workplaceOverlay::onClientTick);
        NeoForge.EVENT_BUS.addListener(WorkplaceInput::onMouseScrolling);
        NeoForge.EVENT_BUS.addListener(WorkplaceInput::onInteractionKeyMapping);
        NeoForge.EVENT_BUS.addListener(ClientRecipes::onRecipesReceived);
        NeoForge.EVENT_BUS.addListener(ClientRecipes::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(ClientGolemWorkplaceCache::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(ClientGolemWorkplaceCache::onEntityLeaveLevel);
        NeoForge.EVENT_BUS.addListener(PrincipleTooltips::onItemTooltip);
    }

    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityTypeRegistry.GOLEM.get(), GolemRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.LIBATION_SLAB.get(), LibationSlabRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.LIBATION_SPOUT.get(), LibationSpoutRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.DRYING_SLAB.get(), DryingSlabRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.CATCH_TROUGH.get(), CatchTroughRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.WABET_BASIN.get(), CuringVesselRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityTypeRegistry.STEEPING_JAR.get(), CuringVesselRenderer::new);
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.GOLEM.get(), GolemScreen::new);
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleTypeRegistry.PRINCIPLE_DRIP.get(), PrincipleDripParticle.Provider::new);
        event.registerSpriteSet(ParticleTypeRegistry.PRINCIPLE_SPLASH.get(), PrincipleSplashParticle.Provider::new);
    }
}
