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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Principles held by a block entity with a fixed total capacity, exposed as a NeoForge {@link ResourceHandler} of
 * {@link FluidResource}. Each registered {@link Principle} occupies its registry-ID slot as its corresponding
 * {@link PrincipleFluid}. Transactional changes sync the owner through its {@link SyncThrottle} on root commit, and
 * notify the owner's content listener, so that recipes that depend on the principles are re-evaluated.
 */
public class PrincipleStorage implements ResourceHandler<FluidResource> {

    private final int capacity;
    private final SyncThrottle sync;
    private final Runnable onContentsChanged;
    private final StorageJournal journal = new StorageJournal();
    private PrincipleContents contents = PrincipleContents.EMPTY;

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
        this.capacity = capacity;
        this.sync = sync;
        this.onContentsChanged = onContentsChanged;
    }

    public PrincipleContents contents() {
        return this.contents;
    }

    /**
     * Replaces the contents without syncing the owner, for loading and for moves the owner syncs itself. The content listener
     * still runs, as cached recipes depend on the contents.
     */
    public void set(PrincipleContents contents) {
        this.contents = contents;
        this.onContentsChanged.run();
    }

    public boolean isEmpty() {
        return this.contents.isEmpty();
    }

    /**
     * @return the comparator signal of the stored principles relative to the capacity.
     */
    public int comparatorSignal() {
        return LibationUtils.comparatorSignal(this.contents.total(), this.capacity);
    }

    @Override
    public int size() {
        return PrincipiaMagicaRegistries.PRINCIPLE.size();
    }

    @Override
    public FluidResource getResource(int index) {
        Holder<Principle> holder = this.holderAt(index);
        return this.contents.get(holder) > 0 ? FluidRegistry.resourceFor(holder) : FluidResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        Holder<Principle> holder = this.holderAt(index);
        return this.contents.get(holder);
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        Objects.checkIndex(index, this.size());
        if (!resource.isEmpty() && !this.isValid(index, resource)) {
            return 0L;
        }
        return this.getAmountAsLong(index) + Math.max(0, this.capacity - this.contents.total());
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, this.size());
        if (!FluidRegistry.isPrincipleFluid(resource)) {
            return false;
        }
        PrincipleFluid fluid = (PrincipleFluid) resource.getFluid();
        return PrincipiaMagicaRegistries.PRINCIPLE.getId(fluid.principle().value()) == index;
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, this.size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!this.isValid(index, resource)) {
            return 0;
        }
        int accepted = Math.min(amount, Math.max(0, this.capacity - this.contents.total()));
        if (accepted > 0) {
            PrincipleFluid fluid = (PrincipleFluid) resource.getFluid();
            this.journal.updateSnapshots(transaction);
            this.contents = this.contents.add(PrincipleContents.of(new PrincipleStack(fluid.principle(), accepted)));
        }
        return accepted;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!FluidRegistry.isPrincipleFluid(resource)) {
            return 0;
        }
        PrincipleFluid fluid = (PrincipleFluid) resource.getFluid();
        int index = PrincipiaMagicaRegistries.PRINCIPLE.getId(fluid.principle().value());
        if (index < 0 || index >= this.size()) {
            return 0;
        }
        return this.insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, this.size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!this.isValid(index, resource)) {
            return 0;
        }
        PrincipleFluid fluid = (PrincipleFluid) resource.getFluid();
        int current = this.contents.get(fluid.principle());
        int extracted = Math.min(amount, current);
        if (extracted > 0) {
            this.journal.updateSnapshots(transaction);
            this.contents = this.contents.subtract(PrincipleContents.of(new PrincipleStack(fluid.principle(), extracted)));
        }
        return extracted;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!FluidRegistry.isPrincipleFluid(resource)) {
            return 0;
        }
        PrincipleFluid fluid = (PrincipleFluid) resource.getFluid();
        int index = PrincipiaMagicaRegistries.PRINCIPLE.getId(fluid.principle().value());
        if (index < 0 || index >= this.size()) {
            return 0;
        }
        return this.extract(index, resource, amount, transaction);
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

    private Holder<Principle> holderAt(int index) {
        Objects.checkIndex(index, this.size());
        return PrincipiaMagicaRegistries.PRINCIPLE.get(index).orElseThrow();
    }

    private final class StorageJournal extends SnapshotJournal<PrincipleContents> {

        @Override
        protected PrincipleContents createSnapshot() {
            return PrincipleStorage.this.contents;
        }

        @Override
        protected void revertToSnapshot(PrincipleContents snapshot) {
            PrincipleStorage.this.contents = snapshot;
        }

        @Override
        protected void onRootCommit(PrincipleContents originalState) {
            if (originalState.equals(PrincipleStorage.this.contents)) {
                return;
            }
            PrincipleStorage.this.onContentsChanged.run();
            boolean emptinessChanged = originalState.isEmpty() != PrincipleStorage.this.contents.isEmpty();
            if (emptinessChanged) {
                PrincipleStorage.this.sync.syncNow();
            } else {
                PrincipleStorage.this.sync.requestSync();
            }
        }
    }
}
