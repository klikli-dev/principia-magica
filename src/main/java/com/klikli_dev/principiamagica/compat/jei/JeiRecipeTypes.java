// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.jei;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.LibationRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import mezz.jei.api.recipe.types.IRecipeHolderType;

public final class JeiRecipeTypes {

    public static final IRecipeHolderType<LibationRecipe> LIBATION = IRecipeHolderType.create(PrincipiaMagica.loc("libation"));
    public static final IRecipeHolderType<CuringRecipe> WABET_CURING = IRecipeHolderType.create(PrincipiaMagica.loc("wabet_curing"));
    public static final IRecipeHolderType<CuringRecipe> STEEPING_JAR_CURING = IRecipeHolderType.create(PrincipiaMagica.loc("steeping_jar_curing"));
    public static final IRecipeHolderType<SteepingRecipe> STEEPING = IRecipeHolderType.create(PrincipiaMagica.loc("steeping"));
    public static final IRecipeHolderType<DryingRecipe> DRYING = IRecipeHolderType.create(PrincipiaMagica.loc("drying"));

    private JeiRecipeTypes() {
    }
}
