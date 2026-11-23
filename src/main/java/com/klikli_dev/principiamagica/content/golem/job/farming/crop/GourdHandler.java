// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.crop;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemDrops;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Harvests ripe gourds (tag {@code golem_harvestable_gourds}). The stem is never touched, it stays and grows a new fruit.
 */
public class GourdHandler implements CropHandler {
    public static final TagKey<Block> HARVESTABLE = TagKey.create(Registries.BLOCK, PrincipiaMagica.loc("golem_harvestable_gourds"));

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean canHandle(BlockState state) {
        return state.is(HARVESTABLE);
    }

    @Override
    public boolean isHarvestable(ServerLevel level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public HarvestResult harvest(ServerLevel level, BlockPos pos, BlockState state, GolemEntity golem) {
        List<ItemStack> drops = GolemDrops.breakBlock(level, golem, pos, state);
        return new HarvestResult(drops, true);
    }
}
