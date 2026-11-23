// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.item;

import com.klikli_dev.principiamagica.content.KeyCombo;
import com.klikli_dev.principiamagica.content.libation.PrincipleSelection;
import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleResource;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaCapabilities;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Small terracotta unguentarium that holds up to {@value #CAPACITY} mB of a single {@link Principle}.
 * Empty phials and phials holding the exact same principle and amount stack up to {@value #STACK_SIZE}.
 */
public class ClayPhialItem extends Item {

    public static final int CAPACITY = 250;
    public static final int STACK_SIZE = 16;

    public ClayPhialItem(Properties properties) {
        super(properties);
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(PrincipiaMagicaCapabilities.Principle.ITEM,
                (stack, access) -> new ClayPhialResourceHandler(access),
                ItemRegistry.CLAY_PHIAL.get());
    }

    public static @Nullable PrincipleStack getStored(ItemStack stack) {
        PrincipleContents contents = stack.get(DataComponentRegistry.STORED_PRINCIPLES.get());
        return contents == null || contents.isEmpty() ? null : contents.stacks().getFirst();
    }

    public static boolean isEmpty(ItemStack stack) {
        return getStored(stack) == null;
    }

    public static boolean isFull(ItemStack stack) {
        PrincipleStack stored = getStored(stack);
        return stored != null && stored.amount() >= CAPACITY;
    }

    public static ItemStack createFilled(Holder<Principle> principle, int amount) {
        ItemStack stack = new ItemStack(ItemRegistry.CLAY_PHIAL.get());
        int clamped = Math.min(amount, CAPACITY);
        if (clamped > 0) {
            stack.set(DataComponentRegistry.STORED_PRINCIPLES.get(), PrincipleContents.of(new PrincipleStack(principle, clamped)));
        }
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        PrincipleStack stored = getStored(stack);
        if (stored != null) {
            return Component.translatable("item.principiamagica.clay_phial.filled", stored.principle().value().name());
        }
        return super.getName(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ResourceHandler<PrincipleResource> blockHandler = level.getCapability(PrincipiaMagicaCapabilities.Principle.BLOCK, pos, context.getClickedFace());
        if (blockHandler == null) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        InteractionHand hand = context.getHand();
        ItemAccess itemAccess = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        ResourceHandler<PrincipleResource> phialHandler = itemAccess.getCapability(PrincipiaMagicaCapabilities.Principle.ITEM);
        if (phialHandler == null) {
            return InteractionResult.PASS;
        }

        PrincipleResource phialResource = phialHandler.getResource(0);
        int phialAmount = phialHandler.getAmountAsInt(0);

        if (phialResource.isEmpty() || phialAmount <= 0) {
            return tryDrawFrom(level, pos, player, blockHandler, phialHandler, CAPACITY);
        }

        // Normal click pours into the block; crouch-click (or blocks that refuse insertion like Libation Slab) tops up.
        if (!player.isShiftKeyDown() || phialAmount >= CAPACITY) {
            int poured = tryPourInto(blockHandler, phialHandler, phialResource, phialAmount);
            if (poured > 0) {
                level.playSound(null, pos, SoundRegistry.PHIAL_EMPTY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }

        if (phialAmount < CAPACITY) {
            int toppedUp = tryTopUpFrom(blockHandler, phialHandler, phialResource, CAPACITY - phialAmount);
            if (toppedUp > 0) {
                level.playSound(null, pos, SoundRegistry.PHIAL_FILL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult tryDrawFrom(Level level, BlockPos pos, Player player,
                                                 ResourceHandler<PrincipleResource> blockHandler,
                                                 ResourceHandler<PrincipleResource> phialHandler,
                                                 int maxAmount) {
        PrincipleResource target = pickTargetPrinciple(player, blockHandler);
        if (target.isEmpty()) {
            return InteractionResult.PASS;
        }
        int moved = transfer(blockHandler, phialHandler, target, maxAmount);
        if (moved <= 0) {
            return InteractionResult.PASS;
        }
        level.playSound(null, pos, SoundRegistry.PHIAL_FILL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private static int tryPourInto(ResourceHandler<PrincipleResource> blockHandler,
                                   ResourceHandler<PrincipleResource> phialHandler,
                                   PrincipleResource resource, int amount) {
        return transfer(phialHandler, blockHandler, resource, amount);
    }

    private static int tryTopUpFrom(ResourceHandler<PrincipleResource> blockHandler,
                                    ResourceHandler<PrincipleResource> phialHandler,
                                    PrincipleResource resource, int space) {
        return transfer(blockHandler, phialHandler, resource, space);
    }

    private static int transfer(ResourceHandler<PrincipleResource> from, ResourceHandler<PrincipleResource> to,
                                PrincipleResource resource, int maxAmount) {
        if (maxAmount <= 0 || resource.isEmpty()) {
            return 0;
        }
        try (Transaction tx = Transaction.open(null)) {
            int available;
            try (Transaction sim = Transaction.open(tx)) {
                available = from.extract(resource, maxAmount, sim);
            }
            if (available <= 0) {
                return 0;
            }
            int inserted = to.insert(resource, available, tx);
            if (inserted <= 0) {
                return 0;
            }
            if (from.extract(resource, inserted, tx) == inserted) {
                tx.commit();
                return inserted;
            }
        }
        return 0;
    }

    private static PrincipleResource pickTargetPrinciple(Player player, ResourceHandler<PrincipleResource> blockHandler) {
        Holder<Principle> selected = PrincipleSelection.get(player);
        if (selected != null) {
            PrincipleResource selectedResource = PrincipleResource.of(selected);
            try (Transaction sim = Transaction.open(null)) {
                if (blockHandler.extract(selectedResource, 1, sim) > 0) {
                    return selectedResource;
                }
            }
        }
        int size = blockHandler.size();
        for (int i = 0; i < size; i++) {
            PrincipleResource resource = blockHandler.getResource(i);
            if (!resource.isEmpty() && blockHandler.getAmountAsInt(i) > 0) {
                return resource;
            }
        }
        return PrincipleResource.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
        adder.accept(Component.translatable("tooltip.principiamagica.clay_phial.hint_use", KeyCombo.RIGHT_CLICK.component()).withStyle(ChatFormatting.DARK_GRAY));
        if (isEmpty(stack)) {
            adder.accept(Component.translatable("tooltip.principiamagica.clay_phial.hint_select", KeyCombo.CROUCH_SCROLL.component()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
