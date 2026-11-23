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
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
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
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.Optional;

/**
 * A stylus click on a block for the golem selected on the stylus. What it does depends on the block and on sneaking:
 * <ul>
 *     <li>container, plain: cycles the container link (supply, deposit, both, removed)</li>
 *     <li>container, sneaking: removes the container link</li>
 *     <li>other block, sneaking: erases the footprint at the block</li>
 *     <li>other block, plain: sets or carves the pending corner</li>
 * </ul>
 */
final class StylusBlockInteraction {

    private StylusBlockInteraction() {
    }

    static void interact(ServerLevel level, ServerPlayer player, ItemStack stack, GolemEntity golem, BlockPos pos, Direction face) {
        boolean container = level.getCapability(Capabilities.Item.BLOCK, pos, face) != null;
        boolean sneaking = player.isShiftKeyDown();
        PendingGlyph pending = stack.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
        boolean hasPending = pending != null && pending.pos().dimension().equals(level.dimension());
        Workplace workplace = golem.getWorkplace(level).orElse(Workplace.EMPTY);

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

    private static void removeLink(ServerLevel level, ServerPlayer player, GolemEntity golem, Workplace workplace, BlockPos pos) {
        Optional<Workplace> updated = StylusActions.removeLink(workplace, pos);
        if (updated.isPresent()) {
            golem.updateWorkplace(current -> updated.get());
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.6F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.link_removed"));
        } else {
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.nothing_to_remove"));
        }
    }

    private static void cycleLink(ServerLevel level, ServerPlayer player, GolemEntity golem, Workplace workplace, BlockPos pos, Direction face) {
        Workplace updated = StylusActions.cycleLink(workplace, pos, face);
        golem.updateWorkplace(current -> updated);
        Optional<ContainerRole> role = updated.links().stream()
                .filter(link -> link.pos().equals(pos))
                .map(ContainerLink::role)
                .findFirst();
        if (role.isPresent()) {
            level.playSound(null, pos, linkSound(role.get()), SoundSource.BLOCKS, 1.0F, 1.0F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.link_set", Component.translatable("golem_role.principiamagica." + role.get().getSerializedName())));
        } else {
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.6F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.link_removed"));
        }
    }

    private static void eraseFootprint(ServerLevel level, ServerPlayer player, GolemEntity golem, Workplace workplace, BlockPos pos) {
        int below = golem.getJob().map(GolemJob::below).orElse(WorkFootprint.DEFAULT_BELOW);
        int above = golem.getJob().map(GolemJob::above).orElse(WorkFootprint.DEFAULT_ABOVE);
        Optional<Workplace> updated = StylusActions.removeFootprintAt(workplace, pos, below, above);
        if (updated.isPresent()) {
            golem.updateWorkplace(current -> updated.get());
            level.playSound(null, pos, SoundEvents.CALCITE_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.footprint_erased"));
        } else {
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.nothing_to_remove"));
        }
    }

    /**
     * The first corner is set on the stylus, the second corner carves the footprint between them.
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
            if (workplace.hasCornerAt(pos)) {
                level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
                player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_occupied"));
                return;
            }

            level.playSound(null, pos, SoundEvents.BRUSH_GRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
            stack.set(DataComponentRegistry.STYLUS_PENDING_GLYPH.get(), PendingGlyph.of(GlobalPos.of(level.dimension(), pos), face));
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.corner_set"));
            return;
        }

        StylusActions.EditResult result = StylusActions.carveResult(workplace, pending.get().glyph(), new CarvedGlyph(pos, face, pending.get().ghostLift()));
        if (result.isSuccess()) {
            stack.remove(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
            golem.updateWorkplace(current -> result.workplace());
            level.playSound(null, pos, SoundEvents.BRUSH_GRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.sendOverlayMessage(Component.translatable("message.principiamagica.stylus.footprint_carved"));
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

    private static SoundEvent linkSound(ContainerRole role) {
        return switch (role) {
            case SUPPLY -> SoundEvents.BARREL_OPEN;
            case DEPOSIT -> SoundEvents.BARREL_CLOSE;
            case BOTH -> SoundEvents.NOTE_BLOCK_CHIME.value();
        };
    }
}
