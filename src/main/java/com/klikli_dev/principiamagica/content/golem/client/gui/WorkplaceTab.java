// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.ScrollList;
import com.klikli_dev.principiamagica.client.gui.widget.TabIcon;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Work zones and container links as a scrollable list. Clicking a zone or a container opens its detail page.
 */
final class WorkplaceTab extends GolemTab {

    private static final int ROW_HEIGHT = 18;
    private static final int LIST_HEIGHT = 112;

    private ScrollList<Row> list;

    WorkplaceTab(Supplier<GolemView> viewSupplier) {
        super(viewSupplier);
    }

    @Override
    public Component title() {
        return Component.translatable("gui.principiamagica.golem.tab.workplace");
    }

    @Override
    public TabIcon icon() {
        return TabIcon.item(new ItemStack(ItemRegistry.COPPER_STYLUS.get()));
    }

    @Override
    protected void build(ContentRect bounds) {
        int x = bounds.x() + 8;
        int y = bounds.y() + 20;
        int width = 160;

        this.list = this.add(new ScrollList<Row>(x, y, width, LIST_HEIGHT, ROW_HEIGHT, WorkplaceTab::extractRow, this::openRow)
                .withSelectable(row -> !(row instanceof Header)));
        this.add(new LabelWidget(x, y + LIST_HEIGHT + 6, width, () -> Component.translatable("gui.principiamagica.golem.workplace_hint")));
    }

    @Override
    public void tick() {
        this.list.tick();
        this.list.setEntries(rows(this.view().workplace()));
    }

    private void openRow(Row row) {
        switch (row) {
            case ZoneRow zone -> this.context().push(new ZoneDetailPage(this.viewSupplier, zone.index()));
            case LinkRow link -> this.context().push(new LinkDetailPage(this.viewSupplier, link.index()));
            case Header header -> {
            }
        }
    }

    private static List<Row> rows(Workplace workplace) {
        List<Row> rows = new ArrayList<>();

        rows.add(new Header(Component.translatable("gui.principiamagica.golem.zones", workplace.footprints().size())));
        if (workplace.footprints().isEmpty()) {
            rows.add(new Header(Component.translatable("gui.principiamagica.golem.no_zones")));
        }
        for (int i = 0; i < workplace.footprints().size(); i++) {
            rows.add(new ZoneRow(i, workplace.footprints().get(i)));
        }

        rows.add(new Header(Component.translatable("gui.principiamagica.golem.containers", workplace.links().size())));
        if (workplace.links().isEmpty()) {
            rows.add(new Header(Component.translatable("gui.principiamagica.golem.no_containers")));
        }
        for (int i = 0; i < workplace.links().size(); i++) {
            rows.add(new LinkRow(i, workplace.links().get(i)));
        }

        return rows;
    }

    private static void extractRow(GuiGraphicsExtractor graphics, Row row, int index, int x, int y, int width, int height, boolean hovered, int textColor) {
        Font font = Minecraft.getInstance().font;
        int textY = y + (height - 8) / 2;

        switch (row) {
            case Header header -> graphics.text(font, font.plainSubstrByWidth(header.text().getString(), width - 4), x + 2, textY, textColor, false);
            case ZoneRow zone -> {
                WorkFootprint footprint = zone.footprint();
                Component text = Component.translatable("gui.principiamagica.golem.zone_row",
                        footprint.width(), footprint.depth(), GolemGuiText.signed(footprint.raise()));
                graphics.text(font, font.plainSubstrByWidth(text.getString(), width - 4), x + 2, textY, textColor, false);
            }
            case LinkRow link -> {
                ContainerLink containerLink = link.link();
                Component role = GolemGuiText.roleName(containerLink.role());
                int roleWidth = font.width(role);
                graphics.item(GolemGuiText.blockItem(containerLink.pos()), x + 1, y + 1);
                int nameWidth = width - 22 - roleWidth - 6;
                graphics.text(font, font.plainSubstrByWidth(GolemGuiText.blockName(containerLink.pos()).getString(), nameWidth), x + 22, textY, textColor, false);
                graphics.text(font, role, x + width - roleWidth - 2, textY, textColor, false);
            }
        }
    }

    private sealed interface Row permits Header, ZoneRow, LinkRow {
    }

    private record Header(Component text) implements Row {
    }

    private record ZoneRow(int index, WorkFootprint footprint) implements Row {
    }

    private record LinkRow(int index, ContainerLink link) implements Row {
    }
}
