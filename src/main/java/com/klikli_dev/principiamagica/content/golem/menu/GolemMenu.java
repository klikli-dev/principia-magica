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
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.registry.MenuRegistry;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Golem GUI. Slots: the axiom (the golem's job), the tool (main hand), its inventory (only usable slots are active), then the player inventory.
 * The golem-side slots are only active on the golem tab. The player inventory is always active.
 * The slot classes are in this package: {@link AxiomSlot}, {@link ToolSlot}, {@link GolemSlot}.
 */
public class GolemMenu extends AbstractContainerMenu implements LayoutMenuView {

    private static final int AXIOM_SLOT = 0;
    private static final int TOOL_SLOT = 1;
    private static final int GOLEM_SLOT_START = 2;
    private static final int PLAYER_SLOT_START = GOLEM_SLOT_START + GolemInventory.MAX_SLOTS;
    private static final int HOTBAR_SLOT_START = PLAYER_SLOT_START + GolemMenuLayout.PLAYER_MAIN_ROWS * GolemMenuLayout.PLAYER_COLUMNS;
    private static final int PLAYER_SLOT_END = HOTBAR_SLOT_START + GolemMenuLayout.PLAYER_COLUMNS;

    private final int golemId;
    private final @Nullable GolemEntity golem;
    private final Player player;
    private final Inventory playerInventory;
    private final Container axiomContainer;
    private final Container toolContainer;
    private final Container golemInventory;
    private final MenuLayoutController layoutController = new MenuLayoutController(this);
    private GolemMenuTab activeTab = GolemMenuTab.GOLEM;

    public GolemMenu(int containerId, Inventory playerInventory, int golemId) {
        super(MenuRegistry.GOLEM.get(), containerId);
        this.golemId = golemId;
        this.player = playerInventory.player;
        this.playerInventory = playerInventory;

        Entity entity = playerInventory.player.level().getEntity(golemId);
        this.golem = entity instanceof GolemEntity found ? found : null;
        this.axiomContainer = this.golem != null ? new AxiomContainer(this.golem, this.player) : new SimpleContainer(1);
        this.toolContainer = this.golem != null ? new GolemToolContainer(this.golem) : new SimpleContainer(1);
        this.golemInventory = this.golem != null ? this.golem.getInventory() : new SimpleContainer(GolemInventory.MAX_SLOTS);

        this.layoutController.bind();
    }

    public int getGolemId() {
        return this.golemId;
    }

    /**
     * @return the tab shown in the menu. The job tab falls back to the golem tab while the golem has no axiom, so a job tab that was open
     * when the axiom was taken is closed without waiting for the client.
     */
    public GolemMenuTab activeTab() {
        return this.activeTab == GolemMenuTab.JOB && !this.hasAxiom() ? GolemMenuTab.GOLEM : this.activeTab;
    }

    /**
     * @return true if the golem has an axiom, i.e. a job. The job is part of the golem's synced spec, so the client knows it too.
     */
    public boolean hasAxiom() {
        return this.golem != null && this.golem.getSpec().job().isPresent();
    }

    /**
     * The client sends the tab ordinal as button id whenever the player switches tabs. The server rejects unknown ids and the job tab
     * while the golem has no axiom.
     */
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId < 0 || buttonId >= GolemMenuTab.values().length) {
            return false;
        }

        GolemMenuTab tab = GolemMenuTab.values()[buttonId];
        if (tab == GolemMenuTab.JOB && !this.hasAxiom()) {
            return false;
        }

        this.activeTab = tab;
        return true;
    }

    /**
     * Ignores clicks on slots that are not active or while the golem is out of reach, so neither can be used from the server either.
     */
    @Override
    public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
        if (slotIndex >= 0 && slotIndex < this.slots.size() && (!this.slots.get(slotIndex).isActive() || !this.stillValid(player))) {
            return;
        }

        super.clicked(slotIndex, buttonNum, containerInput, player);
    }

    /**
     * @return true if the golem-side slots (axiom, tool and golem inventory) are shown and usable.
     */
    boolean isGolemSideActive() {
        return this.activeTab() == GolemMenuTab.GOLEM;
    }

    @Override
    public LayoutSpec layoutSpec() {
        return GolemMenuLayout.LAYOUT;
    }

    @Override
    public void registerBindings(MenuBindingRegistry registry) {
        registry.bind("main.axiom", ctx -> this.addSlot(new AxiomSlot(this, this.axiomContainer, 0, ctx.node().x(), ctx.node().y())));
        registry.bind("main.tool", ctx -> this.addSlot(new ToolSlot(this, this.toolContainer, 0, ctx.node().x(), ctx.node().y())));

        for (int i = 0; i < GolemInventory.MAX_SLOTS; i++) {
            int slot = i;
            registry.bind("golem.slot_" + i, ctx -> this.addSlot(new GolemSlot(this, this.golemInventory, slot, ctx.node().x(), ctx.node().y())));
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
     * @return the axiom slot, which holds the golem's job
     */
    public Slot axiomSlot() {
        return this.slots.get(AXIOM_SLOT);
    }

    /**
     * @return the tool slot
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

    boolean canPlaceAxiom(ItemStack stack) {
        return this.golem != null
                && this.golem.isAlive()
                && this.player.getUUID().equals(this.golem.getOwnerUUID())
                && AxiomItem.jobOf(stack).isPresent();
    }

    /**
     * A golem without a job cannot take a tool, so the tool cannot be placed before an axiom.
     */
    boolean canPlaceTool(ItemStack stack) {
        if (this.golem == null) {
            return false;
        }

        Optional<Holder<GolemJob>> job = this.golem.getSpec().job();
        if (job.isEmpty()) {
            return false;
        }

        Optional<TagKey<Item>> requiredTool = job.get().value().requiredTool();
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
        } else if (!this.moveToGolemSide(stack)) {
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
     * Moves a stack from the player inventory to the golem side. Vanilla merges into matching stacks without checking the target, so every
     * target is gated here: nothing moves while the golem side is inactive, and the golem inventory is limited to its usable slots.
     */
    private boolean moveToGolemSide(ItemStack stack) {
        if (!this.isGolemSideActive()) {
            return false;
        }

        if (this.canPlaceAxiom(stack) && this.moveItemStackTo(stack, AXIOM_SLOT, AXIOM_SLOT + 1, false)) {
            return true;
        }

        if (this.canPlaceTool(stack) && this.moveItemStackTo(stack, TOOL_SLOT, TOOL_SLOT + 1, false)) {
            return true;
        }

        return this.moveItemStackTo(stack, GOLEM_SLOT_START, GOLEM_SLOT_START + this.getUsableGolemSlots(), false);
    }
}
