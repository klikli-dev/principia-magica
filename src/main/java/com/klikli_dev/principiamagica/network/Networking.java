// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.client.network.ClientNetworking;
import com.klikli_dev.principiamagica.network.messages.MessageEraseGlyph;
import com.klikli_dev.principiamagica.network.messages.MessageLiftCorner;
import com.klikli_dev.principiamagica.network.messages.MessageLiftPending;
import com.klikli_dev.principiamagica.network.messages.MessagePickUpGlyph;
import com.klikli_dev.principiamagica.network.messages.MessageSetGolemMode;
import com.klikli_dev.principiamagica.network.messages.MessageSetJobOption;
import com.klikli_dev.principiamagica.network.messages.MessageShiftZone;
import com.klikli_dev.principiamagica.network.messages.MessageSyncGolemWorkplace;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class Networking {

    public static void register(final RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PrincipiaMagica.MODID);

        //to client
        //the lambda only references the client class when invoked, so the server never loads client code.
        registrar.playToClient(MessageSyncGolemWorkplace.TYPE, MessageSyncGolemWorkplace.STREAM_CODEC,
                (message, ctx) -> ClientNetworking.handleSyncGolemWorkplace(message, ctx));

        //to server
        registrar.playToServer(MessageSetGolemMode.TYPE, MessageSetGolemMode.STREAM_CODEC, MessageHandler::handle);
        registrar.playToServer(MessageSetJobOption.TYPE, MessageSetJobOption.STREAM_CODEC, MessageHandler::handle);
        registrar.playToServer(MessageShiftZone.TYPE, MessageShiftZone.STREAM_CODEC, MessageHandler::handle);
        registrar.playToServer(MessageEraseGlyph.TYPE, MessageEraseGlyph.STREAM_CODEC, MessageHandler::handle);
        registrar.playToServer(MessageLiftCorner.TYPE, MessageLiftCorner.STREAM_CODEC, MessageHandler::handle);
        registrar.playToServer(MessagePickUpGlyph.TYPE, MessagePickUpGlyph.STREAM_CODEC, MessageHandler::handle);
        registrar.playToServer(MessageLiftPending.TYPE, MessageLiftPending.STREAM_CODEC, MessageHandler::handle);
    }

    public static <T extends Message> void sendTo(ServerPlayer player, T message) {
        PacketDistributor.sendToPlayer(player, message);
    }
}
