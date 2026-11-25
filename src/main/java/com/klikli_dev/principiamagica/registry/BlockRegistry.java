// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.libation.drying.DryingSlabBlock;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlock;
import com.klikli_dev.principiamagica.content.libation.spout.LibationSpoutBlock;
import com.klikli_dev.principiamagica.content.libation.spout.VesselTapBlock;
import com.klikli_dev.principiamagica.content.libation.trough.CatchTroughBlock;
import com.klikli_dev.principiamagica.content.libation.vessel.SteepingJarBlock;
import com.klikli_dev.principiamagica.content.libation.vessel.WabetBasinBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PrincipiaMagica.MODID);

    /**
     * Bare hands take {@code hardness * 100} ticks, so 0.6 matches breaking planks by hand (3s), and blocks drop their
     * contents regardless of the tool used. The mineable pickaxe tag makes pickaxes faster.
     */
    private static final float HARDNESS = 0.6F;

    public static final DeferredBlock<LibationSlabBlock> LIBATION_SLAB = BLOCKS.registerBlock("libation_slab", LibationSlabBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(HARDNESS, 6.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final DeferredBlock<LibationSpoutBlock> LIBATION_SPOUT = BLOCKS.registerBlock("libation_spout", LibationSpoutBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(HARDNESS, 6.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final DeferredBlock<VesselTapBlock> VESSEL_TAP = BLOCKS.registerBlock("vessel_tap", VesselTapBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(HARDNESS, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final DeferredBlock<CatchTroughBlock> CATCH_TROUGH = BLOCKS.registerBlock("catch_trough", CatchTroughBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .strength(HARDNESS, 4.2F)
                    .sound(SoundType.DECORATED_POT)
                    .noOcclusion());

    public static final DeferredBlock<WabetBasinBlock> WABET_BASIN = BLOCKS.registerBlock("wabet_basin", WabetBasinBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(HARDNESS, 0.8F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final DeferredBlock<SteepingJarBlock> STEEPING_JAR = BLOCKS.registerBlock("steeping_jar", SteepingJarBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .strength(HARDNESS, 6.0F)
                    .sound(SoundType.DECORATED_POT)
                    .noOcclusion());

    public static final DeferredBlock<DryingSlabBlock> DRYING_SLAB = BLOCKS.registerBlock("drying_slab", DryingSlabBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(HARDNESS, 0.8F)
                    .sound(SoundType.STONE)
                    .noOcclusion());
}
