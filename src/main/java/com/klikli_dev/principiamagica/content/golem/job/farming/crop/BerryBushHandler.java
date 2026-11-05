// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.crop;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/**
 * Picks berries from a sweet berry bush. The bush is reset to age 1 instead of being removed, so nothing needs replanting.
 */
public class BerryBushHandler implements CropHandler {
    private static final int HARVEST_AGE = 2;
    private static final int RESET_AGE = 1;

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean canHandle(BlockState state) {
        return state.getBlock() instanceof SweetBerryBushBlock;
    }

    @Override
    public boolean isHarvestable(ServerLevel level, BlockPos pos, BlockState state) {
        return state.getValue(BlockStateProperties.AGE_3) >= HARVEST_AGE;
    }

    @Override
    public HarvestResult harvest(ServerLevel level, BlockPos pos, BlockState state, GolemEntity golem) {
        int age = state.getValue(BlockStateProperties.AGE_3);
        int count = 1 + level.getRandom().nextInt(2) + (age == 3 ? 1 : 0);
        level.setBlock(pos, state.setValue(BlockStateProperties.AGE_3, RESET_AGE), 2);
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);
        return new HarvestResult(List.of(new ItemStack(Items.SWEET_BERRIES, count)), false);
    }
}
