// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.geckolib.renderer.GeoEntityRenderer;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jspecify.annotations.Nullable;

public class GolemRenderer extends GeoEntityRenderer<GolemEntity, GolemRenderState> {
    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel());

        // The tool in the right hand, the carried item between the hands, and the tool on the back. One layer, see GolemItemLayer.
        this.withRenderLayer(new GolemItemLayer(context, this));
    }

    @Override
    public GolemRenderState createRenderState(GolemEntity animatable, @Nullable Void relatedObject) {
        return new GolemRenderState();
    }
}
