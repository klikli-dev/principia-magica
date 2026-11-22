// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, PrincipiaMagica.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GolemMenu>> GOLEM = MENU_TYPES.register("golem",
            () -> IMenuTypeExtension.<GolemMenu>create((windowId, inventory, data) -> new GolemMenu(windowId, inventory, data.readVarInt())));
}
