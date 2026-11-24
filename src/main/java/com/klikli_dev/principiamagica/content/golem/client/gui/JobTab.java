// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.screen.ContentRect;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.widget.GuiSpriteWidget;
import com.klikli_dev.codedefinedgui.api.widget.IconToggleButtonWidget;
import com.klikli_dev.codedefinedgui.api.widget.ItemIconWidget;
import com.klikli_dev.codedefinedgui.api.widget.ScrollListWidget;
import com.klikli_dev.codedefinedgui.api.widget.ToggleSwitch;
import com.klikli_dev.codedefinedgui.api.widget.TooltipAreaWidget;
import com.klikli_dev.codedefinedgui.api.widget.WrappedTextWidget;
import com.klikli_dev.codedefinedgui.api.widget.tab.TabIcon;
import com.klikli_dev.principiamagica.client.gui.GuiStyles;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.LinkRenderer;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.job.JobOption;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageClearContainerLinks;
import com.klikli_dev.principiamagica.network.messages.MessageSetJobOption;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The job tab: the axiom and the job's name, the job's options as switches in a scrollable list, and a summary strip at the bottom.
 * The tab is only enabled while the golem has an axiom.
 * <p>
 * The strip shows the work zones, then the linked containers as a row of their block icons, with a bar in the role colour under each, and
 * the button that unlinks all of them. Hovering the chest heading shows the deposit and supply summary. Hovering an icon shows its container.
 * A missing container shows a barrier icon and its name in red. With more links than fit, the last slot shows the count, and its tooltip
 * the summary.
 */
final class JobTab extends GolemTab {

    static final int ROW_HEIGHT = 16;
    private static final int LIST_X = 10;
    private static final int LIST_Y = 44;
    private static final int LIST_WIDTH = 184;
    private static final int LIST_HEIGHT = 48;
    private static final int SWITCH_MARGIN = 2;
    private static final int LABEL_MARGIN = 4;

    /**
     * The summary strip, relative to the tab. Its icons sit in the strip's middle, the role bars at its bottom edge.
     */
    private static final int STRIP_Y = 99;
    private static final int STRIP_HEIGHT = 22;
    private static final int ICON_Y = 102;
    private static final int ICON_SIZE = 16;
    private static final int ZONE_X = 12;
    private static final int ZONE_LABEL_X = 31;
    private static final int HEADING_X = 76;
    private static final int SLOT_X = 94;
    private static final int SLOT_STEP = 17;
    private static final int SLOT_COUNT = 4;
    private static final int ROLE_MARK_Y = 118;
    private static final int ROLE_MARK_HEIGHT = 2;
    private static final int OVERFLOW_TEXT_Y = 106;
    private static final int CLEAR_BUTTON_X = 172;
    private static final int CLEAR_BUTTON_Y = 100;
    private static final int CLEAR_BUTTON_SIZE = 20;

    private static final int HEADING_COLOR = 0xFFEBAC26;
    private static final int LIST_COLOR = 0xFFF6E2B8;
    private static final int EXPLANATION_COLOR = 0xFFB0A89A;
    private static final int HINT_COLOR = 0xFF3FA59A;
    private static final int MISSING_COLOR = 0xFFFF5555;
    private static final int OPAQUE = 0xFF000000;

    private final StyleContext style = GuiStyles.context();
    private ScrollListWidget<JobOption> optionList;
    private TooltipAreaWidget zoneHint;
    private TooltipAreaWidget summaryHint;
    private final TooltipAreaWidget[] slotHints = new TooltipAreaWidget[SLOT_COUNT];
    private WrappedTextWidget overflowLabel;
    private IconToggleButtonWidget clearLinksButton;

    JobTab(Supplier<GolemView> viewSupplier) {
        super(viewSupplier);
    }

    @Override
    public Component title() {
        return Component.translatable("gui.principiamagica.golem.tab.job");
    }

    @Override
    public TabIcon icon() {
        return (graphics, x, y) -> {
            ItemStack stack = this.axiomStack();
            graphics.item(stack.isEmpty() ? new ItemStack(ItemRegistry.AXIOM_OF_HARVEST.get()) : stack, x, y);
        };
    }

    @Override
    protected void build(ContentRect bounds) {
        this.add(new ItemIconWidget(bounds.x() + 8, bounds.y() + 26, ItemIconWidget.SIZE, this::axiomStack));
        this.add(new WrappedTextWidget(bounds.x() + 28, bounds.y() + 30, 160, () -> GolemGuiText.jobName(this.view()))
                .withStyle(this.style).withMaxLines(1));

        this.add(new GuiSpriteWidget(bounds.x() + 8, bounds.y() + 42, 188, 52, this.sprite("inset")));
        this.optionList = this.add(new ScrollListWidget<JobOption>(bounds.x() + LIST_X, bounds.y() + LIST_Y, LIST_WIDTH, LIST_HEIGHT, ROW_HEIGHT,
                this::renderOption, this::toggleOption)
                .withStyle(this.style)
                .withTooltips(option -> Component.translatable(option.descriptionKey()))
                .withNarration(option -> CommonComponents.optionNameValue(Component.translatable(option.translationKey()),
                        CommonComponents.optionStatus(this.isOn(option)))));

        this.add(new GuiSpriteWidget(bounds.x() + 8, bounds.y() + 95, 188, 3, this.sprite("divider")));

        this.add(new GuiSpriteWidget(bounds.x() + 8, bounds.y() + STRIP_Y, 188, STRIP_HEIGHT, this.sprite("inset")));

        this.add(new GuiSpriteWidget(bounds.x() + ZONE_X, bounds.y() + ICON_Y, ICON_SIZE, ICON_SIZE, this.sprite("icon_zone")));
        this.add(new WrappedTextWidget(bounds.x() + ZONE_LABEL_X, bounds.y() + 105, 40, this::zoneText).withStyle(this.style).withMaxLines(1));
        this.zoneHint = this.add(new TooltipAreaWidget(bounds.x() + 8, bounds.y() + STRIP_Y, 60, STRIP_HEIGHT, Component.empty()));

        this.add(new ItemIconWidget(bounds.x() + HEADING_X, bounds.y() + ICON_Y, ICON_SIZE, () -> new ItemStack(Items.CHEST)));
        this.summaryHint = this.add(new TooltipAreaWidget(bounds.x() + HEADING_X - 2, bounds.y() + STRIP_Y, 18, STRIP_HEIGHT, Component.empty()));

        for (int i = 0; i < SLOT_COUNT; i++) {
            int index = i;
            int x = bounds.x() + SLOT_X + i * SLOT_STEP;
            this.add(new ItemIconWidget(x, bounds.y() + ICON_Y, ICON_SIZE, () -> this.slotStack(index)));
            this.add(new ContainerRoleMarkWidget(x, bounds.y() + ROLE_MARK_Y, ICON_SIZE, ROLE_MARK_HEIGHT, () -> this.slotColor(index)));
            this.slotHints[i] = this.add(new TooltipAreaWidget(x, bounds.y() + STRIP_Y, ICON_SIZE, STRIP_HEIGHT, Component.empty()));
        }
        this.overflowLabel = this.add(new WrappedTextWidget(bounds.x() + SLOT_X + (SLOT_COUNT - 1) * SLOT_STEP, bounds.y() + OVERFLOW_TEXT_Y, ICON_SIZE,
                this::overflowText).withStyle(this.style).withMaxLines(1));

        MutableComponent clearTooltip = Component.empty()
                .append(Component.translatable("gui.principiamagica.golem.clear_links.title").withStyle(Style.EMPTY.withColor(HEADING_COLOR).withBold(true)))
                .append(line(Component.translatable("gui.principiamagica.golem.clear_links.description"), Style.EMPTY.withColor(EXPLANATION_COLOR)));
        this.clearLinksButton = this.add(new IconToggleButtonWidget(bounds.x() + CLEAR_BUTTON_X, bounds.y() + CLEAR_BUTTON_Y, CLEAR_BUTTON_SIZE,
                GuiStyles.sprite("icon_unlink"), clearTooltip, this::clearLinks)
                .withStyle(this.style));
    }

    @Override
    public void tick() {
        List<JobOption> options = this.view().job().map(job -> job.value().options()).orElse(List.of());
        this.optionList.setEntries(options);
        this.optionList.tick();

        List<ContainerLink> links = this.view().workplace().links();
        this.clearLinksButton.active = !links.isEmpty();

        int zones = this.view().workplace().footprints().size();
        this.zoneHint.setText(hint(Component.translatable("gui.principiamagica.golem.zones.title", zones),
                "gui.principiamagica.golem.zones.description", null));

        Component summary = this.summaryTooltip();
        this.summaryHint.setText(summary);

        boolean overflowing = links.size() > SLOT_COUNT;
        for (int i = 0; i < SLOT_COUNT; i++) {
            TooltipAreaWidget slot = this.slotHints[i];
            if (overflowing && i == SLOT_COUNT - 1) {
                slot.visible = true;
                slot.setText(summary);
            } else if (i < shownCount(links.size())) {
                slot.visible = true;
                slot.setText(this.slotTooltip(links.get(i)));
            } else {
                slot.visible = false;
            }
        }
    }

    private ItemStack axiomStack() {
        return this.view().job().map(AxiomItem::createStack).orElse(ItemStack.EMPTY);
    }

    private boolean isOn(JobOption option) {
        return this.view().workplace().options().get(option);
    }

    private void toggleOption(JobOption option) {
        Networking.sendToServer(new MessageSetJobOption(this.golemId(), option.key(), !this.isOn(option)));
    }

    /**
     * Asks the server to remove all container links. The server syncs the workplace back, which updates the strip.
     */
    private void clearLinks() {
        Networking.sendToServer(new MessageClearContainerLinks(this.golemId()));
    }

    /**
     * Draws the option's name on the left and its switch on the right. The name is cut before the switch, never behind it.
     */
    private void renderOption(GuiGraphicsExtractor graphics, JobOption option, int index, int x, int y, int width, int height, boolean hovered, int textColor) {
        Font font = Minecraft.getInstance().font;
        int switchX = x + width - ToggleSwitch.WIDTH - SWITCH_MARGIN;
        int switchY = y + (height - ToggleSwitch.HEIGHT) / 2;

        String name = Component.translatable(option.translationKey()).getString();
        String shown = font.plainSubstrByWidth(name, switchX - x - LABEL_MARGIN * 2);
        graphics.text(font, shown, x + LABEL_MARGIN, y + (height - 8) / 2, textColor, false);

        ToggleSwitch.extract(graphics, this.style, switchX, switchY, this.isOn(option), hovered);
    }

    private Component zoneText() {
        return Component.translatable("gui.principiamagica.golem.zones", this.view().workplace().footprints().size());
    }

    /**
     * @return how many of the links get an icon: all of them, or all but the last slot when there are more than the slots hold
     */
    private static int shownCount(int linkCount) {
        return linkCount > SLOT_COUNT ? SLOT_COUNT - 1 : linkCount;
    }

    /**
     * @return the link shown in the slot, or null if the slot shows no link
     */
    private ContainerLink shownLink(int index) {
        List<ContainerLink> links = this.view().workplace().links();
        if (index >= shownCount(links.size())) {
            return null;
        }

        return links.get(index);
    }

    /**
     * @return the block icon of the link in the slot: its block item, or a barrier if it is missing or has no item
     */
    private ItemStack slotStack(int index) {
        ContainerLink link = this.shownLink(index);
        Level level = Minecraft.getInstance().level;
        if (link == null || level == null) {
            return ItemStack.EMPTY;
        }

        if (LinkRenderer.isMissing(level, link.pos())) {
            return new ItemStack(Items.BARRIER);
        }

        ItemStack stack = new ItemStack(level.getBlockState(link.pos()).getBlock().asItem());
        return stack.isEmpty() ? new ItemStack(Items.BARRIER) : stack;
    }

    /**
     * @return the ARGB colour of the role bar under the slot's icon, red if the container is missing, or null for an empty slot
     */
    private Integer slotColor(int index) {
        ContainerLink link = this.shownLink(index);
        Level level = Minecraft.getInstance().level;
        if (link == null || level == null) {
            return null;
        }

        return LinkRenderer.isMissing(level, link.pos()) ? MISSING_COLOR : OPAQUE | LinkRenderer.colorOf(link.role());
    }

    private Component overflowText() {
        int hidden = this.view().workplace().links().size() - shownCount(this.view().workplace().links().size());
        return Component.literal("+" + hidden);
    }

    /**
     * @return the tooltip of one container: its block name, role, position, and the Stylus hint. A missing container is named in red.
     */
    private Component slotTooltip(ContainerLink link) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return Component.empty();
        }

        boolean missing = LinkRenderer.isMissing(level, link.pos());
        Component name = missing
                ? Component.translatable("gui.principiamagica.golem.missing_container")
                : level.getBlockState(link.pos()).getBlock().getName();
        MutableComponent tooltip = Component.empty().append(name.copy().withStyle(Style.EMPTY.withColor(missing ? MISSING_COLOR : HEADING_COLOR).withBold(true)));
        tooltip.append(line(Component.translatable("golem_role.principiamagica." + link.role().getSerializedName()), Style.EMPTY.withColor(LIST_COLOR)));
        tooltip.append(line(Component.translatable("gui.principiamagica.golem.task_at", GolemGuiText.formatPos(link.pos())), Style.EMPTY.withColor(EXPLANATION_COLOR)));
        return tooltip.append(line(Component.translatable("gui.principiamagica.golem.workplace.tooltip"), Style.EMPTY.withColor(HINT_COLOR).withItalic(true)));
    }

    /**
     * @return the tooltip of the whole strip's containers: the deposit and the supply containers with their positions, and the Stylus hint
     */
    private Component summaryTooltip() {
        MutableComponent tooltip = Component.empty().append(Component.translatable("gui.principiamagica.golem.containers.title")
                .withStyle(Style.EMPTY.withColor(HEADING_COLOR).withBold(true)));
        appendSection(tooltip, "gui.principiamagica.golem.deposit.title", ContainerRole::canDeposit, "gui.principiamagica.golem.deposit.description");
        appendSection(tooltip, "gui.principiamagica.golem.supply.title", ContainerRole::canSupply, "gui.principiamagica.golem.supply.description");
        return tooltip.append(line(Component.translatable("gui.principiamagica.golem.workplace.tooltip"), Style.EMPTY.withColor(HINT_COLOR).withItalic(true)));
    }

    private void appendSection(MutableComponent tooltip, String titleKey, Predicate<ContainerRole> role, String descriptionKey) {
        tooltip.append(line(Component.translatable(titleKey), Style.EMPTY.withColor(HEADING_COLOR).withBold(true)));
        List<Component> names = this.linkNames(role, true);
        if (names.isEmpty()) {
            tooltip.append(line(Component.translatable("gui.principiamagica.golem.none_linked"), Style.EMPTY.withColor(EXPLANATION_COLOR)));
        }

        for (Component name : names) {
            tooltip.append(line(Component.literal("• ").append(name), Style.EMPTY.withColor(LIST_COLOR)));
        }

        tooltip.append(line(Component.translatable(descriptionKey), Style.EMPTY.withColor(EXPLANATION_COLOR)));
    }

    /**
     * @param withPositions whether each name is followed by the position of its container. A missing container is red.
     * @return the names of all linked containers with the role, in the order they were linked
     */
    private List<Component> linkNames(Predicate<ContainerRole> role, boolean withPositions) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }

        List<Component> names = new ArrayList<>();
        for (ContainerLink link : this.view().workplace().links()) {
            if (role.test(link.role())) {
                boolean missing = LinkRenderer.isMissing(level, link.pos());
                Component name = missing
                        ? Component.translatable("gui.principiamagica.golem.missing_container").withStyle(Style.EMPTY.withColor(MISSING_COLOR))
                        : level.getBlockState(link.pos()).getBlock().getName();
                if (withPositions) {
                    int color = missing ? MISSING_COLOR : EXPLANATION_COLOR;
                    MutableComponent at = Component.translatable("gui.principiamagica.golem.task_at", GolemGuiText.formatPos(link.pos()));
                    names.add(name.copy().append(" ").append(at.withStyle(Style.EMPTY.withColor(color))));
                } else {
                    names.add(name);
                }
            }
        }

        return names;
    }

    /**
     * The tooltip of a part of the strip: a gold heading, the description in grey, and the Stylus hint in italic.
     *
     * @param title the heading, e.g. "Work zones: 2"
     * @param descriptionKey the lang key of what the part means
     * @param names the linked containers, or null if the part does not list containers
     */
    private static Component hint(Component title, String descriptionKey, List<Component> names) {
        MutableComponent tooltip = Component.empty().append(title.copy().withStyle(Style.EMPTY.withColor(HEADING_COLOR).withBold(true)));
        if (names != null) {
            if (names.isEmpty()) {
                tooltip.append(line(Component.translatable("gui.principiamagica.golem.none_linked"), Style.EMPTY.withColor(EXPLANATION_COLOR)));
            }

            for (Component name : names) {
                tooltip.append(line(Component.literal("• ").append(name), Style.EMPTY.withColor(LIST_COLOR)));
            }
        }

        return tooltip
                .append(line(Component.translatable(descriptionKey), Style.EMPTY.withColor(EXPLANATION_COLOR)))
                .append(line(Component.translatable("gui.principiamagica.golem.workplace.tooltip"), Style.EMPTY.withColor(HINT_COLOR).withItalic(true)));
    }

    /**
     * @return a new line of the tooltip with the text in the style
     */
    private static MutableComponent line(Component text, Style style) {
        return Component.literal("\n").append(text.copy().withStyle(style));
    }

    private GuiSprite sprite(String name) {
        return this.style.sprite(GuiStyles.part(name), GuiStyles.sprite(name));
    }
}
