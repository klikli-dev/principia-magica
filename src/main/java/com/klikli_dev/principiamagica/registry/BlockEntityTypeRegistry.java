// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.libation.drying.DryingSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.spout.LibationSpoutBlockEntity;
import com.klikli_dev.principiamagica.content.libation.spout.VesselTapBlockEntity;
import com.klikli_dev.principiamagica.content.libation.trough.CatchTroughBlockEntity;
import com.klikli_dev.principiamagica.content.libation.vessel.CuringVesselBlockEntity;
import com.klikli_dev.principiamagica.content.libation.vessel.VesselProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityTypeRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PrincipiaMagica.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LibationSlabBlockEntity>> LIBATION_SLAB = BLOCK_ENTITY_TYPES.register("libation_slab",
            () -> new BlockEntityType<>(LibationSlabBlockEntity::new, BlockRegistry.LIBATION_SLAB.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LibationSpoutBlockEntity>> LIBATION_SPOUT = BLOCK_ENTITY_TYPES.register("libation_spout",
            () -> new BlockEntityType<>(LibationSpoutBlockEntity::new, BlockRegistry.LIBATION_SPOUT.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VesselTapBlockEntity>> VESSEL_TAP = BLOCK_ENTITY_TYPES.register("vessel_tap",
            () -> new BlockEntityType<>(VesselTapBlockEntity::new, BlockRegistry.VESSEL_TAP.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CatchTroughBlockEntity>> CATCH_TROUGH = BLOCK_ENTITY_TYPES.register("catch_trough",
            () -> new BlockEntityType<>(CatchTroughBlockEntity::new, BlockRegistry.CATCH_TROUGH.get()));

    /**
     * The vessels of both types are the same block entity, configured by their properties. The factories refer to the holders
     * by their qualified names, which is fine because they only run once the registry is filled.
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CuringVesselBlockEntity>> WABET_BASIN = BLOCK_ENTITY_TYPES.register("wabet_basin",
            () -> new BlockEntityType<>((pos, state) -> new CuringVesselBlockEntity(BlockEntityTypeRegistry.WABET_BASIN.get(), pos, state, VesselProperties.WABET_BASIN),
                    BlockRegistry.WABET_BASIN.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CuringVesselBlockEntity>> STEEPING_JAR = BLOCK_ENTITY_TYPES.register("steeping_jar",
            () -> new BlockEntityType<>((pos, state) -> new CuringVesselBlockEntity(BlockEntityTypeRegistry.STEEPING_JAR.get(), pos, state, VesselProperties.STEEPING_JAR),
                    BlockRegistry.STEEPING_JAR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingSlabBlockEntity>> DRYING_SLAB = BLOCK_ENTITY_TYPES.register("drying_slab",
            () -> new BlockEntityType<>(DryingSlabBlockEntity::new, BlockRegistry.DRYING_SLAB.get()));
}
