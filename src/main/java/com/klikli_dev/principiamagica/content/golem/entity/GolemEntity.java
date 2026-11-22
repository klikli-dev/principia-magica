// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.klikli_dev.principiamagica.content.golem.ai.GolemAi;
import com.klikli_dev.principiamagica.content.golem.ai.GolemPathNavigation;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.item.GolemItem;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenu;
import com.klikli_dev.principiamagica.content.golem.progression.GolemProgression;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkplaceDefaults;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkplaceSource;
import com.klikli_dev.principiamagica.registry.AttributeRegistry;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.EntityDataSerializerRegistry;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class GolemEntity extends AbstractGolem implements GeoEntity, ContainerUser {
    private static final EntityDataAccessor<GolemSpec> DATA_SPEC = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializerRegistry.GOLEM_SPEC.get());
    private static final EntityDataAccessor<Integer> DATA_STATUS = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MODE = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
    private static final GolemSpec DEFAULT_SPEC = GolemSpec.of(GolemMaterialRegistry.WOOD);

    private final AnimatableInstanceCache animatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
    private final GolemSpecModifiers specModifiers = new GolemSpecModifiers();
    private final GolemInventory inventory = new GolemInventory(this);
    private final GolemBlockers blockers = new GolemBlockers();
    private WorkplaceSource workplaceSource = new WorkplaceSource.Inline(Workplace.EMPTY);
    private @Nullable BlockPos openedContainerPos;

    public GolemEntity(EntityType<? extends GolemEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setRequiredPathLength(48.0F);
        this.setPathfindingMalus(GolemPathNavigation.ON_LEAVES, GolemPathNavigation.ON_LEAVES_MALUS);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GolemPathNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(AttributeRegistry.GOLEM_WORK_SPEED)
                .add(AttributeRegistry.GOLEM_WORK_RANGE)
                .add(AttributeRegistry.GOLEM_INVENTORY_SLOTS);
    }

    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(EntityTypeRegistry.GOLEM.get(), createAttributes().build());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_SPEC, DEFAULT_SPEC);
        entityData.define(DATA_STATUS, GolemStatus.OK.ordinal());
        entityData.define(DATA_MODE, GolemMode.WORK.ordinal());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        GolemAnimations.registerControllers(controllers);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableInstanceCache;
    }

    /**
     * Plays the work swing matching the held tool.
     */
    public void swingWorkArm() {
        this.triggerAnim(GolemAnimations.WORK_CONTROLLER, GolemAnimations.workAnimation(this.getMainHandItem()));
    }

    public GolemSpec getSpec() {
        return this.entityData.get(DATA_SPEC);
    }

    public Optional<GolemJob> getJob() {
        return this.getSpec().job().map(Holder::value);
    }

    public void setSpec(GolemSpec spec) {
        boolean jobChanged = !this.getSpec().job().equals(spec.job());
        this.entityData.set(DATA_SPEC, spec);
        this.applySpec();

        if (jobChanged && this.level() instanceof ServerLevel serverLevel) {
            this.refreshBrain(serverLevel);
        }
    }

    private void applySpec() {
        this.specModifiers.apply(this, this.getSpec());
        this.setHealth(Math.min(this.getHealth(), this.getMaxHealth()));

        if (this.level() instanceof ServerLevel serverLevel) {
            for (ItemStack rest : this.inventory.evictUnusableSlots()) {
                this.spawnAtLocation(serverLevel, rest);
            }
        }
    }

    public @Nullable UUID getOwnerUUID() {
        return this.getBrain().getMemory(MemoryModuleTypeRegistry.OWNER.value()).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID owner) {
        this.getBrain().setMemory(MemoryModuleTypeRegistry.OWNER.value(), owner);
    }

    public void setOwner(Player player) {
        this.setOwnerUUID(player.getUUID());
    }

    public Optional<Player> getOwner(ServerLevel level) {
        UUID owner = this.getOwnerUUID();
        return owner == null ? Optional.empty() : Optional.ofNullable(level.getPlayerByUUID(owner));
    }

    public Optional<GlobalPos> getAnchor() {
        return this.getBrain().getMemory(MemoryModuleTypeRegistry.ANCHOR.value());
    }

    public void setAnchor(@Nullable GlobalPos anchor) {
        this.getBrain().setMemory(MemoryModuleTypeRegistry.ANCHOR.value(), anchor);
    }

    public Optional<Workplace> getWorkplace(ServerLevel level) {
        return this.workplaceSource.resolve(level);
    }

    public void updateWorkplace(UnaryOperator<Workplace> operator) {
        this.workplaceSource = this.workplaceSource.update(operator);
    }

    public GolemStatus getStatus() {
        return GolemStatus.values()[this.entityData.get(DATA_STATUS)];
    }

    public void setStatus(GolemStatus status) {
        this.entityData.set(DATA_STATUS, status.ordinal());
    }

    /**
     * Records that a behaviour or sensor found the golem blocked by the given status.
     */
    public void reportBlocker(GolemStatus status) {
        this.blockers.report(status, this.level().getGameTime());
    }

    /**
     * @return the highest priority blocker that was reported recently, or OK if there is none
     */
    public GolemStatus activeBlocker() {
        return this.blockers.active(this.level().getGameTime());
    }

    public void setOpenedContainerPos(@Nullable BlockPos pos) {
        this.openedContainerPos = pos;
    }

    @Override
    public boolean hasContainerOpen(ContainerOpenersCounter container, BlockPos pos) {
        return pos.equals(this.openedContainerPos);
    }

    @Override
    public double getContainerInteractionRange() {
        return 3.0;
    }

    /**
     * @return the mode of the golem. A purposeless golem (without an axiom) can never work, so it reports Stay instead of Work.
     */
    public GolemMode getMode() {
        GolemMode mode = GolemMode.values()[this.entityData.get(DATA_MODE)];
        return mode == GolemMode.WORK && this.getSpec().job().isEmpty() ? GolemMode.STAY : mode;
    }

    /**
     * Sets the mode. Work is ignored while the golem has no axiom, as it has nothing to work on.
     */
    public void setMode(GolemMode mode) {
        if (mode == GolemMode.WORK && this.getSpec().job().isEmpty()) {
            return;
        }

        this.entityData.set(DATA_MODE, mode.ordinal());

        if (this.level() instanceof ServerLevel) {
            GolemAi.updateActivity(this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Brain<GolemEntity> getBrain() {
        return (Brain<GolemEntity>) super.getBrain();
    }

    @Override
    protected Brain<GolemEntity> makeBrain(Brain.Packed packed) {
        return GolemAi.createBrain(this, packed);
    }

    public void refreshBrain(ServerLevel level) {
        Brain<GolemEntity> oldBrain = this.getBrain();
        oldBrain.stopAll(level, this);
        this.brain = this.makeBrain(oldBrain.pack());
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.getBrain().tick(level, this);
        GolemAi.updateActivity(this);
        super.customServerAiStep(level);

        if (this.getStatus().isProblem() && this.tickCount % 80 == 0) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + this.getBbHeight() + 0.3, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public GolemInventory getInventory() {
        return this.inventory;
    }

    public ItemStack toItemStack() {
        ItemStack stack = GolemItem.createStack(this.getSpec());
        GolemStorage storage = GolemStorage.capture(this);
        if (storage.itemCount() > 0) {
            stack.set(DataComponentRegistry.GOLEM_STORAGE.get(), storage);
        }
        return stack;
    }

    /**
     * On death the golem drops everything it carries: its inventory, its equipment and its axiom item. The vanilla loot
     * table is replaced, so nothing else is dropped. Discarding the golem (e.g. stylus pickup) does not get here.
     */
    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        this.dropCarriedItems(level);
        this.dropExperience(level, source.getEntity());

        Vec3 center = this.position().add(0.0, this.getBbHeight() / 2.0, 0.0);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CLAY.defaultBlockState()),
                center.x, center.y, center.z, 40, this.getBbWidth() / 2.0, this.getBbHeight() / 2.0, this.getBbWidth() / 2.0, 0.0);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.DECORATED_POT_SHATTER, SoundSource.NEUTRAL, 1.0F, 1.0F);

        Component golemName = GolemItem.createStack(this.getSpec()).getHoverName();
        this.getOwner(level).ifPresent(owner -> owner.sendSystemMessage(Component.translatable("message.principiamagica.golem.destroyed",
                golemName, this.getBlockX(), this.getBlockY(), this.getBlockZ())));
    }

    /**
     * Drops the inventory, the equipment and the axiom on the ground. The golem is dying, so the stored items are removed from it.
     */
    private void dropCarriedItems(ServerLevel level) {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            drops.add(this.inventory.removeItemNoUpdate(i));
        }

        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            drops.add(this.getItemBySlot(slot));
            this.setItemSlot(slot, ItemStack.EMPTY);
        }

        this.getSpec().job().ifPresent(job -> drops.add(AxiomItem.createStack(job)));

        for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
                this.spawnAtLocation(level, stack);
            }
        }
    }

    @Override
    public boolean fireImmune() {
        return this.getSpec().material().value().fireImmune();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /**
     * Gives the golem a job. A purposeless golem gets the default work area around its position (unless it already has zones)
     * and switches to Work. The previous job, if any, is replaced without returning its axiom: callers handle that.
     */
    public void installJob(Holder<GolemJob> job, @Nullable ServerPlayer player) {
        boolean wasPurposeless = this.getSpec().job().isEmpty();
        this.setSpec(this.getSpec().withJob(job));

        if (wasPurposeless && this.level() instanceof ServerLevel level) {
            WorkplaceDefaults.applyIfEmpty(this, level);
            this.setMode(GolemMode.WORK);
        }

        if (player != null) {
            GolemProgression.onAxiomGiven(player);
        }
    }

    /**
     * Removes the job and returns its axiom item, or an empty stack if there was no job or no axiom item for it.
     * The golem is purposeless afterwards: it stays, unless it was set to follow.
     */
    public ItemStack takeJob() {
        Optional<Holder<GolemJob>> job = this.getSpec().job();
        if (job.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack axiom = AxiomItem.createStack(job.get());
        this.setSpec(this.getSpec().withoutJob());
        if (this.getMode() != GolemMode.FOLLOW) {
            this.setMode(GolemMode.STAY);
        }
        return axiom;
    }

    private void removeJob(Player player) {
        if (this.getSpec().job().isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.no_job"));
            return;
        }

        ItemStack axiom = this.takeJob();
        if (!axiom.isEmpty()) {
            player.getInventory().placeItemBackInInventory(axiom);
        }
        player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.job_removed"));
    }

    private void openGolemMenu(ServerPlayer player) {
        player.openMenu(
                new SimpleMenuProvider((containerId, inventory, menuPlayer) -> new GolemMenu(containerId, inventory, this.getId()), this.getDisplayName()),
                buf -> buf.writeVarInt(this.getId())
        );
        StylusItem.syncWorkplace(player, this);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !player.getItemInHand(hand).isEmpty()) {
            return super.mobInteract(player, hand);
        }

        if (!player.getUUID().equals(this.getOwnerUUID())) {
            if (!this.level().isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.not_owner"));
            }
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            if (!this.level().isClientSide()) {
                this.removeJob(player);
            }
            return InteractionResult.SUCCESS;
        }

        if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            this.openGolemMenu(serverPlayer);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Spec", GolemSpec.CODEC, this.getSpec());
        output.putString("Mode", this.getMode().name());
        output.store("Workplace", WorkplaceSource.CODEC, this.workplaceSource);
        if (this.getOwnerUUID() != null) {
            output.store("Owner", UUIDUtil.CODEC, this.getOwnerUUID());
        }
        if (this.getAnchor().isPresent()) {
            output.store("Anchor", GlobalPos.CODEC, this.getAnchor().get());
        }
        this.inventory.save(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("Spec", GolemSpec.CODEC).ifPresent(this::setSpec);
        input.read("Owner", UUIDUtil.CODEC).ifPresent(this::setOwnerUUID);
        input.read("Anchor", GlobalPos.CODEC).ifPresent(this::setAnchor);
        this.entityData.set(DATA_MODE, GolemMode.valueOf(input.getStringOr("Mode", GolemMode.WORK.name())).ordinal());
        input.read("Workplace", WorkplaceSource.CODEC).ifPresent(source -> this.workplaceSource = source);
        this.inventory.load(input);
    }
}
