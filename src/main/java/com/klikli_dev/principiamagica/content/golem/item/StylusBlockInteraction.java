// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.ai.behaviour.ContainerAccess;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * A stylus click on a block for the golem selected on the stylus. What it does depends on the block and on sneaking:
 * <ul>
 *     <li>container or linked position, plain: cycles the container link (supply, deposit, both, removed)</li>
 *     <li>container or linked position, sneaking: removes the container link. A linked position whose block is gone counts, so stale links can be removed</li>
 *     <li>other block, sneaking: erases the footprint at the block</li>
 *     <li>other block, plain: sets or finishes the pending work-area corner</li>
 * </ul>
 */
final class StylusBlockInteraction {

    private StylusBlockInteraction() {
    }

    static void interact(ServerLevel level, ServerPlayer player, ItemStack stack, GolemEntity golem, BlockPos pos, Direction face) {
        PendingGlyph pending = stack.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
        boolean hasPending = pending != null && pending.pos().dimension().equals(level.dimension());
        Workplace workplace = golem.getWorkplace(level).orElse(Workplace.EMPTY);
        // A linked position is a container even if its block is gone, so the link can still be cycled or removed
        boolean linked = workplace.links().stream().anyMatch(link -> link.pos().equals(pos));
        boolean container = linked || ContainerAccess.hasItemCapability(level, pos);
        boolean sneaking = player.isShiftKeyDown();

        if (hasPending && !sneaking) {
            placeCorner(level, player, stack, golem, workplace, pos, face);
        } else if (container && sneaking) {
            removeLink(level, player, golem, workplace, pos);
        } else if (container) {
            cycleLink(level, player, golem, workplace, pos, face);
        } else if (sneaking) {
            eraseFootprint(level, player, golem, workplace, pos);
        } else {
            placeCorner(level, player, stack, golem, workplace, pos, face);
        }

        StylusItem.syncWorkplace(player, golem);
    }

    private static void eraseFootprint(ServerLevel level, ServerPlayer player, GolemEntity golem, Workplace workplace, BlockPos pos) {
        int below = golem.getJob().map(GolemJob::below).orElse(WorkFootprint.DEFAULT_BELOW);
        int above = golem.getJob().map(GolemJob::above).orElse(WorkFootprint.DEFAULT_ABOVE);
        Optional<Workplace> updated = StylusActions.removeFootprintAt(workplace, level.dimension(), pos, below, above);
        if (updated.isPresent()) {
            StylusItem.applyWorkplaceEdit(player, golem, updated.get());
            level.playSound(null, pos, SoundEvents.CALCITE_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.footprint_erased"));
        } else {
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.nothing_to_remove"));
        }
    }

    private static void removeLink(ServerLevel level, ServerPlayer player, GolemEntity golem, Workplace workplace, BlockPos pos) {
        Optional<Workplace> updated = StylusActions.removeLink(workplace, pos);
        if (updated.isPresent()) {
            golem.updateWorkplace(current -> updated.get());
            golem.getBrain().eraseMemory(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value());
            golem.getBrain().eraseMemory(MemoryModuleTypeRegistry.UNREACHABLE_TARGETS.value());
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.6F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.link_removed"));
        } else {
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.nothing_to_remove"));
        }
    }

    /**
     * Setting a new link drops the links whose container is gone. Stale positions in unloaded chunks are kept, as they cannot be checked.
     */
    private static void cycleLink(ServerLevel level, ServerPlayer player, GolemEntity golem, Workplace workplace, BlockPos pos, Direction face) {
        boolean isNewLink = workplace.links().stream().noneMatch(link -> link.pos().equals(pos));
        Workplace pruned = isNewLink
                ? StylusActions.pruneLinks(workplace, stalePos -> isUnavailable(level, stalePos))
                : workplace;
        int removed = workplace.links().size() - pruned.links().size();

        Workplace updated = StylusActions.cycleLink(pruned, pos, face);
        golem.updateWorkplace(current -> updated);
        golem.getBrain().eraseMemory(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value());
        golem.getBrain().eraseMemory(MemoryModuleTypeRegistry.UNREACHABLE_TARGETS.value());
        Optional<ContainerRole> role = updated.links().stream()
                .filter(link -> link.pos().equals(pos))
                .map(ContainerLink::role)
                .findFirst();
        if (role.isPresent()) {
            level.playSound(null, pos, linkSound(role.get()), SoundSource.BLOCKS, 1.0F, 1.0F);
            Component roleName = Component.translatable("golem_role.principiamagica." + role.get().getSerializedName());
            if (removed > 0) {
                player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.link_set_pruned", roleName, removed));
            } else {
                player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.link_set", roleName));
            }
        } else {
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.6F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.link_removed"));
        }
    }

    /**
     * The first corner is set on the stylus, the second corner places the footprint between them.
     */
    private static void placeCorner(ServerLevel level, ServerPlayer player, ItemStack stack, GolemEntity golem, Workplace workplace, BlockPos pos, Direction face) {
        if (level.getBlockState(pos).canBeReplaced()) {
            BlockPos below = pos.below();
            if (!level.getBlockState(below).isAir() && !level.getBlockState(below).canBeReplaced()) {
                pos = below;
                face = Direction.UP;
            }
        }

        Optional<PendingGlyph> pending = Optional.ofNullable(stack.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get()))
                .filter(glyph -> glyph.pos().dimension().equals(level.dimension()));
        if (pending.isEmpty()) {
            level.playSound(null, pos, SoundEvents.BRUSH_GRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
            stack.set(DataComponentRegistry.STYLUS_PENDING_GLYPH.get(), PendingGlyph.of(GlobalPos.of(level.dimension(), pos), face));
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_set"));
            return;
        }

        BlockPos secondCorner = pos.offset(0, pending.get().ghostLift(), 0);
        StylusActions.EditResult result = StylusActions.completeMove(workplace, pending.get(), level.dimension(), secondCorner);
        if (result.isSuccess()) {
            boolean wasMove = pending.get().original().isPresent();
            stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
            StylusItem.applyWorkplaceEdit(player, golem, result.workplace());
            level.playSound(null, pos, SoundEvents.BRUSH_GRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.sendOverlayMessage(Component.translatable(wasMove
                    ? "message.principiamagica.stylus.corner_moved"
                    : "message.principiamagica.stylus.footprint_carved"));
            return;
        }

        level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
        switch (result.failure()) {
            case CORNER_OCCUPIED -> player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_occupied"));
            case OUT_OF_BOUNDS -> player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.out_of_bounds"));
            case TOO_LARGE, NOT_FOUND -> player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.footprint_too_large",
                    WorkFootprint.MAX_SIZE, WorkFootprint.MAX_SIZE));
            case null -> {}
        }
    }

    /**
     * @return whether the position is loaded and has no item container anymore. Unloaded positions are never judged.
     */
    private static boolean isUnavailable(ServerLevel level, BlockPos pos) {
        return level.isLoaded(pos) && !ContainerAccess.hasItemCapability(level, pos);
    }

    private static SoundEvent linkSound(ContainerRole role) {
        return switch (role) {
            case SUPPLY -> SoundEvents.BARREL_OPEN;
            case DEPOSIT -> SoundEvents.BARREL_CLOSE;
            case BOTH -> SoundEvents.NOTE_BLOCK_CHIME.value();
        };
    }
}
