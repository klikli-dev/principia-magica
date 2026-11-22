// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.advancement;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.advancement.GolemGivenAxiomTrigger;
import com.klikli_dev.principiamagica.content.golem.advancement.GolemPlacedTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Hidden milestones without a display, so they have no tab, no toast and no icon. They exist for quest mods to hook into.
 * Each one is its own root, as a root without a display has no tab in the advancement screen.
 */
public class PrincipiaMagicaAdvancementSubProvider implements AdvancementSubProvider {

    public static final Identifier GOLEM_PLACED = PrincipiaMagica.loc("golem_placed");
    public static final Identifier GOLEM_GIVEN_AXIOM = PrincipiaMagica.loc("golem_given_axiom");

    @Override
    public void generate(HolderLookup.@NotNull Provider registries, @NotNull Consumer<AdvancementHolder> writer) {
        var golemPlaced = Advancement.Builder.advancement()
                .addCriterion("golem_placed", GolemPlacedTrigger.placed())
                .build(GOLEM_PLACED);

        writer.accept(golemPlaced);

        var golemGivenAxiom = Advancement.Builder.advancement()
                .addCriterion("golem_given_axiom", GolemGivenAxiomTrigger.given())
                .build(GOLEM_GIVEN_AXIOM);

        writer.accept(golemGivenAxiom);
    }
}
