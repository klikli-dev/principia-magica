// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleFluid;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.registry.FluidRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Principles held by a block entity with a fixed total capacity, exposed as a NeoForge {@link FluidStacksResourceHandler}.
 * <p>
 * There is one slot per registered {@link Principle}, the slot index is its registry id. A slot only accepts its own
 * {@link PrincipleFluid}. The slots share the total capacity: the capacity of a slot is what it holds plus what is left of the
 * total, so the builtin insert clamps to the shared remaining capacity, including within a transaction.
 * <p>
 * Changes are committed through the builtin snapshotting. A root commit notifies {@link #onContentsChanged}, which syncs the
 * owner through its {@link SyncThrottle}: at once when the storage becomes empty or non-empty, and requested otherwise. Rolled
 * back changes never reach the owner.
 */
public class PrincipleStorage extends FluidStacksResourceHandler {

    private final SyncThrottle sync;
    private final Runnable onContentsChanged;
    /**
     * True while {@link #set(PrincipleContents)} replaces the contents, which does not sync the owner.
     */
    private boolean replacing;
    /**
     * Whether the storage was empty at the last sync, so that emptiness changes are synced at once.
     */
    private boolean wasEmpty = true;

    /**
     * @param capacity the total amount of principles in mB.
     * @param sync     the throttle of the owning block entity.
     */
    public PrincipleStorage(int capacity, SyncThrottle sync) {
        this(capacity, sync, () -> {});
    }

    /**
     * @param capacity          the total amount of principles in mB.
     * @param sync              the throttle of the owning block entity.
     * @param onContentsChanged runs whenever the contents change, after a transaction commits or the contents are replaced.
     */
    public PrincipleStorage(int capacity, SyncThrottle sync, Runnable onContentsChanged) {
        super(PrincipiaMagicaRegistries.PRINCIPLE.size(), capacity);
        this.sync = sync;
        this.onContentsChanged = onContentsChanged;
    }

    /**
     * @return the principles held, as a snapshot.
     */
    public PrincipleContents contents() {
        List<PrincipleStack> stacks = new ArrayList<>();
        for (int index = 0; index < this.size(); index++) {
            int amount = this.getAmountAsInt(index);
            if (amount > 0) {
                stacks.add(new PrincipleStack(this.holderAt(index), amount));
            }
        }
        return PrincipleContents.of(stacks);
    }

    /**
     * Replaces the contents without syncing the owner, for loading and for moves the owner syncs itself. The content listener
     * still runs, as cached recipes depend on the contents.
     */
    public void set(PrincipleContents contents) {
        this.replacing = true;
        try {
            for (int index = 0; index < this.size(); index++) {
                Holder<Principle> holder = this.holderAt(index);
                int amount = contents.get(holder);
                if (this.getAmountAsInt(index) != amount) {
                    this.set(index, amount > 0 ? FluidRegistry.resourceFor(holder) : FluidResource.EMPTY, amount);
                }
            }
        } finally {
            this.replacing = false;
        }
        this.wasEmpty = this.isEmpty();
        this.onContentsChanged.run();
    }

    /**
     * One saved principle, keyed by its identifier rather than its slot, so that the save does not depend on the registry order
     * or on the principles that existed when it was written.
     *
     * @param principle the identifier of the principle.
     * @param amount    the amount in mB.
     */
    public record Saved(Identifier principle, int amount) {
        public static final Codec<Saved> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("principle").forGetter(Saved::principle),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(Saved::amount)
        ).apply(instance, Saved::new));
    }

    private static final Codec<List<Saved>> SAVED_CODEC = Saved.CODEC.listOf();

    /**
     * Saves the non-empty principles by identifier.
     */
    @Override
    public void serialize(ValueOutput output) {
        List<Saved> saved = new ArrayList<>();
        for (int index = 0; index < this.size(); index++) {
            int amount = this.getAmountAsInt(index);
            if (amount > 0) {
                saved.add(new Saved(this.holderAt(index).unwrapKey().orElseThrow().identifier(), amount));
            }
        }
        output.store("stacks", SAVED_CODEC, saved);
    }

    /**
     * Loads the principles that {@link #serialize} saved, see {@link #load}. Loading does not sync the owner, the owner loads and
     * syncs itself.
     */
    @Override
    public void deserialize(ValueInput input) {
        this.load(input.read("stacks", SAVED_CODEC).orElse(List.of()));
    }

    /**
     * Replaces the contents with the saved principles. Every current principle gets its own slot, the saved amounts go into the
     * slot of their principle wherever they were saved. Principles that no longer exist are dropped.
     *
     * @param saved the saved principles, in any order.
     */
    public void load(List<Saved> saved) {
        int[] amounts = new int[this.size()];
        for (Saved entry : saved) {
            Optional<Holder.Reference<Principle>> holder = PrincipiaMagicaRegistries.PRINCIPLE.get(
                    ResourceKey.create(PrincipiaMagicaRegistries.Keys.PRINCIPLE, entry.principle()));
            if (holder.isPresent()) {
                amounts[PrincipiaMagicaRegistries.PRINCIPLE.getId(holder.get().value())] += entry.amount();
            }
        }

        List<PrincipleStack> stacks = new ArrayList<>();
        for (int index = 0; index < amounts.length; index++) {
            if (amounts[index] > 0) {
                stacks.add(new PrincipleStack(this.holderAt(index), amounts[index]));
            }
        }
        this.set(PrincipleContents.of(stacks));
    }

    public boolean isEmpty() {
        return this.totalAmount() == 0;
    }

    /**
     * @return the comparator signal of the stored principles relative to the capacity.
     */
    public int comparatorSignal() {
        return LibationUtils.comparatorSignal(this.totalAmount(), this.capacity);
    }

    /**
     * @return the total amount of principles in mB, over all slots.
     */
    private int totalAmount() {
        int total = 0;
        for (int index = 0; index < this.size(); index++) {
            total += this.getAmountAsInt(index);
        }
        return total;
    }

    /**
     * A slot accepts only its own principle fluid. Empty slots and the principle held by the slot are the only ones it accepts.
     */
    @Override
    public boolean isValid(int index, FluidResource resource) {
        return FluidRegistry.isPrincipleFluid(resource) && this.indexOf(resource) == index;
    }

    /**
     * The slot can hold what it holds plus what is left of the shared capacity, so the builtin insert clamps to that.
     */
    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return this.getAmountAsInt(index) + Math.max(0, this.capacity - this.totalAmount());
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int index = this.indexOf(resource);
        return index < 0 ? 0 : this.insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int index = this.indexOf(resource);
        return index < 0 ? 0 : this.extract(index, resource, amount, transaction);
    }

    /**
     * Accepts as many principles as the remaining capacity allows, in registry order.
     *
     * @return the leftover that was not accepted.
     */
    public PrincipleContents insert(PrincipleContents offered, boolean simulate) {
        if (offered.isEmpty()) {
            return PrincipleContents.EMPTY;
        }
        List<PrincipleStack> accepted = new ArrayList<>();
        try (Transaction tx = Transaction.open(null)) {
            for (PrincipleStack stack : offered.stacks()) {
                int moved = this.insert(FluidRegistry.resourceFor(stack.principle()), stack.amount(), tx);
                if (moved > 0) {
                    accepted.add(new PrincipleStack(stack.principle(), moved));
                }
            }
            if (!simulate) {
                tx.commit();
            }
        }
        return offered.subtract(PrincipleContents.of(accepted));
    }

    /**
     * @return the registry id of the principle of the resource, or -1 if it is not a principle fluid.
     */
    private int indexOf(FluidResource resource) {
        if (!FluidRegistry.isPrincipleFluid(resource)) {
            return -1;
        }
        PrincipleFluid fluid = (PrincipleFluid) resource.getFluid();
        return PrincipiaMagicaRegistries.PRINCIPLE.getId(fluid.principle().value());
    }

    private Holder<Principle> holderAt(int index) {
        return PrincipiaMagicaRegistries.PRINCIPLE.get(index).orElseThrow();
    }

    /**
     * Syncs the owner when the contents change in a committed transaction. Runs for each changed slot, the first call of a
     * commit already sees the whole commit.
     */
    @Override
    protected void onContentsChanged(int index, FluidStack previousContents) {
        if (this.replacing) {
            return;
        }
        boolean empty = this.isEmpty();
        boolean emptinessChanged = empty != this.wasEmpty;
        this.wasEmpty = empty;
        if (emptinessChanged) {
            this.sync.syncNow();
        } else {
            this.sync.requestSync();
        }
        this.onContentsChanged.run();
    }
}
