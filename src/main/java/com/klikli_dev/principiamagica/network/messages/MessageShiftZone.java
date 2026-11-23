// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

/**
 * Moves a whole footprint of a golem up or down with the stylus, while the player holds the stylus with that golem selected and sneaks.
 * Sent when the player scrolls over the footprint box rather than over one of its corner glyphs.
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
        if (!(player.level() instanceof ServerLevel level)
                || !(level.getEntity(this.entityId) instanceof GolemEntity golem)
                || !golem.isAlive()
                || !player.getUUID().equals(golem.getOwnerUUID())
                || !StylusItem.isWithinRange(player, golem)
                || !StylusItem.holdsSelection(player, golem)) {
            return;
        }

        Workplace workplace = golem.getWorkplace(level).orElse(Workplace.EMPTY);
        if (this.index < 0 || this.index >= workplace.footprints().size()) {
            return;
        }

        WorkFootprint footprint = workplace.footprints().get(this.index);
        if (!GlyphTargeting.isBoxInReach(player.getEyePosition(), footprint.zone(0, 0).aabb())) {
            return;
        }

        StylusActions.EditResult result = StylusActions.shiftFootprint(workplace, this.index, this.delta);
        if (result.isSuccess()) {
            Workplace updated = result.workplace();
            golem.updateWorkplace(current -> updated);
            int applied = updated.footprints().get(this.index).minY() - footprint.minY();
            player.sendOverlayMessage(Component.translatable(
                    "message.principiamagica.stylus.footprint_moved",
                    String.format("%+d", applied)));
            StylusItem.syncWorkplace(player, golem);
            return;
        }

        BlockPos center = BlockPos.containing(footprint.zone(0, 0).aabb().getCenter());
        level.playSound(null, center, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
        switch (result.failure()) {
            case CORNER_OCCUPIED -> player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_occupied"));
            case OUT_OF_BOUNDS -> player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.out_of_bounds"));
            case TOO_LARGE, NOT_FOUND -> {}
            case null -> {}
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
