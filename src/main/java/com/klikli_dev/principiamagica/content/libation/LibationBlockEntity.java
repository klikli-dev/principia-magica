// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base of the libation block entities that are synced to the clients and interacted with by players.
 * <p>
 * This is a shallow base on purpose: it holds no state, only the overrides that every such block entity needs (Java does not
 * inherit them from interfaces), and no-op defaults for the interaction and comparator hooks. The state and behaviour live in
 * composed components, such as {@link PrincipleStorage}, {@link PrincipleDrain} and {@link ProgressTracker}.
 */
public abstract class LibationBlockEntity extends BlockEntity {

    protected LibationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Called when the player uses the block with an item in hand.
     *
     * @return the result of the interaction, {@link InteractionResult#PASS} by default.
     */
    public InteractionResult useItemOn(ItemStack held, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /**
     * Called when the player uses the block with an empty hand.
     *
     * @return the result of the interaction, {@link InteractionResult#PASS} by default.
     */
    public InteractionResult useWithoutItem(Player player) {
        return InteractionResult.PASS;
    }

    /**
     * @return the comparator signal of the block entity, 0 by default.
     */
    public int comparatorSignal() {
        return 0;
    }

    /**
     * Drops the stack at the centre of the block, when the block entity is removed. Does nothing for empty stacks.
     */
    protected void dropItem(ItemStack stack) {
        if (this.level != null && !stack.isEmpty()) {
            Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D, stack);
        }
    }

    /**
     * Drops all the given stacks at the block, when the block entity is removed.
     */
    protected void dropItems(ItemStack... stacks) {
        if (this.level != null) {
            Containers.dropContents(this.level, this.worldPosition, NonNullList.of(ItemStack.EMPTY, stacks));
        }
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
