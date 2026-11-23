// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import com.klikli_dev.principiamagica.content.libation.spout.SpoutBlock;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringEnvironment;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringRecipeInput;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.LibationRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe.SteepingRecipeInput;
import com.klikli_dev.principiamagica.registry.RecipeTypeRegistry;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class LibationUtils {

    private LibationUtils() {
    }

    /**
     * @return the side of the host that holds a spout, or null if it has none.
     */
    public static @Nullable Direction spoutSide(BlockGetter level, BlockPos host) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(host.relative(side));
            if (state.getBlock() instanceof SpoutBlock && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == side) {
                return side;
            }
        }
        return null;
    }

    /**
     * @return the position of the spout of the host if it has one and the spout is open, or null.
     */
    public static @Nullable BlockPos openSpout(BlockGetter level, BlockPos host) {
        Direction side = spoutSide(level, host);
        if (side == null) {
            return null;
        }
        BlockPos spout = host.relative(side);
        return level.getBlockState(spout).getValue(BlockStateProperties.OPEN) ? spout : null;
    }

    /**
     * @return the fluid handler below the spout, which receives its drain, or null.
     */
    public static @Nullable ResourceHandler<FluidResource> drainTarget(Level level, BlockPos spout) {
        return level.getCapability(Capabilities.Fluid.BLOCK, spout.below(), Direction.UP);
    }

    public static Optional<RecipeHolder<LibationRecipe>> libation(Level level, ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        return serverLevel.getServer().getRecipeManager().getRecipeFor(RecipeTypeRegistry.LIBATION.get(), new SingleRecipeInput(stack), serverLevel);
    }

    public static Optional<RecipeHolder<SteepingRecipe>> steeping(Level level, ItemStack stack, PrincipleContents principles) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        return serverLevel.getServer().getRecipeManager().getRecipeFor(RecipeTypeRegistry.STEEPING.get(), new SteepingRecipeInput(stack, principles), serverLevel);
    }

    /**
     * @return true if the stack is the ingredient of any steeping recipe.
     */
    public static boolean isSteepable(Level level, ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return serverLevel.getServer().getRecipeManager().getRecipes().stream()
                .anyMatch(holder -> holder.value() instanceof SteepingRecipe recipe && recipe.ingredient().test(stack));
    }

    public static Optional<RecipeHolder<CuringRecipe>> curing(Level level, PrincipleContents principles, CuringEnvironment environment) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        return serverLevel.getServer().getRecipeManager().getRecipeFor(RecipeTypeRegistry.CURING.get(), new CuringRecipeInput(principles, environment), serverLevel);
    }

    public static Optional<RecipeHolder<DryingRecipe>> drying(Level level, ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        return serverLevel.getServer().getRecipeManager().getRecipeFor(RecipeTypeRegistry.DRYING.get(), new SingleRecipeInput(stack), serverLevel);
    }

    public static void message(Player player, String translationKey) {
        player.sendOverlayMessage(Component.translatable(translationKey));
    }

    /**
     * @return the comparator signal of a container holding amount of its capacity, 0 when empty and 1 to
     * {@link LibationFlow#MAX_SIGNAL} otherwise.
     */
    public static int comparatorSignal(int amount, int capacity) {
        return amount == 0 ? 0 : 1 + amount * (LibationFlow.MAX_SIGNAL - 1) / capacity;
    }

    /**
     * Progress that advances at a constant rate per tick is synced when the rate changes and at the sync interval. Clients use
     * this to show the progress between syncs, see {@link ProgressTracker#display}.
     *
     * @param progress    the progress at the time of the sync.
     * @param rate        the progress per tick since the sync.
     * @param syncTick    the game time of the sync.
     * @param gameTime    the current game time.
     * @param partialTick the fraction of the current tick that has passed.
     * @return the progress to display, not clamped.
     */
    static float displayProgress(float progress, float rate, long syncTick, long gameTime, float partialTick) {
        return progress + rate * Math.max(0.0F, (gameTime - syncTick) + partialTick);
    }

    /**
     * @return the comparator signal of the libation block entity at the position, 0 if there is none.
     */
    public static int comparatorSignal(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof LibationBlockEntity blockEntity ? blockEntity.comparatorSignal() : 0;
    }

    /**
     * Forwards a use with an item in hand to the libation block entity at the position.
     */
    public static InteractionResult useItemOn(Level level, BlockPos pos, ItemStack held, Player player, InteractionHand hand) {
        return level.getBlockEntity(pos) instanceof LibationBlockEntity blockEntity ? blockEntity.useItemOn(held, player, hand) : InteractionResult.PASS;
    }

    /**
     * Forwards a use with an empty hand to the libation block entity at the position.
     */
    public static InteractionResult useWithoutItem(Level level, BlockPos pos, Player player) {
        return level.getBlockEntity(pos) instanceof LibationBlockEntity blockEntity ? blockEntity.useWithoutItem(player) : InteractionResult.PASS;
    }

    /**
     * Builds the ticker of a libation block: the server ticker on the server and the client ticker on the client. Either may be
     * null, for blocks that only tick on one side. The ticker is only built for the block entity type of the block.
     *
     * @param level      the level the block is in.
     * @param type       the type the block asks a ticker for.
     * @param expected   the type of the block entity that the tickers are for.
     * @param serverTick ticks the block entity on the server, or null.
     * @param clientTick ticks the block entity on the client, or null.
     * @return the ticker, or null if the type is not the expected one or the side has no ticker.
     */
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity, E extends BlockEntity> @Nullable BlockEntityTicker<T> ticker(Level level, BlockEntityType<T> type,
                                                                                                    BlockEntityType<E> expected,
                                                                                                    @Nullable BlockEntityTicker<E> serverTick,
                                                                                                    @Nullable BlockEntityTicker<E> clientTick) {
        BlockEntityTicker<E> ticker = level.isClientSide() ? clientTick : serverTick;
        return type == expected ? (BlockEntityTicker<T>) ticker : null;
    }

    /**
     * Saves the block entity, sends its update tag to clients tracking it and updates comparators next to it.
     */
    public static void sync(BlockEntity blockEntity) {
        blockEntity.setChanged();
        Level level = blockEntity.getLevel();
        if (level != null) {
            level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_ALL);
            level.updateNeighbourForOutputSignal(blockEntity.getBlockPos(), blockEntity.getBlockState().getBlock());
        }
    }
}
