// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.libation.PrincipleDripOptions;
import com.klikli_dev.principiamagica.content.libation.PrincipleSplashOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ParticleTypeRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, PrincipiaMagica.MODID);

    public static final DeferredHolder<ParticleType<?>, PrincipleDripOptions.Type> PRINCIPLE_DRIP = PARTICLE_TYPES.register("principle_drip", PrincipleDripOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, PrincipleSplashOptions.Type> PRINCIPLE_SPLASH = PARTICLE_TYPES.register("principle_splash", PrincipleSplashOptions.Type::new);
}
