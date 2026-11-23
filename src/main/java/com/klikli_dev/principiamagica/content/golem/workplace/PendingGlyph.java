// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The first corner of a footprint that is being carved with a stylus. Stored on the stylus until the second corner is set.
 * The second corner is the ghost: it follows the block under the crosshair and carries its own lift, which the player can
 * change before confirming.
 *
 * @param pos       the block and dimension of the first glyph
 * @param face      the face the first glyph sits on
 * @param ghostLift the lift the second corner gets when it is carved, clamped to [-(MAX_SIZE - 1), MAX_SIZE - 1]
 */
public record PendingGlyph(GlobalPos pos, Direction face, int ghostLift) {

    private static final int MAX_GHOST_LIFT = WorkFootprint.MAX_SIZE - 1;

    public static final Codec<PendingGlyph> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(PendingGlyph::pos),
            Direction.CODEC.fieldOf("face").forGetter(PendingGlyph::face),
            Codec.INT.optionalFieldOf("ghost_lift", 0).forGetter(PendingGlyph::ghostLift)
    ).apply(instance, PendingGlyph::new));

    public static final StreamCodec<ByteBuf, PendingGlyph> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, PendingGlyph::pos,
            Direction.STREAM_CODEC, PendingGlyph::face,
            ByteBufCodecs.VAR_INT, PendingGlyph::ghostLift,
            PendingGlyph::new
    );

    /**
     * A first corner that is not lifted, with an unlifted ghost.
     */
    public static PendingGlyph of(GlobalPos pos, Direction face) {
        return new PendingGlyph(pos, face, 0);
    }

    public CarvedGlyph glyph() {
        return new CarvedGlyph(this.pos.pos(), this.face, 0);
    }

    /**
     * @return the pending glyph with the ghost lifted or lowered by delta, clamped to the max zone height range
     */
    public PendingGlyph withGhostLift(int delta) {
        return new PendingGlyph(this.pos, this.face, Math.clamp(this.ghostLift + delta, -MAX_GHOST_LIFT, MAX_GHOST_LIFT));
    }
}
