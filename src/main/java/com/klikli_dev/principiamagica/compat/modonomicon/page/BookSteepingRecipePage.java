// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.modonomicon.data.BookPageType;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.compat.modonomicon.PrincipiaModonomiconPageTypes;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class BookSteepingRecipePage extends PrincipiaRecipePage<SteepingRecipe> {
    public static final Identifier ID = PrincipiaMagica.loc("steeping_recipe");
    public static final MapCodec<BookSteepingRecipePage> CODEC = codec(BookSteepingRecipePage::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, BookSteepingRecipePage> STREAM_CODEC = streamCodec(BookSteepingRecipePage::new);

    public BookSteepingRecipePage(JsonDataHolder common) {
        super(common);
    }

    public BookSteepingRecipePage(NetworkDataHolder common) {
        super(common);
    }

    @Override
    public BookPageType<?> type() {
        return PrincipiaModonomiconPageTypes.STEEPING_RECIPE;
    }
}
