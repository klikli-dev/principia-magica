// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.drying;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * The view of the drying slab that pipes and hoppers see. Drying inputs can only be inserted into the input slot while it is
 * empty, and only the finished items in the output slot can be extracted. The input is never extracted from the outside.
 */
public class DryingSlabItemHandler extends DelegatingResourceHandler<ItemResource> {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;

    public DryingSlabItemHandler(ResourceHandler<ItemResource> delegate) {
        super(delegate);
    }

    /**
     * Accepts drying inputs into the empty input slot. The delegate rejects everything that is not a drying input.
     */
    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (index != INPUT || !this.getResource(INPUT).isEmpty()) {
            return 0;
        }
        return super.insert(index, resource, amount, transaction);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return this.insert(INPUT, resource, amount, transaction);
    }

    /**
     * Extracts finished items from the output slot only.
     */
    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return index == OUTPUT ? super.extract(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return this.extract(OUTPUT, resource, amount, transaction);
    }
}
