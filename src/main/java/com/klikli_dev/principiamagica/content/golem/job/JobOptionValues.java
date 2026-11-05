// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record JobOptionValues(Map<String, Boolean> values) {

    public static final JobOptionValues EMPTY = new JobOptionValues(Map.of());

    public static final Codec<JobOptionValues> CODEC = Codec.unboundedMap(Codec.STRING, Codec.BOOL).xmap(JobOptionValues::new, JobOptionValues::values);

    public static final StreamCodec<ByteBuf, JobOptionValues> STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.BOOL
    ).map(JobOptionValues::new, values -> new HashMap<>(values.values()));

    public static JobOptionValues defaults(List<JobOption> options) {
        Map<String, Boolean> values = new HashMap<>();
        options.forEach(option -> values.put(option.key(), option.defaultValue()));
        return new JobOptionValues(Map.copyOf(values));
    }

    public boolean get(JobOption option) {
        return this.values.getOrDefault(option.key(), option.defaultValue());
    }

    public JobOptionValues with(JobOption option, boolean value) {
        Map<String, Boolean> copy = new HashMap<>(this.values);
        copy.put(option.key(), value);
        return new JobOptionValues(Map.copyOf(copy));
    }
}
