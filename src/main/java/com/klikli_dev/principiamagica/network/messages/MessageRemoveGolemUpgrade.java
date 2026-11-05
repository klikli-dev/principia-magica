// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.network.messages;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.menu.GolemCommands;
import com.klikli_dev.principiamagica.network.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record MessageRemoveGolemUpgrade(int entityId, int index) implements Message {

    public static final Type<MessageRemoveGolemUpgrade> TYPE = new Type<>(PrincipiaMagica.loc("remove_golem_upgrade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageRemoveGolemUpgrade> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageRemoveGolemUpgrade::entityId,
            ByteBufCodecs.VAR_INT, MessageRemoveGolemUpgrade::index,
            MessageRemoveGolemUpgrade::new
    );

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        GolemCommands.findOwnedGolem(player, this.entityId).ifPresent(golem -> {
            ItemStack upgrade = golem.removeUpgrade(this.index);
            if (!upgrade.isEmpty()) {
                player.getInventory().placeItemBackInInventory(upgrade);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
