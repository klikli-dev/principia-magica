// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.particle;

import com.klikli_dev.principiamagica.registry.ParticleTypeRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

public class PrincipiaMagicaParticleDescriptionProvider extends ParticleDescriptionProvider {

    public PrincipiaMagicaParticleDescriptionProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        this.spriteSet(ParticleTypeRegistry.PRINCIPLE_DRIP.get(),
                Identifier.withDefaultNamespace("drip_hang"),
                Identifier.withDefaultNamespace("drip_fall"),
                Identifier.withDefaultNamespace("drip_land"));
        // The vanilla splash sprites are pre-colored blue, which darkens any tint. The generic sprites are white so the fluid color shows as is.
        this.spriteSet(ParticleTypeRegistry.PRINCIPLE_SPLASH.get(),
                Identifier.withDefaultNamespace("generic_0"),
                Identifier.withDefaultNamespace("generic_1"),
                Identifier.withDefaultNamespace("generic_2"),
                Identifier.withDefaultNamespace("generic_3"));
    }
}
