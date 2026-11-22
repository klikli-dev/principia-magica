// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.vessel;

import com.klikli_dev.principiamagica.content.libation.LibationBlockEntity;
import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.content.libation.PrincipleDrain;
import com.klikli_dev.principiamagica.content.libation.PrincipleStorage;
import com.klikli_dev.principiamagica.content.principle.PrincipleContainer;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

/**
 * Holds principles and cures them into compound principles. Drains through an open spout into the drain target below the
 * spout, at the rate of {@link LibationFlow}. What the vessel is, is defined by its {@link VesselProperties}.
 */
public class CuringVesselBlockEntity extends LibationBlockEntity implements PrincipleContainer {
    public static final int SYNC_INTERVAL = 20;

    private final VesselProperties vessel;
    private final PrincipleStorage principles;
    private final PrincipleDrain drain;
    private int progress;
    private boolean curing;
    private CuringRecipe activeRecipe;

    public CuringVesselBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, VesselProperties vessel) {
        super(type, pos, state);
        this.vessel = vessel;
        this.principles = new PrincipleStorage(vessel.capacity(), () -> LibationUtils.sync(this));
        this.drain = new PrincipleDrain(this.principles);
    }

    public VesselProperties properties() {
        return this.vessel;
    }

    public PrincipleContents getPrinciples() {
        return this.principles.contents();
    }

    @Override
    public int comparatorSignal() {
        return this.principles.comparatorSignal();
    }

    /**
     * @return the blended colour of the principles drained last, see {@link PrincipleDrain#color()}.
     */
    public int getDrainColor() {
        return this.drain.color();
    }

    public boolean isCuring() {
        return this.curing;
    }

    /**
     * @return the number of ticks the current curing recipe has progressed.
     */
    public int getProgress() {
        return this.progress;
    }

    @Override
    public PrincipleContents insert(PrincipleContents offered, boolean simulate) {
        return this.principles.insert(offered, simulate);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CuringVesselBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        blockEntity.drain.tick(blockEntity);
        blockEntity.tickCuring(level, pos);
    }

    private void tickCuring(Level level, BlockPos pos) {
        int light = level.getMaxLocalRawBrightness(pos);
        if (!this.vessel.canCure(light) || this.principles.isEmpty()) {
            this.resetCuring();
            return;
        }

        Optional<RecipeHolder<CuringRecipe>> found = LibationUtils.curing(level, this.principles.contents(), this.vessel.environment());
        if (found.isEmpty()) {
            this.resetCuring();
            return;
        }

        CuringRecipe recipe = found.get().value();
        if (!recipe.equals(this.activeRecipe)) {
            this.activeRecipe = recipe;
            this.progress = 0;
        }
        this.progress++;

        if (this.progress >= this.vessel.requiredTime(recipe.time(), light)) {
            this.principles.set(this.principles.contents().subtract(recipe.inputs()).add(PrincipleContents.of(recipe.result())));
            this.progress = 0;
            this.activeRecipe = null;
            this.level.playSound(null, pos, SoundRegistry.VESSEL_CURE.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
            this.setCuring(true);
            LibationUtils.sync(this);
            return;
        }

        if (!this.curing) {
            this.setCuring(true);
            LibationUtils.sync(this);
        } else if (this.progress % SYNC_INTERVAL == 0) {
            LibationUtils.sync(this);
        }
    }

    private void resetCuring() {
        boolean wasCuring = this.curing;
        this.progress = 0;
        this.activeRecipe = null;
        this.setCuring(false);
        if (wasCuring) {
            LibationUtils.sync(this);
        }
    }

    private void setCuring(boolean curing) {
        if (this.curing != curing) {
            this.curing = curing;
            this.updateLid();
        }
    }

    /**
     * Shows the lid of the vessel while it cures, if the vessel has one.
     */
    private void updateLid() {
        if (this.vessel.lid() == null || this.level == null || this.level.isClientSide()) {
            return;
        }
        BlockState state = this.getBlockState();
        if (state.hasProperty(this.vessel.lid()) && state.getValue(this.vessel.lid()) != this.curing) {
            this.level.setBlock(this.worldPosition, state.setValue(this.vessel.lid(), this.curing), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        this.principles.set(components.getOrDefault(DataComponentRegistry.STORED_PRINCIPLES.get(), PrincipleContents.EMPTY));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        if (!this.principles.isEmpty()) {
            components.set(DataComponentRegistry.STORED_PRINCIPLES.get(), this.principles.contents());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("principles", PrincipleContents.CODEC).ifPresent(this.principles::set);
        this.progress = input.getIntOr("progress", 0);
        this.curing = input.getBooleanOr("curing", false);
        this.drain.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("principles", PrincipleContents.CODEC, this.principles.contents());
        output.putInt("progress", this.progress);
        output.putBoolean("curing", this.curing);
        this.drain.save(output);
    }
}
