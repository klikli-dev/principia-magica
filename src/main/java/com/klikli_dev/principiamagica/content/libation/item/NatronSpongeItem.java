// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.item;

import com.klikli_dev.principiamagica.content.KeyCombo;
import com.klikli_dev.principiamagica.content.libation.PrincipleSelection;
import com.klikli_dev.principiamagica.content.libation.PrincipleSplashOptions;
import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleResource;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaCapabilities;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reusable mineral-salt purification sponge. Right-clicking a principle container blots out the HUD-selected
 * principle; crouch-right-clicking purges all principles from the container.
 */
public class NatronSpongeItem extends Item {

    public NatronSpongeItem(Properties properties) {
        super(properties);
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
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        PrincipleContents blotted = player.isShiftKeyDown()
                ? purgeAll(blockHandler)
                : blotSelected(player, blockHandler);

        if (blotted.isEmpty()) {
            return InteractionResult.PASS;
        }

        serverLevel.playSound(null, pos, SoundRegistry.NATRON_SPONGE_BLOT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        serverLevel.sendParticles(new PrincipleSplashOptions(blotted.blendedColor()),
                pos.getX() + 0.5, pos.getY() + 0.85, pos.getZ() + 0.5,
                8, 0.2, 0.1, 0.2, 0.05);
        return InteractionResult.SUCCESS;
    }

    private static PrincipleContents purgeAll(ResourceHandler<PrincipleResource> handler) {
        List<PrincipleStack> removed = new ArrayList<>();
        try (Transaction tx = Transaction.open(null)) {
            int size = handler.size();
            for (int i = 0; i < size; i++) {
                PrincipleResource resource = handler.getResource(i);
                int amount = handler.getAmountAsInt(i);
                if (!resource.isEmpty() && amount > 0) {
                    int extracted = handler.extract(i, resource, amount, tx);
                    if (extracted > 0) {
                        removed.add(new PrincipleStack(resource.principle(), extracted));
                    }
                }
            }
            if (!removed.isEmpty()) {
                tx.commit();
            }
        }
        return PrincipleContents.of(removed);
    }

    private static PrincipleContents blotSelected(Player player, ResourceHandler<PrincipleResource> handler) {
        Holder<Principle> selected = PrincipleSelection.get(player);
        PrincipleResource target = PrincipleResource.EMPTY;
        if (selected != null) {
            PrincipleResource candidate = PrincipleResource.of(selected);
            try (Transaction sim = Transaction.open(null)) {
                if (handler.extract(candidate, 1, sim) > 0) {
                    target = candidate;
                }
            }
        }
        if (target.isEmpty()) {
            int size = handler.size();
            for (int i = 0; i < size; i++) {
                PrincipleResource resource = handler.getResource(i);
                if (!resource.isEmpty() && handler.getAmountAsInt(i) > 0) {
                    target = resource;
                    break;
                }
            }
        }
        if (target.isEmpty()) {
            return PrincipleContents.EMPTY;
        }
        try (Transaction tx = Transaction.open(null)) {
            int extracted = handler.extract(target, Integer.MAX_VALUE, tx);
            if (extracted > 0) {
                tx.commit();
                return PrincipleContents.of(new PrincipleStack(target.principle(), extracted));
            }
        }
        return PrincipleContents.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
        adder.accept(Component.translatable("tooltip.principiamagica.natron_sponge.hint_blot", KeyCombo.RIGHT_CLICK.component()).withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.natron_sponge.hint_purge", KeyCombo.CROUCH_RIGHT_CLICK.component()).withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(Component.translatable("tooltip.principiamagica.natron_sponge.hint_select", KeyCombo.CROUCH_SCROLL.component()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
