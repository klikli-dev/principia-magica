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
import com.klikli_dev.principiamagica.content.libation.RecipeCache;
import com.klikli_dev.principiamagica.content.libation.SyncThrottle;
import com.klikli_dev.principiamagica.content.principle.PrincipleContainer;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe.SteepingRecipeInput;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.FluidRegistry;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Receives principles from the drain target and steeps the single item it holds once the principles are sufficient.
 * <p>
 * The item lives in an {@link ItemStacksResourceHandler} that only accepts steepable items. Players, hoppers and pipes all
 * go through the same handler, so they follow the same rules. Taking the item out in any way resets the steeping.
 * <p>
 * Steeping takes the time of the recipe. The progress advances by one per tick while the item and the principles match a
 * recipe, and resets otherwise. The matched recipe is cached and only looked up again when the item or the principles change.
 * Clients interpolate the progress between syncs, see {@link #getDisplayProgress}.
 * Completed steeping is notified to clients via {@link #EVENT_STEEP} through {@code level.blockEvent}, see {@link #triggerEvent}.
 */
public class CatchTroughBlockEntity extends LibationBlockEntity implements PrincipleContainer {
    public static final int CAPACITY = 500;
    public static final int EVENT_STEEP = 1;

    private final SyncThrottle sync = new SyncThrottle(this);
    private final PrincipleStorage principles = new PrincipleStorage(CAPACITY, this.sync, this::onPrinciplesChanged);
    private final ItemStacksResourceHandler items = new ItemHandler();
    /**
     * The steeping of the held item. Its progress is reset whenever the item changes.
     */
    private final ProgressTracker steeping = new ProgressTracker(this.sync);
    /**
     * The steeping recipe of the held item and the principles.
     */
    private final RecipeCache<SteepingRecipe> steepingRecipe = new RecipeCache<>();
    /**
     * Client-only: the principles shown, eased towards the synced principles, see {@link #getDisplayedPrinciples}.
     */
    private final AmountSmoother displayedPrinciples = new AmountSmoother();

    public CatchTroughBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.CATCH_TROUGH.get(), pos, state);
    }

    /**
     * @return the item held by the trough, or empty.
     */
    public ItemStack getItem() {
        ItemResource resource = this.items.getResource(0);
        return resource.isEmpty() ? ItemStack.EMPTY : resource.toStack(this.items.getAmountAsInt(0));
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
    public ResourceHandler<FluidResource> principleHandler() {
        return this.principles;
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, BlockEntityTypeRegistry.CATCH_TROUGH.get(), (trough, side) -> trough.principles);
        event.registerBlockEntity(Capabilities.Item.BLOCK, BlockEntityTypeRegistry.CATCH_TROUGH.get(), (trough, side) -> trough.items);
    }

    @Override
    public int comparatorSignal() {
        return this.principles.comparatorSignal();
    }

    @Override
    public PrincipleContents insert(PrincipleContents offered, boolean simulate) {
        return this.principles.insert(offered, simulate);
    }

    /**
     * Moves one item from the hand into the item handler. The handler decides whether the item is steepable, the same way it
     * does for pipes.
     */
    @Override
    public InteractionResult useItemOn(ItemStack held, Player player, InteractionHand hand) {
        if (held.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ResourceHandler<ItemResource> handHandler = PlayerInventoryWrapper.of(player).getHandSlot(hand);
        int moved;
        try (Transaction transaction = Transaction.open(null)) {
            moved = ResourceHandlerUtil.move(handHandler, this.items, resource -> true, 1, transaction);
            transaction.commit();
        }
        return moved > 0 ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * Takes the held item back into the player's inventory, through the item handler like pipes do.
     */
    @Override
    public InteractionResult useWithoutItem(Player player) {
        if (!player.getMainHandItem().isEmpty() || this.items.getResource(0).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        try (Transaction transaction = Transaction.open(null)) {
            ResourceStack<ItemResource> taken = ResourceHandlerUtil.extractFirst(this.items, resource -> true, Integer.MAX_VALUE, transaction);
            if (taken == null) {
                return InteractionResult.PASS;
            }
            PlayerInventoryWrapper.of(player).placeItemBackInInventory(taken.resource(), taken.amount(), transaction);
            transaction.commit();
        }
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
        RecipeHolder<SteepingRecipe> recipe = this.steepingRecipe();
        if (recipe == null) {
            this.steeping.stop();
            return;
        }

        SteepingRecipe steep = recipe.value();
        this.steeping.advance(1.0F, steep.time(), () -> {
            ItemStack result = steep.assemble(new SteepingRecipeInput(this.getItem(), this.principles.contents()));
            try (Transaction tx = Transaction.open(null)) {
                for (PrincipleStack stack : steep.principles().stacks()) {
                    this.principles.extract(FluidRegistry.resourceFor(stack.principle()), stack.amount(), tx);
                }
                tx.commit();
            }
            // The result is usually not steepable, so it cannot go through insert, which would refuse it.
            this.items.set(0, ItemResource.of(result), result.getCount());
            this.level.playSound(null, this.worldPosition, SoundRegistry.CATCH_TROUGH_STEEP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), EVENT_STEEP, steep.principles().blendedColor());
        });
    }

    /**
     * @return the steeping recipe of the held item and the principles, or null. Looked up only when the item or the principles
     * changed, or the recipes reloaded, see {@link RecipeCache}.
     */
    private @Nullable RecipeHolder<SteepingRecipe> steepingRecipe() {
        ItemStack item = this.getItem();
        return this.steepingRecipe.get(this.level, () -> item.isEmpty()
                ? Optional.empty()
                : LibationUtils.steeping(this.level, item, this.principles.contents()));
    }

    private void onPrinciplesChanged() {
        this.steepingRecipe.invalidate();
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
        this.dropItem(this.getItem());
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
        this.items.deserialize(input.childOrEmpty("items"));
        input.read("principles", PrincipleContents.CODEC).ifPresent(this.principles::set);
        this.steeping.load(input);
        this.steepingRecipe.invalidate();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.items.serialize(output.child("items"));
        output.store("principles", PrincipleContents.CODEC, this.principles.contents());
        this.steeping.save(output, this.level != null ? this.level.getGameTime() : 0L);
    }

    /**
     * Holds one steepable item. Its contents change at once, as the item is visible and its progress restarts with it.
     */
    private final class ItemHandler extends ItemStacksResourceHandler {
        private ItemHandler() {
            super(1);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return CatchTroughBlockEntity.this.level != null
                    && LibationUtils.isSteepable(CatchTroughBlockEntity.this.level, resource.toStack());
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            CatchTroughBlockEntity.this.steepingRecipe.invalidate();
            CatchTroughBlockEntity.this.steeping.restart();
            CatchTroughBlockEntity.this.sync.syncNow();
        }
    }
}
