// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.content.golem.upgrade.GolemUpgrade;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Locale;

/**
 * Text, color and item helpers shared by the golem GUI tabs and pages.
 */
final class GolemGuiText {

    static final int OK_COLOR = 0xFF1B5E20;
    static final int PROBLEM_COLOR = 0xFFA01818;

    private GolemGuiText() {
    }

    static Component golemName(GolemView view) {
        if (view.golem() == null) {
            return Component.empty();
        }

        if (view.golem().getCustomName() != null) {
            return view.golem().getCustomName();
        }

        return Component.translatable("gui.principiamagica.golem.title", materialName(view));
    }

    static Component materialName(GolemView view) {
        return view.golem() != null
                ? Component.translatable(view.golem().getSpec().material().value().getDescriptionId())
                : Component.empty();
    }

    static Component jobName(GolemView view) {
        return view.job().map(holder -> holder.value().getName()).orElse(Component.translatable("gui.principiamagica.golem.no_core"));
    }

    static Component modeName(GolemMode mode) {
        return Component.translatable("golem_mode.principiamagica." + mode.name().toLowerCase(Locale.ROOT));
    }

    static Component roleName(ContainerRole role) {
        return switch (role) {
            case SUPPLY -> Component.translatable("gui.principiamagica.golem.role_supply");
            case DEPOSIT -> Component.translatable("gui.principiamagica.golem.role_deposit");
            case BOTH -> Component.translatable("gui.principiamagica.golem.role_both");
        };
    }

    static Component blockName(BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        return level != null ? level.getBlockState(pos).getBlock().getName() : Component.empty();
    }

    static ItemStack blockItem(BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        return level != null ? new ItemStack(level.getBlockState(pos).getBlock().asItem()) : ItemStack.EMPTY;
    }

    /**
     * Formats an upgrade attribute modifier as e.g. "+20% Movement Speed" or "+3.0 Golem Inventory Slots".
     */
    static Component attribute(GolemUpgrade.AttributeEntry entry) {
        boolean percent = entry.operation() != AttributeModifier.Operation.ADD_VALUE;
        double amount = percent ? entry.amount() * 100.0 : entry.amount();
        String sign = amount >= 0 ? "+" : "";
        String value = sign + String.format(Locale.ROOT, "%.1f", amount) + (percent ? "%" : "");
        return Component.translatable("gui.principiamagica.golem.attribute", value, attributeName(entry.attribute()));
    }

    private static Component attributeName(Holder<Attribute> attribute) {
        return Component.translatable(attribute.value().getDescriptionId());
    }
}
