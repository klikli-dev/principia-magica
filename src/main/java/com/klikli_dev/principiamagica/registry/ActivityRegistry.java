// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ActivityRegistry {
    public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(Registries.ACTIVITY, PrincipiaMagica.MODID);

    public static final DeferredHolder<Activity, Activity> FOLLOW = ACTIVITIES.register("follow", () -> new Activity(PrincipiaMagica.loc("follow").toString()));
    public static final DeferredHolder<Activity, Activity> STAY = ACTIVITIES.register("stay", () -> new Activity(PrincipiaMagica.loc("stay").toString()));
}
