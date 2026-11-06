// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStorage;
import com.klikli_dev.principiamagica.content.golem.item.CrackedGolemItem;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingJob;
import com.klikli_dev.principiamagica.content.golem.recipe.RepairCrackedGolemRecipe;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CrackedGolemGameTests {

    public static void deathDropsCrackedGolemWithoutLoot(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), pos);
        var job = PrincipiaMagicaRegistries.GOLEM_JOB.get(FarmingJob.KEY).orElseThrow();
        GolemSpec spec = GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(job);
        golem.setSpec(spec);
        golem.setOwnerUUID(UUID.randomUUID());

        BlockPos absolute = helper.absolutePos(pos);
        golem.setAnchor(GlobalPos.of(helper.getLevel().dimension(), absolute));
        golem.updateWorkplace(current -> StylusActions.cycleLink(current, absolute, Direction.UP));
        Workplace expectedWorkplace = StylusActions.toRelative(golem.getWorkplace(helper.getLevel()).orElseThrow(), absolute);

        golem.getInventory().insert(new ItemStack(Items.WHEAT, 3));
        golem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));

        AABB area = new AABB(absolute).inflate(4);
        golem.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);

        helper.runAfterDelay(2, () -> {
            List<ItemStack> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream().map(ItemEntity::getItem).toList();

            helper.assertTrue(count(dropped, ItemRegistry.CRACKED_GOLEM.get()) == 1, "Death should drop exactly one cracked golem");
            helper.assertTrue(count(dropped, ItemRegistry.GOLEM.get()) == 0, "Death must not drop a golem item");
            helper.assertTrue(count(dropped, Items.WHEAT) == 0, "Death must not drop the inventory contents");
            helper.assertTrue(count(dropped, Items.IRON_AXE) == 0, "Death must not drop the equipped tool");
            helper.assertTrue(dropped.size() == 1, "Death should drop nothing but the cracked golem, got " + dropped);

            ItemStack cracked = dropped.stream().filter(stack -> stack.is(ItemRegistry.CRACKED_GOLEM.get())).findFirst().orElseThrow();
            helper.assertTrue(spec.equals(cracked.get(DataComponentRegistry.GOLEM_SPEC.get())), "Cracked golem should keep the spec (axiom, upgrades)");
            helper.assertTrue(expectedWorkplace.equals(cracked.get(DataComponentRegistry.GOLEM_WORKPLACE.get())), "Cracked golem should keep the workplace");

            List<ItemStack> carried = carried(cracked);
            helper.assertTrue(count(carried, Items.WHEAT) == 3, "Cracked golem should carry the inventory, got " + carried);
            helper.assertTrue(count(carried, Items.IRON_AXE) == 1, "Cracked golem should carry the equipped tool, got " + carried);
            helper.succeed();
        });
    }

    public static void discardedGolemDropsNothing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), pos);
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        golem.setOwnerUUID(UUID.randomUUID());
        AABB area = new AABB(helper.absolutePos(pos)).inflate(4);

        golem.discard();

        helper.runAfterDelay(2, () -> {
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).isEmpty(),
                    "Discarding a golem must not drop a cracked golem or loot");
            helper.succeed();
        });
    }

    public static void stylusPickupKeepsInventoryOnGolemItem(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), pos);
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        golem.setOwnerUUID(player.getUUID());
        golem.getInventory().insert(new ItemStack(Items.WHEAT, 3));
        golem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
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

    public static void repairRecipeMatchesAnyOrderAndKeepsComponents(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        GolemSpec spec = GolemSpec.of(GolemMaterialRegistry.WOOD);
        Workplace workplace = StylusActions.cycleLink(Workplace.EMPTY, pos, Direction.UP);
        GolemStorage storage = new GolemStorage(ItemContainerContents.fromItems(List.of(new ItemStack(Items.WHEAT, 3))), ItemContainerContents.EMPTY);
        ItemStack cracked = CrackedGolemItem.createStack(spec, workplace, storage);
        ItemStack stylus = new ItemStack(ItemRegistry.COPPER_STYLUS.get());
        ItemStack clay = new ItemStack(ItemRegistry.QUICKENED_CLAY.get());
        var recipe = new RepairCrackedGolemRecipe();
        var level = helper.getLevel();

        CraftingInput input = grid(clay, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, stylus, ItemStack.EMPTY, cracked, ItemStack.EMPTY, ItemStack.EMPTY);
        helper.assertTrue(recipe.matches(input, level), "Repair should match stylus, cracked golem and clay in any slots");

        ItemStack result = recipe.assemble(input);
        helper.assertTrue(result.is(ItemRegistry.GOLEM.get()), "Repair should result in a golem item");
        helper.assertTrue(spec.equals(result.get(DataComponentRegistry.GOLEM_SPEC.get())), "Repair should keep the spec");
        helper.assertTrue(workplace.equals(result.get(DataComponentRegistry.GOLEM_WORKPLACE.get())), "Repair should keep the workplace");
        helper.assertTrue(storage.equals(result.get(DataComponentRegistry.GOLEM_STORAGE.get())), "Repair should keep the inventory and equipment");

        NonNullList<ItemStack> remainders = recipe.getRemainingItems(input);
        helper.assertTrue(remainders.stream().filter(stack -> !stack.isEmpty()).count() == 1
                        && remainders.stream().anyMatch(stack -> stack.is(ItemRegistry.COPPER_STYLUS.get())),
                "Only the stylus should be returned as remainder, the cracked golem and clay are consumed");

        helper.assertTrue(!recipe.matches(grid(stylus, cracked), level), "Repair needs the quickened clay");
        helper.assertTrue(!recipe.matches(grid(stylus, cracked, cracked, clay), level), "Repair rejects two cracked golems");
        helper.assertTrue(!recipe.matches(grid(stylus, stylus, cracked, clay), level), "Repair rejects two styluses");
        helper.assertTrue(!recipe.matches(grid(stylus, cracked, clay, new ItemStack(Items.STICK)), level), "Repair rejects foreign items");
        helper.assertTrue(!recipe.matches(grid(cracked, clay), level), "Repair needs the stylus");
        helper.succeed();
    }

    /**
     * Full cycle: the golem dies, the cracked golem is repaired and the golem item is placed again. The placed golem has
     * its inventory and tool back.
     */
    public static void repairedGolemRestoresInventoryAndTool(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), pos);
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        golem.setOwnerUUID(UUID.randomUUID());
        golem.getInventory().insert(new ItemStack(Items.WHEAT, 3));
        golem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        AABB area = new AABB(helper.absolutePos(pos)).inflate(4);

        golem.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F);

        helper.runAfterDelay(2, () -> {
            ItemStack cracked = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                    .map(ItemEntity::getItem)
                    .filter(stack -> stack.is(ItemRegistry.CRACKED_GOLEM.get()))
                    .findFirst().orElseThrow(() -> new AssertionError("Death should drop a cracked golem"));

            CraftingInput input = grid(new ItemStack(ItemRegistry.COPPER_STYLUS.get()), cracked, new ItemStack(ItemRegistry.QUICKENED_CLAY.get()));
            ItemStack repaired = new RepairCrackedGolemRecipe().assemble(input);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            BlockPos placeAt = helper.absolutePos(new BlockPos(6, 1, 6));
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(placeAt), Direction.UP, placeAt, false);
            UseOnContext context = new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, repaired, hit);
            InteractionResult result = repaired.getItem().useOn(context);
            helper.assertTrue(result == InteractionResult.SUCCESS, "Placing the repaired golem should succeed, got " + result);

            AABB placedArea = new AABB(placeAt).inflate(2);
            List<GolemEntity> placed = helper.getLevel().getEntitiesOfClass(GolemEntity.class, placedArea);
            helper.assertTrue(placed.size() == 1, "Placing should spawn exactly one golem, got " + placed.size());

            GolemEntity restored = placed.getFirst();
            helper.assertTrue(restored.getInventory().countItem(Items.WHEAT) == 3, "Placed golem should have its inventory back");
            helper.assertTrue(restored.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.IRON_AXE), "Placed golem should hold its axe again");
            helper.succeed();
        });
    }

    private static List<ItemStack> carried(ItemStack cracked) {
        GolemStorage storage = cracked.get(DataComponentRegistry.GOLEM_STORAGE.get());
        if (storage == null) {
            return List.of();
        }

        List<ItemStack> carried = new ArrayList<>();
        storage.inventory().nonEmptyItemCopyStream().forEach(carried::add);
        storage.equipment().nonEmptyItemCopyStream().forEach(carried::add);
        return carried;
    }

    private static CraftingInput grid(ItemStack... stacks) {
        List<ItemStack> items = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
        for (int i = 0; i < stacks.length; i++) {
            items.set(i, stacks[i]);
        }

        return CraftingInput.of(3, 3, items);
    }

    private static int count(List<ItemStack> stacks, Item item) {
        return stacks.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }
}
