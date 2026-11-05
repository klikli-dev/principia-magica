// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.menu.GolemCommands;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.network.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record MessageSetLinkRole(int entityId, int index, ContainerRole role) implements Message {

    public static final Type<MessageSetLinkRole> TYPE = new Type<>(PrincipiaMagica.loc("set_link_role"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSetLinkRole> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageSetLinkRole::entityId,
            ByteBufCodecs.VAR_INT, MessageSetLinkRole::index,
            ContainerRole.STREAM_CODEC, MessageSetLinkRole::role,
            MessageSetLinkRole::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        GolemCommands.findOwnedGolem(player, this.entityId).ifPresent(golem ->
                GolemCommands.editWorkplace(player, golem, workplace -> GolemCommands.setLinkRole(workplace, this.index, this.role)));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
