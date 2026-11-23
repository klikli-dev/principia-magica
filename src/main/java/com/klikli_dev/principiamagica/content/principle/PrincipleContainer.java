// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle;

import com.klikli_dev.principiamagica.registry.FluidRegistry;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Implemented by block entities that hold principles, such as the Catch Trough, Wabet Basin and Steeping Jar.
 */
public interface PrincipleContainer {

    PrincipleContents getPrinciples();

    ResourceHandler<FluidResource> principleHandler();

    /**
     * Offers principles to this container.
     * <p>
     * Partial acceptance is allowed up to the capacity of the container.
     *
     * @param offered  the principles to insert.
     * @param simulate if true, the container is not modified and only the result is computed.
     * @return the leftover: the part of {@code offered} that was NOT accepted. Empty if everything was accepted.
     */
    default PrincipleContents insert(PrincipleContents offered, boolean simulate) {
        if (offered.isEmpty()) {
            return PrincipleContents.EMPTY;
        }
        ResourceHandler<FluidResource> handler = this.principleHandler();
        List<PrincipleStack> accepted = new ArrayList<>();
        try (Transaction tx = Transaction.open(null)) {
            for (PrincipleStack stack : offered.stacks()) {
                int moved = handler.insert(FluidRegistry.resourceFor(stack.principle()), stack.amount(), tx);
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
}
