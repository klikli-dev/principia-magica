// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.principle.PrincipleResource;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;

public final class PrincipiaMagicaCapabilities {

    private PrincipiaMagicaCapabilities() {
    }

    public static final class Principle {
        public static final BlockCapability<ResourceHandler<PrincipleResource>, @Nullable Direction> BLOCK = BlockCapability.createSided(
                PrincipiaMagica.loc("principle_handler"), ResourceHandler.asClass());

        public static final ItemCapability<ResourceHandler<PrincipleResource>, ItemAccess> ITEM = ItemCapability.create(
                PrincipiaMagica.loc("principle_handler"), ResourceHandler.asClass(), ItemAccess.class);

        private Principle() {
        }
    }
}
