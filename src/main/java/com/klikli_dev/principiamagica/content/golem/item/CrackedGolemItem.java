// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.entity.GolemStorage;
import com.klikli_dev.principiamagica.content.golem.recipe.RepairCrackedGolemRecipe;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * The remains of a golem that died. Keeps its spec (axiom, upgrades), its workplace relative to the golem's anchor and
 * its inventory and equipment, so it can be repaired with {@link RepairCrackedGolemRecipe}.
 */
public class CrackedGolemItem extends Item {

    public CrackedGolemItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createStack(GolemSpec spec, Workplace relativeWorkplace, GolemStorage storage) {
        ItemStack stack = new ItemStack(ItemRegistry.CRACKED_GOLEM.get());
        stack.set(DataComponentRegistry.GOLEM_SPEC.get(), spec);
        stack.set(DataComponentRegistry.GOLEM_WORKPLACE.get(), relativeWorkplace);
        stack.set(DataComponentRegistry.GOLEM_STORAGE.get(), storage);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        GolemSpec spec = stack.get(DataComponentRegistry.GOLEM_SPEC.get());
        if (spec == null) {
            return super.getName(stack);
        }

        return Component.translatable("item.principiamagica.cracked_golem.named", Component.translatable(spec.material().value().getDescriptionId()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
        GolemSpec spec = stack.get(DataComponentRegistry.GOLEM_SPEC.get());
        if (spec == null) {
            return;
        }

        GolemItem.appendSpecTooltip(spec, adder);

        adder.accept(spec.job()
                .map(job -> Component.translatable("tooltip.principiamagica.cracked_golem.axiom", job.value().getName()).withStyle(ChatFormatting.GRAY))
                .orElseGet(() -> Component.translatable("tooltip.principiamagica.cracked_golem.no_axiom").withStyle(ChatFormatting.GRAY)));

        GolemStorage storage = stack.get(DataComponentRegistry.GOLEM_STORAGE.get());
        if (storage != null && storage.itemCount() > 0) {
            adder.accept(Component.translatable("tooltip.principiamagica.cracked_golem.carries", storage.itemCount()).withStyle(ChatFormatting.GRAY));
        }

        adder.accept(Component.translatable("tooltip.principiamagica.cracked_golem.repair").withStyle(ChatFormatting.DARK_GRAY));
    }
}
