// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.menu.GolemCommands;
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
 * Lifts or lowers one corner glyph of a footprint, sent when the player sneaks with the stylus and scrolls over the glyph.
 * The glyph is identified by its 3D corner position and may be targeted from a distance.
 */
public record MessageLiftCorner(int entityId, BlockPos cornerPos, int delta) implements Message {

    public static final Type<MessageLiftCorner> TYPE = new Type<>(PrincipiaMagica.loc("lift_corner"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageLiftCorner> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageLiftCorner::entityId,
            BlockPos.STREAM_CODEC, MessageLiftCorner::cornerPos,
            ByteBufCodecs.VAR_INT, MessageLiftCorner::delta,
            MessageLiftCorner::new
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
        GolemCommands.findReachableGlyph(player, workplace, this.cornerPos).ifPresent(glyph -> {
            StylusActions.EditResult result = StylusActions.liftCorner(workplace, glyph.corner(), this.delta);
            if (result.isSuccess()) {
                golem.updateWorkplace(current -> result.workplace());
                player.sendOverlayMessage(Component.translatable(
                        "message.principiamagica.stylus.corner_lifted",
                        String.format("%+d", this.delta)));
                StylusItem.syncWorkplace(player, golem);
                return;
            }

            level.playSound(null, this.cornerPos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
            switch (result.failure()) {
                case CORNER_OCCUPIED -> player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_occupied"));
                case TOO_LARGE -> player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.footprint_too_large",
                        WorkFootprint.MAX_SIZE, WorkFootprint.MAX_SIZE));
                case OUT_OF_BOUNDS -> player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.out_of_bounds"));
                case NOT_FOUND, null -> {}
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
