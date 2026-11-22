// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.modonomicon.data.TriggerType;
import com.klikli_dev.modonomicon.registry.TriggerTypeRegistry;
import com.klikli_dev.modonomicon.research.hook.NoTriggerTarget;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.progression.GolemEventContext;
import com.klikli_dev.principiamagica.content.golem.progression.GolemEventTriggerHandler;

public final class ResearchTriggerTypeRegistry {

    public static final TriggerType<NoTriggerTarget, GolemEventContext> GOLEM_PLACED = TriggerTypeRegistry.registerTargetless(
            PrincipiaMagica.loc("golem_placed"),
            new GolemEventTriggerHandler()
    );

    public static final TriggerType<NoTriggerTarget, GolemEventContext> GOLEM_GIVEN_AXIOM = TriggerTypeRegistry.registerTargetless(
            PrincipiaMagica.loc("golem_given_axiom"),
            new GolemEventTriggerHandler()
    );

    private ResearchTriggerTypeRegistry() {
    }

    public static void bootstrap() {
    }
}
