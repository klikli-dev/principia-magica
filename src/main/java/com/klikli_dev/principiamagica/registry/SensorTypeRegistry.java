// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.ai.sensor.WorkTargetSensor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SensorTypeRegistry {
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(Registries.SENSOR_TYPE, PrincipiaMagica.MODID);

    public static final DeferredHolder<SensorType<?>, SensorType<WorkTargetSensor>> WORK_TARGETS = SENSOR_TYPES.register("work_targets", () -> new SensorType<>(WorkTargetSensor::new));
}
