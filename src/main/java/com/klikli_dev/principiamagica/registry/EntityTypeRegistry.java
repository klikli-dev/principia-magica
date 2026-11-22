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

    private static final float GOLEM_WIDTH = 0.7F;
    private static final float GOLEM_HEIGHT = 1.0F;
    private static final int GOLEM_TRACKING_RANGE_CHUNKS = 10;
    private static final int GOLEM_UPDATE_INTERVAL_TICKS = 3;

    public static final DeferredHolder<EntityType<?>, EntityType<GolemEntity>> GOLEM = ENTITY_TYPES.registerEntityType("golem", GolemEntity::new, MobCategory.MISC, builder -> builder
            .sized(GOLEM_WIDTH, GOLEM_HEIGHT)
            .clientTrackingRange(GOLEM_TRACKING_RANGE_CHUNKS)
            .updateInterval(GOLEM_UPDATE_INTERVAL_TICKS)
    );
}
