// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemInventory;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.upgrade.UpgradeAddResult;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.GolemUpgradeRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class UpgradeGameTests {

    public static void swiftnessIncreasesMovementSpeed(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.STONE));
        double before = golem.getAttributeValue(Attributes.MOVEMENT_SPEED);

        helper.assertTrue(golem.addUpgrade(GolemUpgradeRegistry.SWIFTNESS) == UpgradeAddResult.ADDED, "Swiftness should be added to a stone golem with a free slot");
        double after = golem.getAttributeValue(Attributes.MOVEMENT_SPEED);
        helper.assertTrue(after > before, "Swiftness should increase movement speed, before " + before + ", after " + after);

        golem.removeUpgrade(0);
        helper.assertTrue(Math.abs(golem.getAttributeValue(Attributes.MOVEMENT_SPEED) - before) < 1.0E-9,
                "Removing swiftness should remove its movement speed modifier");

        helper.succeed();
    }

    public static void capacityAddsInventorySlots(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        helper.assertTrue(golem.getInventory().getUsableSlots() == 3, "Golem should start with 3 usable slots, got " + golem.getInventory().getUsableSlots());

        helper.assertTrue(golem.addUpgrade(GolemUpgradeRegistry.CAPACITY) == UpgradeAddResult.ADDED, "Capacity should be added to a wood golem with a free slot");
        helper.assertTrue(golem.getInventory().getUsableSlots() == 6, "Capacity should raise usable slots to 6, got " + golem.getInventory().getUsableSlots());

        helper.succeed();
    }

    public static void capacityRemovalMovesItemsIntoFreeSlots(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        golem.addUpgrade(GolemUpgradeRegistry.CAPACITY);
        for (int i = 3; i < 6; i++) {
            golem.getInventory().setItem(i, new ItemStack(Items.STONE));
        }

        ItemStack removed = golem.removeUpgrade(0);
        helper.assertTrue(removed.is(ItemRegistry.CAPACITY_UPGRADE.get()), "Removing an upgrade should return its item");
        helper.assertTrue(golem.getInventory().getUsableSlots() == 3, "Removing capacity should drop usable slots back to 3");

        int stones = 0;
        for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
            stones += golem.getInventory().getItem(i).getCount();
        }
        helper.assertTrue(stones == 3, "All three stones should be kept, found " + stones);
        for (int i = 3; i < GolemInventory.MAX_SLOTS; i++) {
            helper.assertTrue(golem.getInventory().getItem(i).isEmpty(), "Unusable slot " + i + " should be empty after eviction");
        }

        helper.succeed();
    }

    public static void capacityRemovalDropsOverflow(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        golem.addUpgrade(GolemUpgradeRegistry.CAPACITY);
        for (int i = 0; i < 3; i++) {
            golem.getInventory().setItem(i, new ItemStack(Items.DIRT, Items.DIRT.getDefaultMaxStackSize()));
        }
        for (int i = 3; i < 6; i++) {
            golem.getInventory().setItem(i, new ItemStack(Items.STONE));
        }

        golem.removeUpgrade(0);

        List<ItemEntity> dropped = helper.getEntities(EntityType.ITEM);
        int droppedStones = dropped.stream()
                .filter(item -> item.getItem().is(Items.STONE))
                .mapToInt(item -> item.getItem().getCount())
                .sum();
        helper.assertTrue(droppedStones == 3, "Overflowing stones should be dropped, found " + droppedStones);

        int dirt = 0;
        for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
            dirt += golem.getInventory().getItem(i).is(Items.DIRT) ? golem.getInventory().getItem(i).getCount() : 0;
        }
        helper.assertTrue(dirt == 3 * Items.DIRT.getDefaultMaxStackSize(), "Usable slots should keep their dirt");

        helper.succeed();
    }

    public static void duplicateUpgradeIsRejected(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.STONE));

        helper.assertTrue(golem.addUpgrade(GolemUpgradeRegistry.SWIFTNESS) == UpgradeAddResult.ADDED, "First swiftness should be added");
        helper.assertTrue(golem.addUpgrade(GolemUpgradeRegistry.SWIFTNESS) == UpgradeAddResult.DUPLICATE, "Second swiftness should be rejected as duplicate");
        helper.assertTrue(golem.getSpec().upgrades().size() == 1, "Duplicate should not change the upgrade list");

        helper.succeed();
    }

    public static void upgradeSlotLimitIsRespected(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));

        helper.assertTrue(golem.addUpgrade(GolemUpgradeRegistry.SWIFTNESS) == UpgradeAddResult.ADDED, "Wood golem should accept one upgrade");
        helper.assertTrue(golem.addUpgrade(GolemUpgradeRegistry.CAPACITY) == UpgradeAddResult.NO_FREE_SLOT, "Wood golem should reject a second upgrade");
        helper.assertTrue(golem.getSpec().upgrades().size() == 1, "Rejected upgrade should not be added");

        helper.succeed();
    }
}
