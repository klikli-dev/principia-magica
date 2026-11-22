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
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Picks up a corner glyph to move it horizontally: the footprint is removed, the other corner becomes the pending first corner
 * and the picked corner's lift becomes the ghost lift. The player then right-clicks a block to carve the corner there.
 * Like {@link MessageLiftCorner}, the glyph is identified by its carved block and face and may be targeted from a distance.
 */
public record MessagePickUpGlyph(int entityId, BlockPos pos, Direction face) implements Message {

    public static final Type<MessagePickUpGlyph> TYPE = new Type<>(PrincipiaMagica.loc("pick_up_glyph"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePickUpGlyph> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessagePickUpGlyph::entityId,
            BlockPos.STREAM_CODEC, MessagePickUpGlyph::pos,
            Direction.STREAM_CODEC, MessagePickUpGlyph::face,
            MessagePickUpGlyph::new
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

        Optional<ItemStack> stylus = StylusItem.heldSelectionStylus(player, golem);
        if (stylus.isPresent() && !StylusItem.cancelPending(player, stylus.get())) {
            return;
        }

        Workplace workplace = golem.getWorkplace(level).orElse(Workplace.EMPTY);
        Optional<CarvedGlyph> glyph = GolemCommands.findReachableGlyph(player, workplace, this.pos, this.face);
        if (glyph.isEmpty()) {
            return;
        }

        StylusActions.pickUpGlyph(workplace, glyph.get().pos(), glyph.get().face()).ifPresent(pickUp -> {
            golem.updateWorkplace(current -> pickUp.workplace());
            stylus.ifPresent(stack -> stack.set(DataComponentRegistry.STYLUS_PENDING_GLYPH.get(), StylusActions.pendingOf(level.dimension(), pickUp)));
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_picked_up"));
            StylusItem.syncWorkplace(player, golem);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
