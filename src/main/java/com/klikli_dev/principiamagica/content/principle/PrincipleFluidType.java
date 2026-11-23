// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle;

import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

/**
 * {@link FluidType} for a {@link Principle}, reusing its principle translation key and phial sounds.
 */
public class PrincipleFluidType extends FluidType {

    private final Holder<Principle> principle;

    public PrincipleFluidType(Holder<Principle> principle, Properties properties) {
        super(properties);
        this.principle = principle;
    }

    public Holder<Principle> principle() {
        return this.principle;
    }

    @Override
    public @Nullable SoundEvent getSound(SoundAction action) {
        if (action == SoundActions.BUCKET_FILL) {
            return SoundRegistry.PHIAL_FILL.get();
        }
        if (action == SoundActions.BUCKET_EMPTY) {
            return SoundRegistry.PHIAL_EMPTY.get();
        }
        return super.getSound(action);
    }
}
