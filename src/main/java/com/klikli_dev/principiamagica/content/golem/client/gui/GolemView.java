// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.upgrade.GolemUpgrade;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A snapshot of the golem state the golem GUI shows. The screen rebuilds it every tick; pages and tabs read it on demand.
 *
 * @param golemId the entity id of the golem
 * @param golem the golem entity on the client, or null if it is not loaded
 * @param workplaceKnown whether the server has sent the workplace for this golem. Detail pages only pop
 *                       because data is missing if this is true, so the empty placeholder is never mistaken for data
 * @param workplace the workplace, or {@link Workplace#EMPTY} if not known yet
 * @param mode the current mode
 * @param job the job core's job, if a core is inserted
 * @param upgrades the installed upgrades
 * @param upgradeSlots the upgrade slots of the material
 * @param usableSlots the number of golem inventory slots that are usable
 * @param status the current status of the golem
 */
public record GolemView(
        int golemId,
        @Nullable GolemEntity golem,
        boolean workplaceKnown,
        Workplace workplace,
        GolemMode mode,
        Optional<Holder<GolemJob>> job,
        List<Holder<GolemUpgrade>> upgrades,
        int upgradeSlots,
        int usableSlots,
        GolemStatus status
) {
}
