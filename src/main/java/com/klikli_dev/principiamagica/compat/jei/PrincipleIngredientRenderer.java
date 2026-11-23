// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.jei;

import com.klikli_dev.principiamagica.client.principle.PrincipleRenderer;
import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class PrincipleIngredientRenderer implements IIngredientRenderer<PrincipleStack> {

    /**
     * Adds a principle to a recipe slot. The amount is recipe information, so the slot shows it in its tooltip. The entries of the
     * ingredient list and the bookmarks do not, since their amounts mean nothing.
     */
    public static void addRecipeSlot(IRecipeSlotBuilder slot, PrincipleStack stack) {
        Component amount = Component.translatable("jei.principiamagica.principle.amount", stack.amountText()).withStyle(ChatFormatting.DARK_GRAY);
        slot.add(PrincipleIngredientType.TYPE, stack).addRichTooltipCallback((view, tooltip) -> tooltip.add(amount));
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, PrincipleStack ingredient) {
        PrincipleRenderer.renderIcon(graphics, ingredient.principle(), 0, 0);
    }

    @Override
    @SuppressWarnings("removal")
    public List<Component> getTooltip(PrincipleStack ingredient, TooltipFlag tooltipFlag) {
        Principle principle = ingredient.principle().value();

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(PrincipleIngredientHelper.title(ingredient.principle()));
        tooltip.add(Component.translatable(principle.getDescriptionId() + ".description").withStyle(ChatFormatting.GRAY));
        if (!principle.isPrimal()) {
            tooltip.add(composition(principle));
        }
        return tooltip;
    }

    private static Component composition(Principle principle) {
        MutableComponent composition = Component.empty();
        for (int i = 0; i < principle.components().size(); i++) {
            if (i > 0) {
                composition.append(" + ");
            }
            composition.append(PrincipleRenderer.displayName(principle.components().get(i)));
        }
        return composition;
    }
}
