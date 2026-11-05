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
import net.minecraft.world.item.ItemStack;

public record MessageRemoveGolemAxiom(int entityId) implements Message {

    public static final Type<MessageRemoveGolemAxiom> TYPE = new Type<>(PrincipiaMagica.loc("remove_golem_axiom"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageRemoveGolemAxiom> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageRemoveGolemAxiom::entityId,
            MessageRemoveGolemAxiom::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        GolemCommands.findOwnedGolem(player, this.entityId).ifPresent(golem -> {
            ItemStack axiom = golem.takeJob();
            if (!axiom.isEmpty()) {
                player.getInventory().placeItemBackInInventory(axiom);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
