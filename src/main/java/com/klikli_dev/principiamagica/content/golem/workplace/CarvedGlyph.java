// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A boundary glyph carved with the stylus onto a face of a block. A corner can be lifted on a plumb line: its X and Z stay
 * on the carved block's column, only its Y moves by {@code lift}.
 *
 * @param pos  the block the glyph is carved into, the anchor of the plumb line
 * @param face the face of the block the glyph sits on
 * @param lift the Y offset of the corner from the carved block
 */
public record CarvedGlyph(BlockPos pos, Direction face, int lift) {

    public static final Codec<CarvedGlyph> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(CarvedGlyph::pos),
            Direction.CODEC.fieldOf("face").forGetter(CarvedGlyph::face),
            Codec.INT.optionalFieldOf("lift", 0).forGetter(CarvedGlyph::lift)
    ).apply(instance, CarvedGlyph::new));

    public static final StreamCodec<ByteBuf, CarvedGlyph> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CarvedGlyph::pos,
            Direction.STREAM_CODEC, CarvedGlyph::face,
            ByteBufCodecs.VAR_INT, CarvedGlyph::lift,
            CarvedGlyph::new
    );

    /**
     * An unlifted glyph on the carved block.
     */
    public CarvedGlyph(BlockPos pos, Direction face) {
        this(pos, face, 0);
    }

    /**
     * @return the glyph's identity, ignoring its lift. Used to find a glyph again from a client request.
     */
    public boolean isSameGlyph(CarvedGlyph other) {
        return this.pos.equals(other.pos) && this.face == other.face;
    }

    /**
     * @return the position of the corner, the carved block lifted by {@link #lift}
     */
    public BlockPos corner() {
        return this.pos.offset(0, this.lift, 0);
    }

    public CarvedGlyph withLift(int lift) {
        return new CarvedGlyph(this.pos, this.face, lift);
    }

    public CarvedGlyph offset(int dx, int dy, int dz) {
        return new CarvedGlyph(this.pos.offset(dx, dy, dz), this.face, this.lift);
    }
}
