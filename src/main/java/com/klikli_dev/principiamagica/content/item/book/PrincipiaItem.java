// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.item.book;

import com.klikli_dev.modonomicon.item.ModonomiconCustomItemBase;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.resources.Identifier;

public class PrincipiaItem extends ModonomiconCustomItemBase {
    public static final Identifier PRINCIPIA = PrincipiaMagica.loc("principia");

    public PrincipiaItem(Properties properties) {
        super(PRINCIPIA, properties);
    }
}
