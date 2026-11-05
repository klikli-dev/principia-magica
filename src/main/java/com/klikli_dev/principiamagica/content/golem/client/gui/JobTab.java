// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.TabIcon;
import com.klikli_dev.principiamagica.client.gui.widget.ToggleRow;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.job.JobOption;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageSetJobOption;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Job options as toggle rows. Shows a hint instead if the golem has no job axiom.
 */
final class JobTab extends GolemTab {

    private static final int ROW_SPACING = 20;
    private static final int ROW_HEIGHT = 18;

    private final List<ToggleRow> rows = new ArrayList<>();
    private List<JobOption> rowOptions = List.of();
    private int rowX;
    private int rowY;
    private int rowWidth;

    JobTab(Supplier<GolemView> viewSupplier) {
        super(viewSupplier);
    }

    @Override
    public Component title() {
        return Component.translatable("gui.principiamagica.golem.tab.job");
    }

    @Override
    public TabIcon icon() {
        ItemStack axiom = this.view().job().map(AxiomItem::createStack).filter(stack -> !stack.isEmpty()).orElseGet(() -> new ItemStack(ItemRegistry.AXIOM_OF_HARVEST.get()));
        return TabIcon.item(axiom);
    }

    @Override
    protected void build(ContentRect bounds) {
        int x = bounds.x() + 8;
        int y = bounds.y() + 20;
        this.rowX = x;
        this.rowY = y + 30;
        this.rowWidth = 160;

        this.add(new LabelWidget(x, y, this.rowWidth, () -> GolemGuiText.jobName(this.view())));
        this.add(new LabelWidget(x, y + 12, this.rowWidth, () -> this.view().job().isPresent()
                ? Component.translatable("gui.principiamagica.golem.options")
                : Component.translatable("gui.principiamagica.golem.no_job_hint")));
    }

    @Override
    public void tick() {
        List<JobOption> options = this.view().job().map(job -> job.value().options()).orElse(List.of());
        if (options.equals(this.rowOptions)) {
            return;
        }

        for (ToggleRow row : this.rows) {
            this.removeLive(row);
        }
        this.rows.clear();
        this.rowOptions = List.copyOf(options);

        for (int i = 0; i < options.size(); i++) {
            ToggleRow row = this.createRow(options.get(i), this.rowY + i * ROW_SPACING);
            this.rows.add(row);
            this.addLive(row);
        }
    }

    private ToggleRow createRow(JobOption option, int y) {
        return new ToggleRow(this.rowX, y, this.rowWidth, ROW_HEIGHT, Component.translatable(option.translationKey()),
                () -> this.view().workplace().options().get(option),
                () -> Networking.sendToServer(new MessageSetJobOption(this.golemId(), option.key(), !this.view().workplace().options().get(option))))
                .withDescription(Component.translatable(option.descriptionKey()));
    }
}
