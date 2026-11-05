// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai.behaviour;

import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
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

    public static @Nullable Container containerAt(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof Container container ? container : null;
    }
}
