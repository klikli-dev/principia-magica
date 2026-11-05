// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum ContainerRole implements StringRepresentable {
    SUPPLY,
    DEPOSIT,
    BOTH;

    public static final Codec<ContainerRole> CODEC = StringRepresentable.fromEnum(ContainerRole::values);

    public static final StreamCodec<ByteBuf, ContainerRole> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], ContainerRole::ordinal);

    public ContainerRole next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    public boolean canSupply() {
        return this != DEPOSIT;
    }

    public boolean canDeposit() {
        return this != SUPPLY;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
