// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.screen.ContentRect;
import com.klikli_dev.codedefinedgui.api.screen.GuiHost;
import com.klikli_dev.codedefinedgui.api.screen.WidgetGroup;
import com.klikli_dev.codedefinedgui.api.screen.page.Tab;
import com.klikli_dev.codedefinedgui.api.screen.page.TabHost;
import com.klikli_dev.codedefinedgui.api.style.BuiltinGuiParts;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.widget.GuiSpriteWidget;
import com.klikli_dev.codedefinedgui.api.widget.ScrollListWidget;
import com.klikli_dev.principiamagica.client.gui.GuiStyles;
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
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Golem GUI: a vertical tab rail on the left of the golem panel, and the player inventory panel below the golem panel. The golem panel
 * has a name cartouche at the top, and holds the golem tab or the job tab. The composition is {@link GolemMenuLayout#WIDTH} by
 * {@link GolemMenuLayout#HEIGHT}, and the screen centres it.
 * <p>
 * The screen rebuilds a {@link GolemView} every tick. Tabs and pages read it through a supplier, so their widgets follow the data without
 * being rebuilt, and scroll positions survive refreshes. Tab switches are sent to the server as menu button clicks (see
 * {@link #pressMenuButton}), so the server knows which slots are active.
 */
public class GolemScreen extends AbstractContainerScreen<GolemMenu> implements GuiHost {

    private static final int CARTOUCHE_X = 8;
    private static final int CARTOUCHE_Y = 6;
    private static final int CARTOUCHE_WIDTH = 188;
    private static final int CARTOUCHE_HEIGHT = 17;
    /**
     * The height of the capitals and digits of the font. The font's line height adds one pixel of descender space below the baseline, so the
     * text is centred with this height, not with the line height.
     */
    private static final int TITLE_GLYPH_HEIGHT = 7;

    private GolemView view;
    private @Nullable TabHost tabs;

    public GolemScreen(GolemMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GolemMenuLayout.WIDTH, GolemMenuLayout.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        this.view = this.createView();

        ContentRect screen = new ContentRect(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        ContentRect panel = new ContentRect(this.leftPos + GolemMenuLayout.PANEL_X, this.topPos + GolemMenuLayout.PANEL_Y,
                GolemMenuLayout.PANEL_WIDTH, GolemMenuLayout.PANEL_HEIGHT);
        ContentRect inventory = new ContentRect(this.leftPos + GolemMenuLayout.INVENTORY_PANEL_X, this.topPos + GolemMenuLayout.INVENTORY_PANEL_Y,
                GolemMenuLayout.INVENTORY_PANEL_WIDTH, GolemMenuLayout.INVENTORY_PANEL_HEIGHT);
        ContentRect content = GolemMenuLayout.content(panel);

        StyleContext style = GuiStyles.context();
        this.addGuiWidget(new GuiSpriteWidget(panel.x(), panel.y(), panel.width(), panel.height(), this.sprite(style, "panel")));
        this.addGuiWidget(new GuiSpriteWidget(inventory.x(), inventory.y(), inventory.width(), inventory.height(),
                style.sprite(BuiltinGuiParts.PLAYER_INVENTORY_BACKGROUND, GuiStyles.sprite("inventory_panel"))));
        this.addGuiWidget(new GuiSpriteWidget(content.x() + CARTOUCHE_X, content.y() + CARTOUCHE_Y, CARTOUCHE_WIDTH, CARTOUCHE_HEIGHT,
                this.sprite(style, "cartouche")));

        WidgetGroup playerSlots = new WidgetGroup(this);
        GuiSprite slot = style.sprite(BuiltinGuiParts.PLAYER_SLOT, GuiStyles.sprite("slot"));
        for (Slot playerSlot : this.menu.playerSlots()) {
            playerSlots.add(GolemSlotWidgets.background(screen, playerSlot, slot, -1, -1));
        }

        Supplier<GolemView> views = () -> this.view;
        List<Tab> tabs = List.of(
                new GolemMainTab(views, this.menu, screen),
                new JobTab(views)
        );
        this.tabs = new TabHost(this, panel, tabs, index -> this.selectTab(GolemMenuTab.values()[index]), style);
        this.tabs.setUnavailableTooltip(GolemMenuTab.JOB.ordinal(), Component.translatable("gui.principiamagica.golem.tab.job.disabled"));
        this.updateTabs();
        this.tabs.select(this.menu.activeTab().ordinal());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.view = this.createView();
        if (this.tabs != null) {
            this.updateTabs();
            this.tabs.tick();
        }
    }

    /**
     * The job tab is always shown, but it is disabled while the golem has no axiom. Losing the axiom while the job tab is open switches to the
     * golem tab.
     */
    private void updateTabs() {
        boolean hasAxiom = this.view.job().isPresent();
        this.tabs.setAvailable(GolemMenuTab.JOB.ordinal(), hasAxiom);
        if (!hasAxiom) {
            this.tabs.select(GolemMenuTab.GOLEM.ordinal());
        }
    }

    /**
     * The wheel scrolls the scroll list under the cursor first. Vanilla's container screen only routes the wheel to slots, so the list
     * would never see it otherwise.
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (GuiEventListener child : this.children()) {
            if (child instanceof ScrollListWidget<?> list && list.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * ESC pops the detail page of the selected tab first, before the screen closes.
     */
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE && this.tabs != null && this.tabs.popPage()) {
            return true;
        }

        return super.keyPressed(event);
    }

    /**
     * Draws the golem's name in the cartouche. Vanilla's title and inventory labels are replaced by this, so they never collide with the panels.
     * The name is centred on the cartouche, both ways, without shadow. Coordinates are relative to the screen, like vanilla's labels.
     */
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        String name = this.font.plainSubstrByWidth(GolemGuiText.golemName(this.view).getString(), CARTOUCHE_WIDTH - 2 * CARTOUCHE_X);
        int cartoucheTop = GolemMenuLayout.CONTENT_Y + CARTOUCHE_Y;
        int nameY = cartoucheTop + (CARTOUCHE_HEIGHT - TITLE_GLYPH_HEIGHT) / 2;
        int nameX = GolemMenuLayout.PANEL_X + GolemMenuLayout.PANEL_WIDTH / 2;
        graphics.text(this.font, name, nameX - this.font.width(name) / 2, nameY, GuiStyles.TEXT_PRIMARY, false);
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

    private GuiSprite sprite(StyleContext style, String name) {
        return style.sprite(GuiStyles.part(name), GuiStyles.sprite(name));
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
