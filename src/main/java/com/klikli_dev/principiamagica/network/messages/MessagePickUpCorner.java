// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.network.Message;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Picks up one of a footprint's 8 corners so the player can move and re-place it elsewhere.
 * The opposite diagonal corner becomes the pending anchor and the original footprint is kept for cancellation.
 */
public record MessagePickUpCorner(int entityId, int footprintIndex, int cornerIndex) implements Message {

    public static final Type<MessagePickUpCorner> TYPE = new Type<>(PrincipiaMagica.loc("pick_up_corner"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePickUpCorner> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessagePickUpCorner::entityId,
            ByteBufCodecs.VAR_INT, MessagePickUpCorner::footprintIndex,
            ByteBufCodecs.VAR_INT, MessagePickUpCorner::cornerIndex,
            MessagePickUpCorner::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)
                || !(level.getEntity(this.entityId) instanceof GolemEntity golem)
                || !golem.isAlive()
                || !player.getUUID().equals(golem.getOwnerUUID())
                || !StylusItem.isWithinRange(player, golem)) {
            return;
        }

        Optional<ItemStack> stylus = StylusItem.heldSelectionStylus(player, golem);
        if (stylus.isEmpty()) {
            return;
        }

        ItemStack stack = stylus.get();
        PendingGlyph existingPending = stack.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
        if (existingPending != null && existingPending.pos().dimension().equals(level.dimension())) {
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

        StylusActions.pickUpCorner(workplace, level.dimension(), this.footprintIndex, this.cornerIndex).ifPresent(picked -> {
            stack.set(DataComponentRegistry.STYLUS_PENDING_GLYPH.get(), picked.pending());
            StylusItem.applyWorkplaceEdit(player, golem, picked.workplace());
            level.playSound(null, footprint.cornerBlock(this.cornerIndex), SoundRegistry.STYLUS_PICK_UP_CORNER.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_picked_up"));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
