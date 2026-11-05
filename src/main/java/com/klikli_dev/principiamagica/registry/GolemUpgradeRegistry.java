// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.upgrade.GolemUpgrade;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class GolemUpgradeRegistry {
    public static final DeferredRegister<GolemUpgrade> GOLEM_UPGRADES = DeferredRegister.create(PrincipiaMagicaRegistries.Keys.GOLEM_UPGRADE, PrincipiaMagica.MODID);

    public static final DeferredHolder<GolemUpgrade, GolemUpgrade> SWIFTNESS = GOLEM_UPGRADES.register("swiftness", () -> new GolemUpgrade(
            PrincipiaMagica.loc("upgrade/swiftness"),
            List.of(
                    new GolemUpgrade.AttributeEntry(Attributes.MOVEMENT_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    new GolemUpgrade.AttributeEntry(AttributeRegistry.GOLEM_WORK_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )));

    public static final DeferredHolder<GolemUpgrade, GolemUpgrade> CAPACITY = GOLEM_UPGRADES.register("capacity", () -> new GolemUpgrade(
            PrincipiaMagica.loc("upgrade/capacity"),
            List.of(
                    new GolemUpgrade.AttributeEntry(AttributeRegistry.GOLEM_INVENTORY_SLOTS, 3.0, AttributeModifier.Operation.ADD_VALUE)
            )));
}
