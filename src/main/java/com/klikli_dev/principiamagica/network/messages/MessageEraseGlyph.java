// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

/**
 * Erases the footprint that carries a glyph, sent when the player sneak-right-clicks a glyph they are targeting with the stylus.
 * The glyph may be far away, so the server only checks reach, not the block the player is looking at.
 */
public record MessageEraseGlyph(int entityId, BlockPos pos, Direction face) implements Message {

    public static final Type<MessageEraseGlyph> TYPE = new Type<>(PrincipiaMagica.loc("erase_glyph"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageEraseGlyph> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageEraseGlyph::entityId,
            BlockPos.STREAM_CODEC, MessageEraseGlyph::pos,
            Direction.STREAM_CODEC, MessageEraseGlyph::face,
            MessageEraseGlyph::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)
                || !(level.getEntity(this.entityId) instanceof GolemEntity golem)
                || !golem.isAlive()
                || !player.getUUID().equals(golem.getOwnerUUID())
                || !StylusItem.holdsSelection(player, golem)
                || !GlyphTargeting.isInReach(player.getEyePosition(), this.pos)) {
            return;
        }

        CarvedGlyph glyph = new CarvedGlyph(this.pos, this.face);
        Workplace workplace = golem.getWorkplace(level).orElse(Workplace.EMPTY);
        StylusActions.removeFootprintWithGlyph(workplace, glyph).ifPresent(updated -> {
            golem.updateWorkplace(current -> updated);
            level.playSound(null, this.pos, SoundEvents.CALCITE_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.footprint_erased"));
            StylusItem.syncWorkplace(player, golem);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
