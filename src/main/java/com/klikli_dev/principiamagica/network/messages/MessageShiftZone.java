// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.menu.GolemCommands;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.network.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Raises or lowers a footprint of a golem by the stylus, while the player holds the stylus with that golem selected and sneaks.
 */
public record MessageShiftZone(int entityId, int index, int delta) implements Message {

    public static final Type<MessageShiftZone> TYPE = new Type<>(PrincipiaMagica.loc("shift_zone"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageShiftZone> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageShiftZone::entityId,
            ByteBufCodecs.VAR_INT, MessageShiftZone::index,
            ByteBufCodecs.VAR_INT, MessageShiftZone::delta,
            MessageShiftZone::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        GolemCommands.findOwnedGolem(player, this.entityId)
                .filter(golem -> StylusItem.holdsSelection(player, golem))
                .ifPresent(golem -> {
                    GolemCommands.editWorkplace(player, golem, workplace -> GolemCommands.shiftFootprint(workplace, this.index, this.delta));
                    golem.getWorkplace((ServerLevel) golem.level())
                            .map(Workplace::footprints)
                            .filter(footprints -> this.index >= 0 && this.index < footprints.size())
                            .ifPresent(footprints -> player.sendOverlayMessage(Component.translatable(
                                    "message.principiamagica.stylus.footprint_raised",
                                    String.format("%+d", footprints.get(this.index).raise()))));
                });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
