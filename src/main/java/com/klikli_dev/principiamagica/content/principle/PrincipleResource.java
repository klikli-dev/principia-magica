// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A NeoForge transfer {@link Resource} representing a single {@link Principle}.
 */
public final class PrincipleResource implements Resource {

    public static final PrincipleResource EMPTY = new PrincipleResource(null);

    private final @Nullable Holder<Principle> principle;

    private PrincipleResource(@Nullable Holder<Principle> principle) {
        this.principle = principle;
    }

    public static PrincipleResource of(@Nullable Holder<Principle> principle) {
        return principle == null ? EMPTY : new PrincipleResource(principle);
    }

    @Override
    public boolean isEmpty() {
        return this.principle == null;
    }

    public Holder<Principle> principle() {
        if (this.principle == null) {
            throw new IllegalStateException("Cannot get principle from an empty PrincipleResource");
        }
        return this.principle;
    }

    public Principle value() {
        return this.principle().value();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PrincipleResource other)) {
            return false;
        }
        if (this.principle == null || other.principle == null) {
            return this.principle == other.principle;
        }
        return this.principle.value().equals(other.principle.value());
    }

    @Override
    public int hashCode() {
        return this.principle == null ? 0 : Objects.hashCode(this.principle.value());
    }

    @Override
    public String toString() {
        return this.principle == null ? "PrincipleResource[EMPTY]" : "PrincipleResource[" + this.principle.getRegisteredName() + "]";
    }
}
