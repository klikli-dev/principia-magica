// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * The items a golem carries, kept on a golem item so they survive stylus pickup.
 *
 * @param hand the hand item: the tool while working, or the carried item
 * @param back the back item: the tool while the golem carries something
 */
public record GolemStorage(ItemStack hand, ItemStack back) {

    public static final Codec<GolemStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("hand").forGetter(GolemStorage::hand),
            ItemStack.OPTIONAL_CODEC.fieldOf("back").forGetter(GolemStorage::back)
    ).apply(instance, GolemStorage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GolemStorage> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, GolemStorage::hand,
            ItemStack.OPTIONAL_STREAM_CODEC, GolemStorage::back,
            GolemStorage::new
    );

    /**
     * @return the total number of items stored in the hand and the back
     */
    public int itemCount() {
        return this.hand.getCount() + this.back.getCount();
    }

    /**
     * Snapshots the hand and the back. The golem keeps them, so the caller decides what to do with the snapshot.
     */
    public static GolemStorage capture(GolemEntity golem) {
        return new GolemStorage(golem.getHandItem().copy(), golem.getBackItem().copy());
    }

    /**
     * Puts the stored hand and back item back on the golem. Used when a golem item with storage is placed.
     */
    public void restoreTo(GolemEntity golem) {
        golem.setHandItem(this.hand.copy());
        golem.setBackItem(this.back.copy());
    }
}
