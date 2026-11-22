// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.spout;

import com.klikli_dev.principiamagica.content.libation.LibationClientEffects;
import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * A spout that sits in the cell in front of a host and drains the host through its open channel.
 * <p>
 * The spout faces away from its host, so its host is at {@code pos.relative(facing.getOpposite())}. It drains into the
 * block below itself. A spout is opened and closed by hand or by redstone. It drops as an item when its host is removed.
 */
public abstract class SpoutBlock extends BaseEntityBlock {

    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    protected SpoutBlock(BlockBehaviour.Properties properties) {
        super(properties);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            this.shapes.put(facing, rotatedShape(this.northBounds(), facing));
        }
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.OPEN, false));
    }

    /**
     * @return the box {minX, minY, minZ, maxX, maxY, maxZ} in pixels that covers the spout, for a spout facing north. It must lie
     * within its own cell and cover both the open and the closed pose. Must not depend on instance state.
     */
    protected abstract double[] northBounds();

    /**
     * Rotates the box of a north facing spout to the facing, the same way the blockstate rotates the model: clockwise seen from above.
     */
    private static VoxelShape rotatedShape(double[] bounds, Direction facing) {
        double minX = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        for (double x : new double[]{bounds[0], bounds[3]}) {
            for (double z : new double[]{bounds[2], bounds[5]}) {
                double dx = x - 8.0;
                double dz = z - 8.0;
                double rx = switch (facing) {
                    case EAST -> -dz;
                    case SOUTH -> -dx;
                    case WEST -> dz;
                    default -> dx;
                };
                double rz = switch (facing) {
                    case EAST -> dx;
                    case SOUTH -> -dz;
                    case WEST -> -dx;
                    default -> dz;
                };
                minX = Math.min(minX, 8.0 + rx);
                maxX = Math.max(maxX, 8.0 + rx);
                minZ = Math.min(minZ, 8.0 + rz);
                maxZ = Math.max(maxZ, 8.0 + rz);
            }
        }
        return Block.box(minX, bounds[1], minZ, maxX, bounds[4], maxZ);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    /**
     * @return the block entity type of this spout, used for the ticker.
     */
    protected abstract BlockEntityType<? extends SpoutBlockEntity> blockEntityType();

    /**
     * @return true if the block entity can host this spout.
     */
    protected abstract boolean acceptsHost(@Nullable BlockEntity host);

    /**
     * Plays the sound of the spout opening or closing.
     */
    protected abstract void playToggleSound(Level level, BlockPos pos, boolean open);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.OPEN);
    }

    /**
     * The spout must be placed on a horizontal side of a valid host, and the host may hold only one spout.
     */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (!facing.getAxis().isHorizontal()) {
            return null;
        }
        BlockPos host = context.getClickedPos().relative(facing.getOpposite());
        if (!this.acceptsHost(context.getLevel().getBlockEntity(host)) || LibationUtils.spoutSide(context.getLevel(), host) != null) {
            return null;
        }
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return this.acceptsHost(level.getBlockEntity(pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite())));
    }

    /**
     * Drops the spout as an item once its host is gone. Redstone opens and closes it.
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(BlockStateProperties.OPEN)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, powered), Block.UPDATE_ALL);
            this.playToggleSound(level, pos, powered);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.cycle(BlockStateProperties.OPEN), Block.UPDATE_ALL);
            this.playToggleSound(level, pos, !state.getValue(BlockStateProperties.OPEN));
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Spouts only tick on the client, to drip. Their host drains through them on the server.
     */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return LibationUtils.ticker(level, type, this.blockEntityType(), null, LibationClientEffects.get()::tickSpout);
    }
}
