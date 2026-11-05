// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.item.GolemItem;
import com.klikli_dev.principiamagica.content.golem.job.CommonJobOptions;
import com.klikli_dev.principiamagica.content.golem.job.JobOptionValues;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingJob;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkReservations;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkplaceDefaults;
import com.klikli_dev.principiamagica.datagen.advancement.PrincipiaMagicaAdvancementSubProvider;
import com.klikli_dev.principiamagica.registry.ActivityRegistry;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GolemGameTests {

    public static void spawnsWithBrain(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), pos);
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));

        helper.assertTrue(golem.getMaxHealth() == 8.0F, "Wood golem should have 8 max health, got " + golem.getMaxHealth());

        UUID owner = UUID.randomUUID();
        golem.setOwnerUUID(owner);
        helper.assertTrue(owner.equals(golem.getOwnerUUID()), "Owner memory should hold the owner UUID");

        GlobalPos anchor = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos));
        golem.setAnchor(anchor);
        helper.assertTrue(Optional.of(anchor).equals(golem.getAnchor()), "Anchor memory should hold the anchor position");

        golem.setMode(GolemMode.FOLLOW);
        helper.assertTrue(golem.getBrain().isActive(ActivityRegistry.FOLLOW.value()), "Follow mode should activate the follow activity");

        helper.succeed();
    }

    public static void workZoneContainsAndIterates(GameTestHelper helper) {
        WorkZone zone = WorkZone.of(new BlockPos(2, 1, 1), new BlockPos(0, 0, 0));
        helper.assertTrue(zone.min().equals(new BlockPos(0, 0, 0)) && zone.max().equals(new BlockPos(2, 1, 1)), "WorkZone.of should normalize min and max");
        helper.assertTrue(zone.volume() == 12, "Zone volume should be 12, got " + zone.volume());
        helper.assertTrue(zone.contains(new BlockPos(2, 1, 1)), "Zone should contain its max corner");
        helper.assertTrue(!zone.contains(new BlockPos(3, 0, 0)), "Zone should not contain positions outside of it");
        helper.assertTrue(zone.positionAt(0).equals(new BlockPos(0, 0, 0)), "Index 0 should map to the min corner");
        helper.assertTrue(zone.positionAt(11).equals(new BlockPos(2, 1, 1)), "Last index should map to the max corner");

        int count = 0;
        for (BlockPos pos : zone.positions()) {
            helper.assertTrue(zone.contains(pos), "Iterated position " + pos + " should be inside the zone");
            count++;
        }
        helper.assertTrue(count == zone.volume(), "Iteration should visit every position, visited " + count);

        helper.succeed();
    }

    public static void containerRoleCycles(GameTestHelper helper) {
        helper.assertTrue(ContainerRole.SUPPLY.next() == ContainerRole.DEPOSIT, "SUPPLY should cycle to DEPOSIT");
        helper.assertTrue(ContainerRole.DEPOSIT.next() == ContainerRole.BOTH, "DEPOSIT should cycle to BOTH");
        helper.assertTrue(ContainerRole.BOTH.next() == ContainerRole.SUPPLY, "BOTH should cycle back to SUPPLY");
        helper.assertTrue(ContainerRole.SUPPLY.canSupply() && !ContainerRole.SUPPLY.canDeposit(), "SUPPLY should only supply");
        helper.assertTrue(!ContainerRole.DEPOSIT.canSupply() && ContainerRole.DEPOSIT.canDeposit(), "DEPOSIT should only deposit");
        helper.assertTrue(ContainerRole.BOTH.canSupply() && ContainerRole.BOTH.canDeposit(), "BOTH should supply and deposit");

        helper.succeed();
    }

    public static void workplaceCodecRoundTrips(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        JobOptionValues options = JobOptionValues.defaults(List.of(CommonJobOptions.PICKUP_ITEMS)).with(CommonJobOptions.PICKUP_ITEMS, false);
        Workplace workplace = new Workplace(
                List.of(WorkZone.centered(pos, 4, 1, 2)),
                List.of(new ContainerLink(pos.above(), Direction.UP, ContainerRole.BOTH)),
                options,
                Map.of(pos, BuiltInRegistries.ITEM.wrapAsHolder(Items.WHEAT)),
                true
        );

        Tag encoded = Workplace.CODEC.encodeStart(NbtOps.INSTANCE, workplace).getOrThrow();
        Workplace decoded = Workplace.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        helper.assertTrue(workplace.equals(decoded), "Workplace should survive an NBT round trip unchanged");
        helper.assertTrue(!decoded.options().get(CommonJobOptions.PICKUP_ITEMS), "Option values should survive the round trip");

        helper.succeed();
    }

    public static void workReservationsAreExclusive(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        helper.assertTrue(WorkReservations.tryReserve(level, pos, first, 100), "First golem should reserve a free position");
        helper.assertTrue(!WorkReservations.tryReserve(level, pos, second, 100), "Second golem should not reserve a claimed position");
        helper.assertTrue(WorkReservations.isReservedByOther(level, pos, second), "Position should be reported as reserved by another golem");
        helper.assertTrue(!WorkReservations.isReservedByOther(level, pos, first), "Owner should not see its own reservation as foreign");

        WorkReservations.release(level, pos, second);
        helper.assertTrue(WorkReservations.isReservedByOther(level, pos, second), "Non-owner release should not free the position");

        WorkReservations.release(level, pos, first);
        helper.assertTrue(WorkReservations.tryReserve(level, pos, second, 100), "Released position should be reservable again");

        helper.succeed();
    }

    public static void workplaceDefaultsLinkAdjacentChest(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos golemPos = new BlockPos(2, 2, 2);
        BlockPos chestPos = new BlockPos(3, 2, 2);
        helper.setBlock(chestPos, Blocks.CHEST);

        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), golemPos);
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));

        WorkplaceDefaults.applyIfEmpty(golem, level);

        Workplace workplace = golem.getWorkplace(level).orElseThrow();
        helper.assertTrue(workplace.initialized(), "Defaults should mark the workplace as initialized");
        helper.assertTrue(workplace.zones().size() == 1, "Defaults should create exactly one zone");
        helper.assertTrue(workplace.containsInZone(helper.absolutePos(golemPos)), "Default zone should contain the golem");
        helper.assertTrue(golem.getAnchor().equals(Optional.of(GlobalPos.of(level.dimension(), helper.absolutePos(golemPos)))),
                "Defaults should move the anchor to the golem's position");

        BlockPos absoluteChest = helper.absolutePos(chestPos);
        helper.assertTrue(workplace.links().stream().anyMatch(link -> link.pos().equals(absoluteChest)
                        && link.role() == ContainerRole.BOTH
                        && link.side() == Direction.WEST),
                "Adjacent chest should be auto-linked as BOTH on the face towards the golem");

        helper.succeed();
    }

    public static void defaultsKeepConfiguredWorkplace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        WorkZone configured = WorkZone.of(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(1, 1, 1)));
        golem.updateWorkplace(workplace -> workplace.withZones(List.of(configured)));

        WorkplaceDefaults.applyIfEmpty(golem, level);

        Workplace workplace = golem.getWorkplace(level).orElseThrow();
        helper.assertTrue(workplace.zones().equals(List.of(configured)), "A configured zone should be kept, got " + workplace.zones());

        helper.succeed();
    }

    public static void purposelessGolemStaysAndCannotWork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchorPos = new BlockPos(2, 2, 2);
        helper.setBlock(new BlockPos(3, 2, 2), Blocks.CHEST);

        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        golem.setAnchor(GlobalPos.of(level.dimension(), helper.absolutePos(anchorPos)));

        helper.assertTrue(golem.getMode() == GolemMode.STAY, "Purposeless golem should default to Stay, got " + golem.getMode());

        golem.setMode(GolemMode.WORK);
        helper.assertTrue(golem.getMode() == GolemMode.STAY, "Work should be rejected while purposeless, got " + golem.getMode());

        golem.setMode(GolemMode.FOLLOW);
        helper.assertTrue(golem.getMode() == GolemMode.FOLLOW, "Follow should be accepted while purposeless, got " + golem.getMode());
        helper.assertTrue(golem.getBrain().isActive(ActivityRegistry.FOLLOW.value()), "Follow mode should activate the follow activity");

        golem.setMode(GolemMode.STAY);
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(golem.getStatus() == GolemStatus.NO_PURPOSE, "Purposeless golem should report no purpose, got " + golem.getStatus());
            helper.assertTrue(!golem.getStatus().isProblem(), "No purpose should not count as a problem");
            helper.assertTrue(!golem.getBrain().isActive(Activity.WORK), "Purposeless golem should never activate its work");
            helper.succeed();
        });
    }

    public static void axiomEnablesWorkAndRemovalReturnsToStay(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(PrincipiaMagicaRegistries.GOLEM_JOB.get(FarmingJob.KEY).orElseThrow()));
        golem.setMode(GolemMode.WORK);
        helper.assertTrue(golem.getMode() == GolemMode.WORK, "Work should be accepted with an axiom, got " + golem.getMode());

        golem.takeJob();
        helper.assertTrue(golem.getMode() == GolemMode.STAY, "Removing the axiom should return the golem to Stay, got " + golem.getMode());

        helper.succeed();
    }

    public static void removingAxiomKeepsFollow(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(PrincipiaMagicaRegistries.GOLEM_JOB.get(FarmingJob.KEY).orElseThrow()));
        golem.setMode(GolemMode.FOLLOW);

        ItemStack axiom = golem.takeJob();
        helper.assertTrue(axiom.is(ItemRegistry.AXIOM_OF_HARVEST.get()), "Removing the job should return the harvest axiom");
        helper.assertTrue(golem.getMode() == GolemMode.FOLLOW, "Removing the axiom should keep Follow, got " + golem.getMode());

        helper.succeed();
    }

    public static void axiomGivenStartsWorkInDefaultArea(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        helper.setBlock(new BlockPos(2, 2, 1), Blocks.CHEST);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        golem.setOwnerUUID(player.getUUID());
        ItemStack axiom = ItemRegistry.AXIOM_OF_HARVEST.get().getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, axiom);

        InteractionResult result = axiom.interactLivingEntity(player, golem, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.consumesAction(), "Giving an axiom to an owned golem should succeed, got " + result);
        helper.assertTrue(axiom.isEmpty(), "The axiom should be consumed by the golem");
        helper.assertTrue(golem.getSpec().job().isPresent(), "The golem should have the farming job");
        helper.assertTrue(golem.getMode() == GolemMode.WORK, "The golem should switch to Work, got " + golem.getMode());

        ServerLevel level = helper.getLevel();
        Workplace workplace = golem.getWorkplace(level).orElseThrow();
        helper.assertTrue(workplace.zones().size() == 1, "Giving an axiom should create the default zone");
        WorkZone zone = workplace.zones().getFirst();
        helper.assertTrue(zone.min().equals(helper.absolutePos(new BlockPos(-3, 1, -3))) && zone.max().equals(helper.absolutePos(new BlockPos(5, 4, 5))),
                "Default zone should be 9x9 around the golem, got " + zone.min() + " to " + zone.max());
        BlockPos absoluteChest = helper.absolutePos(new BlockPos(2, 2, 1));
        helper.assertTrue(workplace.links().stream().anyMatch(link -> link.pos().equals(absoluteChest) && link.role() == ContainerRole.BOTH),
                "The adjacent chest should be linked as BOTH");

        helper.succeed();
    }

    public static void axiomGivenKeepsConfiguredWorkplace(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        WorkZone configured = WorkZone.of(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(1, 1, 1)));
        golem.updateWorkplace(workplace -> workplace.withZones(List.of(configured)));

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        golem.setOwnerUUID(player.getUUID());
        ItemStack axiom = ItemRegistry.AXIOM_OF_HARVEST.get().getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, axiom);

        axiom.interactLivingEntity(player, golem, InteractionHand.MAIN_HAND);
        helper.assertTrue(golem.getWorkplace(helper.getLevel()).orElseThrow().zones().equals(List.of(configured)),
                "Giving an axiom should keep the configured work area");

        helper.succeed();
    }

    public static void nonOwnerCannotGiveAxiom(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        golem.setOwnerUUID(UUID.randomUUID());

        Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack axiom = ItemRegistry.AXIOM_OF_HARVEST.get().getDefaultInstance();
        stranger.setItemInHand(InteractionHand.MAIN_HAND, axiom);

        InteractionResult result = axiom.interactLivingEntity(stranger, golem, InteractionHand.MAIN_HAND);
        helper.assertTrue(result == InteractionResult.FAIL, "A non-owner should be refused, got " + result);
        helper.assertTrue(golem.getSpec().job().isEmpty(), "A non-owner should not give the golem a job");
        helper.assertTrue(!axiom.isEmpty(), "A refused axiom should not be consumed");

        helper.succeed();
    }

    public static void axiomGivenHarvestsField(GameTestHelper helper) {
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        for (int x = 2; x <= 4; x++) {
            for (int z = 2; z <= 4; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.FARMLAND);
                helper.setBlock(new BlockPos(x, 2, z), Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
            }
        }
        helper.setBlock(new BlockPos(2, 2, 1), Blocks.CHEST);

        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        golem.setOwnerUUID(player.getUUID());
        ItemStack axiom = ItemRegistry.AXIOM_OF_HARVEST.get().getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, axiom);
        axiom.interactLivingEntity(player, golem, InteractionHand.MAIN_HAND);

        helper.succeedWhen(() -> {
            for (int x = 2; x <= 4; x++) {
                for (int z = 2; z <= 4; z++) {
                    BlockState state = helper.getBlockState(new BlockPos(x, 2, z));
                    helper.assertTrue(!(state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) == 7), "Mature wheat at " + x + "," + z + " should be harvested");
                }
            }
            helper.assertTrue(golem.getInventory().countItem(Items.WHEAT) >= 9, "The golem should carry the harvested wheat, has " + golem.getInventory().countItem(Items.WHEAT));
        });
    }

    public static void axiomGivenLumberjackWithoutAxeReportsMissingTool(GameTestHelper helper) {
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        golem.setOwnerUUID(player.getUUID());
        ItemStack axiom = ItemRegistry.AXIOM_OF_FELLING.get().getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, axiom);
        axiom.interactLivingEntity(player, golem, InteractionHand.MAIN_HAND);

        helper.runAfterDelay(40, () -> {
            helper.assertTrue(golem.getMode() == GolemMode.WORK, "The lumberjack should be in Work, got " + golem.getMode());
            helper.assertTrue(golem.getStatus() == GolemStatus.MISSING_TOOL, "Without an axe the lumberjack should report a missing tool, got " + golem.getStatus());
            helper.succeed();
        });
    }

    public static void placedGolemWithoutAxiomIsPurposeless(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos clicked = helper.absolutePos(new BlockPos(1, 1, 1));
        ItemStack stack = GolemItem.createStack(GolemSpec.of(GolemMaterialRegistry.WOOD));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(clicked), Direction.UP, clicked, false)));

        List<GolemEntity> golems = level.getEntitiesOfClass(GolemEntity.class, new AABB(clicked.above()).inflate(1));
        helper.assertTrue(golems.size() == 1, "Placing the golem should spawn exactly one golem, found " + golems.size());
        GolemEntity golem = golems.getFirst();
        helper.assertTrue(golem.getSpec().job().isEmpty(), "A placed golem should have no axiom");
        helper.assertTrue(golem.getMode() == GolemMode.STAY, "A placed golem without axiom should be in Stay, got " + golem.getMode());
        helper.assertTrue(!golem.removeWhenFarAway(128.0), "A placed golem should never despawn");
        helper.assertTrue(golem.getWorkplace(level).orElseThrow().zones().isEmpty(), "A placed golem should not create a work area before it is given an axiom");

        AdvancementHolder advancement = level.getServer().getAdvancements().get(PrincipiaMagicaAdvancementSubProvider.CLAY_THAT_STANDS);
        helper.assertTrue(advancement != null, "Advancement clay_that_stands is missing");
        helper.assertTrue(advancement.value().criteria().containsKey("golem_placed"), "Clay That Stands should be triggered by golem_placed");

        helper.succeed();
    }
}
