// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.slab;

import com.klikli_dev.principiamagica.content.libation.LibationClientEffects;
import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The libation slab. Its rim has a hole on the side that holds a libation spout.
 * The side is kept in the {@link #SPOUT} property, so the model follows the attached spout.
 */
public class LibationSlabBlock extends BaseEntityBlock {
    public static final MapCodec<LibationSlabBlock> CODEC = simpleCodec(LibationSlabBlock::new);

    public static final EnumProperty<SpoutSide> SPOUT = EnumProperty.create("spout", SpoutSide.class);

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 12, 15);

    public LibationSlabBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SPOUT, SpoutSide.NONE));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SPOUT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(SPOUT, SpoutSide.of(LibationUtils.spoutSide(context.getLevel(), context.getClickedPos())));
    }

    /**
     * A spout placed or removed next to the slab changes the hole in the rim.
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }
        SpoutSide side = SpoutSide.of(LibationUtils.spoutSide(level, pos));
        if (side != state.getValue(SPOUT)) {
            level.setBlock(pos, state.setValue(SPOUT, side), Block.UPDATE_ALL);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LibationSlabBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return LibationUtils.ticker(level, type, BlockEntityTypeRegistry.LIBATION_SLAB.get(), LibationSlabBlockEntity::tick, LibationClientEffects.get()::tickSlab);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return LibationUtils.comparatorSignal(level, pos);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ItemRegistry.LIBATION_SPOUT.get())
                || stack.is(ItemRegistry.CLAY_PHIAL.get())
                || stack.is(ItemRegistry.NATRON_SPONGE.get())) {
            // Let the spout be placed and phial/sponge handle their own principle interactions instead of treating them as washable items.
            return InteractionResult.PASS;
        }
        return LibationUtils.useItemOn(level, pos, stack, player, hand);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return LibationUtils.useWithoutItem(level, pos, player);
    }
}
