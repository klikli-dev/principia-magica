// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.KeyCombo;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageSyncGolemWorkplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.Level;
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

    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

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
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.no_golem_selected"));
            return InteractionResult.FAIL;
        }

        Optional<GolemEntity> selected = findSelectedGolem(player, stack);
        if (selected.isEmpty()) {
            stack.remove(DataComponentRegistry.STYLUS_SELECTION.get());
            stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.golem_not_found"));
            return InteractionResult.FAIL;
        }

        GolemEntity golem = selected.get();
        if (!player.getUUID().equals(golem.getOwnerUUID())) {
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

        Optional<PendingGlyph> cancelled = cancelPending(stack, serverPlayer);
        if (cancelled.isPresent()) {
            String key = cancelled.get().original().isPresent()
                    ? "message.principiamagica.stylus.corner_move_cancelled"
                    : "message.principiamagica.stylus.corner_cancelled";
            serverPlayer.sendOverlayMessage(Component.translatable(key));
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown() && stack.remove(DataComponentRegistry.STYLUS_SELECTION.get()) != null) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.cleared"));
        }

        return InteractionResult.SUCCESS;
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
     * Cancels the pending corner on the stack, restoring the original footprint if the player was moving an existing corner.
     *
     * @return the removed pending glyph, if one was present
     */
    public static Optional<PendingGlyph> cancelPending(ItemStack stack, ServerPlayer player) {
        PendingGlyph removed = stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
        if (removed != null && removed.original().isPresent() && player != null) {
            WorkFootprint original = removed.original().get();
            findSelectedGolem(player, stack).ifPresent(golem -> {
                ServerLevel level = (ServerLevel) golem.level();
                Workplace current = golem.getWorkplace(level).orElse(Workplace.EMPTY);
                applyWorkplaceEdit(player, golem, StylusActions.restoreFootprint(current, original));
            });
        }
        return Optional.ofNullable(removed);
    }

    /**
     * Cancels the pending corner on the stack.
     *
     * @return whether a pending corner was present and removed
     */
    public static boolean cancelPending(ItemStack stack) {
        return cancelPending(stack, null).isPresent();
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
        stack.set(DataComponentRegistry.GOLEM_WORKPLACE.get(), StylusActions.toRelative(workplace, anchorOf(golem), level.dimension()));

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
