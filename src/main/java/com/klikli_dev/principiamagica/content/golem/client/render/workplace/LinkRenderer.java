// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class LinkRenderer {

    public static final int SUPPLY_COLOR = 0x3366FF;
    public static final int DEPOSIT_COLOR = 0xFF8800;
    public static final int BOTH_COLOR = 0x33FF55;

    private LinkRenderer() {
    }

    public static void render(WorkplaceGeometry geometry, ContainerLink link, float alpha, double time) {
        int color = colorOf(link.role());
        float pulse = WorkplaceGeometry.pulse(time, 0.12, 0.55F);
        AABB box = new AABB(link.pos()).inflate(0.01);

        geometry.boxFaces(box, WorkplaceGeometry.argb(color, alpha * 0.05F * pulse));
        geometry.boxEdges(box, WorkplaceGeometry.argb(color, alpha * pulse));

        Vec3 glyphCentre = Vec3.atCenterOf(link.pos()).add(0.0, 0.9 + 0.08 * Math.sin(time * 0.08), 0.0);
        geometry.glyph(glyphCentre, 0.3F, glyphOf(link.role()), WorkplaceGeometry.argb(color, alpha));
    }

    public static int colorOf(ContainerRole role) {
        return switch (role) {
            case SUPPLY -> SUPPLY_COLOR;
            case DEPOSIT -> DEPOSIT_COLOR;
            case BOTH -> BOTH_COLOR;
        };
    }

    private static WorkplaceGeometry.Glyph glyphOf(ContainerRole role) {
        return switch (role) {
            case SUPPLY -> WorkplaceGeometry.Glyph.PLUS;
            case DEPOSIT -> WorkplaceGeometry.Glyph.CROSS;
            case BOTH -> WorkplaceGeometry.Glyph.STAR;
        };
    }
}
