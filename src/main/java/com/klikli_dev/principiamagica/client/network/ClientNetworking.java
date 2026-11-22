// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.network;

import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.network.Message;
import com.klikli_dev.principiamagica.network.messages.MessageSyncGolemWorkplace;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-side networking: sending to the server and handling server-to-client messages.
 * Only referenced from client-dispatched lambdas in {@link com.klikli_dev.principiamagica.network.Networking}, so it is never loaded on a dedicated server.
 */
public class ClientNetworking {

    public static <T extends Message> void sendToServer(T message) {
        ClientPacketDistributor.sendToServer(message);
    }

    public static void handleSyncGolemWorkplace(MessageSyncGolemWorkplace message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientGolemWorkplaceCache.accept(message));
    }
}
