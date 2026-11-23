// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.libation.PrincipleSelection;
import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.network.Message;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public record MessageSelectPrinciple(Optional<Holder<Principle>> principle) implements Message {

    public static final CustomPacketPayload.Type<MessageSelectPrinciple> TYPE =
            new CustomPacketPayload.Type<>(PrincipiaMagica.loc("select_principle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSelectPrinciple> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.holderRegistry(PrincipiaMagicaRegistries.Keys.PRINCIPLE)),
            MessageSelectPrinciple::principle,
            MessageSelectPrinciple::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        PrincipleSelection.set(player, this.principle.orElse(null));
    }
}
