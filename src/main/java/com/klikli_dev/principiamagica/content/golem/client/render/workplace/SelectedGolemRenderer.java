// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import net.minecraft.world.phys.Vec3;

public final class SelectedGolemRenderer {

    public static final int COLOR = 0xFFFFFF;

    private SelectedGolemRenderer() {
    }

    /**
     * A slowly counter-rotating ring under the feet of the selected golem.
     */
    public static void render(WorkplaceGeometry geometry, Vec3 feet, float width, float alpha, double time) {
        double radius = Math.max(0.35, width * 0.75);
        int color = WorkplaceGeometry.argb(COLOR, alpha * 0.7F * WorkplaceGeometry.pulse(time, 0.15, 0.5F));
        geometry.ring(feet.add(0.0, 0.03, 0.0), radius, -time * 0.03, 16, color);
    }
}
