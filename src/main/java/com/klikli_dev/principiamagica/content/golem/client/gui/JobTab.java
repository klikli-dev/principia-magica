// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.GuiStyles;
import com.klikli_dev.principiamagica.client.gui.widget.IconToggleButton;
import com.klikli_dev.principiamagica.client.gui.widget.ItemIcon;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.ScrollList;
import com.klikli_dev.principiamagica.client.gui.widget.SpriteWidget;
import com.klikli_dev.principiamagica.client.gui.widget.TabIcon;
import com.klikli_dev.principiamagica.client.gui.widget.TooltipArea;
import com.klikli_dev.principiamagica.client.gui.widget.ToggleSwitch;
import com.klikli_dev.principiamagica.client.gui.widget.WidgetParts;
import com.klikli_dev.principiamagica.content.golem.item.AxiomItem;
import com.klikli_dev.principiamagica.content.golem.job.JobOption;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
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
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The job tab: the axiom and the job's name, the job's options as switches in a scrollable list, and a summary of the workplace.
 * The tab is only enabled while the golem has an axiom.
 * <p>
 * Each part of the summary has its own hover area. Its tooltip has a gold heading, lists the linked containers with their positions, explains
 * the icon in grey, and ends with the hint to edit them with the Stylus in italic.
 */
final class JobTab extends GolemTab {

    static final int ROW_HEIGHT = 16;
    private static final int LIST_X = 10;
    private static final int LIST_Y = 44;
    private static final int LIST_WIDTH = 184;
    private static final int LIST_HEIGHT = 48;
    private static final int SWITCH_MARGIN = 2;
    private static final int LABEL_MARGIN = 4;
    private static final int JOB_LABEL_WIDTH = 128;
    private static final int CLEAR_BUTTON_X = 162;
    private static final int CLEAR_BUTTON_Y = 24;
    private static final int CLEAR_BUTTON_SIZE = 20;

    private static final int HEADING_COLOR = 0xFFEBAC26;
    private static final int LIST_COLOR = 0xFFF6E2B8;
    private static final int EXPLANATION_COLOR = 0xFFB0A89A;
    private static final int HINT_COLOR = 0xFF3FA59A;

    private final StyleContext style = WidgetParts.defaultStyle();
    private ScrollList<JobOption> optionList;
    private TooltipArea zoneHint;
    private TooltipArea depositHint;
    private TooltipArea supplyHint;
    private IconToggleButton clearLinksButton;

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
        this.add(new ItemIcon(bounds.x() + 8, bounds.y() + 26, ItemIcon.SIZE, this::axiomStack));
        this.add(new LabelWidget(bounds.x() + 28, bounds.y() + 30, JOB_LABEL_WIDTH, () -> GolemGuiText.jobName(this.view()))
                .withStyle(this.style).withMaxLines(1));
        MutableComponent clearTooltip = Component.empty()
                .append(Component.translatable("gui.principiamagica.golem.clear_links.title").withStyle(Style.EMPTY.withColor(HEADING_COLOR).withBold(true)))
                .append(line(Component.translatable("gui.principiamagica.golem.clear_links.description"), Style.EMPTY.withColor(EXPLANATION_COLOR)));
        this.clearLinksButton = this.add(new IconToggleButton(bounds.x() + CLEAR_BUTTON_X, bounds.y() + CLEAR_BUTTON_Y, CLEAR_BUTTON_SIZE,
                GuiStyles.sprite("icon_unlink"), clearTooltip, this::clearLinks)
                .withStyle(this.style));

        this.add(new SpriteWidget(bounds.x() + 8, bounds.y() + 42, 188, 52, this.sprite("inset")));
        this.optionList = this.add(new ScrollList<JobOption>(bounds.x() + LIST_X, bounds.y() + LIST_Y, LIST_WIDTH, LIST_HEIGHT, ROW_HEIGHT,
                this::renderOption, this::toggleOption)
                .withStyle(this.style)
                .withTooltips(option -> Component.translatable(option.descriptionKey()))
                .withNarration(option -> CommonComponents.optionNameValue(Component.translatable(option.translationKey()),
                        CommonComponents.optionStatus(this.isOn(option)))));

        this.add(new SpriteWidget(bounds.x() + 8, bounds.y() + 95, 188, 3, this.sprite("divider")));

        this.add(new SpriteWidget(bounds.x() + 8, bounds.y() + 99, 188, 22, this.sprite("inset")));

        this.add(new SpriteWidget(bounds.x() + 12, bounds.y() + 102, 16, 16, this.sprite("icon_zone")));
        this.add(new LabelWidget(bounds.x() + 31, bounds.y() + 105, 40, this::zoneText).withStyle(this.style).withMaxLines(1));
        this.zoneHint = this.add(new TooltipArea(bounds.x() + 8, bounds.y() + 99, 60, 22, Component.empty()));

        this.add(new SpriteWidget(bounds.x() + 80, bounds.y() + 102, 16, 16, this.sprite("icon_deposit")));
        this.add(new LabelWidget(bounds.x() + 99, bounds.y() + 105, 32, () -> this.linkSummary(ContainerRole::canDeposit, "gui.principiamagica.golem.no_deposit"))
                .withStyle(this.style).withMaxLines(1));
        this.depositHint = this.add(new TooltipArea(bounds.x() + 76, bounds.y() + 99, 60, 22, Component.empty()));

        this.add(new SpriteWidget(bounds.x() + 140, bounds.y() + 102, 16, 16, this.sprite("icon_supply")));
        this.add(new LabelWidget(bounds.x() + 159, bounds.y() + 105, 29, () -> this.linkSummary(ContainerRole::canSupply, "gui.principiamagica.golem.no_supply"))
                .withStyle(this.style).withMaxLines(1));
        this.supplyHint = this.add(new TooltipArea(bounds.x() + 136, bounds.y() + 99, 52, 22, Component.empty()));
    }

    @Override
    public void tick() {
        List<JobOption> options = this.view().job().map(job -> job.value().options()).orElse(List.of());
        this.optionList.setEntries(options);
        this.optionList.tick();

        this.clearLinksButton.active = !this.view().workplace().links().isEmpty();

        int zones = this.view().workplace().footprints().size();
        this.zoneHint.setText(hint(Component.translatable("gui.principiamagica.golem.zones.title", zones),
                "gui.principiamagica.golem.zones.description", null));
        this.depositHint.setText(hint(Component.translatable("gui.principiamagica.golem.deposit.title"),
                "gui.principiamagica.golem.deposit.description", this.linkNames(ContainerRole::canDeposit, true)));
        this.supplyHint.setText(hint(Component.translatable("gui.principiamagica.golem.supply.title"),
                "gui.principiamagica.golem.supply.description", this.linkNames(ContainerRole::canSupply, true)));
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
     * Asks the server to remove all container links. The server syncs the workplace back, which updates the summary.
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
     * @return the first linked container with the role, followed by how many more there are, or the empty text if there is none
     */
    private Component linkSummary(Predicate<ContainerRole> role, String emptyKey) {
        List<Component> names = this.linkNames(role, false);
        if (names.isEmpty()) {
            return Component.translatable(emptyKey);
        }

        if (names.size() == 1) {
            return names.getFirst();
        }

        return Component.empty().append(names.getFirst()).append(" +" + (names.size() - 1));
    }

    /**
     * @param withPositions whether each name is followed by the position of its container
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
                Component name = level.getBlockState(link.pos()).getBlock().getName();
                if (withPositions) {
                    MutableComponent at = Component.translatable("gui.principiamagica.golem.task_at", GolemGuiText.formatPos(link.pos()));
                    names.add(name.copy().append(" ").append(at.withStyle(Style.EMPTY.withColor(EXPLANATION_COLOR))));
                } else {
                    names.add(name);
                }
            }
        }

        return names;
    }

    /**
     * The tooltip of a summary part: a gold heading, the linked containers as a list if the part lists them, a grey line on what the part
     * means, and the Stylus hint in italic.
     *
     * @param title the heading, e.g. "Deposit" or "Work zones: 2"
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
