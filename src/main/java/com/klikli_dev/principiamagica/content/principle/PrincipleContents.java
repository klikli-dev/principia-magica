// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle;

import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Immutable collection of principles with amounts. Iteration order follows the principle registry order.
 * Amounts are always at least 1; principles with zero amount are not stored.
 */
public final class PrincipleContents implements TooltipProvider {

    /**
     * The colour of nothing: white. Used as the blended colour of empty contents.
     */
    public static final int DEFAULT_COLOR = 0xFFFFFF;

    public static final PrincipleContents EMPTY = new PrincipleContents(List.of());

    public static final Codec<PrincipleContents> CODEC = PrincipleStack.CODEC.listOf().xmap(PrincipleContents::of, PrincipleContents::stacks);

    public static final StreamCodec<RegistryFriendlyByteBuf, PrincipleContents> STREAM_CODEC = PrincipleStack.STREAM_CODEC.apply(ByteBufCodecs.list())
            .map(PrincipleContents::of, PrincipleContents::stacks);

    private final List<PrincipleStack> stacks;

    private PrincipleContents(List<PrincipleStack> stacks) {
        this.stacks = stacks;
    }

    public static PrincipleContents of(PrincipleStack... stacks) {
        return of(List.of(stacks));
    }

    /**
     * Creates contents from the given stacks. Stacks of the same principle are merged.
     */
    public static PrincipleContents of(List<PrincipleStack> stacks) {
        Map<Principle, PrincipleStack> merged = new LinkedHashMap<>();
        for (PrincipleStack stack : stacks) {
            merged.merge(stack.principle().value(), stack, (a, b) -> new PrincipleStack(a.principle(), a.amount() + b.amount()));
        }

        List<PrincipleStack> sorted = new ArrayList<>(merged.values());
        sorted.sort(Comparator.comparingInt(stack -> PrincipiaMagicaRegistries.PRINCIPLE.getId(stack.principle().value())));
        return sorted.isEmpty() ? EMPTY : new PrincipleContents(List.copyOf(sorted));
    }

    public List<PrincipleStack> stacks() {
        return this.stacks;
    }

    public int get(Holder<Principle> principle) {
        for (PrincipleStack stack : this.stacks) {
            if (stack.principle().value().equals(principle.value())) {
                return stack.amount();
            }
        }
        return 0;
    }

    public int total() {
        int total = 0;
        for (PrincipleStack stack : this.stacks) {
            total += stack.amount();
        }
        return total;
    }

    public boolean isEmpty() {
        return this.stacks.isEmpty();
    }

    public PrincipleContents add(PrincipleContents other) {
        List<PrincipleStack> combined = new ArrayList<>(this.stacks);
        combined.addAll(other.stacks);
        return of(combined);
    }

    /**
     * @return true if this contains at least the amount of every principle in the required contents.
     */
    public boolean contains(PrincipleContents required) {
        for (PrincipleStack stack : required.stacks) {
            if (this.get(stack.principle()) < stack.amount()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the given amounts.
     *
     * @throws IllegalArgumentException if this does not contain the given amounts.
     */
    public PrincipleContents subtract(PrincipleContents other) {
        if (!this.contains(other)) {
            throw new IllegalArgumentException("Cannot subtract " + other + " from " + this);
        }

        List<PrincipleStack> remaining = new ArrayList<>();
        for (PrincipleStack stack : this.stacks) {
            int amount = stack.amount() - other.get(stack.principle());
            if (amount > 0) {
                remaining.add(new PrincipleStack(stack.principle(), amount));
            }
        }
        return of(remaining);
    }

    /**
     * @return the amount-weighted RGB average of all principle colors, or white if empty. Used for liquid tinting.
     */
    public int blendedColor() {
        long red = 0;
        long green = 0;
        long blue = 0;
        for (PrincipleStack stack : this.stacks) {
            int color = stack.principle().value().color();
            red += (long) ((color >> 16) & 0xFF) * stack.amount();
            green += (long) ((color >> 8) & 0xFF) * stack.amount();
            blue += (long) (color & 0xFF) * stack.amount();
        }

        int total = this.total();
        if (total == 0) {
            return DEFAULT_COLOR;
        }

        return (int) (red / total) << 16 | (int) (green / total) << 8 | (int) (blue / total);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        for (PrincipleStack stack : this.stacks) {
            Principle principle = stack.principle().value();
            consumer.accept(Component.empty()
                    .append(principle.prefixedName())
                    .append(" ")
                    .append(stack.amountText().copy().withColor(principle.color())));
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof PrincipleContents other && this.stacks.equals(other.stacks);
    }

    @Override
    public int hashCode() {
        return this.stacks.hashCode();
    }

    @Override
    public String toString() {
        return "PrincipleContents" + this.stacks;
    }
}
