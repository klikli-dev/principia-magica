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
 * A container the golem may take items from (supply), put items into (deposit) or both.
 * {@code side} is the face of the container the golem accesses its item capability through.
 */
public record ContainerLink(BlockPos pos, Direction side, ContainerRole role) {

    public static final Codec<ContainerLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(ContainerLink::pos),
            Direction.CODEC.fieldOf("side").forGetter(ContainerLink::side),
            ContainerRole.CODEC.fieldOf("role").forGetter(ContainerLink::role)
    ).apply(instance, ContainerLink::new));

    public static final StreamCodec<ByteBuf, ContainerLink> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ContainerLink::pos,
            Direction.STREAM_CODEC, ContainerLink::side,
            ContainerRole.STREAM_CODEC, ContainerLink::role,
            ContainerLink::new
    );

    public ContainerLink withRole(ContainerRole role) {
        return new ContainerLink(this.pos, this.side, role);
    }
}
