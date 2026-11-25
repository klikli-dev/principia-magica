// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon;

import com.klikli_dev.modonomicon.client.render.page.PageRendererRegistry;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookCuringRecipePage;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookCuringRecipePageRenderer;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookDryingRecipePage;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookDryingRecipePageRenderer;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookLibationRecipePage;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookLibationRecipePageRenderer;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookSteepingRecipePage;
import com.klikli_dev.principiamagica.compat.modonomicon.page.BookSteepingRecipePageRenderer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class PrincipiaModonomiconPageRenderers {

    private PrincipiaModonomiconPageRenderers() {
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        PageRendererRegistry.registerPageRenderer(
                BookCuringRecipePage.ID,
                p -> new BookCuringRecipePageRenderer((BookCuringRecipePage) p));

        PageRendererRegistry.registerPageRenderer(
                BookSteepingRecipePage.ID,
                p -> new BookSteepingRecipePageRenderer((BookSteepingRecipePage) p));

        PageRendererRegistry.registerPageRenderer(
                BookDryingRecipePage.ID,
                p -> new BookDryingRecipePageRenderer((BookDryingRecipePage) p));

        PageRendererRegistry.registerPageRenderer(
                BookLibationRecipePage.ID,
                p -> new BookLibationRecipePageRenderer((BookLibationRecipePage) p));
    }
}
