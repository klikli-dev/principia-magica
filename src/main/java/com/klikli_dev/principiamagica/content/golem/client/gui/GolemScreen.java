// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.screen.GuiHost;
import com.klikli_dev.codedefinedgui.api.widget.GuiBackgroundWidget;
import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.Tab;
import com.klikli_dev.principiamagica.client.gui.TabHost;
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
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Golem GUI: a vertical tab bar on the left of a vanilla-sized frame. Each tab is a {@link Tab} with its own detail pages.
 * <p>
 * The screen rebuilds a {@link GolemView} every tick. Tabs and pages read it through a supplier, so their widgets follow
 * the data without being rebuilt, and scroll positions survive refreshes. The selected tab is synced to the server
 * as a menu button click, so the server knows which slots are active.
 */
public class GolemScreen extends AbstractContainerScreen<GolemMenu> implements GuiHost {

    private static final int TEXT_COLOR = 0xFF404040;

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

        Supplier<GolemView> views = () -> this.view;
        List<Tab> tabs = List.of(
                new OverviewTab(views),
                new JobTab(views)
        );
        ContentRect frame = new ContentRect(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        this.tabs = new TabHost(this, frame, tabs, index -> this.selectTab(GolemMenuTab.values()[index]));
        this.tabs.select(this.menu.activeTab().ordinal());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.view = this.createView();
        if (this.tabs != null) {
            this.tabs.tick();
        }
    }

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
        graphics.text(this.font, this.font.plainSubstrByWidth(name, this.imageWidth - 16), 8, 6, TEXT_COLOR, false);
    }

    private void selectTab(GolemMenuTab tab) {
        if (this.menu.activeTab() == tab) {
            return;
        }

        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, tab.ordinal());
        this.menu.clickMenuButton(this.minecraft.player, tab.ordinal());
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
