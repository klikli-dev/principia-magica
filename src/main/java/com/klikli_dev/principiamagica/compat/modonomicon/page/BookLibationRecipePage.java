// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.modonomicon.data.BookPageType;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.compat.modonomicon.PrincipiaModonomiconPageTypes;
import com.klikli_dev.principiamagica.content.principle.recipe.LibationRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class BookLibationRecipePage extends PrincipiaRecipePage<LibationRecipe> {
    public static final Identifier ID = PrincipiaMagica.loc("libation_recipe");
    public static final MapCodec<BookLibationRecipePage> CODEC = codec(BookLibationRecipePage::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, BookLibationRecipePage> STREAM_CODEC = streamCodec(BookLibationRecipePage::new);

    public BookLibationRecipePage(JsonDataHolder common) {
        super(common);
    }

    public BookLibationRecipePage(NetworkDataHolder common) {
        super(common);
    }

    @Override
    public BookPageType<?> type() {
        return PrincipiaModonomiconPageTypes.LIBATION_RECIPE;
    }
}
