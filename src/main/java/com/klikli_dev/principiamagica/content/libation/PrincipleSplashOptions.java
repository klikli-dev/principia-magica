// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import com.klikli_dev.principiamagica.registry.ParticleTypeRegistry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

/**
 * A water splash tinted with a principle colour. It is spawned where a drip lands and where a catch trough steeps an item.
 *
 * @param color the RGB colour of the splash.
 */
public record PrincipleSplashOptions(int color) implements ParticleOptions {

    public static final MapCodec<PrincipleSplashOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(PrincipleSplashOptions::color)
    ).apply(instance, PrincipleSplashOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PrincipleSplashOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PrincipleSplashOptions::color,
            PrincipleSplashOptions::new
    );

    @Override
    public ParticleType<PrincipleSplashOptions> getType() {
        return ParticleTypeRegistry.PRINCIPLE_SPLASH.get();
    }

    public static final class Type extends ParticleType<PrincipleSplashOptions> {
        public Type() {
            super(false);
        }

        @Override
        public MapCodec<PrincipleSplashOptions> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, PrincipleSplashOptions> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
