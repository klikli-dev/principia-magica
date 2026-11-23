// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStorage;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingJob;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GolemRemovalGameTests {

    public static void deathDropsContentsEquipmentAndAxiom(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), pos);
        var job = PrincipiaMagicaRegistries.GOLEM_JOB.get(FarmingJob.KEY).orElseThrow();
        GolemSpec spec = GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(job);
        golem.setSpec(spec);
        golem.setOwnerUUID(UUID.randomUUID());
        golem.setHandItem(new ItemStack(Items.WHEAT, 3));
        golem.setBackItem(new ItemStack(Items.IRON_AXE));
        AABB area = new AABB(helper.absolutePos(pos)).inflate(4);
        ItemStack expectedAxiom = AxiomItem.createStack(job);

        golem.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);

        helper.runAfterDelay(2, () -> {
            List<ItemStack> dropped = droppedStacks(helper, area);

            helper.assertTrue(count(dropped, Items.WHEAT) == 3, "Death should drop the inventory, got " + dropped);
            helper.assertTrue(count(dropped, Items.IRON_AXE) == 1, "Death should drop the equipped tool, got " + dropped);
            helper.assertTrue(dropped.stream().anyMatch(stack -> ItemStack.isSameItemSameComponents(stack, expectedAxiom)),
                    "Death should drop the axiom item for its job, got " + dropped);
            helper.assertTrue(count(dropped, ItemRegistry.GOLEM.get()) == 0, "Death must not drop a golem item");
            helper.succeed();
        });
    }

    public static void deathWithoutAxiomDropsNoAxiom(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), pos);
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        golem.setOwnerUUID(UUID.randomUUID());
        golem.insertCarried(new ItemStack(Items.WHEAT, 3));
        AABB area = new AABB(helper.absolutePos(pos)).inflate(4);

        golem.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);

        helper.runAfterDelay(2, () -> {
            List<ItemStack> dropped = droppedStacks(helper, area);

            helper.assertTrue(count(dropped, Items.WHEAT) == 3, "Death should drop the inventory, got " + dropped);
            helper.assertTrue(dropped.stream().noneMatch(stack -> stack.getItem() instanceof AxiomItem),
                    "A purposeless golem must not drop an axiom, got " + dropped);
            helper.succeed();
        });
    }

    public static void discardedGolemDropsNothing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), pos);
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        golem.setOwnerUUID(UUID.randomUUID());
        golem.setHandItem(new ItemStack(Items.WHEAT, 3));
        golem.setBackItem(new ItemStack(Items.IRON_AXE));
        AABB area = new AABB(helper.absolutePos(pos)).inflate(4);

        golem.discard();

        helper.runAfterDelay(2, () -> {
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).isEmpty(),
                    "Discarding a golem must not drop its inventory, its tool or a golem item");
            helper.succeed();
        });
    }

    public static void stylusPickupKeepsInventoryOnGolemItem(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), pos);
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        golem.setOwnerUUID(player.getUUID());
        golem.setHandItem(new ItemStack(Items.WHEAT, 3));
        golem.setBackItem(new ItemStack(Items.IRON_AXE));
        AABB area = new AABB(helper.absolutePos(pos)).inflate(4);
        player.setShiftKeyDown(true);

        ItemStack stylus = new ItemStack(ItemRegistry.COPPER_STYLUS.get());
        helper.assertTrue(ItemRegistry.COPPER_STYLUS.get().onLeftClickEntity(stylus, player, golem), "Shift-left-click should pick the golem up");

        helper.runAfterDelay(2, () -> {
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).isEmpty(),
                    "Stylus pickup must not drop the inventory or the tool on the ground");

            List<ItemStack> inventory = new ArrayList<>();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                inventory.add(player.getInventory().getItem(i));
            }
            ItemStack golemItem = inventory.stream().filter(stack -> stack.is(ItemRegistry.GOLEM.get())).findFirst().orElseThrow(
                    () -> new AssertionError("Stylus pickup should give the player a golem item"));

            List<ItemStack> carried = carried(golemItem);
            helper.assertTrue(count(carried, Items.WHEAT) == 3, "Golem item should carry the inventory, got " + carried);
            helper.assertTrue(count(carried, Items.IRON_AXE) == 1, "Golem item should carry the equipped tool, got " + carried);
            helper.succeed();
        });
    }

    private static List<ItemStack> droppedStacks(GameTestHelper helper, AABB area) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream().map(ItemEntity::getItem).toList();
    }

    private static List<ItemStack> carried(ItemStack golemItem) {
        GolemStorage storage = golemItem.get(DataComponentRegistry.GOLEM_STORAGE.get());
        if (storage == null) {
            return List.of();
        }

        List<ItemStack> carried = new ArrayList<>();
        carried.add(storage.hand());
        carried.add(storage.back());
        return carried;
    }

    private static int count(List<ItemStack> stacks, Item item) {
        return stacks.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }
}
