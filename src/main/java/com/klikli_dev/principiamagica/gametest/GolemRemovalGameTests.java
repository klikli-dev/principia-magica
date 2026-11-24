// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStorage;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.job.JobOptionValues;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingJob;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingTargets;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public static void stylusPickupAndPlaceAtNewLocationKeepsWorkplace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos originalPos = new BlockPos(1, 2, 1);
        BlockPos chestPos = new BlockPos(1, 2, 3);
        BlockPos newFloorPos = new BlockPos(4, 1, 4);

        helper.setBlock(chestPos, Blocks.CHEST);
        helper.setBlock(newFloorPos, Blocks.STONE);
        helper.setBlock(newFloorPos.above(), Blocks.AIR);

        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), originalPos);
        var job = PrincipiaMagicaRegistries.GOLEM_JOB.get(FarmingJob.KEY).orElseThrow();
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(job));

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        golem.setOwnerUUID(player.getUUID());
        golem.setAnchor(GlobalPos.of(level.dimension(), helper.absolutePos(originalPos)));

        WorkFootprint footprint = WorkFootprint.of(
                new CarvedGlyph(helper.absolutePos(new BlockPos(0, 1, 0)), Direction.UP),
                new CarvedGlyph(helper.absolutePos(new BlockPos(3, 1, 3)), Direction.UP, 2)).withDimension(level.dimension());
        ContainerLink link = new ContainerLink(helper.absolutePos(chestPos), Direction.UP, ContainerRole.BOTH);
        golem.updateWorkplace(current -> current
                .withFootprints(List.of(footprint))
                .withLinks(List.of(link))
                .withOptions(JobOptionValues.EMPTY.with(FarmingTargets.TILL, true))
                .withItemMemory(Map.of(helper.absolutePos(new BlockPos(1, 1, 1)), Items.WHEAT_SEEDS.builtInRegistryHolder()))
                .markInitialized());

        Workplace originalWorkplace = golem.getWorkplace(level).orElseThrow();

        player.setShiftKeyDown(true);
        ItemStack stylus = new ItemStack(ItemRegistry.COPPER_STYLUS.get());
        helper.assertTrue(ItemRegistry.COPPER_STYLUS.get().onLeftClickEntity(stylus, player, golem),
                "Shift-left-click should pick the golem up");

        ItemStack golemItem = ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (candidate.is(ItemRegistry.GOLEM.get())) {
                golemItem = candidate;
                break;
            }
        }
        helper.assertTrue(!golemItem.isEmpty(), "Stylus pickup should give the player a golem item");

        Workplace stored = golemItem.get(DataComponentRegistry.GOLEM_WORKPLACE.get());
        helper.assertTrue(stored != null, "Picked up golem item should store GOLEM_WORKPLACE");

        Workplace crossDimension = StylusActions.forDimension(stored, Level.NETHER);
        helper.assertTrue(crossDimension.footprints().isEmpty() && crossDimension.links().isEmpty()
                        && crossDimension.itemMemory().isEmpty() && !crossDimension.initialized(),
                "Placing a picked up golem in a different dimension must clear old dimension footprints, links, and memory");

        player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, golemItem);
        BlockPos clicked = helper.absolutePos(newFloorPos);
        InteractionResult result = golemItem.useOn(new UseOnContext(
                player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(clicked), Direction.UP, clicked, false)));
        helper.assertTrue(result.consumesAction(), "Placing the golem item at the new location should succeed, got " + result);

        List<GolemEntity> placedGolems = level.getEntitiesOfClass(GolemEntity.class, new AABB(clicked.above()).inflate(1));
        helper.assertTrue(placedGolems.size() == 1, "Placing the golem should spawn exactly one golem, found " + placedGolems.size());
        GolemEntity placed = placedGolems.getFirst();

        helper.assertTrue(placed.getAnchor().equals(Optional.of(GlobalPos.of(level.dimension(), clicked.above()))),
                "Placed golem should update its anchor to the new position, got " + placed.getAnchor());

        Workplace restoredWorkplace = placed.getWorkplace(level).orElseThrow();
        helper.assertTrue(restoredWorkplace.footprints().equals(originalWorkplace.footprints()),
                "Work area footprints should stay at their original world positions, expected "
                        + originalWorkplace.footprints() + " but got " + restoredWorkplace.footprints());
        helper.assertTrue(restoredWorkplace.links().equals(originalWorkplace.links()),
                "Linked storage containers should stay at their original world positions, expected "
                        + originalWorkplace.links() + " but got " + restoredWorkplace.links());
        helper.assertTrue(restoredWorkplace.options().equals(originalWorkplace.options()),
                "Job options should be preserved");
        helper.assertTrue(restoredWorkplace.itemMemory().equals(originalWorkplace.itemMemory()),
                "Item memory should be preserved, expected "
                        + originalWorkplace.itemMemory() + " but got " + restoredWorkplace.itemMemory());
        helper.assertTrue(restoredWorkplace.initialized(), "Workplace initialized flag should be preserved");

        helper.succeed();
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
