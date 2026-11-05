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

public record MessageSetJobOption(int entityId, String key, boolean value) implements Message {

    public static final Type<MessageSetJobOption> TYPE = new Type<>(PrincipiaMagica.loc("set_job_option"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSetJobOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageSetJobOption::entityId,
            ByteBufCodecs.STRING_UTF8, MessageSetJobOption::key,
            ByteBufCodecs.BOOL, MessageSetJobOption::value,
            MessageSetJobOption::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        GolemCommands.findOwnedGolem(player, this.entityId).ifPresent(golem -> golem.getSpec().job().ifPresent(job ->
                GolemCommands.editWorkplace(player, golem, workplace ->
                        GolemCommands.setJobOption(workplace, job.value().options(), this.key, this.value))));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
