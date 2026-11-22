// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.upgrade;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.spec.StatContributor;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * A golem upgrade. A golem can carry each upgrade at most once, so the modifier id is stable per upgrade type.
 * Upgrades contribute attribute modifiers and may run logic every server tick via {@link #onTick(GolemEntity)}.
 */
public class GolemUpgrade implements StatContributor {

    public record AttributeEntry(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
    }

    private final Identifier id;
    private final List<AttributeEntry> attributes;

    public GolemUpgrade(Identifier id, List<AttributeEntry> attributes) {
        this.id = id;
        this.attributes = attributes;
    }

    public Identifier id() {
        return this.id;
    }

    public List<AttributeEntry> attributes() {
        return this.attributes;
    }

    public String getDescriptionId() {
        Identifier key = PrincipiaMagicaRegistries.GOLEM_UPGRADE.getKey(this);
        return "golem_upgrade." + key.getNamespace() + "." + key.getPath();
    }

    public Component getName() {
        return Component.translatable(this.getDescriptionId());
    }

    @Override
    public void collectModifiers(BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        for (AttributeEntry entry : this.attributes) {
            consumer.accept(entry.attribute(), new AttributeModifier(this.id, entry.amount(), entry.operation()));
        }
    }

    /**
     * Called every server tick while the golem carries this upgrade. No-op by default.
     */
    public void onTick(GolemEntity golem) {
    }
}
