// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.magicparticleslib.premade.particle.glow.GlowParticleOptions;
import com.klikli_dev.principiamagica.content.libation.LibationClientEffects;
import com.klikli_dev.principiamagica.content.libation.LiquidArea;
import com.klikli_dev.principiamagica.content.libation.PrincipleDripOptions;
import com.klikli_dev.principiamagica.content.libation.PrincipleSplashOptions;
import com.klikli_dev.principiamagica.content.libation.drying.DryingSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.spout.LibationSpoutBlock;
import com.klikli_dev.principiamagica.content.libation.spout.SpoutBlockEntity;
import com.klikli_dev.principiamagica.content.libation.trough.CatchTroughBlockEntity;
import com.klikli_dev.principiamagica.content.libation.vessel.CuringVesselBlockEntity;
import com.klikli_dev.principiamagica.content.principle.PrincipleContainer;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;


/**
 * Client-only particles of the libation slab, drying slab, catch trough and spouts. Driven by the synced block entity state.
 */
public final class LibationEffects implements LibationClientEffects.Handler {

    private static final float FIZZ_CHANCE = 0.5F;
    private static final float DISSOLVING_SOUND_CHANCE = 0.08F;
    private static final float FIZZ_SIZE = 0.05F;
    private static final int FIZZ_AGE = 14;
    private static final float STEAM_CHANCE = 0.06F;
    private static final int BURST_COUNT = 18;
    private static final float BURST_SIZE = 0.08F;
    private static final int BURST_AGE = 24;
    private static final int BURST_SPLASH_COUNT = 8;

    @Override
    public void tickSlab(Level level, BlockPos pos, BlockState state, LibationSlabBlockEntity slab) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }

        RandomSource random = level.getRandom();
        if (slab.isDissolving()) {
            if (random.nextFloat() < FIZZ_CHANCE) {
                spawnFizz(clientLevel, pos, slab.getDissolveColor());
            }
            if (random.nextFloat() < DISSOLVING_SOUND_CHANCE) {
                clientLevel.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5,
                        SoundRegistry.LIBATION_SLAB_DISSOLVING.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    /**
     * Drips from an open spout or the tap of a vessel. Each drip hangs from the nozzle and falls straight down into the cell below.
     */
    @Override
    public void tickSpout(Level level, BlockPos pos, BlockState state, SpoutBlockEntity spout) {
        if (!(level instanceof ClientLevel clientLevel) || !spout.getDrips().tick()) {
            return;
        }

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        LibationGeometry.Point drip = state.getBlock() instanceof LibationSpoutBlock ? LibationGeometry.SPOUT_DRIP : LibationGeometry.VESSEL_DRIP;
        spawnDrip(clientLevel, pos, facing, drip, hostColor(level, pos, facing));
    }

    @Override
    public void tickVessel(Level level, BlockPos pos, BlockState state, CuringVesselBlockEntity vessel) {
        VesselEffects.tick(level, pos, state, vessel);
    }

    @Override
    public void tickDrying(Level level, BlockPos pos, BlockState state, DryingSlabBlockEntity drying) {
        if (!(level instanceof ClientLevel clientLevel) || level.getMaxLocalRawBrightness(pos.above()) <= 0) {
            return;
        }

        RandomSource random = level.getRandom();
        ItemStack input = drying.getInput();
        if (input.isEmpty() || random.nextFloat() >= STEAM_CHANCE || LibationRecipes.drying(input).isEmpty()) {
            return;
        }
        Vec3 at = LibationGeometry.toWorld(pos, Direction.NORTH, LibationGeometry.DRYING_ITEM_CENTER, LibationGeometry.DRYING_SURFACE_Y + 1.0F, LibationGeometry.DRYING_ITEM_CENTER);
        clientLevel.addParticle(ParticleTypes.WHITE_SMOKE, at.x, at.y, at.z, 0.0, 0.02, 0.0);
    }

    /**
     * Burst of glow particles and splashes when the catch trough steeps an item.
     *
     * @param color the blended colour of the principles used for the steeping.
     */
    @Override
    public void onTroughSteep(Level level, BlockPos pos, int color) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }

        RandomSource random = level.getRandom();
        Vec3 centre = new Vec3(pos.getX() + 0.5, pos.getY() + 0.35, pos.getZ() + 0.5);
        for (int i = 0; i < BURST_COUNT; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double speed = 0.03 + random.nextDouble() * 0.04;
            clientLevel.addParticle(GlowParticleOptions.of(0xFF000000 | color).size(BURST_SIZE).age(BURST_AGE),
                    centre.x, centre.y, centre.z,
                    Math.cos(angle) * speed, 0.05 + random.nextDouble() * 0.05, Math.sin(angle) * speed);
        }
        for (int i = 0; i < BURST_SPLASH_COUNT; i++) {
            spawnSplash(clientLevel, random, centre, color);
        }
    }

    /**
     * Drops a principle drip from a spout or tap tip. The drip hangs, then falls straight down onto the surface of the recipient below the spout.
     */
    static void spawnDrip(ClientLevel level, BlockPos pos, Direction facing, LibationGeometry.Point drip, int color) {
        Vec3 origin = LibationGeometry.toWorld(pos, facing, drip.x(), drip.y(), drip.z());
        float landY = pos.getY() + recipientSurfaceY(level, pos);
        level.addParticle(new PrincipleDripOptions(color, landY), origin.x, origin.y, origin.z, 0.0, 0.0, 0.0);
    }

    /**
     * Height of the surface that receives the drain of the spout at pos, relative to that spout. Uses the liquid level of a
     * trough, basin or jar, or the top of the target block.
     */
    static float recipientSurfaceY(Level level, BlockPos spout) {
        BlockPos target = spout.below();
        float base = target.getY() - spout.getY();
        BlockEntity blockEntity = level.getBlockEntity(target);
        LiquidArea liquid = liquidOf(blockEntity);
        if (liquid != null && blockEntity instanceof PrincipleContainer container) {
            return base + liquid.surfaceY(container.getPrinciples().total());
        }
        return base + 1.0F;
    }

    /**
     * @return the drain colour of the host of a spout, the run-off of a slab or the drain of a vessel.
     */
    static int hostColor(@Nullable Level level, BlockPos spout, Direction facing) {
        BlockEntity host = level == null ? null : level.getBlockEntity(spout.relative(facing.getOpposite()));
        if (host instanceof LibationSlabBlockEntity slab) {
            return slab.getDrainColor();
        }
        if (host instanceof CuringVesselBlockEntity vessel) {
            return vessel.getDrainColor();
        }
        return PrincipleContents.DEFAULT_COLOR;
    }

    private static LiquidArea liquidOf(BlockEntity blockEntity) {
        if (blockEntity instanceof CuringVesselBlockEntity vessel) {
            return vessel.properties().liquid();
        }
        if (blockEntity instanceof CatchTroughBlockEntity) {
            return LibationGeometry.TROUGH;
        }
        return null;
    }

    static void spawnSplash(ClientLevel level, RandomSource random, Vec3 at, int color) {
        double x = at.x + (random.nextDouble() - 0.5) * 0.4;
        double z = at.z + (random.nextDouble() - 0.5) * 0.4;
        level.addParticle(new PrincipleSplashOptions(color), x, at.y, z, 0.0, 0.1, 0.0);
    }

    private static void spawnFizz(ClientLevel level, BlockPos pos, int color) {
        float center = LibationGeometry.SLAB_ITEM_CENTER;
        Vec3 at = LibationGeometry.toWorld(pos, Direction.NORTH, center, LibationGeometry.SLAB_SURFACE_Y + 0.5F, center);
        level.addParticle(GlowParticleOptions.of(0xFF000000 | color).size(FIZZ_SIZE).age(FIZZ_AGE), at.x, at.y, at.z, 0.0, 0.02, 0.0);
    }

    /**
     * Linear blend of two RGB colours.
     *
     * @param t 0 returns {@code from}, 1 returns {@code to}.
     */
    static int mix(int from, int to, float t) {
        return channel(from, to, t, 16) << 16 | channel(from, to, t, 8) << 8 | channel(from, to, t, 0);
    }

    private static int channel(int from, int to, float t, int shift) {
        return (int) Mth.lerp(t, (from >> shift) & 0xFF, (to >> shift) & 0xFF);
    }
}
