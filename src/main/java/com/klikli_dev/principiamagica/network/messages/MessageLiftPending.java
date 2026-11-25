// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.network.Message;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Lifts or lowers the ghost corner of a pending footprint on the stylus, before the player confirms it with a second right-click.
 * Sent when the player sneaks with a stylus that has a pending corner and scrolls, so it needs no glyph target.
 */
public record MessageLiftPending(int delta) implements Message {

    public static final Type<MessageLiftPending> TYPE = new Type<>(PrincipiaMagica.loc("lift_pending"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageLiftPending> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageLiftPending::delta,
            MessageLiftPending::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        Optional<ItemStack> stylus = StylusItem.heldPendingStylus(player);
        stylus.ifPresent(stack -> {
            PendingGlyph pending = stack.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
            if (pending == null) {
                return;
            }

            PendingGlyph lifted = pending.withGhostLift(this.delta);
            stack.set(DataComponentRegistry.STYLUS_PENDING_GLYPH.get(), lifted);
            if (lifted.ghostLift() != pending.ghostLift()) {
                player.level().playSound(null, player.blockPosition(), SoundRegistry.STYLUS_ADJUST_HEIGHT.get(),
                        SoundSource.PLAYERS, 1.0F, this.delta > 0 ? 1.2F : 0.9F);
            } else {
                player.level().playSound(null, player.blockPosition(), SoundRegistry.STYLUS_FAIL.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_lifted",
                    String.format("%+d", lifted.ghostLift())));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
