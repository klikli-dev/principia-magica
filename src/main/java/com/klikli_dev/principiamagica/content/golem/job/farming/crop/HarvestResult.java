// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job.farming.crop;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @param drops   the items the harvest produced, to be collected into the golem's inventory
 * @param removed whether the block was removed (and so needs replanting) instead of being reset in place
 */
public record HarvestResult(List<ItemStack> drops, boolean removed) {
}
