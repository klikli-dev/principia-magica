// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

/**
 * The inventory and equipment of a golem, kept on a golem item so they survive stylus pickup.
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

    /**
     * Snapshots the inventory and equipment. The golem keeps them, so the caller decides what to do with the snapshot.
     */
    public static GolemStorage capture(GolemEntity golem) {
        GolemInventory inventory = golem.getInventory();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            items.add(inventory.getItem(i));
        }

        List<ItemStack> equipment = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            equipment.add(golem.getItemBySlot(slot));
        }

        return new GolemStorage(ItemContainerContents.fromItems(items), ItemContainerContents.fromItems(equipment));
    }

    /**
     * Puts the stored inventory and equipment back on the golem. Used when a golem item with storage is placed.
     */
    public void restoreTo(GolemEntity golem) {
        GolemInventory inventory = golem.getInventory();
        NonNullList<ItemStack> items = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        this.inventory.copyInto(items);
        for (int i = 0; i < items.size(); i++) {
            inventory.setItem(i, items.get(i));
        }

        NonNullList<ItemStack> equipment = NonNullList.withSize(EquipmentSlot.VALUES.size(), ItemStack.EMPTY);
        this.equipment.copyInto(equipment);
        for (int i = 0; i < equipment.size(); i++) {
            golem.setItemSlot(EquipmentSlot.VALUES.get(i), equipment.get(i));
        }
    }
}
