// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import net.minecraft.world.phys.AABB;

public final class ZoneRenderer {

    public static final int COLOR = 0x55FFFF;

    private ZoneRenderer() {
    }

    public static void render(WorkplaceGeometry geometry, WorkZone zone, float alpha, double time) {
        AABB box = zone.aabb().inflate(0.01);
        float pulse = WorkplaceGeometry.pulse(time, 0.12, 0.7F);

        geometry.boxFaces(box, WorkplaceGeometry.argb(COLOR, alpha * 0.06F * pulse));
        geometry.boxEdges(box, WorkplaceGeometry.argb(COLOR, alpha * pulse));

        int corner = WorkplaceGeometry.argb(COLOR, alpha);
        for (int i = 0; i < 8; i++) {
            geometry.glyph(WorkplaceGeometry.boxCorner(box, i), 0.3F, WorkplaceGeometry.Glyph.PLUS, corner);
        }
    }
}
