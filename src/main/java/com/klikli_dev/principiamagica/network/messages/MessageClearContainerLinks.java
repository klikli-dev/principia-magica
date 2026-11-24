// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.menu.GolemCommands;
import com.klikli_dev.principiamagica.network.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Sent by the job tab to remove all container links of a golem.
 */
public record MessageClearContainerLinks(int entityId) implements Message {

    public static final Type<MessageClearContainerLinks> TYPE = new Type<>(PrincipiaMagica.loc("clear_container_links"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageClearContainerLinks> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageClearContainerLinks::entityId,
            MessageClearContainerLinks::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        GolemCommands.findOwnedGolem(player, this.entityId).ifPresent(golem ->
                GolemCommands.editWorkplace(player, golem, GolemCommands::clearLinks));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
