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
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

/**
 * Erases a footprint by index, sent when the player sneak-right-clicks one of a footprint's 8 corners with the stylus.
 */
public record MessageEraseZone(int entityId, int index) implements Message {

    public static final Type<MessageEraseZone> TYPE = new Type<>(PrincipiaMagica.loc("erase_zone"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageEraseZone> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageEraseZone::entityId,
            ByteBufCodecs.VAR_INT, MessageEraseZone::index,
            MessageEraseZone::new
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
        if (!GlyphTargeting.isCornerEditable(player.getEyePosition(), footprint)) {
            return;
        }

        StylusActions.removeFootprint(workplace, this.index).ifPresent(updated -> {
            StylusItem.applyWorkplaceEdit(player, golem, updated);
            BlockPos center = BlockPos.containing(footprint.zone(0, 0).aabb().getCenter());
            level.playSound(null, center, SoundRegistry.STYLUS_ERASE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.footprint_erased"));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
