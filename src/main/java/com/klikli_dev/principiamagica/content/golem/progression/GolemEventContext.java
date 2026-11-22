// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.progression;

import com.klikli_dev.modonomicon.research.hook.TriggerContext;
import net.minecraft.server.level.ServerPlayer;

/**
 * Context of a golem event that advances research, see {@link GolemProgression}.
 */
public record GolemEventContext(ServerPlayer player) implements TriggerContext {
}
