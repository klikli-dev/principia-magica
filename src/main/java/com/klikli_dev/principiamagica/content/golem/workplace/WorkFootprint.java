// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/**
 * A work area carved into the world as a flat rectangle on the XZ plane. Its vertical extent is not stored: the job
 * decides how far above and below the working level it reaches, see {@link #zone}.
 * <p>
 * The working level is {@code groundY + raise}. The ground level is the Y of the carved block, and the raise is
 * player-defined (shift+scroll with the stylus).
 *
 * @param minX    smallest X of the rectangle, inclusive
 * @param minZ    smallest Z of the rectangle, inclusive
 * @param maxX    largest X of the rectangle, inclusive
 * @param maxZ    largest Z of the rectangle, inclusive
 * @param groundY the Y level of the carved surface
 * @param raise   player-defined offset of the working level above the ground, clamped to [MIN_RAISE, MAX_RAISE]
 * @param glyphs  the carved corner glyphs that define the rectangle; empty for default footprints
 */
public record WorkFootprint(int minX, int minZ, int maxX, int maxZ, int groundY, int raise, List<CarvedGlyph> glyphs) {

    public static final int MAX_SIZE = 64;
    public static final int MIN_RAISE = -8;
    public static final int MAX_RAISE = 16;

    /**
     * The vertical reach used for golems without a job, e.g. when erasing: the widest reach of any job.
     */
    public static final int DEFAULT_BELOW = 0;
    public static final int DEFAULT_ABOVE = 3;

    public static final Codec<WorkFootprint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("min_x").forGetter(WorkFootprint::minX),
            Codec.INT.fieldOf("min_z").forGetter(WorkFootprint::minZ),
            Codec.INT.fieldOf("max_x").forGetter(WorkFootprint::maxX),
            Codec.INT.fieldOf("max_z").forGetter(WorkFootprint::maxZ),
            Codec.INT.fieldOf("ground_y").forGetter(WorkFootprint::groundY),
            Codec.INT.optionalFieldOf("raise", 0).forGetter(WorkFootprint::raise),
            CarvedGlyph.CODEC.listOf().optionalFieldOf("glyphs", List.of()).forGetter(WorkFootprint::glyphs)
    ).apply(instance, WorkFootprint::new));

    public static final StreamCodec<ByteBuf, WorkFootprint> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WorkFootprint::minX,
            ByteBufCodecs.VAR_INT, WorkFootprint::minZ,
            ByteBufCodecs.VAR_INT, WorkFootprint::maxX,
            ByteBufCodecs.VAR_INT, WorkFootprint::maxZ,
            ByteBufCodecs.VAR_INT, WorkFootprint::groundY,
            ByteBufCodecs.VAR_INT, WorkFootprint::raise,
            CarvedGlyph.STREAM_CODEC.apply(ByteBufCodecs.list()), WorkFootprint::glyphs,
            WorkFootprint::new
    );

    public WorkFootprint {
        glyphs = List.copyOf(glyphs);
    }

    /**
     * @return the footprint spanned by two carved glyphs. The ground level is the lower of the two glyph blocks.
     */
    public static WorkFootprint of(CarvedGlyph a, CarvedGlyph b) {
        return new WorkFootprint(
                Math.min(a.pos().getX(), b.pos().getX()),
                Math.min(a.pos().getZ(), b.pos().getZ()),
                Math.max(a.pos().getX(), b.pos().getX()),
                Math.max(a.pos().getZ(), b.pos().getZ()),
                Math.min(a.pos().getY(), b.pos().getY()),
                0,
                List.of(a, b)
        );
    }

    /**
     * A default footprint with no glyphs, centred on the column of the anchor.
     */
    public static WorkFootprint centered(BlockPos anchor, int radius, int groundY) {
        return new WorkFootprint(
                anchor.getX() - radius, anchor.getZ() - radius,
                anchor.getX() + radius, anchor.getZ() + radius,
                groundY, 0, List.of()
        );
    }

    public int width() {
        return this.maxX - this.minX + 1;
    }

    public int depth() {
        return this.maxZ - this.minZ + 1;
    }

    /**
     * @return the working level, the Y the job's vertical reach is measured from
     */
    public int level() {
        return this.groundY + this.raise;
    }

    public boolean isTooLarge() {
        return this.width() > MAX_SIZE || this.depth() > MAX_SIZE;
    }

    /**
     * @return whether the column of pos is inside the rectangle, regardless of height
     */
    public boolean containsColumn(BlockPos pos) {
        return pos.getX() >= this.minX && pos.getX() <= this.maxX && pos.getZ() >= this.minZ && pos.getZ() <= this.maxZ;
    }

    public boolean hasGlyphAt(BlockPos pos) {
        return this.glyphs.stream().anyMatch(glyph -> glyph.pos().equals(pos));
    }

    public WorkFootprint withRaise(int raise) {
        return new WorkFootprint(this.minX, this.minZ, this.maxX, this.maxZ, this.groundY, Math.clamp(raise, MIN_RAISE, MAX_RAISE), this.glyphs);
    }

    /**
     * The effective box of the footprint for a job that reaches {@code below} blocks below and {@code above} blocks above
     * the working level. This is the only place where the footprint is converted to a box.
     */
    public WorkZone zone(int below, int above) {
        int level = this.level();
        return WorkZone.of(
                new BlockPos(this.minX, level - below, this.minZ),
                new BlockPos(this.maxX, level + above, this.maxZ)
        );
    }

    public WorkFootprint offset(int dx, int dy, int dz) {
        return new WorkFootprint(
                this.minX + dx, this.minZ + dz, this.maxX + dx, this.maxZ + dz,
                this.groundY + dy, this.raise,
                this.glyphs.stream().map(glyph -> glyph.offset(dx, dy, dz)).toList()
        );
    }
}
