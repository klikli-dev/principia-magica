// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Text and color helpers shared by the golem GUI tabs and pages.
 */
final class GolemGuiText {

    static final int OK_COLOR = 0xFF1B5E20;
    static final int NEUTRAL_COLOR = 0xFF707070;
    static final int PROBLEM_COLOR = 0xFFA01818;

    private GolemGuiText() {
    }

    static Component golemName(GolemView view) {
        if (view.golem() == null) {
            return Component.empty();
        }

        if (view.golem().getCustomName() != null) {
            return view.golem().getCustomName();
        }

        return Component.translatable("gui.principiamagica.golem.title", materialName(view));
    }

    static Component materialName(GolemView view) {
        return view.golem() != null
                ? Component.translatable(view.golem().getSpec().material().value().getDescriptionId())
                : Component.empty();
    }

    static Component jobName(GolemView view) {
        return view.job().map(holder -> holder.value().getName()).orElse(Component.translatable("gui.principiamagica.golem.no_axiom"));
    }

    static Component modeName(GolemMode mode) {
        return Component.translatable("golem_mode.principiamagica." + mode.name().toLowerCase(Locale.ROOT));
    }

}
