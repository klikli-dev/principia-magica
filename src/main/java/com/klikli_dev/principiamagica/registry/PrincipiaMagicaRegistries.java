// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.farming.crop.CropHandler;
import com.klikli_dev.principiamagica.content.golem.spec.GolemMaterial;
import com.klikli_dev.principiamagica.content.golem.upgrade.GolemUpgrade;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class PrincipiaMagicaRegistries {
    public static final Registry<GolemMaterial> GOLEM_MATERIAL = new RegistryBuilder<>(Keys.GOLEM_MATERIAL).sync(true).create();
    public static final Registry<GolemJob> GOLEM_JOB = new RegistryBuilder<>(Keys.GOLEM_JOB).sync(true).create();
    public static final Registry<GolemUpgrade> GOLEM_UPGRADE = new RegistryBuilder<>(Keys.GOLEM_UPGRADE).sync(true).create();
    public static final Registry<CropHandler> CROP_HANDLER = new RegistryBuilder<>(Keys.CROP_HANDLER).sync(true).create();

    public static void onRegisterRegistries(NewRegistryEvent event) {
        event.register(GOLEM_MATERIAL);
        event.register(GOLEM_JOB);
        event.register(GOLEM_UPGRADE);
        event.register(CROP_HANDLER);
    }

    public static final class Keys {
        public static final ResourceKey<Registry<GolemMaterial>> GOLEM_MATERIAL = ResourceKey.createRegistryKey(PrincipiaMagica.loc("golem_material"));
        public static final ResourceKey<Registry<GolemJob>> GOLEM_JOB = ResourceKey.createRegistryKey(PrincipiaMagica.loc("golem_job"));
        public static final ResourceKey<Registry<GolemUpgrade>> GOLEM_UPGRADE = ResourceKey.createRegistryKey(PrincipiaMagica.loc("golem_upgrade"));
        public static final ResourceKey<Registry<CropHandler>> CROP_HANDLER = ResourceKey.createRegistryKey(PrincipiaMagica.loc("crop_handler"));
    }
}
