// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.advancement.GolemPlacedTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CriterionTriggerRegistry {
    public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, PrincipiaMagica.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, GolemPlacedTrigger> GOLEM_PLACED = CRITERION_TRIGGERS.register("golem_placed", GolemPlacedTrigger::new);
}
