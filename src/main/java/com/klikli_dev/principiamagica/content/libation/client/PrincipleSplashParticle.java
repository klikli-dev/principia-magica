// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.PrincipleSplashOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;

/**
 * Tinted water splash for principle colors. Behaves like {@code SplashParticle}, which has a private constructor, so the same
 * gravity and initial velocity are applied here. It uses neutral white sprites (see the particle description provider),
 * because the vanilla splash sprites are blue and would darken the tint.
 */
public class PrincipleSplashParticle extends WaterDropParticle {

    private static final float COLOR_SCALE = 1.0F / 255.0F;
    private static final float GRAVITY = 0.04F;

    private PrincipleSplashParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za, int color, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
        this.gravity = GRAVITY;
        if (ya == 0.0 && (xa != 0.0 || za != 0.0)) {
            this.xd = xa;
            this.yd = 0.1;
            this.zd = za;
        }
        this.setColor(((color >> 16) & 0xFF) * COLOR_SCALE, ((color >> 8) & 0xFF) * COLOR_SCALE, (color & 0xFF) * COLOR_SCALE);
    }

    public static class Provider implements ParticleProvider<PrincipleSplashOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(PrincipleSplashOptions options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new PrincipleSplashParticle(level, x, y, z, xAux, yAux, zAux, options.color(), this.sprites.get(random));
        }
    }
}
