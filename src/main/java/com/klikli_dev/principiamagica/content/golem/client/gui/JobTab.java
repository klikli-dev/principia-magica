// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.screen.ContentRect;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.widget.GuiSpriteWidget;
import com.klikli_dev.codedefinedgui.api.widget.ItemIconWidget;
import com.klikli_dev.codedefinedgui.api.widget.ScrollListWidget;
import com.klikli_dev.codedefinedgui.api.widget.ToggleSwitch;
import com.klikli_dev.codedefinedgui.api.widget.TooltipAreaWidget;
import com.klikli_dev.codedefinedgui.api.widget.WrappedTextWidget;
import com.klikli_dev.codedefinedgui.api.widget.tab.TabIcon;
import com.klikli_dev.principiamagica.client.gui.GuiStyles;
import com.klikli_dev.principiamagica.content.Plurals;
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
 * The strip shows the work zones, then for deposit and for supply a role icon with the containers of that role next to it, and the button
 * that unlinked all containers. The role icon's tooltip is the summary of its containers. Each container is its block icon, hovered for its
 * name, role, position and missing state. With more than four containers, the fourth cell shows a "+" and its tooltip lists the rest.
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
     * The summary strip, relative to the tab, x 8 to 196 and y 99 to 121. Icons sit at y 102, the 2x2 cells of 8px containers next to them.
     */
    private static final int STRIP_Y = 99;
    private static final int STRIP_HEIGHT = 22;
    private static final int ICON_Y = 102;
    private static final int ICON_SIZE = 16;
    private static final int ZONE_X = 12;
    private static final int ZONE_LABEL_X = 31;
    private static final int ZONE_LABEL_WIDTH = 40;
    private static final int DEPOSIT_X = 76;
    private static final int DEPOSIT_CELLS_X = 94;
    private static final int SUPPLY_X = 120;
    private static final int SUPPLY_CELLS_X = 138;
    private static final int CELL_SIZE = 8;
    private static final int CELL_STEP = 9;
    private static final int CELL_COUNT = 4;
    /**
     * Single row of cells, vertically centred on the 16px icon (its centre is at y 110). Two rows start at y 102, the second one 9px lower.
     */
    private static final int CELL_SINGLE_Y = 106;
    private static final int CELL_ROW_TOP = 102;
    private static final int CELL_ROW_STEP = 9;
    private static final int CLEAR_BUTTON_X = 176;
    private static final int CLEAR_BUTTON_Y = 102;
    private static final int CLEAR_BUTTON_SIZE = 15;
    private static final int CLEAR_ICON_SIZE = 12;

    private static final int HEADING_COLOR = 0xFFEBAC26;
    private static final int LIST_COLOR = 0xFFF6E2B8;
    private static final int EXPLANATION_COLOR = 0xFFB0A89A;
    private static final int HINT_COLOR = 0xFF3FA59A;
    private static final int MISSING_COLOR = 0xFFFF5555;

    private final StyleContext style = GuiStyles.context();
    private ScrollListWidget<JobOption> optionList;
    private TooltipAreaWidget zoneHint;
    private final ContainerCells deposit = new ContainerCells(ContainerRole::canDeposit, DEPOSIT_X, DEPOSIT_CELLS_X, "icon_deposit",
            "gui.principiamagica.golem.deposit.title", "gui.principiamagica.golem.deposit.description");
    private final ContainerCells supply = new ContainerCells(ContainerRole::canSupply, SUPPLY_X, SUPPLY_CELLS_X, "icon_supply",
            "gui.principiamagica.golem.supply.title", "gui.principiamagica.golem.supply.description");
    private ScaledIconButtonWidget clearLinksButton;

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
        this.add(new WrappedTextWidget(bounds.x() + ZONE_LABEL_X, bounds.y() + 105, ZONE_LABEL_WIDTH, this::zoneText).withStyle(this.style).withMaxLines(1));
        this.zoneHint = this.add(new TooltipAreaWidget(bounds.x() + 8, bounds.y() + STRIP_Y, 60, STRIP_HEIGHT, Component.empty()));

        this.deposit.build(bounds);
        this.supply.build(bounds);

        MutableComponent clearTooltip = Component.empty()
                .append(Component.translatable("gui.principiamagica.golem.clear_links.title").withStyle(Style.EMPTY.withColor(HEADING_COLOR).withBold(true)))
                .append(line(Component.translatable("gui.principiamagica.golem.clear_links.description"), Style.EMPTY.withColor(EXPLANATION_COLOR)));
        this.clearLinksButton = this.add(new ScaledIconButtonWidget(bounds.x() + CLEAR_BUTTON_X, bounds.y() + CLEAR_BUTTON_Y,
                CLEAR_BUTTON_SIZE, GuiStyles.sprite("icon_unlink"), CLEAR_ICON_SIZE, clearTooltip, this::clearLinks));
        this.clearLinksButton.withStyle(this.style);
    }

    @Override
    public void tick() {
        List<JobOption> options = this.view().job().map(job -> job.value().options()).orElse(List.of());
        this.optionList.setEntries(options);
        this.optionList.tick();

        List<ContainerLink> links = this.view().workplace().links();
        this.clearLinksButton.active = !links.isEmpty();

        int zones = this.view().workplace().footprints().size();
        this.zoneHint.setText(hint(Plurals.of("gui.principiamagica.golem.zones.title", zones),
                "gui.principiamagica.golem.zones.description", null));

        this.deposit.tick(links);
        this.supply.tick(links);
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
        return Plurals.of("gui.principiamagica.golem.zones", this.view().workplace().footprints().size());
    }

    /**
     * @return how many of the containers of a role get a cell: all of them, or the first three when there are more than the four cells hold
     */
    private static int shownCount(int count) {
        return count > CELL_COUNT ? CELL_COUNT - 1 : count;
    }

    /**
     * @return the block icon of the container: its block item, or a barrier if it is missing or has no item
     */
    private ItemStack stackOf(ContainerLink link) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return ItemStack.EMPTY;
        }

        if (LinkRenderer.isMissing(level, link.pos())) {
            return new ItemStack(Items.BARRIER);
        }

        ItemStack stack = new ItemStack(level.getBlockState(link.pos()).getBlock().asItem());
        return stack.isEmpty() ? new ItemStack(Items.BARRIER) : stack;
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
     * @return the names of the containers with their positions, a missing container in red
     */
    private static List<Component> containerNames(List<ContainerLink> links, Level level) {
        List<Component> names = new ArrayList<>();
        for (ContainerLink link : links) {
            boolean missing = LinkRenderer.isMissing(level, link.pos());
            Component name = missing
                    ? Component.translatable("gui.principiamagica.golem.missing_container").withStyle(Style.EMPTY.withColor(MISSING_COLOR))
                    : level.getBlockState(link.pos()).getBlock().getName();
            MutableComponent at = Component.translatable("gui.principiamagica.golem.task_at", GolemGuiText.formatPos(link.pos()));
            names.add(name.copy().append(" ").append(at.withStyle(Style.EMPTY.withColor(missing ? MISSING_COLOR : EXPLANATION_COLOR))));
        }

        return names;
    }

    /**
     * The tooltip of the overflow cell: how many more containers there are, and the ones it does not show.
     */
    private static Component moreTooltip(List<Component> names) {
        MutableComponent tooltip = Component.empty().append(Component.translatable("gui.principiamagica.golem.containers.more", names.size())
                .withStyle(Style.EMPTY.withColor(HEADING_COLOR).withBold(true)));
        for (Component name : names) {
            tooltip.append(line(Component.literal("• ").append(name), Style.EMPTY.withColor(LIST_COLOR)));
        }

        return tooltip.append(line(Component.translatable("gui.principiamagica.golem.workplace.tooltip"), Style.EMPTY.withColor(HINT_COLOR).withItalic(true)));
    }

    /**
     * The tooltip of a part of the strip: a gold heading, the containers as a list if the part lists them, a grey line on what the part
     * means, and the Stylus hint in italic.
     *
     * @param title the heading, e.g. "Work zones: 2"
     * @param descriptionKey the lang key of what the part means
     * @param names the containers, or null if the part does not list containers
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

    /**
     * The containers of one role: the role icon with its summary, and a 2x2 grid of block icons next to it. Up to four containers get a cell;
     * with more, the fourth cell is a "+" that lists the rest. One or two containers sit in a single row, three or four in two rows.
     */
    private final class ContainerCells {

        private final Predicate<ContainerRole> role;
        private final int glyphX;
        private final int gridX;
        private final String iconName;
        private final String titleKey;
        private final String descriptionKey;
        private final ItemIconWidget[] icons = new ItemIconWidget[CELL_COUNT];
        private final TooltipAreaWidget[] hovers = new TooltipAreaWidget[CELL_COUNT];
        private TooltipAreaWidget glyphHint;
        private WrappedTextWidget plus;
        private int originX;
        private int originY;

        ContainerCells(Predicate<ContainerRole> role, int glyphX, int gridX, String iconName, String titleKey, String descriptionKey) {
            this.role = role;
            this.glyphX = glyphX;
            this.gridX = gridX;
            this.iconName = iconName;
            this.titleKey = titleKey;
            this.descriptionKey = descriptionKey;
        }

        void build(ContentRect bounds) {
            this.originX = bounds.x();
            this.originY = bounds.y();

            JobTab.this.add(new GuiSpriteWidget(this.originX + this.glyphX, this.originY + ICON_Y, ICON_SIZE, ICON_SIZE, JobTab.this.sprite(this.iconName)));
            this.glyphHint = JobTab.this.add(new TooltipAreaWidget(this.originX + this.glyphX, this.originY + STRIP_Y, ICON_SIZE, STRIP_HEIGHT, Component.empty()));

            for (int i = 0; i < CELL_COUNT; i++) {
                int index = i;
                this.icons[i] = JobTab.this.add(new ItemIconWidget(0, 0, CELL_SIZE, () -> this.stackAt(index)));
                this.hovers[i] = JobTab.this.add(new TooltipAreaWidget(0, 0, CELL_SIZE, CELL_SIZE, Component.empty()));
            }
            this.plus = JobTab.this.add(new WrappedTextWidget(0, 0, CELL_SIZE, () -> Component.literal("+")).withStyle(JobTab.this.style).withMaxLines(1));

            this.layout(0);
        }

        /**
         * @return the containers of this role, in the order they were linked
         */
        private List<ContainerLink> matching(List<ContainerLink> links) {
            return links.stream().filter(link -> this.role.test(link.role())).toList();
        }

        private ItemStack stackAt(int index) {
            List<ContainerLink> matching = this.matching(JobTab.this.view().workplace().links());
            if (index >= shownCount(matching.size()) || index >= matching.size()) {
                return ItemStack.EMPTY;
            }

            return JobTab.this.stackOf(matching.get(index));
        }

        /**
         * Places the cells for the given number of containers of this role.
         */
        private void layout(int count) {
            boolean singleRow = count <= 2;
            for (int i = 0; i < CELL_COUNT; i++) {
                int x = this.originX + this.gridX + (i % 2) * CELL_STEP;
                int y = this.originY + (singleRow ? CELL_SINGLE_Y : CELL_ROW_TOP + (i / 2) * CELL_ROW_STEP);
                this.icons[i].setX(x);
                this.icons[i].setY(y);
                this.hovers[i].setX(x);
                this.hovers[i].setY(y);
                if (i == CELL_COUNT - 1) {
                    this.plus.setX(x);
                    this.plus.setY(y);
                }
            }
        }

        void tick(List<ContainerLink> links) {
            if (this.level() == null) {
                return;
            }

            List<ContainerLink> matching = this.matching(links);
            int count = matching.size();
            int shown = shownCount(count);
            boolean overflowing = count > CELL_COUNT;
            this.layout(count);

            for (int i = 0; i < CELL_COUNT; i++) {
                boolean icon = i < shown;
                boolean more = overflowing && i == CELL_COUNT - 1;
                this.icons[i].visible = icon;
                this.hovers[i].visible = icon || more;
                if (icon) {
                    this.hovers[i].setText(JobTab.this.slotTooltip(matching.get(i)));
                } else if (more) {
                    this.hovers[i].setText(moreTooltip(containerNames(matching.subList(shown, count), this.level())));
                }
            }
            this.plus.visible = overflowing;

            this.glyphHint.setText(hint(Component.translatable(this.titleKey), this.descriptionKey,
                    containerNames(matching, this.level())));
        }

        private Level level() {
            return Minecraft.getInstance().level;
        }
    }

}
