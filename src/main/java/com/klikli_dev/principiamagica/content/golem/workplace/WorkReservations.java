// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.workplace;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Transient, per-level claims on work positions so two golems never target the same block.
 * Claims expire on their own; expired claims are dropped whenever a level's map is accessed.
 */
public final class WorkReservations {

    private static final Map<ServerLevel, Map<BlockPos, Reservation>> RESERVATIONS = new WeakHashMap<>();

    private WorkReservations() {
    }

    public static boolean tryReserve(ServerLevel level, BlockPos pos, UUID golem, int ticks) {
        Map<BlockPos, Reservation> reservations = reservationsOf(level);
        Reservation existing = reservations.get(pos);
        if (existing != null && !existing.golem().equals(golem)) {
            return false;
        }

        reservations.put(pos, new Reservation(golem, level.getGameTime() + ticks));
        return true;
    }

    public static boolean isReservedByOther(ServerLevel level, BlockPos pos, UUID golem) {
        Reservation existing = reservationsOf(level).get(pos);
        return existing != null && !existing.golem().equals(golem);
    }

    public static void release(ServerLevel level, BlockPos pos, UUID golem) {
        Map<BlockPos, Reservation> reservations = reservationsOf(level);
        Reservation existing = reservations.get(pos);
        if (existing != null && existing.golem().equals(golem)) {
            reservations.remove(pos);
        }
    }

    private static Map<BlockPos, Reservation> reservationsOf(ServerLevel level) {
        Map<BlockPos, Reservation> reservations = RESERVATIONS.computeIfAbsent(level, key -> new HashMap<>());
        long now = level.getGameTime();
        reservations.values().removeIf(reservation -> reservation.expiresAt() < now);
        return reservations;
    }

    private record Reservation(UUID golem, long expiresAt) {
    }
}
