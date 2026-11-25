// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.modonomicon.data.BookPageType;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.compat.modonomicon.PrincipiaModonomiconPageTypes;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class BookDryingRecipePage extends PrincipiaRecipePage<DryingRecipe> {
    public static final Identifier ID = PrincipiaMagica.loc("drying_recipe");
    public static final MapCodec<BookDryingRecipePage> CODEC = codec(BookDryingRecipePage::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, BookDryingRecipePage> STREAM_CODEC = streamCodec(BookDryingRecipePage::new);

    public BookDryingRecipePage(JsonDataHolder common) {
        super(common);
    }

    public BookDryingRecipePage(NetworkDataHolder common) {
        super(common);
    }

    @Override
    public BookPageType<?> type() {
        return PrincipiaModonomiconPageTypes.DRYING_RECIPE;
    }
}
