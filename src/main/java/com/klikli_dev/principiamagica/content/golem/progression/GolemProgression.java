// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.progression;

import com.klikli_dev.modonomicon.bookstate.BookVisualStateManager;
import com.klikli_dev.modonomicon.data.TriggerType;
import com.klikli_dev.modonomicon.research.ResearchServices;
import com.klikli_dev.modonomicon.research.hook.NoTriggerTarget;
import com.klikli_dev.modonomicon.research.state.ResearchStateManager;
import com.klikli_dev.principiamagica.registry.CriterionTriggerRegistry;
import com.klikli_dev.principiamagica.registry.ResearchTriggerTypeRegistry;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side golem events for a player. Each event drives the advancement criterion and the research hook of the same name.
 */
public final class GolemProgression {

    private GolemProgression() {
    }

    public static void onGolemPlaced(ServerPlayer player) {
        CriterionTriggerRegistry.GOLEM_PLACED.get().trigger(player);
        fire(ResearchTriggerTypeRegistry.GOLEM_PLACED, player);
    }

    public static void onAxiomGiven(ServerPlayer player) {
        CriterionTriggerRegistry.GOLEM_GIVEN_AXIOM.get().trigger(player);
        fire(ResearchTriggerTypeRegistry.GOLEM_GIVEN_AXIOM, player);
    }

    private static void fire(TriggerType<NoTriggerTarget, GolemEventContext> type, ServerPlayer player) {
        if (ResearchServices.hooks().fire(type, new GolemEventContext(player))) {
            ResearchStateManager.get().syncFor(player);
            BookVisualStateManager.get().syncFor(player);
        }
    }
}
