// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.vessel;

import com.klikli_dev.principiamagica.content.libation.AmountSmoother;
import com.klikli_dev.principiamagica.content.libation.LibationBlockEntity;
import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.content.libation.PrincipleDrain;
import com.klikli_dev.principiamagica.content.libation.PrincipleStorage;
import com.klikli_dev.principiamagica.content.libation.SyncThrottle;
import com.klikli_dev.principiamagica.content.principle.PrincipleContainer;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.registry.BlockEntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.FluidRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Holds principles and cures them into compound principles. Drains through an open spout into the drain target below the
 * spout, at the rate of {@link LibationFlow}. What the vessel is, is defined by its {@link VesselProperties}.
 */
public class CuringVesselBlockEntity extends LibationBlockEntity implements PrincipleContainer {

    private static final Codec<ResourceKey<Recipe<?>>> RECIPE_ID_CODEC = ResourceKey.codec(Registries.RECIPE);

    private final SyncThrottle sync = new SyncThrottle(this);
    private final VesselProperties vessel;
    private final PrincipleStorage principles;
    private final PrincipleDrain drain;
    /**
     * Client-only: the principles shown, eased towards the synced principles, see {@link #getDisplayedPrinciples}.
     */
    private final AmountSmoother displayedPrinciples = new AmountSmoother();
    private int progress;
    private boolean curing;
    /**
     * The id of the recipe the progress belongs to. Saved with the progress, so that a reload does not reset it.
     */
    private @Nullable ResourceKey<Recipe<?>> activeRecipe;
    /**
     * The curing recipe of the principles in the vessel, valid while {@link #curingRecipeStale} is false. The environment is
     * fixed by the vessel, so only the principles can change the recipe.
     */
    private @Nullable RecipeHolder<CuringRecipe> curingRecipe;
    private boolean curingRecipeStale = true;

    public CuringVesselBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, VesselProperties vessel) {
        super(type, pos, state);
        this.vessel = vessel;
        this.principles = new PrincipleStorage(vessel.capacity(), this.sync, this::onPrinciplesChanged);
        this.drain = new PrincipleDrain(this.principles, this.sync);
    }

    public VesselProperties properties() {
        return this.vessel;
    }

    public PrincipleContents getPrinciples() {
        return this.principles.contents();
    }

    @Override
    public ResourceHandler<FluidResource> principleHandler() {
        return this.principles;
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, BlockEntityTypeRegistry.WABET_BASIN.get(), (vessel, side) -> vessel.principles);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, BlockEntityTypeRegistry.STEEPING_JAR.get(), (vessel, side) -> vessel.principles);
    }

    /**
     * Client-only. Eases the amount of principles shown towards the synced amount, so that throttled syncs look smooth.
     *
     * @return the amount in mB to draw, see {@link AmountSmoother}.
     */
    public float getDisplayedPrinciples(long gameTime, float partialTick) {
        return this.displayedPrinciples.update(this.getPrinciples().total(), gameTime, partialTick);
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
        blockEntity.sync.tick();
    }

    private void tickCuring(Level level, BlockPos pos) {
        int light = level.getMaxLocalRawBrightness(pos);
        if (!this.vessel.canCure(light) || this.principles.isEmpty()) {
            this.resetCuring();
            return;
        }

        RecipeHolder<CuringRecipe> holder = this.curingRecipe(level);
        if (holder == null) {
            this.resetCuring();
            return;
        }

        CuringRecipe recipe = holder.value();
        if (!holder.id().equals(this.activeRecipe)) {
            this.activeRecipe = holder.id();
            this.progress = 0;
        }
        int requiredTime = this.vessel.requiredTime(recipe.time(), light);
        // Capped, so that a paused completion (see below) does not keep counting past the required time.
        this.progress = Math.min(this.progress + 1, requiredTime);

        if (this.progress >= requiredTime) {
            if (this.completeCuring(recipe)) {
                this.progress = 0;
                this.activeRecipe = null;
                this.level.playSound(null, pos, SoundRegistry.VESSEL_CURE.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
                this.setCuring(true);
                this.sync.syncNow();
                return;
            }
            // Paused: the progress stays at the required time and the completion is retried every tick until there is room.
        }

        if (!this.curing) {
            this.setCuring(true);
            this.sync.syncNow();
        } else {
            this.sync.requestSync();
        }
    }

    /**
     * @return the curing recipe of the principles, or null. Looked up only when the principles changed since the last lookup.
     */
    private @Nullable RecipeHolder<CuringRecipe> curingRecipe(Level level) {
        if (this.curingRecipeStale) {
            this.curingRecipe = LibationUtils.curing(level, this.principles.contents(), this.vessel.environment()).orElse(null);
            this.curingRecipeStale = false;
        }
        return this.curingRecipe;
    }

    private void onPrinciplesChanged() {
        this.curingRecipeStale = true;
    }

    /**
     * Consumes the inputs of the recipe and inserts its result, all or nothing.
     *
     * @return true if the recipe was completed. False if the inputs are not fully available or the result does not fully fit,
     * in which case nothing is changed.
     */
    private boolean completeCuring(CuringRecipe recipe) {
        try (Transaction tx = Transaction.open(null)) {
            for (PrincipleStack input : recipe.inputs().stacks()) {
                int extracted = this.principles.extract(FluidRegistry.resourceFor(input.principle()), input.amount(), tx);
                if (extracted != input.amount()) {
                    return false;
                }
            }
            int inserted = this.principles.insert(FluidRegistry.resourceFor(recipe.result().principle()), recipe.result().amount(), tx);
            if (inserted != recipe.result().amount()) {
                return false;
            }
            tx.commit();
            return true;
        }
    }

    private void resetCuring() {
        boolean wasCuring = this.curing;
        this.progress = 0;
        this.activeRecipe = null;
        this.setCuring(false);
        if (wasCuring) {
            this.sync.syncNow();
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
        this.curingRecipeStale = true;
        this.progress = input.getIntOr("progress", 0);
        this.activeRecipe = input.read("active_recipe", RECIPE_ID_CODEC).orElse(null);
        this.curing = input.getBooleanOr("curing", false);
        this.drain.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("principles", PrincipleContents.CODEC, this.principles.contents());
        output.putInt("progress", this.progress);
        if (this.activeRecipe != null) {
            output.store("active_recipe", RECIPE_ID_CODEC, this.activeRecipe);
        }
        output.putBoolean("curing", this.curing);
        this.drain.save(output);
    }
}
