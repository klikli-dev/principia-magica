// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

public record WorkZone(BlockPos min, BlockPos max) {

    public static final Codec<WorkZone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("min").forGetter(WorkZone::min),
            BlockPos.CODEC.fieldOf("max").forGetter(WorkZone::max)
    ).apply(instance, WorkZone::new));

    public static final StreamCodec<ByteBuf, WorkZone> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, WorkZone::min,
            BlockPos.STREAM_CODEC, WorkZone::max,
            WorkZone::new
    );

    public static WorkZone of(BlockPos a, BlockPos b) {
        return new WorkZone(
                new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ())),
                new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()))
        );
    }

    public static WorkZone centered(BlockPos center, int radius, int down, int up) {
        return of(center.offset(-radius, -down, -radius), center.offset(radius, up, radius));
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= this.min.getX() && pos.getX() <= this.max.getX()
                && pos.getY() >= this.min.getY() && pos.getY() <= this.max.getY()
                && pos.getZ() >= this.min.getZ() && pos.getZ() <= this.max.getZ();
    }

    public Iterable<BlockPos> positions() {
        return BlockPos.betweenClosed(this.min, this.max);
    }

    public AABB aabb() {
        return new AABB(this.min.getX(), this.min.getY(), this.min.getZ(), this.max.getX() + 1, this.max.getY() + 1, this.max.getZ() + 1);
    }

    public int width() {
        return this.max.getX() - this.min.getX() + 1;
    }

    public int height() {
        return this.max.getY() - this.min.getY() + 1;
    }

    public int depth() {
        return this.max.getZ() - this.min.getZ() + 1;
    }

    public int volume() {
        return this.width() * this.height() * this.depth();
    }

    /**
     * Maps a linear index in [0, volume) to a position, allowing budgeted incremental scans without iterators.
     */
    public BlockPos positionAt(int index) {
        int width = this.width();
        int area = width * this.depth();
        return new BlockPos(
                this.min.getX() + index % width,
                this.min.getY() + index / area,
                this.min.getZ() + (index / width) % this.depth()
        );
    }
}
