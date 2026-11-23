// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.entity.GolemTask;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Text and state helpers shared by the golem GUI tabs and pages.
 */
final class GolemGuiText {

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

    /**
     * @return the key of the status icon: ok, idle (no purpose), warning (the golem can be fixed in its setup) or problem (it is blocked)
     */
    static String statusIcon(GolemStatus status) {
        return switch (status) {
            case OK -> "status_ok";
            case NO_PURPOSE -> "status_idle";
            case MISSING_TOOL, NEEDS_SUPPLIES -> "status_warning";
            case NO_ZONE, NO_DEPOSIT, DEPOSIT_FULL -> "status_problem";
        };
    }

    /**
     * @return the second line of the status tablet: the status if it is a problem, otherwise the position of the work target
     */
    static Component detailLine(GolemStatus status, GolemTask task) {
        if (status != GolemStatus.OK) {
            return status.description();
        }

        return task.target()
                .map(pos -> Component.translatable("gui.principiamagica.golem.task_at", formatPos(pos)))
                .orElse(Component.empty());
    }

    static String formatPos(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
}
