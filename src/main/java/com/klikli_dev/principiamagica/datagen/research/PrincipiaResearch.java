// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.research;

import com.klikli_dev.modonomicon.api.datagen.research.ResearchNodeRef;
import com.klikli_dev.modonomicon.api.datagen.research.SingleResearchSubProvider;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.advancement.PrincipiaMagicaAdvancementSubProvider;
import net.minecraft.resources.Identifier;

public class PrincipiaResearch extends SingleResearchSubProvider {

    public static final ResearchNodeRef A_WEDGE_OF_COPPER = node("research/advancement/a_wedge_of_copper");
    public static final ResearchNodeRef WET_CLAY = node("research/advancement/wet_clay");

    public PrincipiaResearch() {
        super("principia", PrincipiaMagica.MODID);
    }

    @Override
    protected void generateResearch() {
        this.node(A_WEDGE_OF_COPPER, this.ingress()
                .onAdvancementEarned(PrincipiaMagicaAdvancementSubProvider.A_WEDGE_OF_COPPER)
                .declareFact("research/advancement/a_wedge_of_copper"));

        this.node(WET_CLAY, this.ingress()
                .onAdvancementEarned(PrincipiaMagicaAdvancementSubProvider.WET_CLAY)
                .declareFact("research/advancement/wet_clay"));

        this.researchNodeName(A_WEDGE_OF_COPPER, "A Wedge of Copper");
        this.researchNodeName(WET_CLAY, "Wet Clay");
    }

    static ResearchNodeRef node(String path) {
        return ResearchNodeRef.of(Identifier.fromNamespaceAndPath(PrincipiaMagica.MODID, path));
    }
}
