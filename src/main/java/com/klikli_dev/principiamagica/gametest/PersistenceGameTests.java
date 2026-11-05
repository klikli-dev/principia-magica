// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingJob;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingTargets;
import com.klikli_dev.principiamagica.content.golem.job.JobOptionValues;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.GolemUpgradeRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PersistenceGameTests {
    private static final BlockPos GOLEM_POS = new BlockPos(1, 2, 1);
    private static final BlockPos CHEST_POS = new BlockPos(1, 2, 3);

    /**
     * A golem with a capacity upgrade, items in the upgrade-only slots, a farming job, a workplace and an owner must
     * come back unchanged after saving and loading. Loading must not evict the items from the upgrade slots.
     */
    public static void saveAndLoadKeepsEverything(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(CHEST_POS, Blocks.CHEST);
        GolemEntity golem = helper.spawn(EntityTypeRegistry.GOLEM.get(), GOLEM_POS);
        golem.setSpec(GolemSpec.of(GolemMaterialRegistry.WOOD).withJob(PrincipiaMagicaRegistries.GOLEM_JOB.get(FarmingJob.KEY).orElseThrow()));
        golem.addUpgrade(GolemUpgradeRegistry.CAPACITY);
        golem.setOwnerUUID(UUID.randomUUID());
        golem.setAnchor(GlobalPos.of(level.dimension(), helper.absolutePos(GOLEM_POS)));
        golem.setMode(GolemMode.FOLLOW);

        WorkZone zone = WorkZone.of(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(4, 3, 4)));
        ContainerLink link = new ContainerLink(helper.absolutePos(CHEST_POS), Direction.UP, ContainerRole.DEPOSIT);
        golem.updateWorkplace(workplace -> workplace
                .withZones(List.of(zone))
                .withLinks(List.of(link))
                .withOptions(JobOptionValues.EMPTY.with(FarmingTargets.TILL, true))
                .markInitialized());

        for (int slot = 3; slot < 6; slot++) {
            golem.getInventory().setItem(slot, new ItemStack(Items.WHEAT, slot));
        }

        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        golem.saveWithoutId(output);

        GolemEntity loaded = EntityTypeRegistry.GOLEM.get().create(level, EntitySpawnReason.LOAD);
        helper.assertTrue(loaded != null, "Golem entity should be creatable for loading");
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), output.buildResult()));

        helper.assertTrue(loaded.getSpec().equals(golem.getSpec()), "Spec should survive save and load, got " + loaded.getSpec());
        helper.assertTrue(loaded.getMode() == GolemMode.FOLLOW, "Mode should survive save and load, got " + loaded.getMode());
        helper.assertTrue(loaded.getInventory().getUsableSlots() == 6, "Capacity should apply after load, got " + loaded.getInventory().getUsableSlots());
        for (int slot = 3; slot < 6; slot++) {
            ItemStack stack = loaded.getInventory().getItem(slot);
            helper.assertTrue(stack.is(Items.WHEAT) && stack.getCount() == slot, "Slot " + slot + " should keep its items after load, got " + stack);
        }
        helper.assertTrue(loaded.getOwnerUUID() != null && loaded.getOwnerUUID().equals(golem.getOwnerUUID()), "Owner should survive save and load");
        helper.assertTrue(loaded.getAnchor().equals(golem.getAnchor()), "Anchor should survive save and load, got " + loaded.getAnchor());

        Workplace original = golem.getWorkplace(level).orElseThrow();
        helper.assertTrue(loaded.getWorkplace(level).equals(Optional.of(original)), "Workplace should survive save and load");

        helper.succeed();
    }
}
