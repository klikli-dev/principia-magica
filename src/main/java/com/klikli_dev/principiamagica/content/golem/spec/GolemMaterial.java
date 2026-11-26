// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.spec;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * A golem material: the texture of the golem, the attribute modifiers it adds to the base golem, whether golems of it regenerate,
 * and the material-specific sounds it uses.
 * Materials are registered in the golem material registry, which is the extension point for new materials.
 *
 * @param regenInterval the ticks it takes a golem of this material to regenerate one health, 0 if it does not regenerate
 */
public record GolemMaterial(Identifier texture, List<ModifierEntry> modifiers, int regenInterval, Sounds sounds) {

    public record Sounds(Holder<SoundEvent> place, Holder<SoundEvent> pickUp) {
        public static final Sounds DEFAULT = new Sounds(
                SoundRegistry.GOLEM_PLACE,
                SoundRegistry.GOLEM_PICK_UP
        );
    }

    public GolemMaterial(Identifier texture, List<ModifierEntry> modifiers, int regenInterval) {
        this(texture, modifiers, regenInterval, Sounds.DEFAULT);
    }

    /**
     * A material that does not regenerate. The base golem has no regeneration, materials opt in via the canonical constructor.
     */
    public GolemMaterial(Identifier texture, List<ModifierEntry> modifiers) {
        this(texture, modifiers, 0, Sounds.DEFAULT);
    }

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
