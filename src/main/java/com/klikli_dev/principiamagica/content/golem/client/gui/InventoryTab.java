// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.style.BuiltinGuiParts;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprites;
import com.klikli_dev.codedefinedgui.api.widget.GuiSpriteWidget;
import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.PageContext;
import com.klikli_dev.principiamagica.client.gui.WidgetGroup;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.TabIcon;
import com.klikli_dev.principiamagica.client.gui.widget.WidgetParts;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Tool slot, golem inventory and player inventory. The slots of this tab are only active while this tab is selected
 * (see {@link GolemMenu}), so their backgrounds are only drawn here. Golem slots beyond the usable count get no background.
 */
final class InventoryTab extends GolemTab {

    private final GolemMenu menu;
    private final List<GuiSpriteWidget> golemBackgrounds = new ArrayList<>();
    private boolean[] golemVisible = new boolean[0];

    InventoryTab(Supplier<GolemView> viewSupplier, GolemMenu menu) {
        super(viewSupplier);
        this.menu = menu;
    }

    @Override
    public Component title() {
        return Component.translatable("gui.principiamagica.golem.tab.inventory");
    }

    @Override
    public TabIcon icon() {
        return TabIcon.item(new ItemStack(Items.CHEST));
    }

    @Override
    public void show(PageContext context) {
        super.show(context);
        this.golemVisible = new boolean[this.golemBackgrounds.size()];
        this.syncGolemBackgrounds(context.widgets());
    }

    @Override
    public void tick() {
        this.syncGolemBackgrounds(this.context().widgets());
    }

    @Override
    protected void build(ContentRect bounds) {
        this.add(new LabelWidget(bounds.x() + 30, bounds.y() + 24, 120, () -> Component.translatable("gui.principiamagica.golem.inventory.tool")));
        this.add(new LabelWidget(bounds.x() + 8, bounds.y() + 42, 160, () -> Component.translatable("gui.principiamagica.golem.inventory.golem")));
        this.add(new LabelWidget(bounds.x() + 8, bounds.y() + 72, 160, () -> Component.translatable("container.inventory")));

        this.add(this.slotBackground(bounds, this.menu.toolSlot()));
        for (Slot slot : this.menu.playerSlots()) {
            this.add(this.slotBackground(bounds, slot));
        }
        for (Slot slot : this.menu.golemSlots()) {
            this.golemBackgrounds.add(this.slotBackground(bounds, slot));
        }
    }

    private void syncGolemBackgrounds(WidgetGroup group) {
        int usable = this.view().usableSlots();
        for (int i = 0; i < this.golemBackgrounds.size(); i++) {
            boolean wanted = i < usable;
            if (wanted == this.golemVisible[i]) {
                continue;
            }

            if (wanted) {
                group.add(this.golemBackgrounds.get(i));
            } else {
                group.remove(this.golemBackgrounds.get(i));
            }
            this.golemVisible[i] = wanted;
        }
    }

    private GuiSpriteWidget slotBackground(ContentRect bounds, Slot slot) {
        GuiSprite sprite = WidgetParts.defaultStyle().get(BuiltinGuiParts.PLAYER_SLOT, GuiStyleProperties.SPRITE, GuiSprites.INVENTORY_SLOT);
        return new GuiSpriteWidget(bounds.x() + slot.x - 1, bounds.y() + slot.y - 1, sprite);
    }
}
