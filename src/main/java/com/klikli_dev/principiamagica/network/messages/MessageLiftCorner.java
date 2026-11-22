// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.menu.GolemCommands;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Lifts or lowers one corner glyph of a footprint, sent when the player sneaks with the stylus and scrolls over the glyph.
 * The glyph is identified by its carved block and face, like {@link MessageEraseGlyph}, and may be targeted from a distance.
 */
public record MessageLiftCorner(int entityId, BlockPos pos, Direction face, int delta) implements Message {

    public static final Type<MessageLiftCorner> TYPE = new Type<>(PrincipiaMagica.loc("lift_corner"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageLiftCorner> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageLiftCorner::entityId,
            BlockPos.STREAM_CODEC, MessageLiftCorner::pos,
            Direction.STREAM_CODEC, MessageLiftCorner::face,
            ByteBufCodecs.VAR_INT, MessageLiftCorner::delta,
            MessageLiftCorner::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)
                || !(level.getEntity(this.entityId) instanceof GolemEntity golem)
                || !golem.isAlive()
                || !player.getUUID().equals(golem.getOwnerUUID())
                || !StylusItem.holdsSelection(player, golem)) {
            return;
        }

        Workplace workplace = golem.getWorkplace(level).orElse(Workplace.EMPTY);
        GolemCommands.findReachableGlyph(player, workplace, this.pos, this.face).ifPresent(glyph ->
                StylusActions.liftCorner(workplace, glyph.pos(), glyph.face(), this.delta).ifPresent(updated -> {
                    golem.updateWorkplace(current -> updated);
                    Optional<CarvedGlyph> lifted = StylusActions.findGlyph(updated, glyph.pos(), glyph.face());
                    lifted.ifPresent(corner -> player.sendOverlayMessage(Component.translatable(
                            "message.principiamagica.stylus.corner_lifted",
                            String.format("%+d", corner.lift()))));
                    StylusItem.syncWorkplace(player, golem);
                }));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
