// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai.behaviour;

import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public final class ContainerAccess {

    private ContainerAccess() {
    }

    public static Optional<ContainerLink> linkAt(Workplace workplace, BlockPos pos, Predicate<ContainerRole> role) {
        return workplace.links().stream()
                .filter(link -> link.pos().equals(pos) && role.test(link.role()))
                .findFirst();
    }

    public static @Nullable ResourceHandler<ItemResource> itemHandler(ServerLevel level, ContainerLink link) {
        return level.getCapability(Capabilities.Item.BLOCK, link.pos(), link.side());
    }

    /**
     * Whether a link counts as a usable container for the status checks. An unloaded position is not judged and counts as usable, the same
     * as the retained-link rule of the stylus pruning. The check never loads a chunk: a loaded position needs an item handler.
     */
    public static boolean isUsable(ServerLevel level, ContainerLink link) {
        return !level.isLoaded(link.pos()) || itemHandler(level, link) != null;
    }

    /**
     * @return whether the block at pos exposes an item handler on at least one side. A block that was broken or replaced has none.
     * A handler without a side cannot be linked, as links always query a side.
     */
    public static boolean hasItemCapability(Level level, BlockPos pos) {
        return usableSide(level, pos, null) != null;
    }

    /**
     * @return the side a container link should query: the preferred side if it exposes an item handler, otherwise the first side that does
     * @see #resolveSide(Direction, Predicate)
     */
    public static @Nullable Direction usableSide(Level level, BlockPos pos, @Nullable Direction preferred) {
        return resolveSide(preferred, side -> level.getCapability(Capabilities.Item.BLOCK, pos, side) != null);
    }

    /**
     * Picks the side a container link queries. The preferred side (e.g. the face the stylus clicked) is kept if it is exposed, so
     * containers exposing all faces keep the clicked face. Otherwise the first exposed side in {@link Direction} order is used.
     *
     * @param preferred the side to prefer, or null for none
     * @param exposes   whether the container exposes an item handler on the side
     * @return the side to link, or null if no side is exposed
     */
    public static @Nullable Direction resolveSide(@Nullable Direction preferred, Predicate<Direction> exposes) {
        if (preferred != null && exposes.test(preferred)) {
            return preferred;
        }

        for (Direction side : Direction.values()) {
            if (exposes.test(side)) {
                return side;
            }
        }

        return null;
    }

    public static @Nullable Container containerAt(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof Container container ? container : null;
    }
}
