// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.item;

import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleResource;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * {@link net.neoforged.neoforge.transfer.ResourceHandler} for a {@link ClayPhialItem} backed by {@link ItemAccess}.
 * Holds a single {@link PrincipleResource} up to {@link ClayPhialItem#CAPACITY} mB.
 * Clearing the {@code STORED_PRINCIPLES} component when empty ensures empty phials stack identically.
 */
public class ClayPhialResourceHandler extends ItemAccessResourceHandler<PrincipleResource> {

    public ClayPhialResourceHandler(ItemAccess itemAccess) {
        super(itemAccess, 1);
    }

    @Override
    protected PrincipleResource getResourceFrom(ItemResource accessResource, int index) {
        if (!accessResource.is(ItemRegistry.CLAY_PHIAL.get())) {
            return PrincipleResource.EMPTY;
        }
        PrincipleContents contents = accessResource.get(DataComponentRegistry.STORED_PRINCIPLES.get());
        if (contents == null || contents.isEmpty()) {
            return PrincipleResource.EMPTY;
        }
        return PrincipleResource.of(contents.stacks().getFirst().principle());
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        if (!accessResource.is(ItemRegistry.CLAY_PHIAL.get())) {
            return 0;
        }
        PrincipleContents contents = accessResource.get(DataComponentRegistry.STORED_PRINCIPLES.get());
        if (contents == null || contents.isEmpty()) {
            return 0;
        }
        return contents.stacks().getFirst().amount();
    }

    @Override
    protected @Nullable ItemResource update(ItemResource accessResource, int index, PrincipleResource newResource, int newAmount) {
        if (newAmount <= 0 || newResource.isEmpty()) {
            return accessResource.without(DataComponentRegistry.STORED_PRINCIPLES.get());
        }
        return accessResource.with(DataComponentRegistry.STORED_PRINCIPLES.get(),
                PrincipleContents.of(new PrincipleStack(newResource.principle(), newAmount)));
    }

    @Override
    public boolean isValid(int index, PrincipleResource resource) {
        return this.itemAccess.getResource().is(ItemRegistry.CLAY_PHIAL.get()) && !resource.isEmpty();
    }

    @Override
    protected int getCapacity(int index, PrincipleResource resource) {
        return ClayPhialItem.CAPACITY;
    }
}
