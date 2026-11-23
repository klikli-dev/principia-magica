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
 * Resizes a footprint vertically by moving one of its 8 corners up or down, sent when the player Shift+scrolls
 * over a corner (or while locked onto it) with the stylus.
 */
public record MessageResizeCorner(int entityId, int footprintIndex, int cornerIndex, int delta) implements Message {

    public static final Type<MessageResizeCorner> TYPE = new Type<>(PrincipiaMagica.loc("resize_corner"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageResizeCorner> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageResizeCorner::entityId,
            ByteBufCodecs.VAR_INT, MessageResizeCorner::footprintIndex,
            ByteBufCodecs.VAR_INT, MessageResizeCorner::cornerIndex,
            ByteBufCodecs.VAR_INT, MessageResizeCorner::delta,
            MessageResizeCorner::new
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
        if (this.footprintIndex < 0 || this.footprintIndex >= workplace.footprints().size() || !WorkFootprint.isValidCorner(this.cornerIndex)) {
            return;
        }

        WorkFootprint footprint = workplace.footprints().get(this.footprintIndex);
        if (!GlyphTargeting.isCornerEditable(player.getEyePosition(), footprint)) {
            return;
        }

        StylusActions.EditResult result = StylusActions.resizeCornerY(workplace, this.footprintIndex, this.cornerIndex, this.delta);
        if (result.isSuccess()) {
            StylusItem.applyWorkplaceEdit(player, golem, result.workplace());
            player.sendOverlayMessage(Component.translatable(
                    "message.principiamagica.stylus.corner_lifted",
                    String.format("%+d", this.delta)));
            return;
        }

        level.playSound(null, footprint.cornerBlock(this.cornerIndex), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
        switch (result.failure()) {
            case TOO_LARGE -> player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.footprint_too_large",
                    WorkFootprint.MAX_SIZE, WorkFootprint.MAX_SIZE));
            case OUT_OF_BOUNDS -> player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.out_of_bounds"));
            case CORNER_OCCUPIED, NOT_FOUND -> {}
            case null -> {}
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
