// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import com.klikli_dev.codedefinedgui.api.layout.LayoutMenuView;
import com.klikli_dev.codedefinedgui.api.layout.LayoutSpec;
import com.klikli_dev.codedefinedgui.api.layout.MenuBindingRegistry;
import com.klikli_dev.codedefinedgui.api.layout.MenuLayoutController;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemInventory;
import com.klikli_dev.principiamagica.registry.MenuRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Golem GUI. Slots: the golem's tool (main hand), its inventory (only usable slots are active), then the player inventory.
 */
public class GolemMenu extends AbstractContainerMenu implements LayoutMenuView {

    public static final int BUTTON_SEAL_DETAIL_OPEN = 2;
    public static final int BUTTON_SEAL_DETAIL_CLOSE = 3;

    private static final int TOOL_SLOT = 0;
    private static final int GOLEM_SLOT_START = 1;
    private static final int PLAYER_SLOT_START = GOLEM_SLOT_START + GolemInventory.MAX_SLOTS;
    private static final int HOTBAR_SLOT_START = PLAYER_SLOT_START + GolemMenuLayout.PLAYER_MAIN_ROWS * GolemMenuLayout.PLAYER_COLUMNS;
    private static final int PLAYER_SLOT_END = HOTBAR_SLOT_START + GolemMenuLayout.PLAYER_COLUMNS;

    private final int golemId;
    private final @Nullable GolemEntity golem;
    private final Inventory playerInventory;
    private final Container toolContainer;
    private final Container golemInventory;
    private final MenuLayoutController layoutController = new MenuLayoutController(this);
    private GolemMenuTab activeTab = GolemMenuTab.GOLEM;
    private boolean sealDetailOpen;

    public GolemMenu(int containerId, Inventory playerInventory, int golemId) {
        super(MenuRegistry.GOLEM.get(), containerId);
        this.golemId = golemId;
        this.playerInventory = playerInventory;

        Entity entity = playerInventory.player.level().getEntity(golemId);
        this.golem = entity instanceof GolemEntity found ? found : null;
        this.toolContainer = this.golem != null ? new GolemToolContainer(this.golem) : new SimpleContainer(1);
        this.golemInventory = this.golem != null ? this.golem.getInventory() : new SimpleContainer(GolemInventory.MAX_SLOTS);

        this.layoutController.bind();
    }

    public int getGolemId() {
        return this.golemId;
    }

    /**
     * @return the tab the client has selected. Synced to the server with {@link #clickMenuButton}.
     */
    public GolemMenuTab activeTab() {
        return this.activeTab;
    }

    /**
     * The client sends the tab ordinal as button id whenever the player switches tabs, and the seal detail button ids when it opens or closes
     * the seal detail page. The server rejects unknown ids.
     */
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == BUTTON_SEAL_DETAIL_OPEN || buttonId == BUTTON_SEAL_DETAIL_CLOSE) {
            this.sealDetailOpen = buttonId == BUTTON_SEAL_DETAIL_OPEN;
            return true;
        }

        if (buttonId < 0 || buttonId >= GolemMenuTab.values().length) {
            return false;
        }

        this.activeTab = GolemMenuTab.values()[buttonId];
        return true;
    }

    /**
     * Ignores clicks on slots that are not active, so inactive slots cannot be used from the server either.
     */
    @Override
    public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
        if (slotIndex >= 0 && slotIndex < this.slots.size() && !this.slots.get(slotIndex).isActive()) {
            return;
        }

        super.clicked(slotIndex, buttonNum, containerInput, player);
    }

    /**
     * @return true if the golem-side slots (tool and golem inventory) are shown and usable. The player inventory is always active.
     */
    private boolean isGolemSideActive() {
        return this.activeTab == GolemMenuTab.GOLEM && !this.sealDetailOpen;
    }

    @Override
    public LayoutSpec layoutSpec() {
        return GolemMenuLayout.LAYOUT;
    }

    @Override
    public void registerBindings(MenuBindingRegistry registry) {
        registry.bind("main.tool", ctx -> this.addSlot(new ToolSlot(this.toolContainer, 0, ctx.node().x(), ctx.node().y())));

        for (int i = 0; i < GolemInventory.MAX_SLOTS; i++) {
            int slot = i;
            registry.bind("golem.slot_" + i, ctx -> this.addSlot(new GolemSlot(this.golemInventory, slot, ctx.node().x(), ctx.node().y())));
        }

        for (int i = 0; i < GolemMenuLayout.PLAYER_MAIN_ROWS * GolemMenuLayout.PLAYER_COLUMNS; i++) {
            int slot = i + GolemMenuLayout.PLAYER_COLUMNS;
            registry.bind("player.slot_" + slot, ctx -> this.addSlot(new Slot(this.playerInventory, slot, ctx.node().x(), ctx.node().y())));
        }

        for (int column = 0; column < GolemMenuLayout.PLAYER_COLUMNS; column++) {
            int slot = column;
            registry.bind("player.hotbar_" + column, ctx -> this.addSlot(new Slot(this.playerInventory, slot, ctx.node().x(), ctx.node().y())));
        }
    }

    /**
     * @return the tool slot, which is only active on the inventory tab
     */
    public Slot toolSlot() {
        return this.slots.get(TOOL_SLOT);
    }

    /**
     * @return all golem inventory slots, only the usable ones are active
     */
    public List<Slot> golemSlots() {
        return this.slots.subList(GOLEM_SLOT_START, PLAYER_SLOT_START);
    }

    /**
     * @return all player inventory and hotbar slots
     */
    public List<Slot> playerSlots() {
        return this.slots.subList(PLAYER_SLOT_START, PLAYER_SLOT_END);
    }

    /**
     * @return the number of golem inventory slots the golem can currently use
     */
    public int getUsableGolemSlots() {
        return this.golem != null ? this.golem.getInventory().getUsableSlots() : 0;
    }

    private boolean canPlaceTool(ItemStack stack) {
        if (this.golem == null) {
            return false;
        }

        Optional<TagKey<Item>> requiredTool = this.golem.getSpec().job().flatMap(job -> job.value().requiredTool());
        return requiredTool.map(tag -> stack.is(tag)).orElse(true);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getEntity(this.golemId) instanceof GolemEntity golem
                && golem.isAlive()
                && player.getUUID().equals(golem.getOwnerUUID())
                && golem.distanceToSqr(player) <= GolemCommands.MAX_DISTANCE * GolemCommands.MAX_DISTANCE;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || !slot.isActive()) {
            return result;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < PLAYER_SLOT_START) {
            if (!this.moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, TOOL_SLOT, TOOL_SLOT + 1, false)
                && !this.moveItemStackTo(stack, GOLEM_SLOT_START, PLAYER_SLOT_START, false)) {
            if (index < HOTBAR_SLOT_START) {
                if (!this.moveItemStackTo(stack, HOTBAR_SLOT_START, PLAYER_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, PLAYER_SLOT_START, HOTBAR_SLOT_START, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return result;
    }

    /**
     * A slot on the golem side of the menu. Only shown and usable on the golem tab, while no seal detail page is open.
     */
    private abstract class GolemSideSlot extends Slot {
        GolemSideSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return GolemMenu.this.isGolemSideActive();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.isActive() && super.mayPlace(stack);
        }
    }

    private class ToolSlot extends GolemSideSlot {
        ToolSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return GolemMenu.this.canPlaceTool(stack) && super.mayPlace(stack);
        }
    }

    private class GolemSlot extends GolemSideSlot {
        GolemSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && this.getContainerSlot() < GolemMenu.this.getUsableGolemSlots();
        }
    }

    /**
     * Exposes the golem's main hand as a single-slot container.
     */
    private static final class GolemToolContainer implements Container {
        private final GolemEntity golem;

        GolemToolContainer(GolemEntity golem) {
            this.golem = golem;
        }

        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return this.golem.getMainHandItem().isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return this.golem.getMainHandItem();
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            ItemStack stack = this.golem.getMainHandItem();
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            ItemStack removed = stack.split(count);
            this.setItem(slot, stack);
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = this.golem.getMainHandItem();
            this.golem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            this.golem.setItemSlot(EquipmentSlot.MAINHAND, stack);
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return this.golem.isAlive();
        }

        @Override
        public void clearContent() {
            this.golem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }
}
