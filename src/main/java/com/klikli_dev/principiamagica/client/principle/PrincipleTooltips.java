// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.principle;

import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Lists the principles stored in block items that hold principles in their tooltip. Vanilla only shows the
 * components it knows about, so the principle components are added here using the lines of {@link PrincipleContents}.
 */
public final class PrincipleTooltips {

    private PrincipleTooltips() {
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        addPrinciples(event, stack, display, DataComponentRegistry.STORED_PRINCIPLES.get());
    }

    private static void addPrinciples(ItemTooltipEvent event, ItemStack stack, TooltipDisplay display, DataComponentType<PrincipleContents> type) {
        stack.addToTooltip(type, event.getContext(), display, event.getToolTip()::add, event.getFlags());
    }
}
