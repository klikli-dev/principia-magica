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

import java.util.List;

/**
 * The first corner of a footprint that is being carved with a stylus. Stored on the stylus until the second corner is set.
 * The second corner is the ghost: it follows the block under the crosshair and carries its own lift, which the player can
 * change before confirming.
 * <p>
 * A pending corner from a pick-up keeps the footprint it was picked from in {@code restore}, so cancelling the pending
 * corner puts the footprint back. The footprint is discarded when the corner is carved.
 *
 * @param pos       the block and dimension of the first glyph
 * @param face      the face the first glyph sits on
 * @param lift      the lift of the first glyph
 * @param ghostLift the lift the second corner gets when it is carved, clamped to the lift range
 * @param restore   the footprint to put back if the pending corner is cancelled: the picked footprint, or empty for a fresh corner
 */
public record PendingGlyph(GlobalPos pos, Direction face, int lift, int ghostLift, List<WorkFootprint> restore) {

    public static final Codec<PendingGlyph> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(PendingGlyph::pos),
            Direction.CODEC.fieldOf("face").forGetter(PendingGlyph::face),
            Codec.INT.optionalFieldOf("lift", 0).forGetter(PendingGlyph::lift),
            Codec.INT.optionalFieldOf("ghost_lift", 0).forGetter(PendingGlyph::ghostLift),
            WorkFootprint.CODEC.listOf().optionalFieldOf("restore", List.of()).forGetter(PendingGlyph::restore)
    ).apply(instance, PendingGlyph::new));

    public static final StreamCodec<ByteBuf, PendingGlyph> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, PendingGlyph::pos,
            Direction.STREAM_CODEC, PendingGlyph::face,
            ByteBufCodecs.VAR_INT, PendingGlyph::lift,
            ByteBufCodecs.VAR_INT, PendingGlyph::ghostLift,
            WorkFootprint.STREAM_CODEC.apply(ByteBufCodecs.list()), PendingGlyph::restore,
            PendingGlyph::new
    );

    /**
     * A first corner that is not lifted, with an unlifted ghost and nothing to restore.
     */
    public static PendingGlyph of(GlobalPos pos, Direction face) {
        return new PendingGlyph(pos, face, 0, 0, List.of());
    }

    public CarvedGlyph glyph() {
        return new CarvedGlyph(this.pos.pos(), this.face, this.lift);
    }

    /**
     * @return the pending glyph with the ghost lifted or lowered by delta, clamped to the lift range
     */
    public PendingGlyph withGhostLift(int delta) {
        return new PendingGlyph(this.pos, this.face, this.lift, WorkFootprint.clampLift(this.ghostLift + delta), this.restore);
    }
}
