// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.AgeingCropHandler;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.BerryBushHandler;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.CropHandler;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.GourdHandler;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.NetherWartHandler;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.StackedPlantHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CropHandlerRegistry {
    public static final DeferredRegister<CropHandler> CROP_HANDLERS = DeferredRegister.create(PrincipiaMagicaRegistries.Keys.CROP_HANDLER, PrincipiaMagica.MODID);

    public static final DeferredHolder<CropHandler, AgeingCropHandler> AGEING = CROP_HANDLERS.register("ageing", AgeingCropHandler::new);
    public static final DeferredHolder<CropHandler, NetherWartHandler> NETHER_WART = CROP_HANDLERS.register("nether_wart", NetherWartHandler::new);
    public static final DeferredHolder<CropHandler, GourdHandler> GOURD = CROP_HANDLERS.register("gourd", GourdHandler::new);
    public static final DeferredHolder<CropHandler, StackedPlantHandler> STACKED_PLANT = CROP_HANDLERS.register("stacked_plant", StackedPlantHandler::new);
    public static final DeferredHolder<CropHandler, BerryBushHandler> BERRY_BUSH = CROP_HANDLERS.register("berry_bush", BerryBushHandler::new);
}
