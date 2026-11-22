// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

/**
 * Remembers which statuses behaviours and sensors recently reported as blocking the golem. Each report stays active for a
 * short time, so the status does not flicker while the checks run on cooldowns.
 */
public class GolemBlockers {
    private static final int BLOCKER_TICKS = 40;

    private final Map<GolemStatus, Long> blockers = new EnumMap<>(GolemStatus.class);

    public void report(GolemStatus status, long gameTime) {
        this.blockers.put(status, gameTime + BLOCKER_TICKS);
    }

    /**
     * @return the highest priority blocker that was reported recently, or OK if there is none
     */
    public GolemStatus active(long gameTime) {
        this.blockers.values().removeIf(expiry -> expiry < gameTime);
        return this.blockers.keySet().stream().min(Comparator.comparingInt(GolemStatus::ordinal)).orElse(GolemStatus.OK);
    }
}
