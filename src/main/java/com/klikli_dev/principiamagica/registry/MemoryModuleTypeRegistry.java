// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class MemoryModuleTypeRegistry {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, PrincipiaMagica.MODID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<UUID>> OWNER = MEMORY_MODULE_TYPES.register("owner", () -> new MemoryModuleType<>(Optional.of(UUIDUtil.CODEC)));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<GlobalPos>> ANCHOR = MEMORY_MODULE_TYPES.register("anchor", () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<BlockPos>>> WORK_CANDIDATES = MEMORY_MODULE_TYPES.register("work_candidates", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockPos>> WORK_TARGET = MEMORY_MODULE_TYPES.register("work_target", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Set<BlockPos>>> UNREACHABLE_TARGETS = MEMORY_MODULE_TYPES.register("unreachable_targets", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> RESTOCK_COOLDOWN = MEMORY_MODULE_TYPES.register("restock_cooldown", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> DEPOSIT_COOLDOWN = MEMORY_MODULE_TYPES.register("deposit_cooldown", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> PICKUP_COOLDOWN = MEMORY_MODULE_TYPES.register("pickup_cooldown", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Map<BlockPos, Integer>>> SAPLING_BONEMEAL_ATTEMPTS = MEMORY_MODULE_TYPES.register("sapling_bonemeal_attempts", () -> new MemoryModuleType<>(Optional.empty()));
}
