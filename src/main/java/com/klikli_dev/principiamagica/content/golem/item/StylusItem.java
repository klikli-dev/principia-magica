// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.KeyCombo;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageSyncGolemWorkplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Copper stylus: a crafting tool that is never consumed, and the tool used to configure golems. See the tooltip for the controls.
 * Clicks are dispatched to {@link StylusEntityInteraction} and {@link StylusBlockInteraction}.
 */
public class StylusItem extends Item {

    /**
     * The squared distance within which the selected golem can be reached with the stylus.
     */
    public static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    public StylusItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof GolemEntity golem)) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer) || !(golem.level() instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (!player.getUUID().equals(golem.getOwnerUUID())) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.not_owner"));
            return InteractionResult.FAIL;
        }

        // The held stack, not the one passed in: in creative vanilla passes a copy, and changes to it are lost
        return StylusEntityInteraction.interact(player.getItemInHand(hand), serverPlayer, golem);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        if (stack.get(DataComponentRegistry.STYLUS_SELECTION.get()) == null) {
            serverLevel.playSound(null, context.getClickedPos(), SoundRegistry.STYLUS_FAIL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.no_golem_selected"));
            return InteractionResult.FAIL;
        }

        Optional<GolemEntity> selected = findSelectedGolem(player, stack);
        if (selected.isEmpty()) {
            // A pending corner move still owes its footprint to the golem, so the selection is kept until it can be restored
            if (!hasPendingMove(stack)) {
                stack.remove(DataComponentRegistry.STYLUS_SELECTION.get());
                stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
            }
            serverLevel.playSound(null, context.getClickedPos(), SoundRegistry.STYLUS_FAIL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.golem_not_found"));
            return InteractionResult.FAIL;
        }

        GolemEntity golem = selected.get();
        if (!player.getUUID().equals(golem.getOwnerUUID())) {
            serverLevel.playSound(null, context.getClickedPos(), SoundRegistry.STYLUS_FAIL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.not_owner"));
            return InteractionResult.FAIL;
        }

        StylusBlockInteraction.interact(serverLevel, player, stack, golem, context.getClickedPos(), context.getClickedFace());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        switch (cancelPending(stack, serverPlayer)) {
            case CORNER_CANCELLED -> {
                level.playSound(null, player.blockPosition(), SoundRegistry.STYLUS_DESELECT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_cancelled"));
            }
            case MOVE_CANCELLED -> {
                level.playSound(null, player.blockPosition(), SoundRegistry.STYLUS_DESELECT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_move_cancelled"));
            }
            case MOVE_KEPT -> {
                if (player.isShiftKeyDown()) {
                    // Escape hatch for a move whose golem can never be reached again: the moved work area is lost
                    stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
                    stack.remove(DataComponentRegistry.STYLUS_SELECTION.get());
                    level.playSound(null, player.blockPosition(), SoundRegistry.STYLUS_DESELECT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_move_discarded"));
                } else {
                    level.playSound(null, player.blockPosition(), SoundRegistry.STYLUS_FAIL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_move_golem_not_found"));
                }
            }
            case NONE -> {
                if (player.isShiftKeyDown() && stack.remove(DataComponentRegistry.STYLUS_SELECTION.get()) != null) {
                    level.playSound(null, player.blockPosition(), SoundRegistry.STYLUS_DESELECT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.cleared"));
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * What {@link #cancelPending(ItemStack, ServerPlayer)} did with the pending corner.
     */
    public enum CancelOutcome {
        /** There was no pending corner. */
        NONE,
        /** A pending corner that did not move a footprint was removed. */
        CORNER_CANCELLED,
        /** A pending corner move was cancelled and its original footprint restored to the selected golem. */
        MOVE_CANCELLED,
        /** A pending corner move could not be restored because the selected golem is not reachable, so it stays on the stack. */
        MOVE_KEPT
    }

    private static boolean hasPendingMove(ItemStack stack) {
        PendingGlyph pending = stack.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
        return pending != null && pending.original().isPresent();
    }

    /**
     * Cancels the pending corner on the stack. A pending corner move is only removed once its original footprint has been
     * restored to the selected golem. If the golem is not reachable, the move stays on the stack, so the footprint is not lost.
     */
    public static CancelOutcome cancelPending(ItemStack stack, ServerPlayer player) {
        PendingGlyph pending = stack.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
        if (pending == null) {
            return CancelOutcome.NONE;
        }

        if (pending.original().isEmpty()) {
            stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
            return CancelOutcome.CORNER_CANCELLED;
        }

        Optional<GolemEntity> golem = findSelectedGolem(player, stack);
        if (golem.isEmpty()) {
            return CancelOutcome.MOVE_KEPT;
        }

        ServerLevel level = (ServerLevel) golem.get().level();
        Workplace current = golem.get().getWorkplace(level).orElse(Workplace.EMPTY);
        applyWorkplaceEdit(player, golem.get(), StylusActions.restoreFootprint(current, pending.original().get(), pending.originalMemory()));
        stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
        return CancelOutcome.MOVE_CANCELLED;
    }

    /**
     * Suppresses block interaction (e.g. opening chests) when right-clicking a block with a stylus that has a golem selected,
     * so container linking works with a plain right-click.
     */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof StylusItem && stack.has(DataComponentRegistry.STYLUS_SELECTION.get())) {
            event.setUseBlock(TriState.FALSE);
        }
    }

    /**
     * Applies a workplace edit, clears stale brain cooldowns so the golem reacts immediately, and syncs to the player.
     */
    public static void applyWorkplaceEdit(ServerPlayer player, GolemEntity golem, Workplace updated) {
        golem.updateWorkplace(current -> updated);
        golem.getBrain().eraseMemory(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value());
        golem.getBrain().eraseMemory(MemoryModuleTypeRegistry.UNREACHABLE_TARGETS.value());
        syncWorkplace(player, golem);
    }

    /**
     * Cycles (plain) or removes (sneaking) the container at pos, for a container the client targeted, e.g. in the air or with the link
     * already there. Validates the same things as the other stylus edits: the player owns the golem, holds its selection in range, the
     * stylus has no pending corner, and the position is in reach of the player's eyes.
     *
     * @return whether the container was acted on
     */
    public static boolean targetLink(ServerPlayer player, GolemEntity golem, BlockPos pos, Direction face, boolean remove) {
        if (!player.getUUID().equals(golem.getOwnerUUID()) || !isWithinRange(player, golem) || !holdsSelection(player, golem)) {
            return false;
        }

        // A pending corner in another dimension does not block the click, the same as on the client and in StylusBlockInteraction
        ServerLevel level = (ServerLevel) golem.level();
        Optional<ItemStack> stylus = heldSelectionStylus(player, golem);
        PendingGlyph pending = stylus.map(held -> held.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get())).orElse(null);
        if (stylus.isEmpty() || (pending != null && pending.pos().dimension().equals(level.dimension()))) {
            return false;
        }

        if (!GlyphTargeting.isInReach(player.getEyePosition(), pos) || !isLinkVisible(player, level, pos)) {
            return false;
        }

        return StylusBlockInteraction.targetLink(level, player, golem, pos, face, remove);
    }

    private static final double VISIBILITY_EPSILON = 1.0E-3;

    /**
     * A solid block between the player and the container's box hides it, the same as on the client. The container's own shape lies inside
     * its box, so it does not hide its own link. Air links are visible through empty space.
     */
    private static boolean isLinkVisible(ServerPlayer player, ServerLevel level, BlockPos pos) {
        Vec3 eye = player.getEyePosition();
        AABB box = GlyphTargeting.linkHitBox(pos);
        if (box.contains(eye)) {
            return true;
        }

        Optional<Vec3> entry = box.clip(eye, box.getCenter());
        if (entry.isEmpty()) {
            return false;
        }

        double distance = eye.distanceTo(entry.get());
        if (distance <= VISIBILITY_EPSILON) {
            return true;
        }

        // Stop just short of the box, so a block exactly on the box's surface does not count as hiding it
        Vec3 end = eye.add(entry.get().subtract(eye).scale((distance - VISIBILITY_EPSILON) / distance));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS;
    }

    public static boolean isWithinRange(ServerPlayer player, GolemEntity golem) {
        return golem.distanceToSqr(player) <= MAX_DISTANCE_SQR;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.isShiftKeyDown() || !(entity instanceof GolemEntity golem) || !player.getUUID().equals(golem.getOwnerUUID())) {
            return false;
        }

        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            pickUp(serverPlayer, golem);
        }

        return true;
    }

    /**
     * Picks the golem up: it becomes a golem item with its workplace and its inventory and equipment, which are kept
     * on the item instead of being dropped. Placing the item again restores them.
     */
    private static void pickUp(ServerPlayer player, GolemEntity golem) {
        ServerLevel level = (ServerLevel) golem.level();
        Workplace workplace = golem.getWorkplace(level).orElse(Workplace.EMPTY);

        ItemStack stack = golem.toItemStack();
        stack.set(DataComponentRegistry.GOLEM_WORKPLACE.get(), StylusActions.stampDimension(workplace, level.dimension()));

        level.playSound(null, golem.blockPosition(), golem.getSpec().material().value().sounds().pickUp().value(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        golem.discard();
        player.getInventory().placeItemBackInInventory(stack);
        player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.picked_up"));
    }

    /**
     * Resolves the golem selected on the stack, if it still exists in the player's level and is near the player.
     */
    public static Optional<GolemEntity> findSelectedGolem(ServerPlayer player, ItemStack stack) {
        UUID selected = stack.get(DataComponentRegistry.STYLUS_SELECTION.get());
        if (selected == null || !(player.level() instanceof ServerLevel level)) {
            return Optional.empty();
        }

        if (!(level.getEntity(selected) instanceof GolemEntity golem) || golem.distanceToSqr(player) > MAX_DISTANCE_SQR) {
            return Optional.empty();
        }

        return Optional.of(golem);
    }

    /**
     * @return whether the player holds a stylus that has this golem selected
     */
    public static boolean holdsSelection(ServerPlayer player, GolemEntity golem) {
        return heldStylus(player, stack -> golem.getUUID().equals(stack.get(DataComponentRegistry.STYLUS_SELECTION.get()))).isPresent();
    }

    /**
     * @return the stylus the player holds that has this golem selected, if any. Its pending corner can be changed on the server.
     */
    public static Optional<ItemStack> heldSelectionStylus(ServerPlayer player, GolemEntity golem) {
        return heldStylus(player, stack -> golem.getUUID().equals(stack.get(DataComponentRegistry.STYLUS_SELECTION.get())));
    }

    /**
     * @return the stylus the player holds that has a pending corner, if any
     */
    public static Optional<ItemStack> heldPendingStylus(ServerPlayer player) {
        return heldStylus(player, stack -> stack.has(DataComponentRegistry.STYLUS_PENDING_GLYPH.get()));
    }

    private static Optional<ItemStack> heldStylus(ServerPlayer player, Predicate<ItemStack> predicate) {
        for (ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (stack.getItem() instanceof StylusItem && predicate.test(stack)) {
                return Optional.of(stack);
            }
        }

        return Optional.empty();
    }

    /**
     * Sends the golem workplace to the player. Called whenever the player's stylus selection changes or is modified.
     */
    public static void syncWorkplace(ServerPlayer player, GolemEntity golem) {
        ServerLevel level = (ServerLevel) golem.level();
        Workplace workplace = golem.getWorkplace(level).orElse(Workplace.EMPTY);
        Optional<BlockPos> target = golem.getBrain().getMemory(MemoryModuleTypeRegistry.WORK_TARGET.value());
        Networking.sendTo(player, new MessageSyncGolemWorkplace(golem.getId(), workplace, golem.getAnchor(), target));
    }

    /**
     * Periodically refreshes the workplace of the golem selected on a held stylus, so the client overlay stays current.
     */
    public static void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 10 != 0) {
            return;
        }

        for (ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (stack.getItem() instanceof StylusItem) {
                findSelectedGolem(player, stack).ifPresent(golem -> syncWorkplace(player, golem));
            }
        }
    }

    /**
     * @return the block the golem is anchored to, or its current block if it has no anchor
     */
    static BlockPos anchorOf(GolemEntity golem) {
        return golem.getAnchor().map(GlobalPos::pos).orElse(golem.blockPosition());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
        boolean selected = stack.has(DataComponentRegistry.STYLUS_SELECTION.get());
        adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.selected", selected
                ? Component.translatable("tooltip.principiamagica.copper_stylus.yes")
                : Component.translatable("tooltip.principiamagica.copper_stylus.none")).withStyle(ChatFormatting.GRAY));

        PendingGlyph pending = stack.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
        if (pending != null) {
            BlockPos pos = pending.pos().pos();
            adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.pending_corner",
                    pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GRAY));
        }

        adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.hint_select", KeyCombo.RIGHT_CLICK.component()).withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.hint_pickup", KeyCombo.CROUCH_LEFT_CLICK.component()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
