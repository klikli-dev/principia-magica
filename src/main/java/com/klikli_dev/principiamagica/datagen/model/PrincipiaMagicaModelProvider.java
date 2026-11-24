// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.model;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.client.principle.PrincipleTintSource;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlock;
import com.klikli_dev.principiamagica.content.libation.slab.SpoutSide;
import com.klikli_dev.principiamagica.content.libation.vessel.SteepingJarBlock;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.registry.BlockRegistry;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class PrincipiaMagicaModelProvider extends ModelProvider {

    public PrincipiaMagicaModelProvider(PackOutput packOutput) {
        super(packOutput, PrincipiaMagica.MODID);
    }

    @Override
    public @NotNull String getName() {
        return "Model Definitions - " + this.modId;
    }

    @Override
    protected void registerModels(@NotNull BlockModelGenerators blockModels, @NotNull ItemModelGenerators itemModels) {
        GolemItemModels.register(itemModels);
        itemModels.generateFlatItem(ItemRegistry.COPPER_STYLUS.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ItemRegistry.IMPRINT_TABLET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemRegistry.PRINCIPIA.get(), ModelTemplates.FLAT_ITEM);
        Identifier axiomTabletModel = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(ItemRegistry.AXIOM_TABLET.get()),
                TextureMapping.layer0(new Material(PrincipiaMagica.loc("item/axiom_unfired"))), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(ItemRegistry.AXIOM_TABLET.get(), ItemModelUtils.plainModel(axiomTabletModel));
        itemModels.generateFlatItem(ItemRegistry.QUICKENED_CLAY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemRegistry.AXIOM_OF_FELLING_UNFIRED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemRegistry.AXIOM_OF_FELLING_UNFIRED_STEEPED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemRegistry.AXIOM_OF_HARVEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemRegistry.AXIOM_OF_FELLING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemRegistry.NATRON_SPONGE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemRegistry.UNFIRED_CLAY_PHIAL.get(), ModelTemplates.FLAT_ITEM);

        Identifier emptyPhialModel = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(ItemRegistry.CLAY_PHIAL.get()),
                TextureMapping.layer0(ItemRegistry.CLAY_PHIAL.get()),
                itemModels.modelOutput);
        Identifier filledPhialModel = ModelTemplates.TWO_LAYERED_ITEM.create(
                ModelLocationUtils.getModelLocation(ItemRegistry.CLAY_PHIAL.get(), "_filled"),
                TextureMapping.layered(
                        new Material(PrincipiaMagica.loc("item/clay_phial_filled")),
                        new Material(PrincipiaMagica.loc("item/clay_phial_overlay"))),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(ItemRegistry.CLAY_PHIAL.get(),
                ItemModelUtils.conditional(
                        ItemModelUtils.hasComponent(DataComponentRegistry.STORED_PRINCIPLES.get()),
                        ItemModelUtils.tintedModel(filledPhialModel, new Constant(-1), new PrincipleTintSource(PrincipleContents.DEFAULT_COLOR)),
                        ItemModelUtils.plainModel(emptyPhialModel)));

        slabBlockState(blockModels, BlockRegistry.LIBATION_SLAB.get());
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(BlockRegistry.CATCH_TROUGH.get(), plainVariant("catch_trough")));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(BlockRegistry.DRYING_SLAB.get(), plainVariant("drying_slab")));

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(BlockRegistry.WABET_BASIN.get(), plainVariant("wabet_basin")));
        jarBlockState(blockModels, BlockRegistry.STEEPING_JAR.get());

        openableBlockState(blockModels, BlockRegistry.LIBATION_SPOUT.get(), "libation_spout_off", "libation_spout_on");
        openableBlockState(blockModels, BlockRegistry.VESSEL_TAP.get(), "vessel_tap_closed", "vessel_tap_open");
    }

    /**
     * The rim has a hole on the side where a spout is attached. Without a spout the rim is closed.
     */
    private static void slabBlockState(BlockModelGenerators blockModels, Block block) {
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        generator.with(new ConditionBuilder().term(LibationSlabBlock.SPOUT, SpoutSide.NONE), plainVariant("libation_slab"));
        for (Direction side : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder().term(LibationSlabBlock.SPOUT, SpoutSide.of(side)), plainVariant("libation_slab_spout").with(yRotation(side)));
        }
        blockModels.blockStateOutput.accept(generator);
    }

    /**
     * The jar gets its lid while it cures.
     */
    private static void jarBlockState(BlockModelGenerators blockModels, Block block) {
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        generator.with(plainVariant("steeping_jar"));
        generator.with(new ConditionBuilder().term(SteepingJarBlock.LIDDED, true), plainVariant("steeping_jar_lid"));
        blockModels.blockStateOutput.accept(generator);
    }

    /**
     * The model is rotated by facing and picked by OPEN, so each facing gets its own parts.
     */
    private static void openableBlockState(BlockModelGenerators blockModels, Block block, String closedName, String openName) {
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            VariantMutator rotation = yRotation(facing);
            generator.with(facingCondition(facing).term(BlockStateProperties.OPEN, false), plainVariant(closedName).with(rotation));
            generator.with(facingCondition(facing).term(BlockStateProperties.OPEN, true), plainVariant(openName).with(rotation));
        }
        blockModels.blockStateOutput.accept(generator);
    }

    private static MultiVariant plainVariant(String name) {
        return BlockModelGenerators.plainVariant(PrincipiaMagica.loc("block/" + name));
    }

    private static ConditionBuilder facingCondition(Direction facing) {
        return new ConditionBuilder().term(BlockStateProperties.HORIZONTAL_FACING, facing);
    }

    private static VariantMutator yRotation(Direction facing) {
        return switch (facing) {
            case EAST -> BlockModelGenerators.Y_ROT_90;
            case SOUTH -> BlockModelGenerators.Y_ROT_180;
            case WEST -> BlockModelGenerators.Y_ROT_270;
            default -> BlockModelGenerators.NOP;
        };
    }
}
