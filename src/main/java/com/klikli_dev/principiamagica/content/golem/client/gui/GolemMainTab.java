// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.widget.GuiSpriteWidget;
import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.SegmentedControl;
import com.klikli_dev.principiamagica.client.gui.widget.StatusBadge;
import com.klikli_dev.principiamagica.client.gui.widget.TabIcon;
import com.klikli_dev.principiamagica.client.network.ClientNetworking;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenu;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenuLayout;
import com.klikli_dev.principiamagica.network.messages.MessageSetGolemMode;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * The golem tab: status dot, axiom and tool slots, mode, status hint, golem inventory and the player inventory label.
 * The slots are drawn here, the slots themselves belong to {@link GolemMenu}.
 */
final class GolemMainTab extends GolemTab {

    private static final int MODE_WIDTH = 104;
    private static final int AXIOM_LABEL_WIDTH = 62;
    private static final int CONTENT_WIDTH = 160;

    private final GolemMenu menu;
    private final List<GuiSpriteWidget> golemBackgrounds = new ArrayList<>();
    private ContentRect bounds;
    private StatusBadge status;
    private GolemSlotWidgets.Overlay toolOverlay;

    GolemMainTab(Supplier<GolemView> viewSupplier, GolemMenu menu) {
        super(viewSupplier);
        this.menu = menu;
    }

    @Override
    public Component title() {
        return Component.translatable("gui.principiamagica.golem.tab.golem");
    }

    @Override
    public TabIcon icon() {
        return TabIcon.item(new ItemStack(ItemRegistry.GOLEM.get()));
    }

    @Override
    protected void build(ContentRect bounds) {
        this.bounds = bounds;
        int x = bounds.x() + 8;

        this.status = this.add(new StatusBadge(bounds.right() - 18, bounds.y() + 5, Component.empty(), GolemGuiText.NEUTRAL_COLOR));

        Slot axiom = this.menu.axiomSlot();
        this.add(GolemSlotWidgets.background(bounds, axiom));
        this.add(new LabelWidget(bounds.x() + axiom.x + GolemSlotWidgets.SLOT_SIZE + 4, bounds.y() + axiom.y + 4, AXIOM_LABEL_WIDTH,
                () -> GolemGuiText.jobName(this.view())));

        Slot tool = this.menu.toolSlot();
        this.add(GolemSlotWidgets.background(bounds, tool));
        this.toolOverlay = this.add(GolemSlotWidgets.overlay(bounds.x() + tool.x - 1, bounds.y() + tool.y - 1,
                Component.translatable("gui.principiamagica.golem.tool.disabled")));

        this.add(new SegmentedControl<GolemMode>(bounds.x() + GolemMenuLayout.MODE_X, bounds.y() + GolemMenuLayout.MODE_Y, MODE_WIDTH, 18,
                List.of(GolemMode.values()), GolemGuiText::modeName,
                () -> this.view().mode(),
                mode -> ClientNetworking.sendToServer(new MessageSetGolemMode(this.golemId(), mode)))
                .withEnabled(mode -> mode != GolemMode.WORK || this.view().job().isPresent())
                .withTooltip(this::modeTooltip));

        this.add(new LabelWidget(x, bounds.y() + GolemMenuLayout.STATUS_Y, CONTENT_WIDTH, this::statusHint));

        for (Slot slot : this.menu.golemSlots()) {
            this.golemBackgrounds.add(this.add(GolemSlotWidgets.background(bounds, slot)));
        }
        this.updateGolemSlots(this.view().usableSlots());

        this.add(new LabelWidget(x, bounds.y() + GolemMenuLayout.INVENTORY_LABEL_Y, CONTENT_WIDTH, () -> Component.translatable("container.inventory")));
    }

    @Override
    public void tick() {
        GolemView view = this.view();

        GolemStatus status = view.status();
        this.status.update(Component.empty(), statusColor(status), Component.translatable(status.translationKey()));

        this.toolOverlay.visible = view.job().isEmpty();
        this.updateGolemSlots(view.usableSlots());
    }

    /**
     * Only the usable golem slots get a background. The others are not drawn at all, and their {@link GolemMenu} slots are inactive.
     */
    private void updateGolemSlots(int usableSlots) {
        for (int i = 0; i < this.golemBackgrounds.size(); i++) {
            this.golemBackgrounds.get(i).visible = i < usableSlots;
        }
    }

    private Component statusHint() {
        GolemStatus status = this.view().status();
        return status == GolemStatus.OK
                ? Component.empty()
                : Component.translatable("gui.principiamagica.golem.status_hint." + status.name().toLowerCase(Locale.ROOT));
    }

    private Component modeTooltip(GolemMode mode) {
        return switch (mode) {
            case WORK -> this.view().job().isPresent()
                    ? Component.translatable("gui.principiamagica.golem.mode.work.tooltip")
                    : Component.translatable("gui.principiamagica.golem.mode.work.disabled");
            case FOLLOW -> Component.translatable("gui.principiamagica.golem.mode.follow.tooltip");
            case STAY -> Component.translatable("gui.principiamagica.golem.mode.stay.tooltip");
        };
    }

    private static int statusColor(GolemStatus status) {
        if (status == GolemStatus.OK) {
            return GolemGuiText.OK_COLOR;
        }

        return status.isProblem() ? GolemGuiText.PROBLEM_COLOR : GolemGuiText.NEUTRAL_COLOR;
    }
}
