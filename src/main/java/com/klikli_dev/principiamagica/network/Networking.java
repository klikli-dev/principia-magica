// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.network.messages.MessageEraseZone;
import com.klikli_dev.principiamagica.network.messages.MessageLiftPending;
import com.klikli_dev.principiamagica.network.messages.MessagePickUpCorner;
import com.klikli_dev.principiamagica.network.messages.MessageResizeCorner;
import com.klikli_dev.principiamagica.network.messages.MessageSetGolemMode;
import com.klikli_dev.principiamagica.network.messages.MessageSetJobOption;
import com.klikli_dev.principiamagica.network.messages.MessageSyncGolemWorkplace;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class Networking {

    public static void register(final RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PrincipiaMagica.MODID);

        //to client
        registrar.playToClient(MessageSyncGolemWorkplace.TYPE, MessageSyncGolemWorkplace.STREAM_CODEC, MessageHandler::handle);

        //to server
        registrar.playToServer(MessageSetGolemMode.TYPE, MessageSetGolemMode.STREAM_CODEC, MessageHandler::handle);
        registrar.playToServer(MessageSetJobOption.TYPE, MessageSetJobOption.STREAM_CODEC, MessageHandler::handle);
        registrar.playToServer(MessageEraseZone.TYPE, MessageEraseZone.STREAM_CODEC, MessageHandler::handle);
        registrar.playToServer(MessageLiftPending.TYPE, MessageLiftPending.STREAM_CODEC, MessageHandler::handle);
        registrar.playToServer(MessageResizeCorner.TYPE, MessageResizeCorner.STREAM_CODEC, MessageHandler::handle);
        registrar.playToServer(MessagePickUpCorner.TYPE, MessagePickUpCorner.STREAM_CODEC, MessageHandler::handle);
    }

    public static <T extends Message> void sendTo(ServerPlayer player, T message) {
        if (player.connection != null && player.connection.hasChannel(message)) {
            PacketDistributor.sendToPlayer(player, message);
        }
    }

    public static <T extends Message> void sendToServer(T message) {
        ClientPacketDistributor.sendToServer(message);
    }
}
