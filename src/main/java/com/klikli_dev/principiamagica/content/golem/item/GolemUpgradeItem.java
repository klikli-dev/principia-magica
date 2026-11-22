// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.upgrade.GolemUpgrade;
import com.klikli_dev.principiamagica.content.golem.upgrade.UpgradeAddResult;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * One item per upgrade. Right-click an owned golem to install the upgrade, if it has a free upgrade slot and does not
 * have the upgrade yet. Removing upgrades is done in the golem GUI.
 */
public class GolemUpgradeItem extends Item {
    private final ResourceKey<GolemUpgrade> upgrade;

    public GolemUpgradeItem(Properties properties, ResourceKey<GolemUpgrade> upgrade) {
        super(properties);
        this.upgrade = upgrade;
    }

    public ResourceKey<GolemUpgrade> getUpgrade() {
        return this.upgrade;
    }

    /**
     * Creates the upgrade item for the given upgrade, or an empty stack if no upgrade item is registered for it.
     */
    public static ItemStack createStack(Holder<GolemUpgrade> upgrade) {
        ResourceKey<GolemUpgrade> key = upgrade.unwrapKey().orElse(null);
        if (key == null) {
            return ItemStack.EMPTY;
        }

        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof GolemUpgradeItem upgradeItem && upgradeItem.getUpgrade().equals(key))
                .findFirst()
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof GolemEntity golem)) {
            return InteractionResult.PASS;
        }

        if (golem.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!player.getUUID().equals(golem.getOwnerUUID())) {
            return InteractionResult.PASS;
        }

        Holder<GolemUpgrade> holder = PrincipiaMagicaRegistries.GOLEM_UPGRADE.get(this.upgrade).orElse(null);
        if (holder == null) {
            return InteractionResult.FAIL;
        }

        UpgradeAddResult result = golem.addUpgrade(holder);
        switch (result) {
            case ADDED -> {
                stack.consume(1, player);
                player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.upgrade_added", holder.value().getName()));
                return InteractionResult.SUCCESS;
            }
            case DUPLICATE -> {
                player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.upgrade_duplicate"));
                return InteractionResult.FAIL;
            }
            case NO_FREE_SLOT -> {
                player.sendOverlayMessage(Component.translatable("message.principiamagica.golem.upgrade_no_slot"));
                return InteractionResult.FAIL;
            }
            default -> {
                return InteractionResult.FAIL;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
        GolemUpgrade upgrade = PrincipiaMagicaRegistries.GOLEM_UPGRADE.getValue(this.upgrade);
        if (upgrade == null) {
            return;
        }

        adder.accept(Component.translatable(upgrade.getDescriptionId() + ".description").withStyle(ChatFormatting.GRAY));
    }
}
