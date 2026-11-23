// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.entity.GolemTask;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Locale;

/**
 * Text and state helpers shared by the golem GUI tabs and pages.
 */
final class GolemGuiText {

    private static final int TEXT_COLOR = 0xFF3B0F13;
    private static final int WARNING_COLOR = 0xFF8A5A0E;
    private static final int PROBLEM_COLOR = 0xFFA03A22;

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
        return switch (status.severity()) {
            case OK -> "status_ok";
            case IDLE -> "status_idle";
            case WARNING -> "status_warning";
            case PROBLEM -> "status_problem";
        };
    }

    /**
     * The first line of the status tablet. A status that is not OK wins over the task: the golem is not working on it while it is blocked.
     *
     * @return the status headline, e.g. "No deposit container", coloured by severity. The task while the golem works.
     */
    static Component headline(GolemStatus status, GolemTask task) {
        if (status == GolemStatus.OK) {
            return task.describe();
        }

        return Component.translatable(status.translationKey()).withStyle(Style.EMPTY.withColor(headlineColor(status)));
    }

    /**
     * The second line of the status tablet: the short hint of the status, what to do about it, or the position of the work target while the
     * golem works.
     */
    static Component hintLine(GolemStatus status, GolemTask task) {
        if (status != GolemStatus.OK) {
            return Component.translatable("gui.principiamagica.golem.status_hint." + status.name().toLowerCase(Locale.ROOT));
        }

        return task.target()
                .map(pos -> Component.translatable("gui.principiamagica.golem.task_at", formatPos(pos)))
                .orElse(Component.empty());
    }

    /**
     * The tooltip of the whole status tablet: the full text of the status and its explanation, so nothing is lost to the ellipsis.
     */
    static Component tooltip(GolemStatus status, GolemTask task) {
        if (status == GolemStatus.OK) {
            MutableComponent text = Component.empty().append(task.describe());
            return task.target()
                    .map(pos -> text.append("\n").append(Component.translatable("gui.principiamagica.golem.task_at", formatPos(pos))))
                    .orElse(text);
        }

        return Component.empty()
                .append(Component.translatable(status.translationKey()).withStyle(Style.EMPTY.withColor(headlineColor(status))))
                .append("\n").append(hintLine(status, task))
                .append("\n").append(status.description());
    }

    private static int headlineColor(GolemStatus status) {
        return switch (status.severity()) {
            case OK, IDLE -> TEXT_COLOR;
            case WARNING -> WARNING_COLOR;
            case PROBLEM -> PROBLEM_COLOR;
        };
    }

    static String formatPos(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
}
