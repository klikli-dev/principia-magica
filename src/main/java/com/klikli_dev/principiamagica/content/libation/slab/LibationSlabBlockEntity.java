// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.slab;

import com.klikli_dev.principiamagica.content.libation.LibationBlockEntity;
import com.klikli_dev.principiamagica.content.libation.AmountSmoother;
import com.klikli_dev.principiamagica.content.libation.ExtractOnlyResourceHandler;
import com.klikli_dev.principiamagica.content.libation.LibationFlow;
import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.content.libation.PrincipleDrain;
import com.klikli_dev.principiamagica.content.libation.PrincipleStorage;
import com.klikli_dev.principiamagica.content.libation.ProgressTracker;
import com.klikli_dev.principiamagica.content.libation.RecipeCache;
import com.klikli_dev.principiamagica.content.libation.SyncThrottle;
import com.klikli_dev.principiamagica.content.libation.item.ClayPhialItem;
import com.klikli_dev.principiamagica.content.libation.item.NatronSpongeItem;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.LibationRecipe;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.FluidRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Holds a stack of one washable item type and water. Water is poured in through the fluid transfer API, so buckets and other
 * fluid containers work. The items dissolve one at a time, as long as the water covers their principles and the internal
 * storage has room for them. The stored principles drain through an open spout into the drain target below the spout.
 * <p>
 * Water and items can be inserted from pipes and hoppers through the block capabilities. Neither can be extracted that way.
 */
public class LibationSlabBlockEntity extends LibationBlockEntity {
    public static final int WATER_CAPACITY = LibationFlow.CAPACITY;
    public static final int STORAGE_CAPACITY = LibationFlow.CAPACITY;
    public static final int DISSOLVE_TIME = 40;
    public static final int NO_SLOT = -1;

    private final SyncThrottle sync = new SyncThrottle(this);
    private final ItemStacksResourceHandler items = new ItemHandler();
    private final FluidStacksResourceHandler water = new WaterHandler();
    private final ResourceHandler<FluidResource> waterInput = new InsertOnlyResourceHandler<>(this.water);
    private final ResourceHandler<ItemResource> itemInput = new InsertOnlyResourceHandler<>(this.items);
    private int dissolveSlot = NO_SLOT;
    private final ProgressTracker dissolve = new ProgressTracker(this.sync);
    /**
     * The libation recipe of the stack on the slab.
     */
    private final RecipeCache<LibationRecipe> dissolveRecipe = new RecipeCache<>();
    private int dissolveColor = PrincipleContents.DEFAULT_COLOR;
    private final PrincipleStorage stored = new PrincipleStorage(STORAGE_CAPACITY, this.sync);
    private final ResourceHandler<FluidResource> principleOutput = new ExtractOnlyResourceHandler<>(this.stored);
    private final ResourceHandler<FluidResource> fluidCapability = new CombinedResourceHandler<>(this.waterInput, this.principleOutput);
    private final PrincipleDrain drain = new PrincipleDrain(this.stored, this.sync);
    /**
     * Client-only: the water level shown, eased towards the synced water, see {@link #getDisplayedWater}.
     */
    private final AmountSmoother displayedWater = new AmountSmoother();

    public LibationSlabBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.LIBATION_SLAB.get(), pos, state);
    }

    /**
     * @return the stack on the slab, or empty. The stack holds one item type.
     */
    public ItemStack getStack() {
        ItemResource resource = this.items.getResource(0);
        return resource.isEmpty() ? ItemStack.EMPTY : resource.toStack(this.items.getAmountAsInt(0));
    }

    /**
     * @return the stored water in mB.
     */
    public int getWater() {
        return this.water.getAmountAsInt(0);
    }

    /**
     * @return the comparator signal of the stored water relative to its capacity.
     */
    @Override
    public int comparatorSignal() {
        return LibationUtils.comparatorSignal(this.getWater(), WATER_CAPACITY);
    }

    public int getDissolveSlot() {
        return this.dissolveSlot;
    }

    public boolean isDissolving() {
        return this.dissolveSlot != NO_SLOT;
    }

    /**
     * @return the progress of the dissolving item in ticks, interpolated from the synced progress so that it is smooth on the client.
     * @see ProgressTracker#display
     */
    public float getDissolveProgress(long gameTime, float partialTick) {
        return this.dissolve.display(gameTime, partialTick);
    }

    /**
     * Client-only. Eases the water level shown towards the synced water, so that throttled syncs look smooth.
     *
     * @return the water in mB to draw, see {@link AmountSmoother}.
     */
    public float getDisplayedWater(long gameTime, float partialTick) {
        return this.displayedWater.update(this.getWater(), gameTime, partialTick);
    }

    /**
     * @return the blended color of the principles that dissolve, for the fizz visuals.
     */
    public int getDissolveColor() {
        return this.dissolveColor;
    }

    /**
     * @return the principles that dissolved and wait in the internal storage until an open spout drains them.
     */
    public PrincipleContents getStored() {
        return this.stored.contents();
    }

    /**
     * @return the blended color of the principles drained last, for the drip visuals.
     */
    public int getDrainColor() {
        return this.drain.color();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LibationSlabBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        blockEntity.drain.tick(blockEntity);
        blockEntity.tickDissolve();
        blockEntity.sync.tick();
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, BlockEntityTypeRegistry.LIBATION_SLAB.get(), (slab, side) -> slab.fluidCapability);
        event.registerBlockEntity(Capabilities.Item.BLOCK, BlockEntityTypeRegistry.LIBATION_SLAB.get(), (slab, side) -> slab.itemInput);
    }

    @Override
    public InteractionResult useItemOn(ItemStack held, Player player, InteractionHand hand) {
        if (held.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        int waterBefore = this.getWater();
        if (FluidUtil.interactWithFluidHandler(player, hand, this.worldPosition, this.waterInput, null)) {
            if (this.getWater() > waterBefore) {
                this.level.playSound(null, this.worldPosition, SoundRegistry.LIBATION_SLAB_POUR.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            this.sync.syncNow();
            return InteractionResult.SUCCESS;
        }
        // Phials and sponges act on the stored principles. They are left to their own use, which runs after this block.
        if (held.getItem() instanceof ClayPhialItem || held.getItem() instanceof NatronSpongeItem) {
            return InteractionResult.PASS;
        }
        if (!FluidUtil.getFirstStackContained(held).isEmpty()) {
            LibationUtils.message(player, this.getWater() >= WATER_CAPACITY
                    ? "message.principiamagica.libation_slab.water_full"
                    : "message.principiamagica.libation_slab.water_only");
            return InteractionResult.SUCCESS;
        }

        // The same handler that pipes and hoppers insert into decides whether the item is accepted.
        int moved;
        try (Transaction transaction = Transaction.open(null)) {
            ResourceHandler<ItemResource> handHandler = PlayerInventoryWrapper.of(player).getHandSlot(hand);
            moved = ResourceHandlerUtil.move(handHandler, this.itemInput, resource -> true, held.getCount(), transaction);
            transaction.commit();
        }
        if (moved > 0) {
            this.level.playSound(null, this.worldPosition, SoundRegistry.LIBATION_SLAB_PLACE_ITEM.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        // The handler refused the item, explain why. The rules themselves live in the item handler.
        if (LibationUtils.libation(this.level, held).isEmpty()) {
            LibationUtils.message(player, "message.principiamagica.libation_slab.not_washable");
            return InteractionResult.SUCCESS;
        }
        ItemStack current = this.getStack();
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, held)) {
            LibationUtils.message(player, "message.principiamagica.libation_slab.other_item");
            return InteractionResult.SUCCESS;
        }
        LibationUtils.message(player, "message.principiamagica.libation_slab.full");
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
        if (this.items.getResource(0).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (this.isDissolving()) {
            return InteractionResult.SUCCESS;
        }

        // Pipes only insert into the slab, see itemInput. Players take the stack back through the item handler.
        try (Transaction transaction = Transaction.open(null)) {
            ResourceStack<ItemResource> taken = ResourceHandlerUtil.extractFirst(this.items, resource -> true, Integer.MAX_VALUE, transaction);
            if (taken == null) {
                return InteractionResult.PASS;
            }
            PlayerInventoryWrapper.of(player).placeItemBackInInventory(taken.resource(), taken.amount(), transaction);
            transaction.commit();
        }
        this.level.playSound(null, this.worldPosition, SoundRegistry.LIBATION_SLAB_TAKE_ITEM.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    /**
     * Advances the dissolving item, or starts dissolving the stack if the water and the internal storage can take its principles.
     */
    private void tickDissolve() {
        if (!this.isDissolving()) {
            this.tryStartDissolve();
            return;
        }

        this.dissolve.advance(1.0F, DISSOLVE_TIME, this::finishDissolve);
    }

    private void tryStartDissolve() {
        if (this.items.getResource(0).isEmpty() || this.getWater() <= 0) {
            return;
        }

        RecipeHolder<LibationRecipe> recipe = this.dissolveRecipe();
        if (recipe == null || !this.canDissolve(recipe.value().principles())) {
            return;
        }
        this.dissolveSlot = 0;
        this.dissolve.restart();
        this.dissolveColor = recipe.value().principles().blendedColor();
        this.sync.syncNow();
    }

    /**
     * Completes the dissolving item. Its principles go into the internal storage, from where they drain through a spout.
     * Runs as the completion of the dissolve progress, which syncs the result afterwards.
     */
    private void finishDissolve() {
        RecipeHolder<LibationRecipe> recipe = this.dissolveRecipe();

        if (this.items.getResource(0).isEmpty() || recipe == null || !this.canDissolve(recipe.value().principles())) {
            this.dissolveSlot = NO_SLOT;
            return;
        }

        PrincipleContents principles = recipe.value().principles();
        try (Transaction transaction = Transaction.open(null)) {
            this.items.extract(0, this.items.getResource(0), 1, transaction);
            this.water.extract(0, FluidResource.of(Fluids.WATER), waterCost(principles), transaction);
            for (PrincipleStack principleStack : principles.stacks()) {
                this.stored.insert(FluidRegistry.resourceFor(principleStack.principle()), principleStack.amount(), transaction);
            }
            transaction.commit();
        }
        this.dissolveColor = principles.blendedColor();
        this.dissolveSlot = NO_SLOT;

        this.level.playSound(null, this.worldPosition, SoundRegistry.LIBATION_SLAB_DISSOLVE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * @return the libation recipe of the stack on the slab, or null. Looked up only when the stack changed or the recipes
     * reloaded, see {@link RecipeCache}.
     */
    private @Nullable RecipeHolder<LibationRecipe> dissolveRecipe() {
        ItemStack stack = this.getStack();
        return this.dissolveRecipe.get(this.level, () -> stack.isEmpty()
                ? Optional.empty()
                : LibationUtils.libation(this.level, stack));
    }

    /**
     * @return the water in mB that dissolving the principles costs. Principles convert 1:1 into principle fluid.
     */
    public static int waterCost(PrincipleContents principles) {
        return principles.total();
    }

    /**
     * @return true if the water covers the principles and the internal storage has room for all of them.
     */
    private boolean canDissolve(PrincipleContents principles) {
        if (waterCost(principles) > this.getWater()) {
            return false;
        }
        try (Transaction transaction = Transaction.open(null)) {
            for (PrincipleStack principleStack : principles.stacks()) {
                if (this.stored.insert(FluidRegistry.resourceFor(principleStack.principle()), principleStack.amount(), transaction) < principleStack.amount()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.dropItem(this.getStack());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items.deserialize(input.childOrEmpty("items"));
        this.dissolveRecipe.invalidate();
        this.water.deserialize(input.childOrEmpty("water"));
        this.dissolveSlot = input.getIntOr("dissolve_slot", NO_SLOT);
        this.dissolve.load(input);
        this.dissolveColor = input.getIntOr("dissolve_color", PrincipleContents.DEFAULT_COLOR);
        this.stored.deserialize(input.childOrEmpty("stored"));
        this.drain.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.items.serialize(output.child("items"));
        this.water.serialize(output.child("water"));
        output.putInt("dissolve_slot", this.dissolveSlot);
        this.dissolve.save(output, this.level != null ? this.level.getGameTime() : 0L);
        output.putInt("dissolve_color", this.dissolveColor);
        this.stored.serialize(output.child("stored"));
        this.drain.save(output);
    }

    /**
     * Holds one item type up to its max stack size, limited to washable items. Changes sync to the clients at once, as the
     * items are visible.
     */
    private final class ItemHandler extends ItemStacksResourceHandler {
        private ItemHandler() {
            super(1);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return LibationSlabBlockEntity.this.level != null && LibationUtils.libation(LibationSlabBlockEntity.this.level, resource.toStack()).isPresent();
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            LibationSlabBlockEntity.this.dissolveRecipe.invalidate();
            LibationSlabBlockEntity.this.sync.syncNow();
        }
    }

    /**
     * Holds water in mB. Only water is accepted. Water that appears or runs out syncs at once, the level changes are requested.
     */
    private final class WaterHandler extends FluidStacksResourceHandler {
        private WaterHandler() {
            super(1, WATER_CAPACITY);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return resource.getFluid() == Fluids.WATER;
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            boolean emptinessChanged = previousContents.isEmpty() != (LibationSlabBlockEntity.this.getWater() == 0);
            if (emptinessChanged) {
                LibationSlabBlockEntity.this.sync.syncNow();
            } else {
                LibationSlabBlockEntity.this.sync.requestSync();
            }
        }
    }
}
