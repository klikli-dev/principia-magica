// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.screen.GuiHost;
import com.klikli_dev.codedefinedgui.api.widget.GuiBackgroundWidget;
import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.Tab;
import com.klikli_dev.principiamagica.client.gui.TabHost;
import com.klikli_dev.principiamagica.client.gui.WidgetGroup;
import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache.Entry;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenu;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenuLayout;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenuTab;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Golem GUI: a vertical tab bar on the left of a 176x222 frame. The Golem tab and the Job tab are {@link Tab}s with their own
 * detail pages. The player inventory backgrounds belong to the screen, so they stay drawn on both tabs.
 * <p>
 * The screen rebuilds a {@link GolemView} every tick. Tabs and pages read it through a supplier, so their widgets follow
 * the data without being rebuilt, and scroll positions survive refreshes. Tab switches and seal detail pages are sent to the
 * server as menu button clicks (see {@link #pressMenuButton}), so the server knows which slots are active.
 */
public class GolemScreen extends AbstractContainerScreen<GolemMenu> implements GuiHost {

    private static final int TEXT_COLOR = 0xFF404040;
    private static final int NAME_RESERVED_WIDTH = 28;
    private static final int PREVIEW_SIZE = 24;
    private static final float PREVIEW_OFFSET_Y = 0.0625F;

    private GolemView view;
    private @Nullable TabHost tabs;

    public GolemScreen(GolemMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GolemMenuLayout.WIDTH, GolemMenuLayout.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        this.view = this.createView();
        this.addGuiWidget(new GuiBackgroundWidget(this));

        ContentRect frame = new ContentRect(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        WidgetGroup playerSlots = new WidgetGroup(this);
        for (Slot slot : this.menu.playerSlots()) {
            playerSlots.add(GolemSlotWidgets.background(frame, slot));
        }

        Supplier<GolemView> views = () -> this.view;
        List<Tab> tabs = List.of(
                new GolemMainTab(views, this.menu, this::pressMenuButton),
                new JobTab(views)
        );
        this.tabs = new TabHost(this, frame, tabs, index -> this.selectTab(GolemMenuTab.values()[index]));
        this.tabs.select(this.menu.activeTab().ordinal());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.view = this.createView();
        if (this.tabs != null) {
            boolean hasJob = this.view.job().isPresent();
            this.tabs.setEnabled(GolemMenuTab.JOB.ordinal(), hasJob);
            if (!hasJob) {
                this.tabs.select(GolemMenuTab.GOLEM.ordinal());
            }
            this.tabs.tick();
        }
    }

    /**
     * ESC pops a seal detail page first. The pop hides the page, which sends the close button to the server.
     */
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE && this.tabs != null && this.tabs.popPage()) {
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        String name = GolemGuiText.golemName(this.view).getString();
        graphics.text(this.font, this.font.plainSubstrByWidth(name, this.imageWidth - 16 - NAME_RESERVED_WIDTH), 8, 6, TEXT_COLOR, false);

        if (this.showsGolemPreview() && this.view.golem() != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                    GolemMenuLayout.PREVIEW_X, GolemMenuLayout.PREVIEW_Y,
                    GolemMenuLayout.PREVIEW_X + GolemMenuLayout.PREVIEW_WIDTH, GolemMenuLayout.PREVIEW_Y + GolemMenuLayout.PREVIEW_HEIGHT,
                    PREVIEW_SIZE, PREVIEW_OFFSET_Y, mouseX - this.leftPos, mouseY - this.topPos, this.view.golem());
        }
    }

    /**
     * The preview is drawn on the golem tab's root page only, so it disappears with the golem tab's widgets.
     */
    private boolean showsGolemPreview() {
        return this.menu.activeTab() == GolemMenuTab.GOLEM && !this.menu.isSealDetailOpen();
    }

    private void selectTab(GolemMenuTab tab) {
        if (this.menu.activeTab() == tab) {
            return;
        }

        this.pressMenuButton(tab.ordinal());
    }

    /**
     * Sends a menu button to the server and applies it to the client menu, so the client's slot activity matches the server.
     */
    private void pressMenuButton(int buttonId) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        this.menu.clickMenuButton(this.minecraft.player, buttonId);
    }

    private GolemView createView() {
        int golemId = this.menu.getGolemId();
        @Nullable GolemEntity golem = this.minecraft.level != null && this.minecraft.level.getEntity(golemId) instanceof GolemEntity found ? found : null;
        Optional<Entry> entry = ClientGolemWorkplaceCache.latest(golemId);
        Workplace workplace = entry.map(Entry::workplace).orElse(Workplace.EMPTY);

        return new GolemView(
                golemId,
                golem,
                entry.isPresent(),
                workplace,
                golem != null ? golem.getMode() : GolemMode.WORK,
                golem != null ? golem.getSpec().job() : Optional.empty(),
                golem != null ? golem.getSpec().upgrades() : List.of(),
                golem != null ? golem.getSpec().material().value().upgradeSlots() : 0,
                this.menu.getUsableGolemSlots(),
                golem != null ? golem.getStatus() : GolemStatus.OK
        );
    }

    @Override
    public int leftPos() {
        return this.leftPos;
    }

    @Override
    public int topPos() {
        return this.topPos;
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public int imageWidth() {
        return this.imageWidth;
    }

    @Override
    public int imageHeight() {
        return this.imageHeight;
    }

    @Override
    public <T extends AbstractWidget> T addGuiWidget(T widget) {
        return this.addRenderableWidget(widget);
    }

    @Override
    public void removeGuiWidget(AbstractWidget widget) {
        this.removeWidget(widget);
    }
}
