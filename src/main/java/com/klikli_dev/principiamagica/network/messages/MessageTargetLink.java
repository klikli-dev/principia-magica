// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Sent when the player clicks a container link the stylus targets in the air, i.e. without a block hit, e.g. because its container is gone.
 * Plain click cycles the link, sneaking removes it. The server validates the link like any other stylus edit.
 */
public record MessageTargetLink(int entityId, BlockPos pos, boolean remove) implements Message {

    public static final Type<MessageTargetLink> TYPE = new Type<>(PrincipiaMagica.loc("target_link"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageTargetLink> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageTargetLink::entityId,
            BlockPos.STREAM_CODEC, MessageTargetLink::pos,
            ByteBufCodecs.BOOL, MessageTargetLink::remove,
            MessageTargetLink::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        if (player.level() instanceof ServerLevel level
                && level.getEntity(this.entityId) instanceof GolemEntity golem
                && golem.isAlive()) {
            StylusItem.targetLink(player, golem, this.pos, this.remove);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
