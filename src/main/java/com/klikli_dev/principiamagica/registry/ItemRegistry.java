// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.item.GolemBatonItem;
import com.klikli_dev.principiamagica.content.golem.item.GolemCoreItem;
import com.klikli_dev.principiamagica.content.golem.item.GolemItem;
import com.klikli_dev.principiamagica.content.golem.item.GolemUpgradeItem;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingJob;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.LumberjackJob;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PrincipiaMagica.MODID);

    public static final DeferredItem<GolemItem> GOLEM = ITEMS.registerItem("golem", p -> new GolemItem(p.stacksTo(16)));

    public static final DeferredItem<GolemBatonItem> GOLEM_BATON = ITEMS.registerItem("golem_baton", p -> new GolemBatonItem(p.stacksTo(1)));

    public static final DeferredItem<GolemCoreItem> FARMING_CORE = ITEMS.registerItem("farming_core", p -> new GolemCoreItem(p.stacksTo(1), FarmingJob.KEY));

    public static final DeferredItem<GolemCoreItem> LUMBERJACK_CORE = ITEMS.registerItem("lumberjack_core", p -> new GolemCoreItem(p.stacksTo(1), LumberjackJob.KEY));

    public static final DeferredItem<GolemUpgradeItem> SWIFTNESS_UPGRADE = ITEMS.registerItem("swiftness_upgrade",
            p -> new GolemUpgradeItem(p.stacksTo(16), GolemUpgradeRegistry.SWIFTNESS.getKey()));

    public static final DeferredItem<GolemUpgradeItem> CAPACITY_UPGRADE = ITEMS.registerItem("capacity_upgrade",
            p -> new GolemUpgradeItem(p.stacksTo(16), GolemUpgradeRegistry.CAPACITY.getKey()));
}
