// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Transactional helpers for moving and querying {@link PrincipleResource}s via {@link ResourceHandler}.
 */
public final class PrincipleTransferUtil {

    private PrincipleTransferUtil() {
    }

    /**
     * Inserts each stack in {@code contents} into {@code handler}, returning the total amount accepted.
     */
    public static int insertContents(ResourceHandler<PrincipleResource> handler, PrincipleContents contents, @Nullable TransactionContext transaction) {
        if (contents.isEmpty()) {
            return 0;
        }
        int totalInserted = 0;
        try (Transaction tx = Transaction.open(transaction)) {
            for (PrincipleStack stack : contents.stacks()) {
                totalInserted += handler.insert(PrincipleResource.of(stack.principle()), stack.amount(), tx);
            }
            tx.commit();
        }
        return totalInserted;
    }

    /**
     * @return true if {@code handler} can accept the full amount of every stack in {@code contents}.
     */
    public static boolean canInsertAll(ResourceHandler<PrincipleResource> handler, PrincipleContents contents) {
        if (contents.isEmpty()) {
            return true;
        }
        try (Transaction tx = Transaction.open(null)) {
            for (PrincipleStack stack : contents.stacks()) {
                int accepted = handler.insert(PrincipleResource.of(stack.principle()), stack.amount(), tx);
                if (accepted < stack.amount()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Extracts the exact amounts in {@code required} from {@code handler}.
     *
     * @return true if every stack was extracted in full; if false, the inner transaction is aborted and nothing is extracted.
     */
    public static boolean extractAll(ResourceHandler<PrincipleResource> handler, PrincipleContents required, @Nullable TransactionContext transaction) {
        if (required.isEmpty()) {
            return true;
        }
        try (Transaction tx = Transaction.open(transaction)) {
            for (PrincipleStack stack : required.stacks()) {
                int extracted = handler.extract(PrincipleResource.of(stack.principle()), stack.amount(), tx);
                if (extracted < stack.amount()) {
                    return false;
                }
            }
            tx.commit();
            return true;
        }
    }

    /**
     * Moves up to {@code maxTotal} mB across slots in slot order from {@code from} to {@code to}.
     *
     * @return the {@link PrincipleContents} actually moved.
     */
    public static PrincipleContents extractUpTo(ResourceHandler<PrincipleResource> from, ResourceHandler<PrincipleResource> to,
                                              int maxTotal, @Nullable TransactionContext transaction) {
        if (maxTotal <= 0) {
            return PrincipleContents.EMPTY;
        }
        List<PrincipleStack> moved = new ArrayList<>();
        int remaining = maxTotal;
        try (Transaction tx = Transaction.open(transaction)) {
            int size = from.size();
            for (int index = 0; index < size && remaining > 0; index++) {
                PrincipleResource resource = from.getResource(index);
                if (resource.isEmpty()) {
                    continue;
                }
                int available = Math.min(from.getAmountAsInt(index), remaining);
                if (available <= 0) {
                    continue;
                }
                int maxExtract;
                try (Transaction sim = Transaction.open(tx)) {
                    maxExtract = from.extract(index, resource, available, sim);
                }
                if (maxExtract <= 0) {
                    continue;
                }
                try (Transaction moveTx = Transaction.open(tx)) {
                    int inserted = to.insert(resource, maxExtract, moveTx);
                    if (inserted > 0 && from.extract(index, resource, inserted, moveTx) == inserted) {
                        moveTx.commit();
                        moved.add(new PrincipleStack(resource.principle(), inserted));
                        remaining -= inserted;
                    }
                }
            }
            tx.commit();
        }
        return PrincipleContents.of(moved);
    }

    /**
     * Reads all non-empty slots in {@code handler} into an immutable {@link PrincipleContents}.
     */
    public static PrincipleContents snapshot(ResourceHandler<PrincipleResource> handler) {
        List<PrincipleStack> stacks = new ArrayList<>();
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            PrincipleResource resource = handler.getResource(index);
            int amount = handler.getAmountAsInt(index);
            if (!resource.isEmpty() && amount > 0) {
                stacks.add(new PrincipleStack(resource.principle(), amount));
            }
        }
        return PrincipleContents.of(stacks);
    }
}
