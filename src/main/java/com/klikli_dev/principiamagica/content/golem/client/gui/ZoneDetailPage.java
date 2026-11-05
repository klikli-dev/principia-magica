// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.TextButton;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageRemoveZone;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Size, ground level and raise offset of a footprint, with a button to remove it. Pops back to the list if the footprint disappears.
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

        this.add(new LabelWidget(x, y, width, () -> this.footprint()
                .map(footprint -> Component.translatable("gui.principiamagica.golem.zone.size", footprint.width(), footprint.depth()))
                .orElse(Component.empty())));
        this.add(new LabelWidget(x, y + 12, width, () -> this.footprint()
                .map(footprint -> Component.translatable("gui.principiamagica.golem.zone.ground", footprint.groundY()))
                .orElse(Component.empty())));
        this.add(new LabelWidget(x, y + 24, width, () -> this.footprint()
                .map(footprint -> Component.translatable("gui.principiamagica.golem.zone.raise", GolemGuiText.signed(footprint.raise())))
                .orElse(Component.empty())));

        this.add(new TextButton(x, y + 44, 120, 18, Component.translatable("gui.principiamagica.golem.zone.remove"), () -> {
            Networking.sendToServer(new MessageRemoveZone(this.golemId(), this.index));
            this.context().pop();
        }));
    }

    @Override
    public void tick() {
        if (this.view().workplaceKnown() && this.footprint().isEmpty()) {
            this.context().pop();
        }
    }

    private Optional<WorkFootprint> footprint() {
        List<WorkFootprint> footprints = this.view().workplace().footprints();
        return this.index < footprints.size() ? Optional.of(footprints.get(this.index)) : Optional.empty();
    }
}
