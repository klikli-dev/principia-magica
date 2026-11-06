// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * The inventory and equipment of a golem, kept on a cracked golem or a golem item so they survive death and pickup.
 * Inventory slots follow {@link GolemInventory}, equipment follows {@link EquipmentSlot#VALUES}.
 */
public record GolemStorage(ItemContainerContents inventory, ItemContainerContents equipment) {

    public static final Codec<GolemStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemContainerContents.CODEC.fieldOf("inventory").forGetter(GolemStorage::inventory),
            ItemContainerContents.CODEC.fieldOf("equipment").forGetter(GolemStorage::equipment)
    ).apply(instance, GolemStorage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GolemStorage> STREAM_CODEC = StreamCodec.composite(
            ItemContainerContents.STREAM_CODEC, GolemStorage::inventory,
            ItemContainerContents.STREAM_CODEC, GolemStorage::equipment,
            GolemStorage::new
    );

    /**
     * @return the total number of items stored in the inventory and equipment
     */
    public int itemCount() {
        return this.inventory.nonEmptyItemCopyStream().mapToInt(stack -> stack.getCount()).sum()
                + this.equipment.nonEmptyItemCopyStream().mapToInt(stack -> stack.getCount()).sum();
    }
}
