// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.principle;

import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Shared GUI rendering of principles, used by the HUD and by recipe viewers.
 */
public final class PrincipleRenderer {

    public static final int ICON_SIZE = 16;

    private PrincipleRenderer() {
    }

    /**
     * Renders the 16x16 icon of the principle.
     */
    public static void renderIcon(GuiGraphicsExtractor graphics, Holder<Principle> principle, int x, int y) {
        Identifier id = PrincipiaMagicaRegistries.PRINCIPLE.getKey(principle.value());
        graphics.blit(RenderPipelines.GUI_TEXTURED, Principle.texture(id), x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }

    /**
     * @return the translated name of the principle, coloured with the principle colour.
     */
    public static Component displayName(Holder<Principle> principle) {
        return principle.value().name();
    }
}
