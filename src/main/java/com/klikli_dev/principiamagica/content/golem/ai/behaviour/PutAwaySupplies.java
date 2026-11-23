// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.ai.behaviour;

import net.minecraft.world.entity.item.ItemEntity;

import net.minecraft.world.item.Items;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.entity.GolemTask;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * Puts a supply the golem holds back where it came from, when the job needs its hand for something else: the tool is needed (e.g. tilling
 * needs the hoe), or there is harvest output to collect that does not merge into the held stack.
 * <p>
 * The supply goes to a SUPPLY or BOTH container first, then to a DEPOSIT container. Only if no container can take it, it is dropped
 * near the anchor. Every put-away blocks restocking of the same item for a while, so a supply cannot be fetched and put away again
 * in a tight loop.
 */
public class PutAwaySupplies extends MoveToAndInteract {
    /**
     * Restocking is blocked briefly after a put-away. The complementary conditions already prevent a loop (see {@link #needsHandFree}), so
     * this is only a short backstop: a long block would stall planting when a new spot appears right after a put-away.
     */
    public static final int RESTOCK_BLOCK_TICKS = 100;
    /** Pick-up of items is blocked for this long after a last-resort drop, so the golem does not pick its own drop straight up again. */
    private static final int PICKUP_BLOCK_TICKS = 1200;
    private static final int COOLDOWN_TICKS = 100;

    public PutAwaySupplies() {
        super(Map.of(MemoryModuleTypeRegistry.DEPOSIT_COOLDOWN.value(), MemoryStatus.VALUE_ABSENT), 0.6F);
    }

    @Override
    protected GolemTask.TaskType taskType() {
        return GolemTask.TaskType.DEPOSIT;
    }

    @Override
    protected ItemStack displayItem(ServerLevel level, GolemEntity golem, BlockPos target) {
        return blockItem(level, target);
    }

    /**
     * The golem puts a held supply away only when the job has no target for it (the exact complement of {@link RestockItems}, which only
     * fetches a supply the job wants) and something else needs the hand: work is waiting, or the job's tool is missing. Because the two
     * conditions are complementary, a supply cannot be fetched and put away again for the same reason. A supply nobody needs stays in the
     * hand. Excess items are deposited first and are not put away here.
     */
    public static boolean needsHandFree(ServerLevel level, GolemEntity golem) {
        ItemStack hand = golem.getHandItem();
        if (hand.isEmpty() || golem.isTool(hand) || DepositItems.carriesExcess(golem)) {
            return false;
        }

        boolean wanted = golem.getJob().flatMap(job -> golem.getWorkplace(level).map(workplace -> job.wantsSupply(level, golem, workplace, hand))).orElse(false);
        if (wanted) {
            return false;
        }

        // The candidate list is refreshed by the sensor only every few ticks, so each candidate is checked again right now.
        boolean workWaiting = golem.getJob().map(job -> golem.getBrain().getMemory(MemoryModuleTypeRegistry.WORK_CANDIDATES.value())
                .map(candidates -> candidates.stream().anyMatch(pos -> job.targetFinder().isTarget(level, golem, pos)))
                .orElse(false)).orElse(false);
        return workWaiting || golem.activeBlocker() == GolemStatus.MISSING_TOOL;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GolemEntity golem) {
        return needsHandFree(level, golem) && super.checkExtraStartConditions(level, golem);
    }

    @Override
    protected Optional<BlockPos> findTarget(ServerLevel level, GolemEntity golem) {
        return golem.getWorkplace(level).flatMap(workplace -> workplace.links().stream()
                .filter(link -> link.role().canSupply() || link.role().canDeposit())
                .map(ContainerLink::pos)
                .distinct()
                .filter(pos -> isAvailable(level, golem, pos))
                .filter(pos -> this.accepts(level, golem, workplace, pos))
                .min(Comparator.<BlockPos>comparingInt(pos -> supplyRank(workplace, pos))
                        .thenComparingDouble(pos -> pos.distSqr(golem.blockPosition()))));
    }

    /**
     * Supply containers come first, so a supply is returned to where it was taken from when possible.
     */
    private static int supplyRank(Workplace workplace, BlockPos pos) {
        return ContainerAccess.linkAt(workplace, pos, ContainerRole::canSupply).isPresent() ? 0 : 1;
    }

    @Override
    protected boolean isTargetValid(ServerLevel level, GolemEntity golem, BlockPos pos) {
        return !golem.getHandItem().isEmpty() && golem.getWorkplace(level)
                .map(workplace -> this.accepts(level, golem, workplace, pos))
                .orElse(false);
    }

    /**
     * @return true if the container at pos can take at least one of the held items. The insert is simulated, nothing moves.
     */
    private boolean accepts(ServerLevel level, GolemEntity golem, Workplace workplace, BlockPos pos) {
        ResourceHandler<ItemResource> handler = this.handlerAt(level, workplace, pos);
        ItemStack hand = golem.getHandItem();
        if (handler == null || hand.isEmpty()) {
            return false;
        }

        try (Transaction transaction = Transaction.openRoot()) {
            return handler.insert(ItemResource.of(hand), hand.getCount(), transaction) > 0;
        }
    }

    private ResourceHandler<ItemResource> handlerAt(ServerLevel level, Workplace workplace, BlockPos pos) {
        return ContainerAccess.linkAt(workplace, pos, role -> role.canSupply() || role.canDeposit())
                .map(link -> ContainerAccess.itemHandler(level, link))
                .orElse(null);
    }

    @Override
    protected void interact(ServerLevel level, GolemEntity golem, BlockPos pos) {
        ItemStack hand = golem.getHandItem();
        ResourceHandler<ItemResource> handler = golem.getWorkplace(level).map(workplace -> this.handlerAt(level, workplace, pos)).orElse(null);
        if (hand.isEmpty() || handler == null) {
            return;
        }

        int moved;
        try (Transaction transaction = Transaction.openRoot()) {
            moved = handler.insert(ItemResource.of(hand), hand.getCount(), transaction);
            transaction.commit();
        }

        hand.shrink(moved);
        golem.setHandItem(hand.isEmpty() ? ItemStack.EMPTY : hand);
        golem.getBrain().setMemory(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value(), RESTOCK_BLOCK_TICKS);
    }

    /**
     * No container can take the supply: it is dropped near the anchor as the last resort. Restocking and picking up are blocked for a while.
     */
    @Override
    protected void onNoTarget(ServerLevel level, GolemEntity golem) {
        ItemStack hand = golem.getHandItem();
        if (hand.isEmpty() || golem.isTool(hand)) {
            return;
        }

        BlockPos drop = golem.getAnchor().filter(anchor -> anchor.dimension().equals(level.dimension())).map(GlobalPos::pos).orElse(golem.blockPosition());
        Block.popResource(level, drop, hand.copy());
        golem.setHandItem(ItemStack.EMPTY);
        golem.getBrain().setMemory(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value(), RESTOCK_BLOCK_TICKS);
        golem.getBrain().setMemory(MemoryModuleTypeRegistry.PICKUP_COOLDOWN.value(), PICKUP_BLOCK_TICKS);
        golem.getBrain().setMemory(MemoryModuleTypeRegistry.DEPOSIT_COOLDOWN.value(), COOLDOWN_TICKS);
    }

    @Override
    protected boolean reserveTarget() {
        return false;
    }
}
