// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

public class DataComponentRegistry {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, PrincipiaMagica.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GolemSpec>> GOLEM_SPEC = DATA_COMPONENTS.registerComponentType("golem_spec", builder -> builder
            .persistent(GolemSpec.CODEC)
            .networkSynchronized(GolemSpec.STREAM_CODEC)
            .cacheEncoding()
    );

    /**
     * Relative workplace (positions relative to the golem's anchor) of a golem picked up with the stylus.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Workplace>> GOLEM_WORKPLACE = DATA_COMPONENTS.registerComponentType("golem_workplace", builder -> builder
            .persistent(Workplace.CODEC)
            .networkSynchronized(Workplace.STREAM_CODEC)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> STYLUS_SELECTION = DATA_COMPONENTS.registerComponentType("stylus_selection", builder -> builder
            .persistent(UUIDUtil.CODEC)
            .networkSynchronized(UUIDUtil.STREAM_CODEC)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> STYLUS_PENDING_CORNER = DATA_COMPONENTS.registerComponentType("stylus_pending_corner", builder -> builder
            .persistent(GlobalPos.CODEC)
            .networkSynchronized(GlobalPos.STREAM_CODEC)
    );

    /**
     * Relative workplace copied from a golem with the stylus (positions relative to the source golem's anchor).
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Workplace>> STYLUS_CLIPBOARD = DATA_COMPONENTS.registerComponentType("stylus_clipboard", builder -> builder
            .persistent(Workplace.CODEC)
            .networkSynchronized(Workplace.STREAM_CODEC)
    );
}
