// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Optional;

/**
 * What the golem is doing right now: the task type, the block it works on (if any) and an item that names the work (e.g. the crop being
 * harvested). Set on the server by the behaviours and synced to the client, so the golem GUI can show it live.
 *
 * @param type    the task type
 * @param target  the block the golem works on or walks to, if any
 * @param display an item that names the work, e.g. the crop being harvested. Empty if there is none
 */
public record GolemTask(TaskType type, Optional<BlockPos> target, ItemStack display) {

    public static final GolemTask IDLE = new GolemTask(TaskType.IDLE, Optional.empty(), ItemStack.EMPTY);

    public static final StreamCodec<RegistryFriendlyByteBuf, GolemTask> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(index -> TaskType.values()[index], TaskType::ordinal), GolemTask::type,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), GolemTask::target,
            ItemStack.OPTIONAL_STREAM_CODEC, GolemTask::display,
            GolemTask::new
    );

    /**
     * What the golem does. The order is the order of the lang keys, see {@link #describe()}.
     */
    public enum TaskType {
        IDLE,
        WAIT,
        HARVEST,
        PLANT,
        TILL,
        BONEMEAL,
        CLEAR,
        FELL,
        PICKUP,
        DEPOSIT,
        RESTOCK,
        FOLLOW,
        STAY
    }

    /**
     * @return a task of the given type on the target, without a display item
     */
    public static GolemTask of(TaskType type, Optional<BlockPos> target) {
        return new GolemTask(type, target, ItemStack.EMPTY);
    }

    /**
     * @return the player-facing text, e.g. "Harvesting Wheat" or "Depositing into 12, 64, -30". The display item names the work if there is one,
     * the target names it by position otherwise.
     */
    public Component describe() {
        String key = "golem_task." + PrincipiaMagica.MODID + "." + this.type.name().toLowerCase(Locale.ROOT);
        Component subject;
        if (!this.display.isEmpty()) {
            subject = this.display.getHoverName();
        } else if (this.target.isPresent()) {
            BlockPos pos = this.target.get();
            subject = Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        } else {
            subject = Component.translatable("golem_task." + PrincipiaMagica.MODID + ".work_area");
        }

        return Component.translatable(key, subject);
    }
}
