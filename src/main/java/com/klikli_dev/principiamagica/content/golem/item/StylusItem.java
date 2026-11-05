// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageSyncGolemWorkplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Copper stylus: a crafting tool that is never consumed, and the tool used to configure golems. See the tooltip for the controls.
 */
public class StylusItem extends Item {

    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    public StylusItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        return new ItemStackTemplate(this);
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

        if (player.isShiftKeyDown()) {
            Workplace clipboard = stack.get(DataComponentRegistry.STYLUS_CLIPBOARD.get());
            if (clipboard == null) {
                Workplace workplace = golem.getWorkplace((ServerLevel) golem.level()).orElse(Workplace.EMPTY);
                Workplace copied = StylusActions.toRelative(workplace, anchorOf(golem));
                stack.set(DataComponentRegistry.STYLUS_CLIPBOARD.get(), copied);
                serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.copied",
                        copied.footprints().size(), copied.links().size()));
            } else {
                BlockPos anchor = anchorOf(golem);
                golem.updateWorkplace(current -> StylusActions.paste(current, clipboard, anchor));
                serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.pasted"));
                syncWorkplace(serverPlayer, golem);
            }
        } else {
            UUID selected = stack.get(DataComponentRegistry.STYLUS_SELECTION.get());
            if (golem.getUUID().equals(selected)) {
                stack.remove(DataComponentRegistry.STYLUS_SELECTION.get());
                serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.deselected"));
            } else {
                stack.set(DataComponentRegistry.STYLUS_SELECTION.get(), golem.getUUID());
                stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
                serverPlayer.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.selected"));
                syncWorkplace(serverPlayer, golem);
            }
        }

        return InteractionResult.SUCCESS;
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
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.golem_not_found"));
            return InteractionResult.FAIL;
        }

        GolemEntity golem = selected.get();
        if (!player.getUUID().equals(golem.getOwnerUUID())) {
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.not_owner"));
            return InteractionResult.FAIL;
        }

        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        boolean container = level.getCapability(Capabilities.Item.BLOCK, pos, face) != null;
        boolean sneaking = player.isShiftKeyDown();
        Workplace workplace = golem.getWorkplace(serverLevel).orElse(Workplace.EMPTY);
        int below = golem.getJob().map(GolemJob::below).orElse(WorkFootprint.DEFAULT_BELOW);
        int above = golem.getJob().map(GolemJob::above).orElse(WorkFootprint.DEFAULT_ABOVE);

        if (container) {
            Workplace updated = StylusActions.cycleLink(workplace, pos, face);
            golem.updateWorkplace(current -> updated);
            Optional<ContainerRole> role = updated.links().stream()
                    .filter(link -> link.pos().equals(pos))
                    .map(ContainerLink::role)
                    .findFirst();
            Component message = role.isPresent()
                    ? Component.translatable("message.principiamagica.stylus.link_set", Component.translatable("golem_role.principiamagica." + role.get().getSerializedName()))
                    : Component.translatable("message.principiamagica.stylus.link_removed");
            player.sendOverlayMessage(message);
        } else if (sneaking) {
            Optional<Workplace> updated = StylusActions.removeFootprintAt(workplace, pos, below, above);
            if (updated.isPresent()) {
                golem.updateWorkplace(current -> updated.get());
                serverLevel.playSound(null, pos, SoundEvents.CALCITE_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);
                player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.footprint_erased"));
            } else {
                player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.nothing_to_remove"));
            }
        } else {
            serverLevel.playSound(null, pos, SoundEvents.BRUSH_GRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
            Optional<PendingGlyph> pending = Optional.ofNullable(stack.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get()))
                    .filter(glyph -> glyph.pos().dimension().equals(level.dimension()));
            if (pending.isEmpty()) {
                stack.set(DataComponentRegistry.STYLUS_PENDING_GLYPH.get(), new PendingGlyph(GlobalPos.of(level.dimension(), pos), face));
                player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_set"));
            } else {
                stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
                Optional<Workplace> updated = StylusActions.carve(workplace, pending.get().glyph(), new CarvedGlyph(pos, face));
                if (updated.isPresent()) {
                    golem.updateWorkplace(current -> updated.get());
                    player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.footprint_carved"));
                } else {
                    player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.footprint_too_large",
                            WorkFootprint.MAX_SIZE, WorkFootprint.MAX_SIZE));
                }
            }
        }

        syncWorkplace(player, golem);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            stack.remove(DataComponentRegistry.STYLUS_SELECTION.get());
            stack.remove(DataComponentRegistry.STYLUS_CLIPBOARD.get());
            stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.cleared"));
        } else {
            stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
        }

        return InteractionResult.SUCCESS;
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

    private static void pickUp(ServerPlayer player, GolemEntity golem) {
        ServerLevel level = (ServerLevel) golem.level();
        Workplace workplace = golem.getWorkplace(level).orElse(Workplace.EMPTY);

        ItemStack stack = golem.toItemStack();
        stack.set(DataComponentRegistry.GOLEM_WORKPLACE.get(), StylusActions.toRelative(workplace, anchorOf(golem)));

        for (int i = 0; i < golem.getInventory().getContainerSize(); i++) {
            ItemStack contained = golem.getInventory().removeItemNoUpdate(i);
            Containers.dropItemStack(level, golem.getX(), golem.getY(), golem.getZ(), contained);
        }

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
        for (ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (stack.getItem() instanceof StylusItem && golem.getUUID().equals(stack.get(DataComponentRegistry.STYLUS_SELECTION.get()))) {
                return true;
            }
        }

        return false;
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

    private static BlockPos anchorOf(GolemEntity golem) {
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

        Workplace clipboard = stack.get(DataComponentRegistry.STYLUS_CLIPBOARD.get());
        adder.accept(clipboard == null
                ? Component.translatable("tooltip.principiamagica.copper_stylus.clipboard_empty").withStyle(ChatFormatting.GRAY)
                : Component.translatable("tooltip.principiamagica.copper_stylus.clipboard", clipboard.footprints().size(), clipboard.links().size()).withStyle(ChatFormatting.GRAY));

        adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.hint_select").withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.hint_copy_paste").withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.hint_link").withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.hint_zone").withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.hint_raise").withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.hint_remove").withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.hint_pickup").withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.copper_stylus.hint_clear").withStyle(ChatFormatting.DARK_GRAY));
    }
}
