// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.TextButton;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageRemoveZone;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Size and bounds of a work zone, with a button to remove it. Pops back to the list if the zone disappears.
 */
final class ZoneDetailPage extends GolemDetailPage {

    private final int index;

    ZoneDetailPage(Supplier<GolemView> viewSupplier, int index) {
        super(viewSupplier, Component.translatable("gui.principiamagica.golem.zone.title", index + 1));
        this.index = index;
    }

    @Override
    protected void build(ContentRect bounds) {
        int y = this.addHeader(bounds);
        int x = bounds.x() + 8;
        int width = 160;

        this.add(new LabelWidget(x, y, width, () -> this.zone()
                .map(zone -> Component.translatable("gui.principiamagica.golem.zone.size", zone.width(), zone.height(), zone.depth()))
                .orElse(Component.empty())));
        this.add(new LabelWidget(x, y + 12, width, () -> this.zone()
                .map(zone -> Component.translatable("gui.principiamagica.golem.zone.from", zone.min().getX(), zone.min().getY(), zone.min().getZ()))
                .orElse(Component.empty())));
        this.add(new LabelWidget(x, y + 24, width, () -> this.zone()
                .map(zone -> Component.translatable("gui.principiamagica.golem.zone.to", zone.max().getX(), zone.max().getY(), zone.max().getZ()))
                .orElse(Component.empty())));

        this.add(new TextButton(x, y + 44, 120, 18, Component.translatable("gui.principiamagica.golem.zone.remove"), () -> {
            Networking.sendToServer(new MessageRemoveZone(this.golemId(), this.index));
            this.context().pop();
        }));
    }

    @Override
    public void tick() {
        if (this.view().workplaceKnown() && this.zone().isEmpty()) {
            this.context().pop();
        }
    }

    private Optional<WorkZone> zone() {
        List<WorkZone> zones = this.view().workplace().zones();
        return this.index < zones.size() ? Optional.of(zones.get(this.index)) : Optional.empty();
    }
}
