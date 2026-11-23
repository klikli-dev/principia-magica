// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class TargetBeamRenderer {

    public static final int COLOR = 0xFFFF00;

    private TargetBeamRenderer() {
    }

    /**
     * A dashed beam from the golem to its current work target, with a cross marking the target block.
     */
    public static void render(WorkplaceGeometry geometry, Vec3 from, BlockPos target, float alpha, double time) {
        Vec3 to = Vec3.atCenterOf(target);
        geometry.beam(from, to, WorkplaceGeometry.argb(COLOR, alpha * WorkplaceGeometry.pulse(time, 0.2, 0.6F)));
        geometry.glyph(to, 0.35F, WorkplaceGeometry.Glyph.CROSS, WorkplaceGeometry.argb(COLOR, alpha));
    }
}
