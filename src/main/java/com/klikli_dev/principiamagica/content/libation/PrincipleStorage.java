// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Principles held by a block entity with a fixed total capacity.
 */
public class PrincipleStorage {

    private final int capacity;
    private PrincipleContents contents = PrincipleContents.EMPTY;

    public PrincipleStorage(int capacity) {
        this.capacity = capacity;
    }

    public PrincipleContents contents() {
        return this.contents;
    }

    public void set(PrincipleContents contents) {
        this.contents = contents;
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

    /**
     * Accepts as many principles as the remaining capacity allows, in registry order.
     *
     * @return the leftover that was not accepted.
     */
    public PrincipleContents insert(PrincipleContents offered, boolean simulate) {
        int space = Math.max(0, this.capacity - this.contents.total());
        PrincipleContents accepted = takeUpTo(offered, space);
        if (!simulate) {
            this.contents = this.contents.add(accepted);
        }
        return offered.subtract(accepted);
    }

    /**
     * @return a part of the given contents with at most {@code limit} mB in total, in registry order.
     */
    public static PrincipleContents takeUpTo(PrincipleContents contents, int limit) {
        List<PrincipleStack> taken = new ArrayList<>();
        int remaining = limit;
        for (PrincipleStack stack : contents.stacks()) {
            if (remaining <= 0) {
                break;
            }
            int amount = Math.min(stack.amount(), remaining);
            taken.add(new PrincipleStack(stack.principle(), amount));
            remaining -= amount;
        }
        return PrincipleContents.of(taken);
    }
}
