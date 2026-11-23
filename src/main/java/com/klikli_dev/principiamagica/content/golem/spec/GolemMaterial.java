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

/**
 * A golem material: the texture of the golem and the attribute modifiers it adds to the base golem.
 * Materials are registered in the golem material registry, which is the extension point for new materials.
 */
public record GolemMaterial(Identifier texture, List<ModifierEntry> modifiers) {

    public record ModifierEntry(Holder<Attribute> attribute, AttributeModifier modifier) {
    }

    public static ModifierEntry modifier(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        return new ModifierEntry(attribute, new AttributeModifier(PrincipiaMagica.loc("material"), amount, operation));
    }

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
