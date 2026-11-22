// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle;

import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PrincipleStack(Holder<Principle> principle, int amount) {

    public static final Codec<PrincipleStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PrincipiaMagicaRegistries.PRINCIPLE.holderByNameCodec().fieldOf("principle").forGetter(PrincipleStack::principle),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(PrincipleStack::amount)
    ).apply(instance, PrincipleStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PrincipleStack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(PrincipiaMagicaRegistries.Keys.PRINCIPLE), PrincipleStack::principle,
            ByteBufCodecs.VAR_INT, PrincipleStack::amount,
            PrincipleStack::new
    );

    public PrincipleStack {
        if (amount < 1) {
            throw new IllegalArgumentException("Principle amount must be at least 1, got " + amount);
        }
    }

    /**
     * @return the amount in mB, formatted for display.
     */
    public Component amountText() {
        return Component.translatable("principle.principiamagica.amount", this.amount);
    }
}
