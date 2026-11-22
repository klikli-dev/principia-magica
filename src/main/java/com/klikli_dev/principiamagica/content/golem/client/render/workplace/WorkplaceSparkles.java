// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.magicparticleslib.premade.particle.glow.GlowParticleOptions;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Client-only glow particles that travel along the edges of the zones of the selected golem.
 */
public final class WorkplaceSparkles {

    private static final float SIZE = 0.06F;
    private static final int AGE = 20;
    private static final double SPEED = 0.03;

    private WorkplaceSparkles() {
    }

    public static void spawn(ClientLevel level, List<WorkZone> zones) {
        if (zones.isEmpty()) {
            return;
        }

        RandomSource random = level.getRandom();
        AABB box = zones.get(random.nextInt(zones.size())).aabb();
        int axis = random.nextInt(3);
        double direction = random.nextBoolean() ? SPEED : -SPEED;

        double x = axis == 0 ? Mth.lerp(random.nextDouble(), box.minX, box.maxX) : randomEdge(random, box.minX, box.maxX);
        double y = axis == 1 ? Mth.lerp(random.nextDouble(), box.minY, box.maxY) : randomEdge(random, box.minY, box.maxY);
        double z = axis == 2 ? Mth.lerp(random.nextDouble(), box.minZ, box.maxZ) : randomEdge(random, box.minZ, box.maxZ);

        GlowParticleOptions options = GlowParticleOptions.of(0xFF000000 | ZoneRenderer.COLOR).size(SIZE).age(AGE);
        level.addParticle(options, x, y, z,
                axis == 0 ? direction : 0.0,
                axis == 1 ? direction : 0.0,
                axis == 2 ? direction : 0.0);
    }

    private static double randomEdge(RandomSource random, double min, double max) {
        return random.nextBoolean() ? min : max;
    }
}
