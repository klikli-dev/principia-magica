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
 * @param footprints          the carved work areas
 * @param itemMemory          generic per-position memory for jobs (e.g. which item was last grown at a field spot)
 * @param initialized         whether the zero-config defaults were already applied, so removing all footprints is respected
 */
public record Workplace(
        List<WorkFootprint> footprints,
        List<ContainerLink> links,
        JobOptionValues options,
        Map<BlockPos, Holder<Item>> itemMemory,
        boolean initialized
) {

    public static final Workplace EMPTY = new Workplace(List.of(), List.of(), JobOptionValues.EMPTY, Map.of(), false);

    public static final Codec<Workplace> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WorkFootprint.CODEC.listOf().optionalFieldOf("footprints", List.of()).forGetter(Workplace::footprints),
            ContainerLink.CODEC.listOf().optionalFieldOf("links", List.of()).forGetter(Workplace::links),
            JobOptionValues.CODEC.optionalFieldOf("options", JobOptionValues.EMPTY).forGetter(Workplace::options),
            MemoryEntry.CODEC.listOf().optionalFieldOf("item_memory", List.of()).forGetter(Workplace::memoryEntries),
            Codec.BOOL.optionalFieldOf("initialized", false).forGetter(Workplace::initialized)
    ).apply(instance, (footprints, links, options, memory, initialized) -> new Workplace(footprints, links, options, MemoryEntry.toMap(memory), initialized)));

    public static final StreamCodec<RegistryFriendlyByteBuf, Workplace> STREAM_CODEC = StreamCodec.composite(
            WorkFootprint.STREAM_CODEC.apply(ByteBufCodecs.list()), Workplace::footprints,
            ContainerLink.STREAM_CODEC.apply(ByteBufCodecs.list()), Workplace::links,
            JobOptionValues.STREAM_CODEC, Workplace::options,
            MemoryEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), Workplace::memoryEntries,
            ByteBufCodecs.BOOL, Workplace::initialized,
            (footprints, links, options, memory, initialized) -> new Workplace(footprints, links, options, MemoryEntry.toMap(memory), initialized)
    );

    public Workplace withFootprints(List<WorkFootprint> footprints) {
        List<WorkFootprint> kept = List.copyOf(footprints);
        Map<BlockPos, Holder<Item>> memory = this.itemMemory.entrySet().stream()
                .filter(entry -> kept.stream().anyMatch(footprint -> footprint.containsColumn(entry.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Workplace(kept, this.links, this.options, memory, this.initialized);
    }

    public Workplace withLinks(List<ContainerLink> links) {
        return new Workplace(this.footprints, List.copyOf(links), this.options, this.itemMemory, this.initialized);
    }

    public Workplace withOptions(JobOptionValues options) {
        return new Workplace(this.footprints, this.links, options, this.itemMemory, this.initialized);
    }

    public Workplace withItemMemory(Map<BlockPos, Holder<Item>> itemMemory) {
        return new Workplace(this.footprints, this.links, this.options, Map.copyOf(itemMemory), this.initialized);
    }

    public Workplace markInitialized() {
        return new Workplace(this.footprints, this.links, this.options, this.itemMemory, true);
    }

    /**
     * @return the effective boxes of all footprints for a job that reaches the given distances from the working level
     */
    public List<WorkZone> zones(int below, int above) {
        return this.footprints.stream().map(footprint -> footprint.zone(below, above)).toList();
    }

    public boolean containsInZone(int below, int above, BlockPos pos) {
        return this.zones(below, above).stream().anyMatch(zone -> zone.contains(pos));
    }

    public boolean hasGlyphAt(BlockPos pos) {
        return this.footprints.stream().anyMatch(footprint -> footprint.hasGlyphAt(pos));
    }

    /**
     * @return the workplace without the footprints that have a glyph at pos. Returns the workplace unchanged if none has.
     */
    public Workplace withoutGlyphAt(BlockPos pos) {
        if (!this.hasGlyphAt(pos)) {
            return this;
        }

        return this.withFootprints(this.footprints.stream().filter(footprint -> !footprint.hasGlyphAt(pos)).toList());
    }

    /**
     * @return the index of the smallest footprint whose rectangle contains the column of pos, or -1 if none does
     */
    public int footprintIndexAt(BlockPos pos) {
        int best = -1;
        int bestArea = Integer.MAX_VALUE;
        for (int i = 0; i < this.footprints.size(); i++) {
            WorkFootprint footprint = this.footprints.get(i);
            int area = footprint.width() * footprint.depth();
            if (footprint.containsColumn(pos) && area < bestArea) {
                best = i;
                bestArea = area;
            }
        }

        return best;
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
