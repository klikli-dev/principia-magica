// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.network.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import com.klikli_dev.principiamagica.content.golem.menu.GolemCommands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public record MessageRemoveZone(int entityId, int index) implements Message {

    public static final Type<MessageRemoveZone> TYPE = new Type<>(PrincipiaMagica.loc("remove_zone"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageRemoveZone> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageRemoveZone::entityId,
            ByteBufCodecs.VAR_INT, MessageRemoveZone::index,
            MessageRemoveZone::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        GolemCommands.findOwnedGolem(player, this.entityId).ifPresent(golem ->
                GolemCommands.editWorkplace(player, golem, workplace -> GolemCommands.removeZone(workplace, this.index)));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
