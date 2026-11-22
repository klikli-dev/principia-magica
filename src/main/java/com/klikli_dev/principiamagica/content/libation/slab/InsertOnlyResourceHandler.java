// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.slab;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes a handler for insertion only. Extraction always moves nothing.
 */
public class InsertOnlyResourceHandler<T extends Resource> extends DelegatingResourceHandler<T> {

    public InsertOnlyResourceHandler(ResourceHandler<T> delegate) {
        super(delegate);
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
