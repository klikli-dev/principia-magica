// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle;

import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * A magical essence. Principles are not items: they are released by washing items and accumulate in receivers.
 * <p>
 * Primal principles have no components but an opposite. Compound principles are cured from exactly two components.
 *
 * @param color      RGB color (0xRRGGBB) used for tooltips and liquid tinting.
 * @param components empty for primal principles, two entries for compound principles.
 * @param opposite   the opposed primal principle, only present for primal principles.
 */
public record Principle(int color, List<Holder<Principle>> components, Optional<Holder<Principle>> opposite) {

    public static final String PREFIXED_NAME_KEY = "principle.principiamagica.prefixed";

    public static Principle primal(int color, Holder<Principle> opposite) {
        return new Principle(color, List.of(), Optional.of(opposite));
    }

    public static Principle compound(int color, Holder<Principle> first, Holder<Principle> second) {
        return new Principle(color, List.of(first, second), Optional.empty());
    }

    public boolean isPrimal() {
        return this.components.isEmpty();
    }

    public String getDescriptionId() {
        Identifier id = PrincipiaMagicaRegistries.PRINCIPLE.getKey(this);
        return "principle." + id.getNamespace() + "." + id.getPath();
    }

    /**
     * @return the translated name, coloured with the principle colour.
     */
    public Component name() {
        return Component.translatable(this.getDescriptionId()).withColor(this.color);
    }

    /**
     * Player-facing name that makes clear this is a principle, e.g. "Principle: Loam". Also makes principles searchable by
     * the word "principle" in JEI.
     *
     * @return the coloured name with the principle prefix.
     */
    public Component prefixedName() {
        return Component.translatable(PREFIXED_NAME_KEY, this.name());
    }

    /**
     * Icon texture for the principle with the given registry id, following the {@code <ns>:textures/principle/<path>.png} convention.
     */
    public static Identifier texture(Identifier principleId) {
        return principleId.withPath("textures/principle/" + principleId.getPath() + ".png");
    }
}
