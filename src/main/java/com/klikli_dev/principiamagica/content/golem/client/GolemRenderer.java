// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jspecify.annotations.Nullable;

public class GolemRenderer extends GeoEntityRenderer<GolemEntity, GolemRenderState> {
    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel());

        // The hand item, the tool or the carried item, on the right hand bone.
        this.withRenderLayer(new ItemInHandGeoLayer<>(context, this, "RightHandItem", null));
        this.withRenderLayer(new GolemBackItemLayer(context, this));
    }

    @Override
    public GolemRenderState createRenderState(GolemEntity animatable, @Nullable Void relatedObject) {
        return new GolemRenderState();
    }
}
