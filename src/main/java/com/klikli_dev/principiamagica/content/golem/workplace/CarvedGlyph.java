// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;

/**
 * A boundary glyph carved with the stylus onto a face of a block.
 *
 * @param pos  the block the glyph is carved into
 * @param face the face of the block the glyph sits on
 */
public record CarvedGlyph(BlockPos pos, Direction face) {

    public static final Codec<CarvedGlyph> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(CarvedGlyph::pos),
            Direction.CODEC.fieldOf("face").forGetter(CarvedGlyph::face)
    ).apply(instance, CarvedGlyph::new));

    public static final StreamCodec<ByteBuf, CarvedGlyph> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CarvedGlyph::pos,
            Direction.STREAM_CODEC, CarvedGlyph::face,
            CarvedGlyph::new
    );

    public CarvedGlyph offset(int dx, int dy, int dz) {
        return new CarvedGlyph(this.pos.offset(dx, dy, dz), this.face);
    }
}
