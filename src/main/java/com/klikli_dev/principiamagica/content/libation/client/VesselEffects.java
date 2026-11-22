// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.magicparticleslib.premade.particle.glow.GlowParticleOptions;
import com.klikli_dev.principiamagica.content.libation.LiquidArea;
import com.klikli_dev.principiamagica.content.libation.vessel.CuringVesselBlockEntity;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringEnvironment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only particles of the Wabet Basin and the Steeping Jar.
 */
public final class VesselEffects {

    private static final int GOLD = 0xFFD36B;
    private static final int SHADOW = 0x1E1426;
    private static final float GLINT_CHANCE = 0.12F;
    private static final float GLINT_SIZE = 0.04F;
    private static final int GLINT_AGE = 30;
    private static final float WISP_CHANCE = 0.08F;
    private static final float WISP_SCALE = 0.8F;
    private static final float WISP_RADIUS = 3.5F;
    private static final float TAU = (float) (Math.PI * 2.0);

    private VesselEffects() {
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CuringVesselBlockEntity vessel) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }

        RandomSource random = level.getRandom();
        int blend = vessel.getPrinciples().blendedColor();

        if (!vessel.isCuring()) {
            return;
        }

        if (vessel.properties().environment() == CuringEnvironment.DARK) {
            if (random.nextFloat() < WISP_CHANCE) {
                spawnWisp(clientLevel, random, pos, blend);
            }
        } else if (random.nextFloat() < GLINT_CHANCE) {
            spawnGlint(clientLevel, random, pos, vessel.properties().liquid(), vessel.getPrinciples().total(), blend);
        }
    }

    /**
     * Golden glint rising from the surface of the Wabet Basin while curing in sunlight.
     */
    private static void spawnGlint(ClientLevel level, RandomSource random, BlockPos pos, LiquidArea liquid, int total, int blend) {
        double x = pos.getX() + Mth.lerp(random.nextFloat(), liquid.minX(), liquid.maxX()) * LibationGeometry.PIXEL;
        double z = pos.getZ() + Mth.lerp(random.nextFloat(), liquid.minZ(), liquid.maxZ()) * LibationGeometry.PIXEL;
        double y = pos.getY() + liquid.surfaceY(total);
        int color = LibationEffects.mix(GOLD, blend, 0.35F);
        level.addParticle(GlowParticleOptions.of(0xFF000000 | color).size(GLINT_SIZE).age(GLINT_AGE),
                x, y, z, 0.0, 0.02 + random.nextDouble() * 0.02, 0.0);
    }

    /**
     * Faint dark wisp rising from the mouth of the curing Steeping Jar.
     */
    private static void spawnWisp(ClientLevel level, RandomSource random, BlockPos pos, int blend) {
        float angle = random.nextFloat() * TAU;
        float x = 8.0F + Mth.cos(angle) * WISP_RADIUS;
        float z = 8.0F + Mth.sin(angle) * WISP_RADIUS;
        float y = 14.0F + random.nextFloat() * 2.0F;
        Vec3 at = LibationGeometry.toWorld(pos, Direction.NORTH, x, y, z);
        level.addParticle(new DustParticleOptions(LibationEffects.mix(SHADOW, blend, 0.25F), WISP_SCALE), at.x, at.y, at.z, 0.0, 0.01, 0.0);
    }
}
