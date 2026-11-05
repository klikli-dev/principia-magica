// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.gametest;

import com.klikli_dev.principiamagica.datagen.advancement.PrincipiaMagicaAdvancementSubProvider;
import net.minecraft.gametest.framework.GameTestHelper;

public class ProgressionGameTests {

    public static void clayAdvancementAndPrincipiaRecipeExist(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        helper.assertTrue(server.getAdvancements().get(PrincipiaMagicaAdvancementSubProvider.THE_CLAY_REMEMBERS) != null,
                "Advancement the_clay_remembers is missing");
        helper.assertTrue(server.getRecipeManager().byKey(PrincipiaMagicaAdvancementSubProvider.PRINCIPIA_RECIPE).isPresent(),
                "Recipe principia is missing");
        helper.succeed();
    }
}
