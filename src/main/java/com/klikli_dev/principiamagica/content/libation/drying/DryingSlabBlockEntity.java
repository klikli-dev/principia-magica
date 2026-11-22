// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.drying;

import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
public class DryingSlabBlockEntity extends BlockEntity {
    public static final int FULL_LIGHT = 15;
    public static final int SYNC_INTERVAL = 20;

    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private float progress;
    /**
     * The progress per tick, the light divided by {@link #FULL_LIGHT}. Zero while drying is paused or stalled.
     */
    private float rate;
    /**
     * The game time at which the progress and rate were saved, set on the client when the data is loaded.
     */
    private long syncTick;
    private int syncCooldown;

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
        return LibationUtils.displayProgress(this.progress, this.rate, this.syncTick, gameTime, partialTick);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DryingSlabBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        blockEntity.tickDrying(level, pos);
    }

    public InteractionResult useItemOn(ItemStack held) {
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
            this.progress = 0;
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
            this.progress = 0;
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
            if (this.progress != 0 || this.rate != 0) {
                this.progress = 0;
                this.rate = 0;
                LibationUtils.sync(this);
            }
            return;
        }

        ItemStack result = recipe.get().value().assemble(new SingleRecipeInput(this.input.copyWithCount(1)));
        int light = level.getMaxLocalRawBrightness(pos.above());
        if (light <= 0 || !this.canMergeOutput(result)) {
            if (this.rate != 0) {
                this.rate = 0;
                LibationUtils.sync(this);
            }
            return;
        }

        float rate = light / (float) FULL_LIGHT;
        this.progress += rate;
        if (this.progress >= recipe.get().value().time()) {
            this.input.shrink(1);
            this.output = this.output.isEmpty() ? result.copy() : this.output.copyWithCount(this.output.getCount() + result.getCount());
            this.progress = 0;
            this.rate = rate;
            level.playSound(null, pos, SoundRegistry.DRYING_SLAB_DRIED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            LibationUtils.sync(this);
            return;
        }

        boolean rateChanged = rate != this.rate;
        this.rate = rate;
        if (rateChanged || ++this.syncCooldown >= SYNC_INTERVAL) {
            this.syncCooldown = 0;
            LibationUtils.sync(this);
        }
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
        if (this.level != null) {
            Containers.dropContents(this.level, pos, NonNullList.of(ItemStack.EMPTY, this.input, this.output));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.input = input.read("input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.output = input.read("output", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.progress = input.getFloatOr("progress", 0.0F);
        this.rate = input.getFloatOr("rate", 0.0F);
        this.syncTick = input.getLongOr("sync_tick", 0L);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("input", ItemStack.OPTIONAL_CODEC, this.input);
        output.store("output", ItemStack.OPTIONAL_CODEC, this.output);
        output.putFloat("progress", this.progress);
        output.putFloat("rate", this.rate);
        output.putLong("sync_tick", this.level != null ? this.level.getGameTime() : 0L);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
