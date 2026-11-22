// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.spec;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.function.BiConsumer;

public class GolemMaterial implements StatContributor {

    public record ModifierEntry(Holder<Attribute> attribute, AttributeModifier modifier) {
    }

    private final Identifier texture;
    private final boolean fireImmune;
    private final List<ModifierEntry> modifiers;

    public GolemMaterial(Identifier texture, boolean fireImmune, List<ModifierEntry> modifiers) {
        this.texture = texture;
        this.fireImmune = fireImmune;
        this.modifiers = modifiers;
    }

    public static ModifierEntry modifier(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        return new ModifierEntry(attribute, new AttributeModifier(PrincipiaMagica.loc("material"), amount, operation));
    }

    public Identifier texture() {
        return this.texture;
    }

    public boolean fireImmune() {
        return this.fireImmune;
    }

    @Override
    public void collectModifiers(BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        for (ModifierEntry entry : this.modifiers) {
            consumer.accept(entry.attribute(), entry.modifier());
        }
    }

    public String getDescriptionId() {
        Identifier id = PrincipiaMagicaRegistries.GOLEM_MATERIAL.getKey(this);
        return "golem_material." + id.getNamespace() + "." + id.getPath();
    }
}
