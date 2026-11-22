// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MessageHandler {

    /**
     * Handles client-to-server messages. Server-to-client messages are handled in
     * {@link com.klikli_dev.principiamagica.client.network.ClientNetworking}.
     */
    public static <T extends Message> void handle(T message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            MinecraftServer server = ctx.player().level().getServer();
            message.onServerReceived(server, (ServerPlayer) ctx.player());
        });
    }
}
