// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.PrincipleDripOptions;
import com.klikli_dev.principiamagica.content.libation.PrincipleSplashOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * Tinted drip that hangs at the spout, falls and lands at the surface of the recipient with a splash.
 * Uses the vanilla drip sprites in the order hang, fall, land.
 */
public class PrincipleDripParticle extends SingleQuadParticle {

    private static final int HANG_TICKS = 5;
    private static final int LAND_TICKS = 12;
    private static final int MAX_FALL_TICKS = 200;
    private static final int PHASE_HANG = 0;
    private static final int PHASE_FALL = 1;
    private static final int PHASE_LAND = 2;
    private static final int PHASE_MAX = PHASE_LAND;
    private static final float COLOR_SCALE = 1.0F / 255.0F;
    /**
     * Gravity acting on falling drips in blocks per tick squared.
     */
    private static final float GRAVITY = 0.04F;

    private final SpriteSet sprites;
    private final float landY;
    private final int color;
    private final int hangTicks;
    private boolean landed;

    private PrincipleDripParticle(ClientLevel level, double x, double y, double z, PrincipleDripOptions options, SpriteSet sprites, Vec3 velocity) {
        super(level, x, y, z, sprites.get(PHASE_HANG, PHASE_MAX));
        this.sprites = sprites;
        this.landY = options.landY();
        this.color = options.color();
        this.hangTicks = velocity.lengthSqr() > 0.0 ? 0 : HANG_TICKS;
        this.lifetime = this.hangTicks + MAX_FALL_TICKS;
        this.xd = velocity.x;
        this.yd = velocity.y;
        this.zd = velocity.z;
        this.hasPhysics = false;
        int color = options.color();
        this.setColor(((color >> 16) & 0xFF) * COLOR_SCALE, ((color >> 8) & 0xFF) * COLOR_SCALE, (color & 0xFF) * COLOR_SCALE);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (++this.age >= this.lifetime) {
            this.remove();
            return;
        }
        if (this.landed) {
            return;
        }
        if (this.age <= this.hangTicks) {
            this.setSprite(this.sprites.get(PHASE_HANG, PHASE_MAX));
            return;
        }

        this.setSprite(this.sprites.get(PHASE_FALL, PHASE_MAX));
        this.yd -= GRAVITY;
        this.move(this.xd, this.yd, this.zd);
        if (this.y <= this.landY) {
            this.land();
        }
    }

    private void land() {
        this.landed = true;
        this.y = this.landY;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.setSprite(this.sprites.get(PHASE_LAND, PHASE_MAX));
        this.lifetime = this.age + LAND_TICKS;
        this.level.addParticle(new PrincipleSplashOptions(this.color), this.x, this.y, this.z, 0.0, 0.1, 0.0);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    public static class Provider implements ParticleProvider<PrincipleDripOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(PrincipleDripOptions options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new PrincipleDripParticle(level, x, y, z, options, this.sprites, new Vec3(xAux, yAux, zAux));
        }
    }
}
