// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.StreamCodec;

/**
 * The first glyph of a footprint that is being carved with a stylus. Stored on the stylus until the second corner is set.
 *
 * @param pos  the block and dimension of the first glyph
 * @param face the face the first glyph sits on
 */
public record PendingGlyph(GlobalPos pos, Direction face) {

    public static final Codec<PendingGlyph> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(PendingGlyph::pos),
            Direction.CODEC.fieldOf("face").forGetter(PendingGlyph::face)
    ).apply(instance, PendingGlyph::new));

    public static final StreamCodec<ByteBuf, PendingGlyph> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, PendingGlyph::pos,
            Direction.STREAM_CODEC, PendingGlyph::face,
            PendingGlyph::new
    );

    public CarvedGlyph glyph() {
        return new CarvedGlyph(this.pos.pos(), this.face);
    }
}
