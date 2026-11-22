// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.drying;

import com.klikli_dev.principiamagica.content.libation.LibationBlockEntity;
import com.klikli_dev.principiamagica.content.libation.LibationFlow;
import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.content.libation.ProgressTracker;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

/**
 * Holds one stack of items to dry and one stack of the dried results. Only the first input item dries at a time, the
 * result is merged into the output stack. Drying pauses while the output cannot accept the result.
 * <p>
 * The progress advances by a rate that depends on the light, so it is only synced when the rate changes. Clients
 * interpolate the progress between syncs, see {@link #getDisplayProgress}.
 * <p>
 * TODO: Hopper automation is not supported.
 */
public class DryingSlabBlockEntity extends LibationBlockEntity {

    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    /**
     * The drying of the first input item. Its rate is the light divided by {@link LibationFlow#FULL_LIGHT}.
     */
    private final ProgressTracker drying = new ProgressTracker(() -> LibationUtils.sync(this));

    public DryingSlabBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.DRYING_SLAB.get(), pos, state);
    }

    /**
     * @return the stack of items that is drying, may be empty.
     */
    public ItemStack getInput() {
        return this.input;
    }

    /**
     * @return the stack of dried items, may be empty.
     */
    public ItemStack getOutput() {
        return this.output;
    }

    /**
     * @return the progress of the first input item in ticks at full light, see {@link DryingRecipe#time()}, interpolated
     * from the synced progress and rate so that it is smooth on the client.
     */
    public float getDisplayProgress(long gameTime, float partialTick) {
        return this.drying.display(gameTime, partialTick);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DryingSlabBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        blockEntity.tickDrying(level, pos);
    }

    @Override
    public InteractionResult useItemOn(ItemStack held, Player player, InteractionHand hand) {
        if (held.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (LibationUtils.drying(this.level, held).isEmpty()) {
            return InteractionResult.PASS;
        }

        if (this.input.isEmpty()) {
            int count = Math.min(held.getCount(), held.getMaxStackSize());
            this.input = held.copyWithCount(count);
            this.drying.restart();
            held.shrink(count);
        } else if (ItemStack.isSameItemSameComponents(this.input, held) && this.input.getCount() < this.input.getMaxStackSize()) {
            int count = Math.min(held.getCount(), this.input.getMaxStackSize() - this.input.getCount());
            this.input.grow(count);
            held.shrink(count);
        } else {
            return InteractionResult.PASS;
        }

        LibationUtils.sync(this);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useWithoutItem(Player player) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack;
        if (!this.output.isEmpty()) {
            stack = this.output;
            this.output = ItemStack.EMPTY;
        } else if (!this.input.isEmpty()) {
            stack = this.input;
            this.input = ItemStack.EMPTY;
            this.drying.restart();
        } else {
            return InteractionResult.PASS;
        }

        player.getInventory().placeItemBackInInventory(stack);
        LibationUtils.sync(this);
        return InteractionResult.SUCCESS;
    }

    private void tickDrying(Level level, BlockPos pos) {
        Optional<RecipeHolder<DryingRecipe>> recipe = this.input.isEmpty() ? Optional.empty() : LibationUtils.drying(level, this.input);
        if (recipe.isEmpty()) {
            this.drying.stop();
            return;
        }

        DryingRecipe dry = recipe.get().value();
        ItemStack result = dry.assemble(new SingleRecipeInput(this.input.copyWithCount(1)));
        int light = level.getMaxLocalRawBrightness(pos.above());
        if (light <= 0 || !this.canMergeOutput(result)) {
            this.drying.pause();
            return;
        }

        float rate = light / (float) LibationFlow.FULL_LIGHT;
        this.drying.advance(rate, dry.time(), () -> {
            this.input.shrink(1);
            this.output = this.output.isEmpty() ? result.copy() : this.output.copyWithCount(this.output.getCount() + result.getCount());
            level.playSound(null, pos, SoundRegistry.DRYING_SLAB_DRIED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        });
    }

    /**
     * @return true if the result of drying the input item fits into the output stack. Drying stalls while it does not.
     */
    public boolean canMergeOutput(ItemStack result) {
        if (this.output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(this.output, result)
                && this.output.getCount() + result.getCount() <= this.output.getMaxStackSize();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.dropItems(this.input, this.output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.input = input.read("input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.output = input.read("output", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.drying.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("input", ItemStack.OPTIONAL_CODEC, this.input);
        output.store("output", ItemStack.OPTIONAL_CODEC, this.output);
        this.drying.save(output, this.level != null ? this.level.getGameTime() : 0L);
    }

}
