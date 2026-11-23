// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.klikli_dev.principiamagica.content.golem.item.GolemItem;

public class GolemItemRenderer extends GeoItemRenderer<GolemItem> {

    /**
     * GeckoLib's default item offset lifts the model by 0.51 blocks, which suits models standing on the floor of the item cube.
     * The golem is about 1.2 blocks tall with its feet at the origin, so it is moved down to be centered in the item cube.
     */
    private static final float CENTER_Y = -0.1F;

    public GolemItemRenderer() {
        super(new GolemItemModel());
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> renderPassInfo) {
        renderPassInfo.poseStack().translate(0.5F, CENTER_Y, 0.5F);
    }
}
