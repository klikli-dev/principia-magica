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
    public static final DeferredHolder<SoundEvent, SoundEvent> LIBATION_SLAB_DISSOLVING = register("libation_slab.dissolving");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIBATION_SLAB_PLACE_ITEM = register("libation_slab.place_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIBATION_SLAB_TAKE_ITEM = register("libation_slab.take_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIBATION_SPOUT_OPEN = register("libation_spout.open");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIBATION_SPOUT_CLOSE = register("libation_spout.close");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPOUT_DRIP = register("spout.drip");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATCH_TROUGH_STEEP = register("catch_trough.steep");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATCH_TROUGH_PLACE_ITEM = register("catch_trough.place_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATCH_TROUGH_TAKE_ITEM = register("catch_trough.take_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> VESSEL_TAP_OPEN = register("vessel.tap_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> VESSEL_TAP_CLOSE = register("vessel.tap_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> VESSEL_CURE = register("vessel.cure");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEEPING_JAR_LID_CLOSE = register("steeping_jar.lid_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEEPING_JAR_LID_OPEN = register("steeping_jar.lid_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRYING_SLAB_DRIED = register("drying_slab.dried");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRYING_SLAB_PLACE_ITEM = register("drying_slab.place_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRYING_SLAB_TAKE_ITEM = register("drying_slab.take_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> PHIAL_FILL = register("phial.fill");
    public static final DeferredHolder<SoundEvent, SoundEvent> PHIAL_EMPTY = register("phial.empty");
    public static final DeferredHolder<SoundEvent, SoundEvent> NATRON_SPONGE_BLOT = register("natron_sponge.blot");
    public static final DeferredHolder<SoundEvent, SoundEvent> PRINCIPLE_SELECT = register("libation.principle_select");

    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_PLACE = register("golem.place");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_PICK_UP = register("golem.pick_up");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_HURT = register("golem.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_DEATH = register("golem.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_HEAL = register("golem.heal");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_AXIOM_INSERT = register("golem.axiom_insert");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_AXIOM_REMOVE = register("golem.axiom_remove");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_EQUIP_TOOL = register("golem.equip_tool");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_PICKUP_ITEM = register("golem.pickup_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_STEP = register("golem.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_TOOL_STOW = register("golem.tool_stow");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_TOOL_UNSTOW = register("golem.tool_unstow");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_WOOD_PLACE = register("golem.wood.place");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_WOOD_PICK_UP = register("golem.wood.pick_up");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_WOOD_STEP = register("golem.wood.step");

    public static final DeferredHolder<SoundEvent, SoundEvent> IMPRINT_TABLET_COPY = register("imprint_tablet.copy");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMPRINT_TABLET_PASTE = register("imprint_tablet.paste");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMPRINT_TABLET_CLEAR = register("imprint_tablet.clear");

    public static final DeferredHolder<SoundEvent, SoundEvent> STYLUS_SELECT = register("stylus.select");
    public static final DeferredHolder<SoundEvent, SoundEvent> STYLUS_DESELECT = register("stylus.deselect");
    public static final DeferredHolder<SoundEvent, SoundEvent> STYLUS_CARVE = register("stylus.carve");
    public static final DeferredHolder<SoundEvent, SoundEvent> STYLUS_PICK_UP_CORNER = register("stylus.pick_up_corner");
    public static final DeferredHolder<SoundEvent, SoundEvent> STYLUS_ADJUST_HEIGHT = register("stylus.adjust_height");
    public static final DeferredHolder<SoundEvent, SoundEvent> STYLUS_ERASE = register("stylus.erase");
    public static final DeferredHolder<SoundEvent, SoundEvent> STYLUS_LINK_SUPPLY = register("stylus.link_supply");
    public static final DeferredHolder<SoundEvent, SoundEvent> STYLUS_LINK_DEPOSIT = register("stylus.link_deposit");
    public static final DeferredHolder<SoundEvent, SoundEvent> STYLUS_LINK_BOTH = register("stylus.link_both");
    public static final DeferredHolder<SoundEvent, SoundEvent> STYLUS_UNLINK = register("stylus.unlink");
    public static final DeferredHolder<SoundEvent, SoundEvent> STYLUS_FAIL = register("stylus.fail");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, SoundEvent::createVariableRangeEvent);
    }
}
