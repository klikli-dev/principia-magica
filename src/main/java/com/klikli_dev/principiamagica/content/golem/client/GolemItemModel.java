// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.item.GolemItem;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * The golem entity model, used for the golem item. The texture follows the material of the item's {@link GolemSpec}.
 */
public class GolemItemModel extends DefaultedEntityGeoModel<GolemItem> {

    public GolemItemModel() {
        super(PrincipiaMagica.loc("golem"));
    }

    @Override
    public void addAdditionalStateData(GolemItem animatable, @Nullable Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);

        if (relatedObject instanceof GeoItemRenderer.RenderData renderData) {
            ItemStack stack = renderData.itemStack();
            GolemSpec spec = stack.get(DataComponentRegistry.GOLEM_SPEC.get());
            if (spec != null) {
                renderState.addGeckolibData(GolemModel.TEXTURE, spec.material().value().texture());
            }
        }
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(GolemModel.TEXTURE, GolemMaterialRegistry.WOOD.value().texture());
    }
}
