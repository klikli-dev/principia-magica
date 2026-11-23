// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes a handler for extraction only. Insertion always moves nothing.
 */
public class ExtractOnlyResourceHandler<T extends Resource> extends DelegatingResourceHandler<T> {

    public ExtractOnlyResourceHandler(ResourceHandler<T> delegate) {
        super(delegate);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
