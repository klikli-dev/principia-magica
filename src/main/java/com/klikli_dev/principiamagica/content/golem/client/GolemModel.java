// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class GolemModel extends DefaultedEntityGeoModel<GolemEntity> {
    public static final DataTicket<Identifier> TEXTURE = DataTicket.create("golem_texture", Identifier.class);

    public GolemModel() {
        super(PrincipiaMagica.loc("golem"));
    }

    @Override
    public void addAdditionalStateData(GolemEntity animatable, @Nullable Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);

        renderState.addGeckolibData(TEXTURE, animatable.getSpec().material().value().texture());
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, GolemMaterialRegistry.WOOD.value().texture());
    }
}
