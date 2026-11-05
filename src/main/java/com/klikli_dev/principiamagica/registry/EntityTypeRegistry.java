// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityTypeRegistry {
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(PrincipiaMagica.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<GolemEntity>> GOLEM = ENTITY_TYPES.registerEntityType("golem", GolemEntity::new, MobCategory.MISC, builder -> builder
            .sized(0.7F, 1.0F)
            .clientTrackingRange(10)
            .updateInterval(3)
    );
}
