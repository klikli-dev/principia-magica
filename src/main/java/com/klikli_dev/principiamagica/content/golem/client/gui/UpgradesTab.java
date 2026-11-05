// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.ScrollList;
import com.klikli_dev.principiamagica.client.gui.widget.TabIcon;
import com.klikli_dev.principiamagica.content.golem.upgrade.GolemUpgrade;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Used upgrade slots and the installed upgrades as a scrollable list. Clicking an upgrade opens its detail page.
 */
final class UpgradesTab extends GolemTab {

    private static final int ROW_HEIGHT = 18;
    private static final int LIST_HEIGHT = 116;

    private ScrollList<UpgradeRow> list;

    UpgradesTab(Supplier<GolemView> viewSupplier) {
        super(viewSupplier);
    }

    @Override
    public Component title() {
        return Component.translatable("gui.principiamagica.golem.tab.upgrades");
    }

    @Override
    public TabIcon icon() {
        return TabIcon.item(new ItemStack(ItemRegistry.SWIFTNESS_UPGRADE.get()));
    }

    @Override
    protected void build(ContentRect bounds) {
        int x = bounds.x() + 8;
        int y = bounds.y() + 20;
        int width = 160;

        this.add(new LabelWidget(x, y, width, () -> Component.translatable("gui.principiamagica.golem.upgrades",
                this.view().upgrades().size(), this.view().upgradeSlots())));
        this.list = this.add(new ScrollList<UpgradeRow>(x, y + 14, width, LIST_HEIGHT, ROW_HEIGHT, UpgradesTab::extractRow, this::openUpgrade)
                .withEmptyText(Component.translatable("gui.principiamagica.golem.no_upgrades")));
    }

    @Override
    public void tick() {
        this.list.tick();
        List<UpgradeRow> rows = new ArrayList<>();
        List<Holder<GolemUpgrade>> upgrades = this.view().upgrades();
        for (int i = 0; i < upgrades.size(); i++) {
            rows.add(new UpgradeRow(i, upgrades.get(i)));
        }
        this.list.setEntries(rows);
    }

    private void openUpgrade(UpgradeRow row) {
        this.context().push(new UpgradeDetailPage(this.viewSupplier, row.upgrade(), row.index()));
    }

    private static void extractRow(GuiGraphicsExtractor graphics, UpgradeRow row, int index, int x, int y, int width, int height, boolean hovered, int textColor) {
        Font font = Minecraft.getInstance().font;
        int textY = y + (height - 8) / 2;
        graphics.text(font, font.plainSubstrByWidth(row.upgrade().value().getName().getString(), width - 16), x + 2, textY, textColor, false);
        graphics.text(font, ">", x + width - 8, textY, textColor, false);
    }

    private record UpgradeRow(int index, Holder<GolemUpgrade> upgrade) {
    }
}
