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
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.menu.GolemCommands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public record MessageSetGolemMode(int entityId, GolemMode mode) implements Message {

    public static final Type<MessageSetGolemMode> TYPE = new Type<>(PrincipiaMagica.loc("set_golem_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSetGolemMode> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageSetGolemMode::entityId,
            ByteBufCodecs.idMapper(i -> GolemMode.values()[i], GolemMode::ordinal), MessageSetGolemMode::mode,
            MessageSetGolemMode::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        GolemCommands.findOwnedGolem(player, this.entityId).ifPresent(golem -> golem.setMode(this.mode));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
