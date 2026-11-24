// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.research;

import com.klikli_dev.modonomicon.api.datagen.research.ResearchDisplayModel;
import com.klikli_dev.modonomicon.api.datagen.research.ResearchFactRef;
import com.klikli_dev.modonomicon.api.datagen.research.SingleResearchSubProvider;
import com.klikli_dev.modonomicon.research.data.ResearchGroups;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.ResearchTriggerTypeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class PrincipiaResearch extends SingleResearchSubProvider {

    public static final ResearchFactRef GOLEM_PLACED = factRef("golem_placed");
    public static final ResearchFactRef GOLEM_GIVEN_AXIOM = factRef("golem_given_axiom");

    public PrincipiaResearch() {
        super("principia", PrincipiaMagica.MODID);
    }

    @Override
    protected void generateResearch() {
        var golemPlacedFact = this.hook()
                .on(ResearchTriggerTypeRegistry.GOLEM_PLACED)
                .declareFact(GOLEM_PLACED.id().getPath());
        this.factDisplay(golemPlacedFact, ItemRegistry.GOLEM.get(), "Place a golem",
                "A body must stand on its own before it can be given a purpose.");

        var golemGivenAxiomFact = this.hook()
                .on(ResearchTriggerTypeRegistry.GOLEM_GIVEN_AXIOM)
                .declareFact(GOLEM_GIVEN_AXIOM.id().getPath());
        this.factDisplay(golemGivenAxiomFact, ItemRegistry.AXIOM_OF_HARVEST.get(), "Give a golem an axiom",
                "Hold an axiom and right-click the golem with it.");
    }

    /**
     * Sets the icon, title and description shown for a fact in requirement blocks.
     * Golem events have no default presenter, so the facts are grouped into the knowledge category, like the advancements they replace.
     */
    private void factDisplay(ResearchFactRef fact, Item icon, String title, @Nullable String description) {
        var titleKey = Util.makeDescriptionId("research_fact", fact.id());
        var descriptionKey = description != null ? titleKey + ".description" : null;
        this.research().display(fact, ResearchDisplayModel.create()
                .withIcon(icon)
                .withTitle(titleKey)
                .withDescription(descriptionKey)
                .withGroup(ResearchGroups.KNOWLEDGE));
        this.add(titleKey, title);
        if (description != null) {
            this.add(descriptionKey, description);
        }
    }

    static ResearchFactRef factRef(String path) {
        return ResearchFactRef.of(Identifier.fromNamespaceAndPath(PrincipiaMagica.MODID, path));
    }
}
