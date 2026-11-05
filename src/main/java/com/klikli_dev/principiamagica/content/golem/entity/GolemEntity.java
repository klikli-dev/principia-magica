// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import com.klikli_dev.principiamagica.content.golem.ai.GolemAi;
import com.klikli_dev.principiamagica.content.golem.item.GolemBatonItem;
import com.klikli_dev.principiamagica.content.golem.item.GolemCoreItem;
import com.klikli_dev.principiamagica.content.golem.item.GolemItem;
import com.klikli_dev.principiamagica.content.golem.item.GolemUpgradeItem;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenu;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.upgrade.GolemUpgrade;
import com.klikli_dev.principiamagica.content.golem.upgrade.UpgradeAddResult;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkplaceSource;
import com.klikli_dev.principiamagica.registry.AttributeRegistry;
import com.klikli_dev.principiamagica.registry.EntityDataSerializerRegistry;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class GolemEntity extends AbstractGolem implements GeoEntity, ContainerUser {
    private static final EntityDataAccessor<GolemSpec> DATA_SPEC = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializerRegistry.GOLEM_SPEC.get());
    private static final EntityDataAccessor<Boolean> DATA_CARRYING = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_STATUS = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MODE = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
    private static final GolemSpec DEFAULT_SPEC = GolemSpec.of(GolemMaterialRegistry.WOOD);
    private static final int BLOCKER_TICKS = 40;

    public static final String WORK_CONTROLLER = "work";
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.golem.idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.golem.walk");
    private static final RawAnimation CARRY_ANIM = RawAnimation.begin().thenLoop("animation.golem.carry");
    private static final RawAnimation CARRY_BOB_ANIM = RawAnimation.begin().thenLoop("animation.golem.carry_bob");
    private static final RawAnimation ARM_SWING_ANIM = RawAnimation.begin().thenPlay("animation.golem.arm_swing");
    private static final RawAnimation ARM_SWING_LEFT_ANIM = RawAnimation.begin().thenPlay("animation.golem.arm_swing_left");
    private static final RawAnimation ARM_SWING_RIGHT_ANIM = RawAnimation.begin().thenPlay("animation.golem.arm_swing_right");

    private final AnimatableInstanceCache animatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
    private final List<AppliedModifier> appliedModifiers = new ArrayList<>();
    private final GolemInventory inventory = new GolemInventory(this);
    private final Map<GolemStatus, Long> blockers = new EnumMap<>(GolemStatus.class);
    private WorkplaceSource workplaceSource = new WorkplaceSource.Inline(Workplace.EMPTY);
    private @Nullable BlockPos openedContainerPos;

    public GolemEntity(EntityType<? extends GolemEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setRequiredPathLength(48.0F);
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
        entityData.define(DATA_CARRYING, false);
        entityData.define(DATA_STATUS, GolemStatus.OK.ordinal());
        entityData.define(DATA_MODE, GolemMode.WORK.ordinal());
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_CARRYING, !this.inventory.isEmpty());
        }
    }

    public boolean isCarrying() {
        return this.entityData.get(DATA_CARRYING);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<GolemEntity>("base", 5, this::baseAnimationHandler),
                new AnimationController<GolemEntity>("carry", 5, this::carryAnimationHandler),
                new AnimationController<GolemEntity>(WORK_CONTROLLER, 0, test -> PlayState.STOP)
                        .triggerableAnim("arm_swing", ARM_SWING_ANIM)
                        .triggerableAnim("arm_swing_left", ARM_SWING_LEFT_ANIM)
                        .triggerableAnim("arm_swing_right", ARM_SWING_RIGHT_ANIM)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableInstanceCache;
    }

    private PlayState baseAnimationHandler(AnimationTest<GolemEntity> test) {
        return test.setAndContinue(test.isMoving() ? WALK_ANIM : IDLE_ANIM);
    }

    private PlayState carryAnimationHandler(AnimationTest<GolemEntity> test) {
        if (!test.animatable().isCarrying()) {
            return PlayState.STOP;
        }

        return test.setAndContinue(test.isMoving() ? CARRY_BOB_ANIM : CARRY_ANIM);
    }

    public void swingWorkArm() {
        this.triggerAnim(WORK_CONTROLLER, "arm_swing");
    }

    public void swingLeftArm() {
        this.triggerAnim(WORK_CONTROLLER, "arm_swing_left");
    }

    public void swingRightArm() {
        this.triggerAnim(WORK_CONTROLLER, "arm_swing_right");
    }

    public GolemSpec getSpec() {
        return this.entityData.get(DATA_SPEC);
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
        for (AppliedModifier applied : this.appliedModifiers) {
            AttributeInstance instance = this.getAttribute(applied.attribute());
            if (instance != null) {
                instance.removeModifier(applied.id());
            }
        }
        this.appliedModifiers.clear();

        this.getSpec().collectModifiers((attribute, modifier) -> {
            AttributeInstance instance = this.getAttribute(attribute);
            if (instance != null) {
                instance.addOrUpdateTransientModifier(modifier);
                this.appliedModifiers.add(new AppliedModifier(attribute, modifier.id()));
            }
        });

        this.setHealth(Math.min(this.getHealth(), this.getMaxHealth()));
        this.evictUnusableSlots();
    }

    /**
     * Moves items out of inventory slots that are no longer usable (e.g. after an upgrade was removed). Items are moved
     * into free usable slots first, the rest is dropped at the golem so nothing is lost.
     */
    private void evictUnusableSlots() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = this.inventory.getUsableSlots(); i < GolemInventory.MAX_SLOTS; i++) {
            ItemStack stack = this.inventory.removeItemNoUpdate(i);
            ItemStack rest = this.inventory.insert(stack);
            if (!rest.isEmpty()) {
                this.spawnAtLocation(serverLevel, rest);
            }
        }
    }

    /**
     * Adds the upgrade if the golem does not have it yet and its material has a free upgrade slot.
     */
    public UpgradeAddResult addUpgrade(Holder<GolemUpgrade> upgrade) {
        GolemSpec spec = this.getSpec();
        if (spec.upgrades().stream().anyMatch(existing -> existing.value() == upgrade.value())) {
            return UpgradeAddResult.DUPLICATE;
        }

        if (spec.upgrades().size() >= spec.material().value().upgradeSlots()) {
            return UpgradeAddResult.NO_FREE_SLOT;
        }

        this.setSpec(spec.withUpgrade(upgrade));
        return UpgradeAddResult.ADDED;
    }

    /**
     * Removes the upgrade at the index and returns its item, or an empty stack if the index is out of range.
     */
    public ItemStack removeUpgrade(int index) {
        List<Holder<GolemUpgrade>> upgrades = this.getSpec().upgrades();
        if (index < 0 || index >= upgrades.size()) {
            return ItemStack.EMPTY;
        }

        Holder<GolemUpgrade> upgrade = upgrades.get(index);
        this.setSpec(this.getSpec().withoutUpgrade(index));
        return GolemUpgradeItem.createStack(upgrade);
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

    public WorkplaceSource getWorkplaceSource() {
        return this.workplaceSource;
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
     * Records that a behaviour or sensor found the golem blocked by the given status. The blocker stays active for a
     * short time, so the status does not flicker while the checks run on cooldowns.
     */
    public void reportBlocker(GolemStatus status) {
        this.blockers.put(status, this.level().getGameTime() + BLOCKER_TICKS);
    }

    /**
     * @return the highest priority blocker that was reported recently, or OK if there is none
     */
    public GolemStatus activeBlocker() {
        long now = this.level().getGameTime();
        this.blockers.values().removeIf(expiry -> expiry < now);
        return this.blockers.keySet().stream().min(Comparator.comparingInt(GolemStatus::ordinal)).orElse(GolemStatus.OK);
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

    public GolemMode getMode() {
        return GolemMode.values()[this.entityData.get(DATA_MODE)];
    }

    public void setMode(GolemMode mode) {
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

        for (Holder<GolemUpgrade> upgrade : this.getSpec().upgrades()) {
            upgrade.value().onTick(this);
        }

        if (this.getStatus().isProblem() && this.tickCount % 80 == 0) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + this.getBbHeight() + 0.3, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public GolemInventory getInventory() {
        return this.inventory;
    }

    public ItemStack toItemStack() {
        return GolemItem.createStack(this.getSpec());
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
     * Removes the job and returns its core item, or an empty stack if there was no job or no core item for it.
     */
    public ItemStack takeJob() {
        Optional<Holder<GolemJob>> job = this.getSpec().job();
        if (job.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack core = GolemCoreItem.createStack(job.get());
        this.setSpec(this.getSpec().withoutJob());
        return core;
    }

    private void removeJob(Player player) {
        if (this.getSpec().job().isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.no_job"));
            return;
        }

        ItemStack core = this.takeJob();
        if (!core.isEmpty()) {
            player.getInventory().placeItemBackInInventory(core);
        }
        player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.job_removed"));
    }

    private void openGolemMenu(ServerPlayer player) {
        player.openMenu(
                new SimpleMenuProvider((containerId, inventory, menuPlayer) -> new GolemMenu(containerId, inventory, this.getId()), this.getDisplayName()),
                buf -> buf.writeVarInt(this.getId())
        );
        GolemBatonItem.syncWorkplace(player, this);
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

    private record AppliedModifier(Holder<Attribute> attribute, Identifier id) {
    }
}
