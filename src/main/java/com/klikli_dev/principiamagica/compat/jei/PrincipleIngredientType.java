// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.jei;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import mezz.jei.api.ingredients.IIngredientType;

public final class PrincipleIngredientType {

    public static final IIngredientType<PrincipleStack> TYPE = new IIngredientType<>() {
        @Override
        public Class<? extends PrincipleStack> getIngredientClass() {
            return PrincipleStack.class;
        }

        @Override
        public String getUid() {
            return PrincipiaMagica.loc("principle").toString();
        }
    };

    private PrincipleIngredientType() {
    }
}
