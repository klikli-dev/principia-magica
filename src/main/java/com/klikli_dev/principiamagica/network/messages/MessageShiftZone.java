// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.menu.GolemCommands;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
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

import java.util.List;

/**
 * Moves a whole footprint of a golem up or down with the stylus, while the player holds the stylus with that golem selected and sneaks.
 * Sent when the player scrolls over the footprint body rather than over one of its corner glyphs.
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
                    ServerLevel level = (ServerLevel) golem.level();
                    List<WorkFootprint> before = golem.getWorkplace(level).map(Workplace::footprints).orElse(List.of());
                    GolemCommands.editWorkplace(player, golem, workplace -> GolemCommands.shiftFootprint(workplace, this.index, this.delta));
                    List<WorkFootprint> after = golem.getWorkplace(level).map(Workplace::footprints).orElse(List.of());
                    if (this.index >= 0 && this.index < before.size() && after.size() == before.size()) {
                        int applied = after.get(this.index).minY() - before.get(this.index).minY();
                        player.sendOverlayMessage(Component.translatable(
                                "message.principiamagica.stylus.footprint_raised",
                                String.format("%+d", applied)));
                    }
                });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
