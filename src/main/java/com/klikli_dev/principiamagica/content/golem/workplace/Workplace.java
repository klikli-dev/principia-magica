// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import com.klikli_dev.principiamagica.content.golem.job.JobOptionValues;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Where and how a golem works. The anchor is not stored here; it lives in the brain's ANCHOR memory.
 *
 * @param itemMemory generic per-position memory for jobs (e.g. which item was last grown at a field spot)
 * @param initialized whether the zero-config defaults were already applied, so removing all zones is respected
 */
public record Workplace(
        List<WorkZone> zones,
        List<ContainerLink> links,
        JobOptionValues options,
        Map<BlockPos, Holder<Item>> itemMemory,
        boolean initialized
) {

    public static final Workplace EMPTY = new Workplace(List.of(), List.of(), JobOptionValues.EMPTY, Map.of(), false);

    public static final Codec<Workplace> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WorkZone.CODEC.listOf().optionalFieldOf("zones", List.of()).forGetter(Workplace::zones),
            ContainerLink.CODEC.listOf().optionalFieldOf("links", List.of()).forGetter(Workplace::links),
            JobOptionValues.CODEC.optionalFieldOf("options", JobOptionValues.EMPTY).forGetter(Workplace::options),
            MemoryEntry.CODEC.listOf().optionalFieldOf("item_memory", List.of()).forGetter(Workplace::memoryEntries),
            Codec.BOOL.optionalFieldOf("initialized", false).forGetter(Workplace::initialized)
    ).apply(instance, (zones, links, options, memory, initialized) -> new Workplace(zones, links, options, MemoryEntry.toMap(memory), initialized)));

    public static final StreamCodec<RegistryFriendlyByteBuf, Workplace> STREAM_CODEC = StreamCodec.composite(
            WorkZone.STREAM_CODEC.apply(ByteBufCodecs.list()), Workplace::zones,
            ContainerLink.STREAM_CODEC.apply(ByteBufCodecs.list()), Workplace::links,
            JobOptionValues.STREAM_CODEC, Workplace::options,
            MemoryEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), Workplace::memoryEntries,
            ByteBufCodecs.BOOL, Workplace::initialized,
            (zones, links, options, memory, initialized) -> new Workplace(zones, links, options, MemoryEntry.toMap(memory), initialized)
    );

    public Workplace withZones(List<WorkZone> zones) {
        List<WorkZone> kept = List.copyOf(zones);
        Map<BlockPos, Holder<Item>> memory = this.itemMemory.entrySet().stream()
                .filter(entry -> containsInZone(kept, entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Workplace(kept, this.links, this.options, memory, this.initialized);
    }

    public Workplace withLinks(List<ContainerLink> links) {
        return new Workplace(this.zones, List.copyOf(links), this.options, this.itemMemory, this.initialized);
    }

    public Workplace withOptions(JobOptionValues options) {
        return new Workplace(this.zones, this.links, options, this.itemMemory, this.initialized);
    }

    public Workplace withItemMemory(Map<BlockPos, Holder<Item>> itemMemory) {
        return new Workplace(this.zones, this.links, this.options, Map.copyOf(itemMemory), this.initialized);
    }

    public Workplace markInitialized() {
        return new Workplace(this.zones, this.links, this.options, this.itemMemory, true);
    }

    public boolean containsInZone(BlockPos pos) {
        return containsInZone(this.zones, pos);
    }

    private static boolean containsInZone(List<WorkZone> zones, BlockPos pos) {
        return zones.stream().anyMatch(zone -> zone.contains(pos));
    }

    private List<MemoryEntry> memoryEntries() {
        return this.itemMemory.entrySet().stream().map(entry -> new MemoryEntry(entry.getKey(), entry.getValue())).toList();
    }

    private record MemoryEntry(BlockPos pos, Holder<Item> item) {
        static final Codec<MemoryEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(MemoryEntry::pos),
                BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(MemoryEntry::item)
        ).apply(instance, MemoryEntry::new));

        static final StreamCodec<RegistryFriendlyByteBuf, MemoryEntry> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, MemoryEntry::pos,
                ByteBufCodecs.holderRegistry(Registries.ITEM), MemoryEntry::item,
                MemoryEntry::new
        );

        static Map<BlockPos, Holder<Item>> toMap(List<MemoryEntry> entries) {
            Map<BlockPos, Holder<Item>> map = new HashMap<>();
            entries.forEach(entry -> map.put(entry.pos(), entry.item()));
            return Map.copyOf(map);
        }
    }
}
