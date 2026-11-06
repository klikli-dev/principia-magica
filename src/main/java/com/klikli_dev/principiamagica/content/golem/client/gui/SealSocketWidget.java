// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.style.BuiltinGuiParts;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprites;
import com.klikli_dev.principiamagica.client.gui.widget.WidgetParts;
import com.klikli_dev.principiamagica.content.golem.item.GolemUpgradeItem;
import com.klikli_dev.principiamagica.content.golem.upgrade.GolemUpgrade;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * One upgrade (seal) socket of the golem tab. Shows the installed upgrade's item, or an empty socket. Hovering shows the
 * name and effects, clicking an installed upgrade calls {@code onPress} with it.
 */
final class SealSocketWidget extends AbstractWidget {

    private static final int SIZE = GolemSlotWidgets.SLOT_SIZE;
    private static final int HOVER_COLOR = 0x60FFFFFF;

    private final Supplier<@Nullable Holder<GolemUpgrade>> upgrade;
    private final Consumer<Holder<GolemUpgrade>> onPress;
    private @Nullable Holder<GolemUpgrade> shown;
    private ItemStack icon = ItemStack.EMPTY;

    /**
     * @param x the left edge of the item position; the socket background is drawn one pixel up and left of it
     * @param y the top edge of the item position
     * @param upgrade the upgrade in this socket, read every tick
     * @param onPress called with the upgrade when an installed upgrade is clicked
     */
    SealSocketWidget(int x, int y, Supplier<@Nullable Holder<GolemUpgrade>> upgrade, Consumer<Holder<GolemUpgrade>> onPress) {
        super(x - 1, y - 1, SIZE, SIZE, Component.empty());
        this.upgrade = upgrade;
        this.onPress = onPress;
    }

    /**
     * Refreshes the shown upgrade, its icon and its tooltip. Tooltips are only recreated when the upgrade changes.
     */
    void tick() {
        @Nullable Holder<GolemUpgrade> current = this.upgrade.get();
        if (Objects.equals(current, this.shown)) {
            return;
        }

        this.shown = current;
        if (current == null) {
            this.icon = ItemStack.EMPTY;
            this.setTooltip(null);
            return;
        }

        this.icon = GolemUpgradeItem.createStack(current);
        Component tooltip = Component.empty().append(current.value().getName());
        for (GolemUpgrade.AttributeEntry entry : current.value().attributes()) {
            tooltip = tooltip.copy().append(Component.literal("\n")).append(GolemGuiText.attribute(entry));
        }
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GuiSprite sprite = WidgetParts.defaultStyle().get(BuiltinGuiParts.PLAYER_SLOT, GuiStyleProperties.SPRITE, GuiSprites.INVENTORY_SLOT);
        sprite.extractRenderState(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        if (!this.icon.isEmpty()) {
            graphics.item(this.icon, this.getX() + 1, this.getY() + 1);
        }
        if (this.isHovered()) {
            graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + SIZE - 1, this.getY() + SIZE - 1, HOVER_COLOR);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (this.shown != null) {
            this.onPress.accept(this.shown);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
