// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.drying;

import com.klikli_dev.principiamagica.content.libation.LibationBlockEntity;
import com.klikli_dev.principiamagica.content.libation.LibationFlow;
import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.content.libation.ProgressTracker;
import com.klikli_dev.principiamagica.content.libation.SyncThrottle;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import static com.klikli_dev.principiamagica.content.libation.drying.DryingSlabItemHandler.INPUT;
import static com.klikli_dev.principiamagica.content.libation.drying.DryingSlabItemHandler.OUTPUT;

/**
 * Holds one stack of items to dry and one stack of the dried results. Only the first input item dries at a time, the
 * result is merged into the output stack. Drying pauses while the output cannot accept the result.
 * <p>
 * The stacks live in an item handler. Hoppers and pipes insert into it through {@link DryingSlabItemHandler}, which only
 * accepts drying inputs into an empty input and only extracts the finished items.
 * <p>
 * The progress advances by a rate that depends on the light, so it is only synced when the rate changes. Clients
 * interpolate the progress between syncs, see {@link #getDisplayProgress}.
 */
public class DryingSlabBlockEntity extends LibationBlockEntity {

    private final SyncThrottle sync = new SyncThrottle(this);
    private final ItemStacksResourceHandler items = new ItemHandler();
    private final ResourceHandler<ItemResource> itemCapability = new DryingSlabItemHandler(this.items);
    /**
     * The drying of the first input item. Its rate is the light divided by {@link LibationFlow#FULL_LIGHT}.
     */
    private final ProgressTracker drying = new ProgressTracker(this.sync);
    /**
     * The drying recipe of the input item, valid while {@link #dryingRecipeStale} is false.
     */
    private @Nullable RecipeHolder<DryingRecipe> dryingRecipe;
    private boolean dryingRecipeStale = true;

    public DryingSlabBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.DRYING_SLAB.get(), pos, state);
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, BlockEntityTypeRegistry.DRYING_SLAB.get(), (slab, side) -> slab.itemCapability);
    }

    /**
     * @return the stack of items that is drying, may be empty.
     */
    public ItemStack getInput() {
        return this.stackOf(INPUT);
    }

    /**
     * @return the stack of dried items, may be empty.
     */
    public ItemStack getOutput() {
        return this.stackOf(OUTPUT);
    }

    private ItemStack stackOf(int slot) {
        ItemResource resource = this.items.getResource(slot);
        return resource.isEmpty() ? ItemStack.EMPTY : resource.toStack(this.items.getAmountAsInt(slot));
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
        blockEntity.sync.tick();
    }

    @Override
    public InteractionResult useItemOn(ItemStack held, Player player, InteractionHand hand) {
        if (held.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // The same handler that pipes and hoppers insert into decides whether the item dries. Its restart of the drying
        // happens when the input changes, see ItemHandler.
        int moved;
        try (Transaction transaction = Transaction.open(null)) {
            ResourceHandler<ItemResource> handHandler = PlayerInventoryWrapper.of(player).getHandSlot(hand);
            moved = ResourceHandlerUtil.move(handHandler, this.itemCapability, resource -> true, held.getCount(), transaction);
            transaction.commit();
        }
        return moved > 0 ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * Takes the dried items first, then the input. Taking the input restarts its drying, see ItemHandler.
     */
    @Override
    public InteractionResult useWithoutItem(Player player) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        int slot = this.items.getResource(OUTPUT).isEmpty() ? INPUT : OUTPUT;
        if (this.items.getResource(slot).isEmpty()) {
            return InteractionResult.PASS;
        }
        try (Transaction transaction = Transaction.open(null)) {
            ItemResource resource = this.items.getResource(slot);
            int extracted = this.items.extract(slot, resource, this.items.getAmountAsInt(slot), transaction);
            PlayerInventoryWrapper.of(player).placeItemBackInInventory(resource, extracted, transaction);
            transaction.commit();
        }
        return InteractionResult.SUCCESS;
    }

    private void tickDrying(Level level, BlockPos pos) {
        RecipeHolder<DryingRecipe> recipe = this.dryingRecipe(level);
        if (recipe == null) {
            this.drying.stop();
            return;
        }

        ItemStack input = this.getInput();
        DryingRecipe dry = recipe.value();
        ItemStack result = dry.assemble(new SingleRecipeInput(input.copyWithCount(1)));
        int light = level.getMaxLocalRawBrightness(pos.above());
        if (light <= 0 || !this.canMergeOutput(result)) {
            this.drying.pause();
            return;
        }

        float rate = light / (float) LibationFlow.FULL_LIGHT;
        this.drying.advance(rate, dry.time(), () -> {
            try (Transaction transaction = Transaction.open(null)) {
                this.items.extract(INPUT, ItemResource.of(input), 1, transaction);
                this.items.insert(OUTPUT, ItemResource.of(result), result.getCount(), transaction);
                transaction.commit();
            }
            level.playSound(null, pos, SoundRegistry.DRYING_SLAB_DRIED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        });
    }

    /**
     * @return the drying recipe of the input item, or null. Looked up only when the input item changed since the last lookup.
     */
    private @Nullable RecipeHolder<DryingRecipe> dryingRecipe(Level level) {
        if (this.dryingRecipeStale) {
            ItemStack input = this.getInput();
            this.dryingRecipe = input.isEmpty() ? null : LibationUtils.drying(level, input).orElse(null);
            this.dryingRecipeStale = false;
        }
        return this.dryingRecipe;
    }

    /**
     * @return true if the result of drying the input item fits into the output stack. Drying stalls while it does not.
     */
    public boolean canMergeOutput(ItemStack result) {
        ItemStack output = this.getOutput();
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.dropItems(this.getInput(), this.getOutput());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items.deserialize(input.childOrEmpty("items"));
        this.dryingRecipeStale = true;
        this.drying.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.items.serialize(output.child("items"));
        this.drying.save(output, this.level != null ? this.level.getGameTime() : 0L);
    }

    /**
     * Holds the input and the output. Changes sync to the clients at once, as the items are visible. Only drying inputs are
     * valid in the input slot.
     */
    private final class ItemHandler extends ItemStacksResourceHandler {
        private ItemHandler() {
            super(2);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index == OUTPUT || (DryingSlabBlockEntity.this.level != null
                    && LibationUtils.drying(DryingSlabBlockEntity.this.level, resource.toStack()).isPresent());
        }

        /**
         * Changing the input item restarts its drying, whether a pipe, a hand or the drying itself changed it. Merging more of
         * the same item into the input does not restart it.
         */
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            if (index == INPUT) {
                DryingSlabBlockEntity.this.dryingRecipeStale = true;
                if (!ItemStack.isSameItemSameComponents(previousContents, DryingSlabBlockEntity.this.getInput())) {
                    DryingSlabBlockEntity.this.drying.restart();
                }
            }
            DryingSlabBlockEntity.this.sync.syncNow();
        }
    }
}
