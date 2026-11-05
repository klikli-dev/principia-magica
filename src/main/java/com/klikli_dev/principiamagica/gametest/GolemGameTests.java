// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.job.CommonJobOptions;
import com.klikli_dev.principiamagica.content.golem.job.JobOptionValues;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkReservations;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkplaceDefaults;
import com.klikli_dev.principiamagica.registry.ActivityRegistry;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

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
        BlockPos anchorPos = new BlockPos(2, 2, 2);
        BlockPos chestPos = new BlockPos(3, 2, 2);
        helper.setBlock(chestPos, Blocks.CHEST);

        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), new BlockPos(1, 2, 1));
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD));
        golem.setAnchor(GlobalPos.of(level.dimension(), helper.absolutePos(anchorPos)));

        WorkplaceDefaults.initializeIfNeeded(golem, level);

        Workplace workplace = golem.getWorkplace(level).orElseThrow();
        helper.assertTrue(workplace.initialized(), "Defaults should mark the workplace as initialized");
        helper.assertTrue(workplace.zones().size() == 1, "Defaults should create exactly one zone");
        helper.assertTrue(workplace.containsInZone(helper.absolutePos(anchorPos)), "Default zone should contain the anchor");

        BlockPos absoluteChest = helper.absolutePos(chestPos);
        helper.assertTrue(workplace.links().stream().anyMatch(link -> link.pos().equals(absoluteChest)
                        && link.role() == ContainerRole.BOTH
                        && link.side() == Direction.WEST),
                "Adjacent chest should be auto-linked as BOTH on the face towards the anchor");

        helper.succeed();
    }
}
