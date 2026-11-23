// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.slab;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * The side of a libation slab that holds a spout, or {@link #NONE}. Drives the holed rim of the slab model.
 */
public enum SpoutSide implements StringRepresentable {
    NONE(null),
    NORTH(Direction.NORTH),
    EAST(Direction.EAST),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST);

    private final @Nullable Direction direction;

    SpoutSide(@Nullable Direction direction) {
        this.direction = direction;
    }

    /**
     * @return the side for the given horizontal direction, or {@link #NONE} for null.
     */
    public static SpoutSide of(@Nullable Direction direction) {
        if (direction == null) {
            return NONE;
        }
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NONE;
        };
    }

    /**
     * @return the horizontal direction of the side, or null for {@link #NONE}.
     */
    public @Nullable Direction direction() {
        return this.direction;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
