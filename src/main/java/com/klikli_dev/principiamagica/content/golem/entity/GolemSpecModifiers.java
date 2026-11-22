// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies the attribute modifiers of a golem's spec. The modifiers applied last time are removed first, so a spec change
 * never stacks them.
 */
public class GolemSpecModifiers {

    private record AppliedModifier(Holder<Attribute> attribute, Identifier id) {
    }

    private final List<AppliedModifier> applied = new ArrayList<>();

    public void apply(GolemEntity golem, GolemSpec spec) {
        for (AppliedModifier applied : this.applied) {
            AttributeInstance instance = golem.getAttribute(applied.attribute());
            if (instance != null) {
                instance.removeModifier(applied.id());
            }
        }
        this.applied.clear();

        spec.collectModifiers((attribute, modifier) -> {
            AttributeInstance instance = golem.getAttribute(attribute);
            if (instance != null) {
                instance.addOrUpdateTransientModifier(modifier);
                this.applied.add(new AppliedModifier(attribute, modifier.id()));
            }
        });
    }
}
