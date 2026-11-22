// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.spec;

import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Optional;
import java.util.function.BiConsumer;

public record GolemSpec(Holder<GolemMaterial> material, Optional<Holder<GolemJob>> job) {

    public static final Codec<GolemSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PrincipiaMagicaRegistries.GOLEM_MATERIAL.holderByNameCodec().fieldOf("material").forGetter(GolemSpec::material),
            PrincipiaMagicaRegistries.GOLEM_JOB.holderByNameCodec().optionalFieldOf("job").forGetter(GolemSpec::job)
    ).apply(instance, GolemSpec::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GolemSpec> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(PrincipiaMagicaRegistries.Keys.GOLEM_MATERIAL), GolemSpec::material,
            ByteBufCodecs.optional(ByteBufCodecs.holderRegistry(PrincipiaMagicaRegistries.Keys.GOLEM_JOB)), GolemSpec::job,
            GolemSpec::new
    );

    public static GolemSpec of(Holder<GolemMaterial> material) {
        return new GolemSpec(material, Optional.empty());
    }

    public GolemSpec withJob(Holder<GolemJob> job) {
        return new GolemSpec(this.material, Optional.of(job));
    }

    public GolemSpec withoutJob() {
        return new GolemSpec(this.material, Optional.empty());
    }

    public void collectModifiers(BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        this.material.value().collectModifiers(consumer);
    }
}
