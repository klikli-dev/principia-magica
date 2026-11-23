// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.style.BuiltinGuiParts;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.GuiStyles;
import com.klikli_dev.principiamagica.client.gui.widget.IconToggleButton;
import com.klikli_dev.principiamagica.client.gui.widget.ItemIcon;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.ProgressBar;
import com.klikli_dev.principiamagica.client.gui.widget.SpriteWidget;
import com.klikli_dev.principiamagica.client.gui.widget.TabIcon;
import com.klikli_dev.principiamagica.client.gui.widget.TooltipArea;
import com.klikli_dev.principiamagica.client.gui.widget.WidgetParts;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.entity.GolemTask;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenu;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageSetGolemMode;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The golem tab: the portrait and health on the left, the axiom with the job, the hand and back slots, the mode buttons and the status
 * tablet on the right. The portrait is a {@link GolemPortrait} widget.
 * <p>
 * Slots belong to {@link GolemMenu}. This tab only draws their sprites, ghosts and hints.
 */
final class GolemMainTab extends GolemTab {

    /**
     * Content-relative positions of the widgets. The content is the golem panel's rectangle inside its border.
     */
    static final int PORTRAIT_X = 8;
    static final int PORTRAIT_Y = 26;
    static final int PORTRAIT_WIDTH = 50;
    static final int PORTRAIT_HEIGHT = 66;
    static final int PORTRAIT_SCALE = 30;
    static final int HEALTH_X = 8;
    static final int HEALTH_Y = 109;
    static final int JOB_X = 104;
    static final int JOB_Y = 29;
    static final int JOB_WIDTH = 92;
    static final int STATUS_X = 74;
    static final int STATUS_Y = 82;
    static final int STATUS_WIDTH = 122;
    static final int STATUS_HEIGHT = 35;
    static final int STATUS_TEXT_OFFSET = 16;
    static final int STATUS_ICON_SIZE = 11;
    static final int STATUS_TEXT_RIGHT_PADDING = 2;
    static final int MODE_Y = 56;
    static final int MODE_SIZE = 24;
    static final int MODE_SPACING = 26;
    static final int MODE_X = 120;

    private final GolemMenu menu;
    private final ContentRect screen;
    private final StyleContext style = WidgetParts.defaultStyle();
    private final Map<GolemMode, IconToggleButton> modes = new EnumMap<>(GolemMode.class);
    private ContentRect bounds;
    private ProgressBar health;
    private SpriteWidget axiomGhost;
    private TooltipArea axiomHint;
    private SpriteWidget handGhost;
    private TooltipArea handHint;
    private TooltipArea backHint;
    private SpriteWidget statusIcon;
    private ItemIcon statusItem;
    private LabelWidget headlineLabel;
    private LabelWidget hintLabel;
    private TooltipArea statusTooltip;

    /**
     * @param viewSupplier the golem view of the screen
     * @param menu the menu, for the slots
     * @param screen the screen's rectangle, the slot positions are relative to it
     */
    GolemMainTab(Supplier<GolemView> viewSupplier, GolemMenu menu, ContentRect screen) {
        super(viewSupplier);
        this.menu = menu;
        this.screen = screen;
    }

    @Override
    public Component title() {
        return Component.translatable("gui.principiamagica.golem.tab.golem");
    }

    @Override
    public TabIcon icon() {
        return TabIcon.sprite(GuiStyles.sprite("icon_golem"));
    }

    @Override
    protected void build(ContentRect bounds) {
        this.bounds = bounds;
        this.add(new SpriteWidget(bounds.x() + PORTRAIT_X, bounds.y() + PORTRAIT_Y, 58, 80, this.sprite("portrait_frame")));
        // The niche interior is 4 px in from the frame's left and 10 px down from its top.
        this.add(new GolemPortrait(bounds.x() + PORTRAIT_X + 4, bounds.y() + PORTRAIT_Y + 10, PORTRAIT_WIDTH, PORTRAIT_HEIGHT, PORTRAIT_SCALE,
                () -> this.view().golem()));

        this.health = this.add(new ProgressBar(bounds.x() + HEALTH_X, bounds.y() + HEALTH_Y, 58, 7, this::healthFraction).withStyle(this.style));

        this.buildGolemSlots(bounds);
        this.buildJobLabels(bounds);
        this.buildModes(bounds);
        this.buildStatusTablet(bounds);
    }

    private void buildGolemSlots(ContentRect bounds) {
        Slot axiom = this.menu.axiomSlot();
        this.add(GolemSlotWidgets.background(this.screen, axiom, this.sprite("slot_axiom"), -5, -5));
        this.axiomGhost = this.add(GolemSlotWidgets.itemSprite(this.screen, axiom, this.sprite("ghost_axiom")));
        this.axiomHint = this.add(GolemSlotWidgets.hint(this.screen, axiom, Component.translatable("gui.principiamagica.golem.axiom.tooltip")));

        Slot hand = this.menu.handSlot();
        this.add(GolemSlotWidgets.background(this.screen, hand, this.slotSprite(), -1, -1));
        this.handGhost = this.add(GolemSlotWidgets.itemSprite(this.screen, hand, this.sprite("ghost_hand")));
        this.handHint = this.add(GolemSlotWidgets.hint(this.screen, hand, Component.translatable("gui.principiamagica.golem.hand.tooltip")));

        Slot back = this.menu.backSlot();
        this.add(GolemSlotWidgets.background(this.screen, back, this.slotSprite(), -1, -1));
        this.backHint = this.add(GolemSlotWidgets.hint(this.screen, back, Component.translatable("gui.principiamagica.golem.back.tooltip")));
    }

    private void buildJobLabels(ContentRect bounds) {
        this.add(new LabelWidget(bounds.x() + JOB_X, bounds.y() + JOB_Y, JOB_WIDTH, () -> GolemGuiText.jobName(this.view()))
                .withStyle(this.style).withMaxLines(1));
        this.add(new LabelWidget(bounds.x() + JOB_X, bounds.y() + JOB_Y + 11, JOB_WIDTH, this::jobLine)
                .withStyle(this.style).withColor(GuiStyles.TEXT_SECONDARY).withMaxLines(1));
    }

    private void buildModes(ContentRect bounds) {
        for (GolemMode mode : GolemMode.values()) {
            int index = this.modes.size();
            IconToggleButton button = new IconToggleButton(
                    bounds.x() + MODE_X + index * MODE_SPACING, bounds.y() + MODE_Y, MODE_SIZE,
                    this.sprite(modeSprite(mode)), Component.empty(),
                    () -> Networking.sendToServer(new MessageSetGolemMode(this.golemId(), mode))
            ).withStyle(this.style);
            this.modes.put(mode, this.add(button));
        }
    }

    /**
     * The status tablet: the status icon, then two lines. Line one is the status headline, or the task while the golem works. Line two is the
     * hint on what to do, or the work target. Both lines share an x, which moves right when the task's item is shown. The whole tablet has a
     * tooltip with the full text, so nothing is lost to the ellipsis.
     */
    private void buildStatusTablet(ContentRect bounds) {
        this.add(new SpriteWidget(bounds.x() + STATUS_X, bounds.y() + STATUS_Y, STATUS_WIDTH, STATUS_HEIGHT, this.sprite("inset")));
        this.statusIcon = this.add(new SpriteWidget(bounds.x() + STATUS_X + 3, bounds.y() + STATUS_Y + 3, STATUS_ICON_SIZE, STATUS_ICON_SIZE,
                () -> this.sprite(GolemGuiText.statusIcon(this.view().status()))));
        // The task's item takes the place of the status icon while the golem works without a problem.
        this.statusItem = this.add(new ItemIcon(bounds.x() + STATUS_X + 3, bounds.y() + STATUS_Y + 3, STATUS_ICON_SIZE, this::taskDisplay));
        this.headlineLabel = this.add(new LabelWidget(bounds.x() + STATUS_X + STATUS_TEXT_OFFSET, bounds.y() + STATUS_Y + 3, 96, this::headline)
                .withStyle(this.style).withMaxLines(1));
        this.hintLabel = this.add(new LabelWidget(bounds.x() + STATUS_X + STATUS_TEXT_OFFSET, bounds.y() + STATUS_Y + 13, 96, this::hintLine)
                .withStyle(this.style).withColor(GuiStyles.TEXT_SECONDARY).withMaxLines(2));
        this.statusTooltip = this.add(new TooltipArea(bounds.x() + STATUS_X, bounds.y() + STATUS_Y, STATUS_WIDTH, STATUS_HEIGHT, Component.empty()));
    }

    @Override
    public void tick() {
        GolemView view = this.view();
        GolemEntity golem = view.golem();

        this.updateHealthTooltip(golem);

        boolean hasAxiom = view.job().isPresent();
        this.axiomGhost.visible = !hasAxiom;
        this.axiomHint.visible = !hasAxiom;

        boolean handEmpty = golem == null || golem.getHandItem().isEmpty();
        this.handGhost.visible = handEmpty;
        this.handHint.visible = handEmpty;

        this.backHint.visible = golem == null || golem.getBackItem().isEmpty();

        for (Map.Entry<GolemMode, IconToggleButton> entry : this.modes.entrySet()) {
            IconToggleButton button = entry.getValue();
            button.setSelected(view.mode() == entry.getKey());
            button.active = entry.getKey() != GolemMode.WORK || hasAxiom;
            button.setTooltipText(this.modeTooltip(entry.getKey(), hasAxiom));
        }

        this.updateStatusTablet(view.status(), this.task());
    }

    /**
     * Places the status text and its item. A problem replaces the task, so the item of the task is only shown while the golem is OK.
     */
    private void updateStatusTablet(GolemStatus status, GolemTask task) {
        boolean showItem = status == GolemStatus.OK && !task.display().isEmpty();
        this.statusIcon.visible = !showItem;
        this.statusItem.visible = showItem;

        int textX = this.bounds.x() + STATUS_X + STATUS_TEXT_OFFSET;
        int textRight = this.bounds.x() + STATUS_X + STATUS_WIDTH - STATUS_TEXT_RIGHT_PADDING;
        this.headlineLabel.place(textX, textRight - textX);
        this.hintLabel.place(textX, textRight - textX);

        this.statusTooltip.setText(GolemGuiText.tooltip(status, task));
    }

    private void updateHealthTooltip(GolemEntity golem) {
        if (golem == null) {
            this.health.setTooltip(null);
            return;
        }

        this.health.setTooltip(Tooltip.create(Component.translatable("gui.principiamagica.golem.health",
                Math.round(golem.getHealth()), Math.round(golem.getMaxHealth()))));
    }

    private double healthFraction() {
        GolemEntity golem = this.view().golem();
        if (golem == null || golem.getMaxHealth() <= 0) {
            return 0.0;
        }

        return golem.getHealth() / golem.getMaxHealth();
    }

    private Component jobLine() {
        return this.view().job().isPresent()
                ? GolemGuiText.modeName(this.view().mode())
                : Component.translatable(GolemStatus.NO_PURPOSE.translationKey());
    }

    private GolemTask task() {
        GolemEntity golem = this.view().golem();
        return golem != null ? golem.getTask() : GolemTask.IDLE;
    }

    private ItemStack taskDisplay() {
        return this.task().display();
    }

    private Component headline() {
        return GolemGuiText.headline(this.view().status(), this.task());
    }

    private Component hintLine() {
        return GolemGuiText.hintLine(this.view().status(), this.task());
    }

    private Component modeTooltip(GolemMode mode, boolean hasAxiom) {
        return switch (mode) {
            case WORK -> hasAxiom
                    ? Component.translatable("gui.principiamagica.golem.mode.work.tooltip")
                    : Component.translatable("gui.principiamagica.golem.mode.work.disabled");
            case FOLLOW -> Component.translatable("gui.principiamagica.golem.mode.follow.tooltip");
            case STAY -> Component.translatable("gui.principiamagica.golem.mode.stay.tooltip");
        };
    }

    private static String modeSprite(GolemMode mode) {
        return switch (mode) {
            case WORK -> "mode_work";
            case FOLLOW -> "mode_follow";
            case STAY -> "mode_stay";
        };
    }

    private GuiSprite sprite(String name) {
        return this.style.sprite(GuiStyles.part(name), GuiStyles.sprite(name));
    }

    private GuiSprite slotSprite() {
        return this.style.sprite(BuiltinGuiParts.PLAYER_SLOT, GuiStyles.sprite("slot"));
    }
}
