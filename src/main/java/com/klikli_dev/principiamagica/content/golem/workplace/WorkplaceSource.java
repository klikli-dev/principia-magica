// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Indirection through which jobs and the golem resolve their workplace.
 * <p>
 * Only {@link Inline} exists in v1. Future: a {@code Shared(GlobalPos)} variant for a placed sigil-like block that
 * many golems subscribe to (a seal-style shared task pool). Jobs must only access the workplace via {@link #resolve}.
 */
public sealed interface WorkplaceSource permits WorkplaceSource.Inline {

    // Future: encode a "type" discriminator (dispatch) once a second variant exists.
    Codec<WorkplaceSource> CODEC = Workplace.CODEC.xmap(Inline::new, source -> ((Inline) source).workplace());

    Optional<Workplace> resolve(ServerLevel level);

    WorkplaceSource update(UnaryOperator<Workplace> operator);

    record Inline(Workplace workplace) implements WorkplaceSource {

        @Override
        public Optional<Workplace> resolve(ServerLevel level) {
            return Optional.of(this.workplace);
        }

        @Override
        public WorkplaceSource update(UnaryOperator<Workplace> operator) {
            return new Inline(operator.apply(this.workplace));
        }
    }
}
