// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Sent when the player clicks a container the stylus targets: a container link in the air, or a container block under the crosshair.
 * Plain click cycles the container link, sneaking removes it, like the click on the block would. The server validates the target like any
 * other stylus edit.
 *
 * @param face the face the player clicked, used to pick the side of a new link
 */
public record MessageTargetLink(int entityId, BlockPos pos, Direction face, boolean remove) implements Message {

    public static final Type<MessageTargetLink> TYPE = new Type<>(PrincipiaMagica.loc("target_link"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageTargetLink> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageTargetLink::entityId,
            BlockPos.STREAM_CODEC, MessageTargetLink::pos,
            Direction.STREAM_CODEC, MessageTargetLink::face,
            ByteBufCodecs.BOOL, MessageTargetLink::remove,
            MessageTargetLink::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        if (player.level() instanceof ServerLevel level
                && level.getEntity(this.entityId) instanceof GolemEntity golem
                && golem.isAlive()) {
            StylusItem.targetLink(player, golem, this.pos, this.face, this.remove);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
