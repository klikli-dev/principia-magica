// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.spec;

import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.upgrade.GolemUpgrade;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public record GolemSpec(Holder<GolemMaterial> material, Optional<Holder<GolemJob>> job, List<Holder<GolemUpgrade>> upgrades) {

    public static final Codec<GolemSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PrincipiaMagicaRegistries.GOLEM_MATERIAL.holderByNameCodec().fieldOf("material").forGetter(GolemSpec::material),
            PrincipiaMagicaRegistries.GOLEM_JOB.holderByNameCodec().optionalFieldOf("job").forGetter(GolemSpec::job),
            PrincipiaMagicaRegistries.GOLEM_UPGRADE.holderByNameCodec().listOf().optionalFieldOf("upgrades", List.of()).forGetter(GolemSpec::upgrades)
    ).apply(instance, GolemSpec::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GolemSpec> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(PrincipiaMagicaRegistries.Keys.GOLEM_MATERIAL), GolemSpec::material,
            ByteBufCodecs.optional(ByteBufCodecs.holderRegistry(PrincipiaMagicaRegistries.Keys.GOLEM_JOB)), GolemSpec::job,
            ByteBufCodecs.holderRegistry(PrincipiaMagicaRegistries.Keys.GOLEM_UPGRADE).apply(ByteBufCodecs.list()), GolemSpec::upgrades,
            GolemSpec::new
    );

    public static GolemSpec of(Holder<GolemMaterial> material) {
        return new GolemSpec(material, Optional.empty(), List.of());
    }

    public GolemSpec withJob(Holder<GolemJob> job) {
        return new GolemSpec(this.material, Optional.of(job), this.upgrades);
    }

    public GolemSpec withoutJob() {
        return new GolemSpec(this.material, Optional.empty(), this.upgrades);
    }

    public GolemSpec withUpgrade(Holder<GolemUpgrade> upgrade) {
        List<Holder<GolemUpgrade>> upgrades = new ArrayList<>(this.upgrades);
        upgrades.add(upgrade);
        return new GolemSpec(this.material, this.job, List.copyOf(upgrades));
    }

    public GolemSpec withoutUpgrade(int index) {
        List<Holder<GolemUpgrade>> upgrades = new ArrayList<>(this.upgrades);
        upgrades.remove(index);
        return new GolemSpec(this.material, this.job, List.copyOf(upgrades));
    }

    public void collectModifiers(BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        this.material.value().collectModifiers(consumer);
        for (Holder<GolemUpgrade> upgrade : this.upgrades) {
            upgrade.value().collectModifiers(consumer);
        }
    }
}
