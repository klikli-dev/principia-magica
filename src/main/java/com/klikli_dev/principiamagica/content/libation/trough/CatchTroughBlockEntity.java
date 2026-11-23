// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.trough;

import com.klikli_dev.principiamagica.content.libation.AmountSmoother;
import com.klikli_dev.principiamagica.content.libation.LibationBlockEntity;
import com.klikli_dev.principiamagica.content.libation.LibationClientEffects;
import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.content.libation.PrincipleStorage;
import com.klikli_dev.principiamagica.content.libation.ProgressTracker;
import com.klikli_dev.principiamagica.content.libation.SyncThrottle;
import com.klikli_dev.principiamagica.content.principle.PrincipleContainer;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe.SteepingRecipeInput;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

/**
 * Receives principles from the drain target and steeps the single item it holds once the principles are sufficient.
 * <p>
 * Steeping takes the time of the recipe. The progress advances by one per tick while the item and the principles match a
 * recipe, and resets otherwise. Clients interpolate the progress between syncs, see {@link #getDisplayProgress}.
 * Completed steeping is notified to clients via {@link #EVENT_STEEP} through {@code level.blockEvent}, see {@link #triggerEvent}.
 */
public class CatchTroughBlockEntity extends LibationBlockEntity implements PrincipleContainer {
    public static final int CAPACITY = 500;
    public static final int EVENT_STEEP = 1;

    private final SyncThrottle sync = new SyncThrottle(this);
    private final PrincipleStorage principles = new PrincipleStorage(CAPACITY, this.sync);
    /**
     * The steeping of the held item. Its progress is reset whenever the item or the principles no longer match a recipe.
     */
    private final ProgressTracker steeping = new ProgressTracker(this.sync);
    /**
     * Client-only: the principles shown, eased towards the synced principles, see {@link #getDisplayedPrinciples}.
     */
    private final AmountSmoother displayedPrinciples = new AmountSmoother();
    private ItemStack item = ItemStack.EMPTY;

    public CatchTroughBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.CATCH_TROUGH.get(), pos, state);
    }

    public ItemStack getItem() {
        return this.item;
    }

    /**
     * Client-only. Eases the amount of principles shown towards the synced amount, so that throttled syncs look smooth.
     *
     * @return the amount in mB to draw, see {@link AmountSmoother}.
     */
    public float getDisplayedPrinciples(long gameTime, float partialTick) {
        return this.displayedPrinciples.update(this.principles.contents().total(), gameTime, partialTick);
    }

    /**
     * @return the progress of the steeping in ticks, see {@link SteepingRecipe#time()}, interpolated from the synced progress
     * and rate so that it is smooth on the client.
     */
    public float getDisplayProgress(long gameTime, float partialTick) {
        return this.steeping.display(gameTime, partialTick);
    }

    @Override
    public PrincipleContents getPrinciples() {
        return this.principles.contents();
    }

    @Override
    public int comparatorSignal() {
        return this.principles.comparatorSignal();
    }

    @Override
    public PrincipleContents insert(PrincipleContents offered, boolean simulate) {
        return this.principles.insert(offered, simulate);
    }

    @Override
    public InteractionResult useItemOn(ItemStack held, Player player, InteractionHand hand) {
        if (held.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (this.item.isEmpty() && LibationUtils.isSteepable(this.level, held)) {
            this.item = held.copyWithCount(1);
            this.steeping.restart();
            held.shrink(1);
            this.sync.syncNow();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useWithoutItem(Player player) {
        if (!player.getMainHandItem().isEmpty() || this.item.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        player.getInventory().placeItemBackInInventory(this.item);
        this.item = ItemStack.EMPTY;
        this.steeping.restart();
        this.sync.syncNow();
        return InteractionResult.SUCCESS;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CatchTroughBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        blockEntity.tickSteeping();
        blockEntity.sync.tick();
    }

    /**
     * Advances the steeping of the held item by one tick. Once the progress reaches the recipe time, the held item is replaced
     * by the result and exactly the recipe principles are consumed.
     */
    private void tickSteeping() {
        Optional<RecipeHolder<SteepingRecipe>> recipe = this.item.getCount() == 1
                ? LibationUtils.steeping(this.level, this.item, this.principles.contents())
                : Optional.empty();
        if (recipe.isEmpty()) {
            this.steeping.stop();
            return;
        }

        SteepingRecipe steep = recipe.get().value();
        this.steeping.advance(1.0F, steep.time(), () -> {
            this.item = steep.assemble(new SteepingRecipeInput(this.item, this.principles.contents()));
            this.principles.set(this.principles.contents().subtract(steep.principles()));
            this.level.playSound(null, this.worldPosition, SoundRegistry.CATCH_TROUGH_STEEP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), EVENT_STEEP, steep.principles().blendedColor());
        });
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_STEEP && this.level != null && this.level.isClientSide()) {
            LibationClientEffects.get().onTroughSteep(this.level, this.worldPosition, param);
        }
        return id == EVENT_STEEP;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.dropItem(this.item);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        this.principles.set(components.getOrDefault(DataComponentRegistry.STORED_PRINCIPLES.get(), PrincipleContents.EMPTY));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        if (!this.principles.isEmpty()) {
            components.set(DataComponentRegistry.STORED_PRINCIPLES.get(), this.principles.contents());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.item = input.read("item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        input.read("principles", PrincipleContents.CODEC).ifPresent(this.principles::set);
        this.steeping.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("item", ItemStack.OPTIONAL_CODEC, this.item);
        output.store("principles", PrincipleContents.CODEC, this.principles.contents());
        this.steeping.save(output, this.level != null ? this.level.getGameTime() : 0L);
    }

}
