// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.PageContext;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.TextButton;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenu;
import com.klikli_dev.principiamagica.content.golem.upgrade.GolemUpgrade;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageRemoveGolemUpgrade;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Description and attribute effects of an installed upgrade, with a button to remove it. While it is shown the menu is told
 * that the seal detail page is open, so the golem-side slots are inactive. Showing and hiding it sends the matching menu button.
 */
final class UpgradeDetailPage extends GolemDetailPage {

    private static final int EFFECT_SPACING = 11;
    private static final int REMOVE_Y = 122;

    private final Holder<GolemUpgrade> upgrade;
    private final int index;
    private final IntConsumer menuButtons;

    UpgradeDetailPage(Supplier<GolemView> viewSupplier, Holder<GolemUpgrade> upgrade, int index, IntConsumer menuButtons) {
        super(viewSupplier, upgrade.value().getName());
        this.upgrade = upgrade;
        this.index = index;
        this.menuButtons = menuButtons;
    }

    @Override
    public void show(PageContext context) {
        super.show(context);
        this.menuButtons.accept(GolemMenu.BUTTON_SEAL_DETAIL_OPEN);
    }

    @Override
    public void hide() {
        super.hide();
        this.menuButtons.accept(GolemMenu.BUTTON_SEAL_DETAIL_CLOSE);
    }

    @Override
    protected void build(ContentRect bounds) {
        int y = this.addHeader(bounds);
        int x = bounds.x() + 8;
        int width = 160;

        this.add(new LabelWidget(x, y, width, () -> Component.translatable(this.upgrade.value().getDescriptionId() + ".description")));

        int effectsY = y + 30;
        this.add(new LabelWidget(x, effectsY, width, () -> Component.translatable("gui.principiamagica.golem.upgrade.effects")));
        int i = 0;
        for (GolemUpgrade.AttributeEntry entry : this.upgrade.value().attributes()) {
            this.add(new LabelWidget(x + 4, effectsY + 12 + i * EFFECT_SPACING, width - 4, () -> GolemGuiText.attribute(entry)));
            i++;
        }

        this.add(new TextButton(x, bounds.y() + REMOVE_Y, 120, 16, Component.translatable("gui.principiamagica.golem.upgrade.remove"), () -> {
            Networking.sendToServer(new MessageRemoveGolemUpgrade(this.golemId(), this.index));
            this.context().pop();
        }));
    }

    @Override
    public void tick() {
        if (this.view().workplaceKnown() && this.view().upgrades().size() <= this.index) {
            this.context().pop();
        }
    }
}
