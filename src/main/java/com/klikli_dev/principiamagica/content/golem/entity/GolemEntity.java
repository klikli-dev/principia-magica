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
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.Item;
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
    private static final EntityDataAccessor<ItemStack> DATA_BACK = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<GolemTask> DATA_TASK = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializerRegistry.GOLEM_TASK.get());
    private static final GolemSpec DEFAULT_SPEC = GolemSpec.of(GolemMaterialRegistry.WOOD);
    private static final int WORKPLACE_SYNC_TICKS = 10;

    private final AnimatableInstanceCache animatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
    private final GolemSpecModifiers specModifiers = new GolemSpecModifiers();
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
                .add(AttributeRegistry.GOLEM_WORK_SPEED);
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
        entityData.define(DATA_BACK, ItemStack.EMPTY);
        entityData.define(DATA_TASK, GolemTask.IDLE);
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
     * The stow and unstow animations move the tool at 0.25 s, so the client draws the tool on its old slot until then.
     */
    private static final int TOOL_SWITCH_TICKS = 5;

    /**
     * Client only. The tool's place on the screen, which lags the synced state by {@link #TOOL_SWITCH_TICKS} after a stow or unstow.
     */
    private boolean toolDrawnOnBack;
    private int toolSwitchTicks;
    private boolean toolWasInHand;
    private boolean toolWasOnBack;

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.updateToolDisplay();
        }
    }

    /**
     * Client only: starts the stow or unstow animation when the tool moves between hand and back, and switches the drawn slot at the
     * animation's switch point. A tool that appears or disappears in one slot is not moved, so it gets no animation.
     */
    private void updateToolDisplay() {
        boolean inHand = this.isHeldTool(this.getHandItem());
        boolean onBack = !this.getBackItem().isEmpty();

        if (this.toolSwitchTicks > 0 && --this.toolSwitchTicks == 0) {
            this.toolDrawnOnBack = onBack;
        }

        if (onBack && this.toolWasInHand && !inHand) {
            this.toolSwitchTicks = TOOL_SWITCH_TICKS;
            this.triggerAnim(GolemAnimations.WORK_CONTROLLER, GolemAnimations.STOW);
        } else if (inHand && this.toolWasOnBack && !onBack) {
            this.toolSwitchTicks = TOOL_SWITCH_TICKS;
            this.triggerAnim(GolemAnimations.WORK_CONTROLLER, GolemAnimations.UNSTOW);
        } else if (this.toolSwitchTicks == 0) {
            this.toolDrawnOnBack = onBack;
        }

        this.toolWasInHand = inHand;
        this.toolWasOnBack = onBack;
    }

    /**
     * Client only, for rendering. On the server the synced state is the state.
     *
     * @return true if the tool is drawn on the back, false if it is drawn in the hand
     */
    public boolean isToolDrawnOnBack() {
        return this.toolDrawnOnBack;
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
     * @return what the golem is doing right now. Synced to the client, so the GUI can show it live.
     */
    public GolemTask getTask() {
        return this.entityData.get(DATA_TASK);
    }

    public void setTask(GolemTask task) {
        this.entityData.set(DATA_TASK, task);
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

        // The golem GUI shows the workplace live, so players with it open get the workplace every few ticks.
        if (this.tickCount % WORKPLACE_SYNC_TICKS == 0) {
            for (ServerPlayer player : level.players()) {
                if (player.containerMenu instanceof GolemMenu menu && menu.getGolemId() == this.getId()) {
                    StylusItem.syncWorkplace(player, this);
                }
            }
        }
    }

    /**
     * The golem has two item slots. The hand is the main hand equipment: the tool while working, or the carried item.
     * The back is synced to the client and holds the tool while the golem carries something.
     *
     * @return the hand item, the tool or the carried item
     */
    public ItemStack getHandItem() {
        return this.getMainHandItem();
    }

    public void setHandItem(ItemStack stack) {
        this.setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    /**
     * @return the back item, the tool while the golem carries something
     */
    public ItemStack getBackItem() {
        return this.entityData.get(DATA_BACK);
    }

    public void setBackItem(ItemStack stack) {
        this.entityData.set(DATA_BACK, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }

    /**
     * @return true if the stack is a tool the golem's job accepts. Without a job, nothing is a tool.
     */
    public boolean isTool(ItemStack stack) {
        return !stack.isEmpty() && this.getJob().map(job -> job.isTool(stack)).orElse(false);
    }

    /**
     * A held tool is any tool, the job's tool or not. The golem's job decides which tool it works with ({@link #isTool}), the hand and
     * the back hold any tool.
     *
     * @return true if the stack is a tool the golem can hold in its hand or on its back
     */
    public boolean isHeldTool(ItemStack stack) {
        return !stack.isEmpty() && (this.isTool(stack) || stack.has(DataComponents.TOOL));
    }

    /**
     * @return true if the hand holds an item that is not a tool, i.e. the golem carries something between its hands. A tool in the hand is
     * held like a tool, its arms do not carry anything.
     */
    public boolean isCarrying() {
        ItemStack hand = this.getHandItem();
        return !hand.isEmpty() && !this.isHeldTool(hand);
    }

    /**
     * @return the number of items of the given type in the hand, 0 if the hand holds the tool or another item
     */
    public int countCarried(Item item) {
        ItemStack hand = this.getHandItem();
        return hand.is(item) && !this.isTool(hand) ? hand.getCount() : 0;
    }

    /**
     * @return how many items matching the template fit into the golem's slots. Tools never fit, they go in via {@link #equipTool}.
     */
    public int carryFitAmount(ItemStack template) {
        if (template.isEmpty() || this.isTool(template)) {
            return 0;
        }

        ItemStack hand = this.getHandItem();
        if (hand.isEmpty()) {
            return template.getMaxStackSize();
        }

        if (this.isTool(hand)) {
            return this.getBackItem().isEmpty() ? template.getMaxStackSize() : 0;
        }

        return ItemStack.isSameItemSameComponents(hand, template) ? hand.getMaxStackSize() - hand.getCount() : 0;
    }

    /**
     * Puts as much of the stack into the hand as fits. A tool in the hand moves to the back first, so the golem carries
     * the item in hand and keeps the tool on its back. Shrinks the given stack in place and returns it.
     */
    public ItemStack insertCarried(ItemStack stack) {
        if (stack.isEmpty() || this.carryFitAmount(stack) == 0) {
            return stack;
        }

        if (this.isTool(this.getHandItem())) {
            this.setBackItem(this.getHandItem());
            this.setHandItem(ItemStack.EMPTY);
        }

        ItemStack hand = this.getHandItem();
        if (hand.isEmpty()) {
            int amount = Math.min(stack.getCount(), stack.getMaxStackSize());
            this.setHandItem(stack.copyWithCount(amount));
            stack.shrink(amount);
        } else {
            int amount = Math.min(stack.getCount(), hand.getMaxStackSize() - hand.getCount());
            hand.grow(amount);
            stack.shrink(amount);
            this.setHandItem(hand);
        }

        return stack;
    }

    /**
     * Removes items of the given type from the hand, e.g. a seed that was planted.
     */
    public void consumeCarried(Item item, int count) {
        ItemStack hand = this.getHandItem();
        if (!hand.is(item) || this.isTool(hand)) {
            return;
        }

        hand.shrink(count);
        this.setHandItem(hand.isEmpty() ? ItemStack.EMPTY : hand);
    }

    /**
     * Moves the tool from the back into the hand, if the hand is free. Used once the golem has nothing else to hold.
     *
     * @return true if the tool was moved
     */
    public boolean swapToolIntoHand() {
        ItemStack back = this.getBackItem();
        if (!this.getHandItem().isEmpty() || !this.isTool(back)) {
            return false;
        }

        this.setHandItem(back);
        this.setBackItem(ItemStack.EMPTY);
        return true;
    }

    /**
     * Puts a tool into the golem's slots, following the slot rules: the back slot's tool is replaced first, then the hand's tool, then the
     * free hand, then the free back.
     *
     * @return the item displaced from the slot, which goes back to the player. The tool itself if no slot can take it.
     */
    public ItemStack equipTool(ItemStack tool) {
        ItemStack back = this.getBackItem();
        if (this.isTool(back)) {
            this.setBackItem(tool);
            return back;
        }

        ItemStack hand = this.getHandItem();
        if (this.isTool(hand)) {
            this.setHandItem(tool);
            return hand;
        }

        if (hand.isEmpty()) {
            this.setHandItem(tool);
            return ItemStack.EMPTY;
        }

        if (back.isEmpty()) {
            this.setBackItem(tool);
            return ItemStack.EMPTY;
        }

        return tool;
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
        drops.add(this.getBackItem());
        this.setBackItem(ItemStack.EMPTY);

        // The hand is the main hand equipment, so the loop over the equipment slots drops it together with the armour.
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
        if (hand != InteractionHand.MAIN_HAND) {
            return super.mobInteract(player, hand);
        }

        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty()) {
            if (!this.isTool(held) || !player.getUUID().equals(this.getOwnerUUID())) {
                return super.mobInteract(player, hand);
            }

            if (!this.level().isClientSide()) {
                // The tool goes into the golem's slots. A tool already in a slot comes back to the player.
                ItemStack tool = held.split(1);
                ItemStack displaced = this.equipTool(tool);
                if (displaced == tool) {
                    player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.slots_full"));
                }

                if (!displaced.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(displaced);
                }
            }
            return InteractionResult.SUCCESS;
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
        if (!this.getBackItem().isEmpty()) {
            output.store("BackItem", ItemStack.CODEC, this.getBackItem());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("Spec", GolemSpec.CODEC).ifPresent(this::setSpec);
        input.read("Owner", UUIDUtil.CODEC).ifPresent(this::setOwnerUUID);
        input.read("Anchor", GlobalPos.CODEC).ifPresent(this::setAnchor);
        this.entityData.set(DATA_MODE, GolemMode.valueOf(input.getStringOr("Mode", GolemMode.WORK.name())).ordinal());
        input.read("Workplace", WorkplaceSource.CODEC).ifPresent(source -> this.workplaceSource = source);
        input.read("BackItem", ItemStack.CODEC).ifPresent(this::setBackItem);
    }
}
