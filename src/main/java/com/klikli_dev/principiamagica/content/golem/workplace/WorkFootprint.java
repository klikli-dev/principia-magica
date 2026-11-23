// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Optional;

/**
 * A work area carved into the world as a box: a rectangle on the XZ plane spanning {@code minY} to {@code maxY}.
 * <p>
 * The corners of the rectangle are the carved glyphs. Each glyph is carved on a block and can be lifted on a plumb line,
 * so the X and Z of a corner stay on its block's column and only its Y moves. The Y extent of a footprint with glyphs is
 * therefore derived from its corners: the lowest and highest corner. A glyphless footprint (the default area) keeps its
 * Y extent as stored.
 * <p>
 * A <b>flat</b> footprint ({@code minY == maxY}) has no height of its own. A <b>volume</b> footprint ({@code minY < maxY}) spans
 * its corners. Both get the job's reach around their box, see {@link #zone}.
 *
 * @param minX  smallest X of the rectangle, inclusive
 * @param minZ  smallest Z of the rectangle, inclusive
 * @param maxX  largest X of the rectangle, inclusive
 * @param maxZ  largest Z of the rectangle, inclusive
 * @param minY  lowest Y of the box, inclusive. Derived from the corners if there are glyphs.
 * @param maxY  highest Y of the box, inclusive. Derived from the corners if there are glyphs.
 * @param glyphs the carved corner glyphs that define the rectangle; empty for default footprints
 */
public record WorkFootprint(int minX, int minZ, int maxX, int maxZ, int minY, int maxY, List<CarvedGlyph> glyphs) {

    public static final int MAX_SIZE = 64;

    /**
     * The Y range a footprint or corner can be moved within, the build limits of the overworld.
     */
    public static final int MIN_Y = -64;
    public static final int MAX_Y = 320;

    /**
     * The vertical reach used for golems without a job, e.g. when erasing: the widest reach of any job.
     */
    public static final int DEFAULT_BELOW = 0;
    public static final int DEFAULT_ABOVE = 3;

    private static final Codec<WorkFootprint> CURRENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("min_x").forGetter(WorkFootprint::minX),
            Codec.INT.fieldOf("min_z").forGetter(WorkFootprint::minZ),
            Codec.INT.fieldOf("max_x").forGetter(WorkFootprint::maxX),
            Codec.INT.fieldOf("max_z").forGetter(WorkFootprint::maxZ),
            Codec.INT.fieldOf("min_y").forGetter(WorkFootprint::minY),
            Codec.INT.fieldOf("max_y").forGetter(WorkFootprint::maxY),
            CarvedGlyph.CODEC.listOf().optionalFieldOf("glyphs", List.of()).forGetter(WorkFootprint::glyphs)
    ).apply(instance, WorkFootprint::new));

    /**
     * The format before boxes: a single ground level plus a player-defined raise. Decoding only, the getters are placeholders
     * because {@link #CODEC} always encodes the current format. Loads as a flat footprint at ground + raise.
     */
    private static final Codec<WorkFootprint> LEGACY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("min_x").forGetter(WorkFootprint::minX),
            Codec.INT.fieldOf("min_z").forGetter(WorkFootprint::minZ),
            Codec.INT.fieldOf("max_x").forGetter(WorkFootprint::maxX),
            Codec.INT.fieldOf("max_z").forGetter(WorkFootprint::maxZ),
            Codec.INT.fieldOf("ground_y").forGetter(WorkFootprint::minY),
            Codec.INT.optionalFieldOf("raise", 0).forGetter(WorkFootprint::maxY),
            CarvedGlyph.CODEC.listOf().optionalFieldOf("glyphs", List.of()).forGetter(WorkFootprint::glyphs)
    ).apply(instance, WorkFootprint::fromLegacy));

    /**
     * Reads the current format, or the legacy one if the current one is missing. Always writes the current format.
     */
    public static final Codec<WorkFootprint> CODEC = Codec.either(CURRENT_CODEC, LEGACY_CODEC)
            .xmap(either -> either.map(footprint -> footprint, footprint -> footprint), Either::left);

    public static final StreamCodec<ByteBuf, WorkFootprint> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WorkFootprint::minX,
            ByteBufCodecs.VAR_INT, WorkFootprint::minZ,
            ByteBufCodecs.VAR_INT, WorkFootprint::maxX,
            ByteBufCodecs.VAR_INT, WorkFootprint::maxZ,
            ByteBufCodecs.VAR_INT, WorkFootprint::minY,
            ByteBufCodecs.VAR_INT, WorkFootprint::maxY,
            CarvedGlyph.STREAM_CODEC.apply(ByteBufCodecs.list()), WorkFootprint::glyphs,
            WorkFootprint::new
    );

    public WorkFootprint {
        glyphs = List.copyOf(glyphs);
        if (!glyphs.isEmpty()) {
            minY = glyphs.stream().mapToInt(glyph -> glyph.corner().getY()).min().getAsInt();
            maxY = glyphs.stream().mapToInt(glyph -> glyph.corner().getY()).max().getAsInt();
        }
    }

    /**
     * Loads the legacy format as a flat footprint at ground + raise. The glyphs are lifted onto that level, so both corners sit there.
     */
    private static WorkFootprint fromLegacy(int minX, int minZ, int maxX, int maxZ, int groundY, int raise, List<CarvedGlyph> glyphs) {
        int level = groundY + raise;
        List<CarvedGlyph> lifted = glyphs.stream()
                .map(glyph -> glyph.withLift(level - glyph.pos().getY()))
                .toList();
        return new WorkFootprint(minX, minZ, maxX, maxZ, level, level, lifted);
    }

    /**
     * @return the footprint spanned by two carved glyphs. Its box spans both corners, including their lifts.
     */
    public static WorkFootprint of(CarvedGlyph a, CarvedGlyph b) {
        return new WorkFootprint(
                Math.min(a.pos().getX(), b.pos().getX()),
                Math.min(a.pos().getZ(), b.pos().getZ()),
                Math.max(a.pos().getX(), b.pos().getX()),
                Math.max(a.pos().getZ(), b.pos().getZ()),
                Math.min(a.corner().getY(), b.corner().getY()),
                Math.max(a.corner().getY(), b.corner().getY()),
                List.of(a, b)
        );
    }

    /**
     * A default flat footprint with no glyphs, centred on the column of the anchor.
     */
    public static WorkFootprint centered(BlockPos anchor, int radius, int groundY) {
        return new WorkFootprint(
                anchor.getX() - radius, anchor.getZ() - radius,
                anchor.getX() + radius, anchor.getZ() + radius,
                groundY, groundY, List.of()
        );
    }

    public int width() {
        return this.maxX - this.minX + 1;
    }

    public int depth() {
        return this.maxZ - this.minZ + 1;
    }

    public int height() {
        return this.maxY - this.minY + 1;
    }

    /**
     * @return whether both corners are at the same height. Flat footprints get the job's reach, see {@link #zone}.
     */
    public boolean isFlat() {
        return this.minY == this.maxY;
    }

    public boolean isTooLarge() {
        return this.width() > MAX_SIZE || this.depth() > MAX_SIZE || this.height() > MAX_SIZE;
    }

    public boolean isOutOfBounds() {
        return this.minY < MIN_Y || this.maxY > MAX_Y;
    }

    /**
     * @return whether the column of pos is inside the rectangle, regardless of height
     */
    public boolean containsColumn(BlockPos pos) {
        return pos.getX() >= this.minX && pos.getX() <= this.maxX && pos.getZ() >= this.minZ && pos.getZ() <= this.maxZ;
    }

    /**
     * @return whether a glyph has its 3D corner at cornerPos
     */
    public boolean hasCornerAt(BlockPos cornerPos) {
        return this.glyphs.stream().anyMatch(glyph -> glyph.corner().equals(cornerPos));
    }

    /**
     * @return the glyph whose 3D corner is at cornerPos
     */
    public Optional<CarvedGlyph> findGlyphByCorner(BlockPos cornerPos) {
        return this.glyphs.stream()
                .filter(glyph -> glyph.corner().equals(cornerPos))
                .findFirst();
    }

    /**
     * @return whether a glyph is carved into pos or has a corner at pos
     */
    public boolean hasGlyphAt(BlockPos pos) {
        return this.findGlyph(pos, null).isPresent();
    }

    /**
     * @return the glyph carved into pos on the given face, or the glyph on any face if face is null, or the glyph whose corner is at pos
     */
    public Optional<CarvedGlyph> findGlyph(BlockPos pos, Direction face) {
        return this.glyphs.stream()
                .filter(glyph -> (glyph.pos().equals(pos) || glyph.corner().equals(pos)) && (face == null || glyph.face() == face))
                .findFirst();
    }

    /**
     * The effective box of the footprint for a job that reaches {@code below} blocks below and {@code above} blocks above
     * the footprint. This is the only place where the footprint is converted to a box. The box spans from
     * {@code minY - below} to {@code maxY + above}, so a flat footprint (height 1) is the special case where both corners
     * share a level. A volume is padded the same way, so the job's reach still covers the crops above its top layer.
     */
    public WorkZone zone(int below, int above) {
        return WorkZone.of(
                new BlockPos(this.minX, this.minY - below, this.minZ),
                new BlockPos(this.maxX, this.maxY + above, this.maxZ)
        );
    }

    /**
     * Lifts or lowers the corner carried by the glyph, clamped to {@link #MIN_Y}..{@link #MAX_Y}. The corner stays on its carved block's column.
     *
     * @return the footprint with the corner moved, or this footprint unchanged if it carries no such glyph
     */
    public WorkFootprint liftGlyph(CarvedGlyph glyph, int delta) {
        return new WorkFootprint(this.minX, this.minZ, this.maxX, this.maxZ, this.minY, this.maxY,
                this.glyphs.stream()
                        .map(candidate -> {
                            if (!candidate.corner().equals(glyph.corner())) {
                                return candidate;
                            }
                            int newY = Math.clamp(candidate.corner().getY() + delta, MIN_Y, MAX_Y);
                            return candidate.withLift(newY - candidate.pos().getY());
                        })
                        .toList());
    }

    /**
     * Moves the whole footprint up or down. Glyphs keep their carved blocks and their lifts change, so the corners move with the box.
     * The move is clamped to {@link #MIN_Y}..{@link #MAX_Y}.
     *
     * @return the moved footprint
     */
    public WorkFootprint moved(int delta) {
        int shift = this.clampShift(delta);
        return new WorkFootprint(this.minX, this.minZ, this.maxX, this.maxZ, this.minY + shift, this.maxY + shift,
                this.glyphs.stream().map(glyph -> glyph.withLift(glyph.lift() + shift)).toList());
    }

    private int clampShift(int delta) {
        int low = MIN_Y - this.minY;
        int high = MAX_Y - this.maxY;
        return low > high ? 0 : Math.clamp(delta, low, high);
    }

    public WorkFootprint offset(int dx, int dy, int dz) {
        return new WorkFootprint(
                this.minX + dx, this.minZ + dz, this.maxX + dx, this.maxZ + dz,
                this.minY + dy, this.maxY + dy,
                this.glyphs.stream().map(glyph -> glyph.offset(dx, dy, dz)).toList()
        );
    }
}
