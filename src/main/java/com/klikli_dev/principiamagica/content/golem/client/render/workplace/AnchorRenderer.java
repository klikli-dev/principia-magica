// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class AnchorRenderer {

    public static final int COLOR = 0xFFFFFF;

    private static final double RING_RADIUS = 0.85;
    private static final int RING_SEGMENTS = 24;

    private AnchorRenderer() {
    }

    /**
     * A rune ring on the ground around the anchor that slowly rotates, with four radial ticks.
     */
    public static void render(WorkplaceGeometry geometry, BlockPos anchor, float alpha, double time) {
        Vec3 centre = new Vec3(anchor.getX() + 0.5, anchor.getY() + 0.03, anchor.getZ() + 0.5);
        double rotation = time * 0.02;
        int color = WorkplaceGeometry.argb(COLOR, alpha * WorkplaceGeometry.pulse(time, 0.1, 0.6F));

        geometry.ring(centre, RING_RADIUS, rotation, RING_SEGMENTS, color);
        for (int i = 0; i < 4; i++) {
            double angle = rotation + i * Math.PI / 2.0;
            Vec3 direction = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            geometry.ribbon(centre.add(direction.scale(0.6)), centre.add(direction.scale(1.05)), color);
        }
    }
}
