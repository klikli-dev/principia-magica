// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record MessageSyncGolemWorkplace(
        int entityId,
        Workplace workplace,
        Optional<GlobalPos> anchor,
        Optional<BlockPos> currentTarget
) implements Message {

    public static final Type<MessageSyncGolemWorkplace> TYPE = new Type<>(PrincipiaMagica.loc("sync_golem_workplace"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSyncGolemWorkplace> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            MessageSyncGolemWorkplace::entityId,
            Workplace.STREAM_CODEC,
            MessageSyncGolemWorkplace::workplace,
            ByteBufCodecs.optional(GlobalPos.STREAM_CODEC),
            MessageSyncGolemWorkplace::anchor,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
            MessageSyncGolemWorkplace::currentTarget,
            MessageSyncGolemWorkplace::new
    );

    @Override
    public void onClientReceived(Minecraft minecraft, Player player) {
        ClientGolemWorkplaceCache.accept(this);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
