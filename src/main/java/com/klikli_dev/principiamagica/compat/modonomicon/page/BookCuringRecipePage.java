// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.modonomicon.data.BookPageType;
import com.klikli_dev.principiamagica.compat.modonomicon.PrincipiaModonomiconPageTypes;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class BookCuringRecipePage extends PrincipiaRecipePage<CuringRecipe> {
    public static final Identifier ID = PrincipiaMagica.loc("curing_recipe");
    public static final MapCodec<BookCuringRecipePage> CODEC = codec(BookCuringRecipePage::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, BookCuringRecipePage> STREAM_CODEC = streamCodec(BookCuringRecipePage::new);

    public BookCuringRecipePage(JsonDataHolder common) {
        super(common);
    }

    public BookCuringRecipePage(NetworkDataHolder common) {
        super(common);
    }

    @Override
    public BookPageType<?> type() {
        return PrincipiaModonomiconPageTypes.CURING_RECIPE;
    }
}
