// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import com.klikli_dev.principiamagica.registry.ParticleTypeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

/**
 * A drop of principle colour. It hangs at the spout, falls and lands with a splash at landY, the surface of the recipient.
 *
 * @param color the RGB colour of the drop.
 * @param landY the world height at which the drop lands.
 */
public record PrincipleDripOptions(int color, float landY) implements ParticleOptions {

    public static final MapCodec<PrincipleDripOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(PrincipleDripOptions::color),
            Codec.FLOAT.fieldOf("land_y").forGetter(PrincipleDripOptions::landY)
    ).apply(instance, PrincipleDripOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PrincipleDripOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PrincipleDripOptions::color,
            ByteBufCodecs.FLOAT, PrincipleDripOptions::landY,
            PrincipleDripOptions::new
    );

    @Override
    public ParticleType<PrincipleDripOptions> getType() {
        return ParticleTypeRegistry.PRINCIPLE_DRIP.get();
    }

    public static final class Type extends ParticleType<PrincipleDripOptions> {
        public Type() {
            super(false);
        }

        @Override
        public MapCodec<PrincipleDripOptions> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, PrincipleDripOptions> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
