// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, PrincipiaMagica.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> LIBATION_SLAB_POUR = register("libation_slab.pour");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIBATION_SLAB_DISSOLVE = register("libation_slab.dissolve");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIBATION_SLAB_PLACE_ITEM = register("libation_slab.place_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATCH_TROUGH_STEEP = register("catch_trough.steep");
    public static final DeferredHolder<SoundEvent, SoundEvent> VESSEL_TAP_OPEN = register("vessel.tap_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> VESSEL_TAP_CLOSE = register("vessel.tap_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> VESSEL_CURE = register("vessel.cure");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRYING_SLAB_DRIED = register("drying_slab.dried");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, SoundEvent::createVariableRangeEvent);
    }
}
