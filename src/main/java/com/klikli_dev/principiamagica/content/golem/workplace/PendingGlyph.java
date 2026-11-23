// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Optional;

/**
 * The first corner of a footprint that is being carved with a stylus, or the fixed opposite corner of a footprint
 * whose corner was picked up to be moved. Stored on the stylus until the second corner is set.
 * The second corner is the ghost: it follows the block under the crosshair and carries its own lift, which the player can
 * change before confirming.
 *
 * @param pos            the block and dimension of the first/fixed corner
 * @param face           the face the first glyph sits on
 * @param ghostLift      the lift the second corner gets when it is placed, clamped to [-(MAX_SIZE - 1), MAX_SIZE - 1]
 * @param original       the original footprint if this pending state came from picking up an existing corner (restored on cancel)
 * @param originalMemory the item memory that was inside {@code original} and was dropped when it was picked up. Restored on cancel,
 *                       and on completion where it still lies inside the new footprint.
 */
public record PendingGlyph(GlobalPos pos, Direction face, int ghostLift, Optional<WorkFootprint> original, Map<BlockPos, Holder<Item>> originalMemory) {

    private static final int MAX_GHOST_LIFT = WorkFootprint.MAX_SIZE - 1;

    public static final Codec<PendingGlyph> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(PendingGlyph::pos),
            Direction.CODEC.fieldOf("face").forGetter(PendingGlyph::face),
            Codec.INT.optionalFieldOf("ghost_lift", 0).forGetter(PendingGlyph::ghostLift),
            WorkFootprint.CODEC.optionalFieldOf("original").forGetter(PendingGlyph::original),
            Workplace.ITEM_MEMORY_CODEC.optionalFieldOf("original_memory", Map.of()).forGetter(PendingGlyph::originalMemory)
    ).apply(instance, PendingGlyph::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PendingGlyph> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, PendingGlyph::pos,
            Direction.STREAM_CODEC, PendingGlyph::face,
            ByteBufCodecs.VAR_INT, PendingGlyph::ghostLift,
            ByteBufCodecs.optional(WorkFootprint.STREAM_CODEC), PendingGlyph::original,
            Workplace.ITEM_MEMORY_STREAM_CODEC, PendingGlyph::originalMemory,
            PendingGlyph::new
    );

    public PendingGlyph(GlobalPos pos, Direction face, int ghostLift) {
        this(pos, face, ghostLift, Optional.empty(), Map.of());
    }

    /**
     * A first corner that is not lifted, with an unlifted ghost.
     */
    public static PendingGlyph of(GlobalPos pos, Direction face) {
        return new PendingGlyph(pos, face, 0, Optional.empty(), Map.of());
    }

    /**
     * A pending corner created by picking up one corner of an existing footprint to move it.
     *
     * @param originalMemory the item memory dropped by the pick up, see {@link #originalMemory}
     */
    public static PendingGlyph moving(GlobalPos anchorPos, int ghostLift, WorkFootprint original, Map<BlockPos, Holder<Item>> originalMemory) {
        return new PendingGlyph(anchorPos, Direction.UP, ghostLift, Optional.of(original), originalMemory);
    }

    public CarvedGlyph glyph() {
        return new CarvedGlyph(this.pos.pos(), this.face, 0);
    }

    /**
     * @return the pending glyph with the ghost lifted or lowered by delta, clamped to the max zone height range
     */
    public PendingGlyph withGhostLift(int delta) {
        return new PendingGlyph(this.pos, this.face, Math.clamp(this.ghostLift + delta, -MAX_GHOST_LIFT, MAX_GHOST_LIFT), this.original, this.originalMemory);
    }
}
