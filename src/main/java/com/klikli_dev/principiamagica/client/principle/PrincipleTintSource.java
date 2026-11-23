// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.principle;

import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Tints an item layer by the blended colour of its {@code STORED_PRINCIPLES} component, falling back to {@code defaultColor}.
 */
public record PrincipleTintSource(int defaultColor) implements ItemTintSource {

    public static final MapCodec<PrincipleTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(PrincipleTintSource::defaultColor)
    ).apply(instance, PrincipleTintSource::new));

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        PrincipleContents contents = itemStack.get(DataComponentRegistry.STORED_PRINCIPLES.get());
        if (contents != null && !contents.isEmpty()) {
            return ARGB.opaque(contents.blendedColor());
        }
        return ARGB.opaque(this.defaultColor);
    }

    @Override
    public MapCodec<PrincipleTintSource> type() {
        return MAP_CODEC;
    }
}
