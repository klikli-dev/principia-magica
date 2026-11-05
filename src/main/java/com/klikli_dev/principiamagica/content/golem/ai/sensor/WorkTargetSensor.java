// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai.sensor;

import com.klikli_dev.principiamagica.content.golem.ai.behaviour.MoveToAndInteract;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.WorkTargetFinder;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Incrementally scans the workplace zones with the job's {@link WorkTargetFinder} and keeps the
 * nearest valid candidates in WORK_CANDIDATES. Each run only checks a budget of positions; a cursor
 * remembers where the previous run stopped.
 */
public class WorkTargetSensor extends Sensor<GolemEntity> {
    public static final int SCAN_RATE = 5;
    public static final int SCAN_BUDGET = 96;
    public static final int MAX_CANDIDATES = 64;

    private int cursor;

    public WorkTargetSensor() {
        super(SCAN_RATE);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(MemoryModuleTypeRegistry.WORK_CANDIDATES.value());
    }

    @Override
    protected void doTick(ServerLevel level, GolemEntity body) {
        Optional<Workplace> workplace = body.getWorkplace(level);
        Optional<GolemJob> job = body.getSpec().job().map(Holder::value);
        if (workplace.isEmpty() || job.isEmpty()) {
            body.getBrain().eraseMemory(MemoryModuleTypeRegistry.WORK_CANDIDATES.value());
            this.cursor = 0;
            return;
        }

        WorkTargetFinder finder = job.get().targetFinder();
        List<WorkZone> zones = workplace.get().zones();
        int total = zones.stream().mapToInt(WorkZone::volume).sum();
        List<BlockPos> candidates = new ArrayList<>(body.getBrain().getMemory(MemoryModuleTypeRegistry.WORK_CANDIDATES.value()).orElse(List.of()));
        candidates.removeIf(pos -> !workplace.get().containsInZone(pos) || !finder.isTarget(level, body, pos));

        GolemStatus blocked = null;
        if (total > 0) {
            for (int i = 0; i < SCAN_BUDGET && candidates.size() < MAX_CANDIDATES; i++) {
                BlockPos pos = this.positionAt(zones, this.cursor);
                this.cursor = (this.cursor + 1) % total;

                if (candidates.contains(pos) || !MoveToAndInteract.isAvailable(level, body, pos)) {
                    continue;
                }

                if (finder.isTarget(level, body, pos)) {
                    candidates.add(pos);
                } else {
                    blocked = higherPriority(blocked, finder.blockedReason(level, body, pos).orElse(null));
                }
            }
        }

        candidates.sort(Comparator.comparingDouble(pos -> pos.distSqr(body.blockPosition())));
        body.getBrain().setMemory(MemoryModuleTypeRegistry.WORK_CANDIDATES.value(), candidates);

        if (candidates.isEmpty() && blocked != null) {
            body.reportBlocker(blocked);
        }
    }

    private static GolemStatus higherPriority(GolemStatus current, @Nullable GolemStatus candidate) {
        if (candidate == null) {
            return current;
        }

        return current == null || candidate.ordinal() < current.ordinal() ? candidate : current;
    }

    private BlockPos positionAt(List<WorkZone> zones, int index) {
        int remaining = index;
        for (WorkZone zone : zones) {
            if (remaining < zone.volume()) {
                return zone.positionAt(remaining);
            }
            remaining -= zone.volume();
        }

        return zones.getFirst().min();
    }
}
