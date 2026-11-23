// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.progression;

import com.klikli_dev.modonomicon.data.TriggerType;
import com.klikli_dev.modonomicon.research.data.ResearchDataManager;
import com.klikli_dev.modonomicon.research.data.ResearchHookDefinition;
import com.klikli_dev.modonomicon.research.hook.NoTriggerTarget;
import com.klikli_dev.modonomicon.research.hook.TriggerHandler;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Golem events are targetless: every hook of the trigger type applies to every event.
 */
public class GolemEventTriggerHandler implements TriggerHandler<NoTriggerTarget, GolemEventContext> {

    @Override
    public List<ResearchHookDefinition<NoTriggerTarget>> resolve(TriggerType<NoTriggerTarget, GolemEventContext> type, ServerPlayer player, GolemEventContext context) {
        return ResearchDataManager.get().hooksForType(type);
    }

    @Override
    public boolean matches(NoTriggerTarget target, GolemEventContext context) {
        return true;
    }
}
