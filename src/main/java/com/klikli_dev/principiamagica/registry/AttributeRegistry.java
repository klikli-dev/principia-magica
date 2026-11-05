// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AttributeRegistry {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, PrincipiaMagica.MODID);

    public static final DeferredHolder<Attribute, Attribute> GOLEM_WORK_SPEED = ATTRIBUTES.register("golem_work_speed", () ->
            new RangedAttribute("attribute.principiamagica.golem_work_speed", 1.0, 0.0, 16.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> GOLEM_WORK_RANGE = ATTRIBUTES.register("golem_work_range", () ->
            new RangedAttribute("attribute.principiamagica.golem_work_range", 16.0, 1.0, 64.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> GOLEM_INVENTORY_SLOTS = ATTRIBUTES.register("golem_inventory_slots", () ->
            new RangedAttribute("attribute.principiamagica.golem_inventory_slots", 3.0, 0.0, 9.0).setSyncable(true));
}
