// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.jei;

import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class PrincipleIngredientHelper implements IIngredientHelper<PrincipleStack> {

    @Override
    public IIngredientType<PrincipleStack> getIngredientType() {
        return PrincipleIngredientType.TYPE;
    }

    @Override
    public String getDisplayName(PrincipleStack ingredient) {
        return title(ingredient.principle()).getString();
    }

    /**
     * @return the JEI title of a principle, "Principle: " followed by the principle name in its colour.
     */
    public static Component title(Holder<Principle> principle) {
        return principle.value().prefixedName();
    }

    @Override
    public Object getUid(PrincipleStack ingredient, UidContext context) {
        return this.getIdentifier(ingredient);
    }

    @Override
    public long getAmount(PrincipleStack ingredient) {
        return ingredient.amount();
    }

    @Override
    public PrincipleStack copyWithAmount(PrincipleStack ingredient, long amount) {
        return new PrincipleStack(ingredient.principle(), (int) Math.max(1, amount));
    }

    @Override
    public PrincipleStack copyIngredient(PrincipleStack ingredient) {
        return new PrincipleStack(ingredient.principle(), ingredient.amount());
    }

    @Override
    public Identifier getIdentifier(PrincipleStack ingredient) {
        return PrincipiaMagicaRegistries.PRINCIPLE.getKey(ingredient.principle().value());
    }

    @Override
    public String getErrorInfo(PrincipleStack ingredient) {
        return String.valueOf(ingredient);
    }
}
