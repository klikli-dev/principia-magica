// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.slab;

import com.klikli_dev.principiamagica.content.libation.LibationFlow;
import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.content.libation.PrincipleDrain;
import com.klikli_dev.principiamagica.content.libation.PrincipleStorage;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.recipe.LibationRecipe;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Optional;

/**
 * Holds a stack of one washable item type and water. Water is poured in through the fluid transfer API, so buckets and other
 * fluid containers work. The items dissolve one at a time, as long as the water covers their principles and the internal
 * storage has room for them. The stored principles drain through an open spout into the drain target below the spout.
 * <p>
 * Water and items can be inserted from pipes and hoppers through the block capabilities. Neither can be extracted that way.
 */
public class LibationSlabBlockEntity extends BlockEntity {
    public static final int WATER_CAPACITY = LibationFlow.CAPACITY;
    public static final int STORAGE_CAPACITY = LibationFlow.CAPACITY;
    public static final int DISSOLVE_TIME = 40;
    public static final int NO_SLOT = -1;

    private final ItemStacksResourceHandler items = new ItemHandler();
    private final FluidStacksResourceHandler water = new WaterHandler();
    private final ResourceHandler<FluidResource> waterInput = new InsertOnlyResourceHandler<>(this.water);
    private final ResourceHandler<ItemResource> itemInput = new InsertOnlyResourceHandler<>(this.items);
    private int dissolveSlot = NO_SLOT;
    private int dissolveProgress;
    private int dissolveColor = PrincipleContents.DEFAULT_COLOR;
    private final PrincipleStorage stored = new PrincipleStorage(STORAGE_CAPACITY, () -> LibationUtils.sync(this));
    private final PrincipleDrain drain = new PrincipleDrain(this.stored);

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
    public int comparatorSignal() {
        return LibationUtils.comparatorSignal(this.getWater(), WATER_CAPACITY);
    }

    public int getDissolveSlot() {
        return this.dissolveSlot;
    }

    public boolean isDissolving() {
        return this.dissolveSlot != NO_SLOT;
    }

    public int getDissolveProgress() {
        return this.dissolveProgress;
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
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, BlockEntityTypeRegistry.LIBATION_SLAB.get(), (slab, side) -> slab.waterInput);
        event.registerBlockEntity(Capabilities.Item.BLOCK, BlockEntityTypeRegistry.LIBATION_SLAB.get(), (slab, side) -> slab.itemInput);
    }

    public InteractionResult useItemOn(ItemStack held, Player player, InteractionHand hand) {
        if (held.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (FluidUtil.interactWithFluidHandler(player, hand, this.worldPosition, this.waterInput, null)) {
            LibationUtils.sync(this);
            return InteractionResult.SUCCESS;
        }
        if (!FluidUtil.getFirstStackContained(held).isEmpty()) {
            LibationUtils.message(player, this.getWater() >= WATER_CAPACITY
                    ? "message.principiamagica.libation_slab.water_full"
                    : "message.principiamagica.libation_slab.water_only");
            return InteractionResult.SUCCESS;
        }

        if (LibationUtils.libation(this.level, held).isEmpty()) {
            LibationUtils.message(player, "message.principiamagica.libation_slab.not_washable");
            return InteractionResult.SUCCESS;
        }

        ItemStack current = this.getStack();
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, held)) {
            LibationUtils.message(player, "message.principiamagica.libation_slab.other_item");
            return InteractionResult.SUCCESS;
        }

        int inserted;
        try (Transaction transaction = Transaction.open(null)) {
            inserted = this.items.insert(0, ItemResource.of(held), held.getCount(), transaction);
            transaction.commit();
        }
        if (inserted == 0) {
            LibationUtils.message(player, "message.principiamagica.libation_slab.full");
            return InteractionResult.SUCCESS;
        }

        held.shrink(inserted);
        this.level.playSound(null, this.worldPosition, SoundRegistry.LIBATION_SLAB_PLACE_ITEM.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

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

        ItemStack taken;
        try (Transaction transaction = Transaction.open(null)) {
            ItemResource resource = this.items.getResource(0);
            int extracted = this.items.extract(0, resource, this.items.getAmountAsInt(0), transaction);
            taken = resource.toStack(extracted);
            transaction.commit();
        }
        player.getInventory().placeItemBackInInventory(taken);
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

        this.dissolveProgress++;
        if (this.dissolveProgress >= DISSOLVE_TIME) {
            this.finishDissolve();
        } else {
            LibationUtils.sync(this);
        }
    }

    private void tryStartDissolve() {
        ItemStack stack = this.getStack();
        if (stack.isEmpty() || this.getWater() <= 0) {
            return;
        }

        Optional<RecipeHolder<LibationRecipe>> recipe = LibationUtils.libation(this.level, stack);
        if (recipe.isEmpty() || !this.canDissolve(recipe.get().value().principles())) {
            return;
        }
        this.dissolveSlot = 0;
        this.dissolveProgress = 0;
        this.dissolveColor = recipe.get().value().principles().blendedColor();
        LibationUtils.sync(this);
    }

    /**
     * Completes the dissolving item. Its principles go into the internal storage, from where they drain through a spout.
     */
    private void finishDissolve() {
        ItemStack stack = this.getStack();
        Optional<RecipeHolder<LibationRecipe>> recipe = LibationUtils.libation(this.level, stack);

        if (stack.isEmpty() || recipe.isEmpty() || !this.canDissolve(recipe.get().value().principles())) {
            this.dissolveSlot = NO_SLOT;
            this.dissolveProgress = 0;
            LibationUtils.sync(this);
            return;
        }

        PrincipleContents principles = recipe.get().value().principles();
        try (Transaction transaction = Transaction.open(null)) {
            this.items.extract(0, ItemResource.of(stack), 1, transaction);
            this.water.extract(0, FluidResource.of(Fluids.WATER), waterCost(principles), transaction);
            transaction.commit();
        }
        this.stored.insert(principles, false);
        this.dissolveColor = principles.blendedColor();
        this.dissolveSlot = NO_SLOT;
        this.dissolveProgress = 0;

        this.level.playSound(null, this.worldPosition, SoundRegistry.LIBATION_SLAB_DISSOLVE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        LibationUtils.sync(this);
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
        return waterCost(principles) <= this.getWater() && this.stored.insert(principles, true).isEmpty();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        ItemStack stack = this.getStack();
        if (this.level != null && !stack.isEmpty()) {
            Containers.dropItemStack(this.level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items.deserialize(input.childOrEmpty("items"));
        this.water.deserialize(input.childOrEmpty("water"));
        this.dissolveSlot = input.getIntOr("dissolve_slot", NO_SLOT);
        this.dissolveProgress = input.getIntOr("dissolve_progress", 0);
        this.dissolveColor = input.getIntOr("dissolve_color", PrincipleContents.DEFAULT_COLOR);
        input.read("stored", PrincipleContents.CODEC).ifPresent(this.stored::set);
        this.drain.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.items.serialize(output.child("items"));
        this.water.serialize(output.child("water"));
        output.putInt("dissolve_slot", this.dissolveSlot);
        output.putInt("dissolve_progress", this.dissolveProgress);
        output.putInt("dissolve_color", this.dissolveColor);
        output.store("stored", PrincipleContents.CODEC, this.stored.contents());
        this.drain.save(output);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Holds one item type up to its max stack size, limited to washable items. Changes sync to the clients.
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
            LibationUtils.sync(LibationSlabBlockEntity.this);
        }
    }

    /**
     * Holds water in mB. Only water is accepted. Changes sync to the clients.
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
            LibationUtils.sync(LibationSlabBlockEntity.this);
        }
    }
}
