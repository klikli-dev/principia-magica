// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.item;

import com.klikli_dev.principiamagica.content.KeyCombo;
import com.klikli_dev.principiamagica.content.libation.PrincipleSelection;
import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.FluidRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Small terracotta unguentarium that holds up to {@value #CAPACITY} mB of a single {@link Principle} fluid.
 * Empty phials and phials holding the exact same principle and amount stack up to {@value #STACK_SIZE}.
 */
public class ClayPhialItem extends Item {

    public static final int CAPACITY = 250;
    public static final int STACK_SIZE = 16;

    public ClayPhialItem(Properties properties) {
        super(properties);
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Fluid.ITEM,
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
        ResourceHandler<FluidResource> blockHandler = level.getCapability(Capabilities.Fluid.BLOCK, pos, context.getClickedFace());
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
        ResourceHandler<FluidResource> phialHandler = itemAccess.getCapability(Capabilities.Fluid.ITEM);
        if (phialHandler == null) {
            return InteractionResult.PASS;
        }

        FluidResource phialResource = phialHandler.getResource(0);
        int phialAmount = phialHandler.getAmountAsInt(0);

        if (phialResource.isEmpty() || phialAmount <= 0) {
            Holder<Principle> selected = PrincipleSelection.get(player);
            ResourceStack<FluidResource> moved = selected != null
                    ? ResourceHandlerUtil.moveFirst(blockHandler, phialHandler,
                            r -> r.getFluid() == FluidRegistry.fluidFor(selected), CAPACITY, null)
                    : null;
            if (moved == null) {
                moved = ResourceHandlerUtil.moveFirst(blockHandler, phialHandler, FluidRegistry::isPrincipleFluid, CAPACITY, null);
            }
            if (moved == null) {
                return InteractionResult.PASS;
            }
            level.playSound(null, pos, SoundRegistry.PHIAL_FILL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        // Normal click pours into the block; crouch-click (or blocks that refuse insertion like Libation Slab) tops up.
        if (!player.isShiftKeyDown() || phialAmount >= CAPACITY) {
            ResourceStack<FluidResource> poured = ResourceHandlerUtil.moveFirst(
                    phialHandler, blockHandler, FluidRegistry::isPrincipleFluid, CAPACITY, null);
            if (poured != null) {
                level.playSound(null, pos, SoundRegistry.PHIAL_EMPTY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }

        if (phialAmount < CAPACITY) {
            ResourceStack<FluidResource> toppedUp = ResourceHandlerUtil.moveFirst(
                    blockHandler, phialHandler, phialResource::equals, CAPACITY - phialAmount, null);
            if (toppedUp != null) {
                level.playSound(null, pos, SoundRegistry.PHIAL_FILL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
        adder.accept(Component.translatable("tooltip.principiamagica.clay_phial.hint_use", KeyCombo.RIGHT_CLICK.component()).withStyle(ChatFormatting.DARK_GRAY));
        if (isEmpty(stack)) {
            adder.accept(Component.translatable("tooltip.principiamagica.clay_phial.hint_select", KeyCombo.CROUCH_SCROLL.component()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
