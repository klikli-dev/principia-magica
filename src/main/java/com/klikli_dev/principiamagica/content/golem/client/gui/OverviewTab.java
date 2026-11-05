// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.SegmentedControl;
import com.klikli_dev.principiamagica.client.gui.widget.StatusBadge;
import com.klikli_dev.principiamagica.client.gui.widget.TabIcon;
import com.klikli_dev.principiamagica.client.gui.widget.TextButton;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageRemoveGolemAxiom;
import com.klikli_dev.principiamagica.network.messages.MessageSetGolemMode;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/**
 * Golem name, material, job, status, mode and axiom removal.
 */
final class OverviewTab extends GolemTab {

    private StatusBadge status;
    private TextButton removeAxiom;

    OverviewTab(Supplier<GolemView> viewSupplier) {
        super(viewSupplier);
    }

    @Override
    public Component title() {
        return Component.translatable("gui.principiamagica.golem.tab.overview");
    }

    @Override
    public TabIcon icon() {
        return TabIcon.item(new ItemStack(ItemRegistry.GOLEM.get()));
    }

    @Override
    protected void build(ContentRect bounds) {
        int x = bounds.x() + 8;
        int y = bounds.y() + 20;
        int width = 160;

        this.add(new LabelWidget(x, y, width, () -> Component.translatable("gui.principiamagica.golem.material", GolemGuiText.materialName(this.view()))));
        this.add(new LabelWidget(x, y + 12, width, () -> Component.translatable("gui.principiamagica.golem.job", GolemGuiText.jobName(this.view()))));
        this.add(new LabelWidget(x, y + 24, width, () -> Component.translatable("gui.principiamagica.golem.upgrades", this.view().upgrades().size(), this.view().upgradeSlots())));

        this.status = this.add(new StatusBadge(x, y + 42, Component.empty(), GolemGuiText.OK_COLOR));

        this.add(new LabelWidget(x, y + 58, width, () -> Component.translatable("gui.principiamagica.golem.mode")));
        this.add(new SegmentedControl<GolemMode>(x, y + 70, width, 18, List.of(GolemMode.values()), GolemGuiText::modeName,
                () -> this.view().mode(),
                mode -> Networking.sendToServer(new MessageSetGolemMode(this.golemId(), mode)))
                .withEnabled(mode -> mode != GolemMode.WORK || this.view().job().isPresent()));

        this.removeAxiom = this.add(new TextButton(x, y + 100, 110, 18, Component.translatable("gui.principiamagica.golem.remove_axiom"),
                () -> Networking.sendToServer(new MessageRemoveGolemAxiom(this.golemId())))
                .withTooltip(Component.translatable("gui.principiamagica.golem.remove_axiom.tooltip")));
    }

    @Override
    public void tick() {
        GolemView view = this.view();
        this.removeAxiom.active = view.job().isPresent();

        GolemStatus status = view.status();
        this.status.update(
                Component.translatable(status.translationKey()),
                status.isProblem() ? GolemGuiText.PROBLEM_COLOR : GolemGuiText.OK_COLOR,
                Component.translatable(status.translationKey() + ".description")
        );
    }
}
