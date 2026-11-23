// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.GuiStyles;
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
import com.klikli_dev.principiamagica.network.messages.MessageSetJobOption;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The job tab: the axiom and the job's name, the job's options as switches in a scrollable list, and a summary of the workplace.
 * The tab is only enabled while the golem has an axiom.
 */
final class JobTab extends GolemTab {

    static final int ROW_HEIGHT = 16;
    private static final int LIST_X = 10;
    private static final int LIST_Y = 44;
    private static final int LIST_WIDTH = 184;
    private static final int LIST_HEIGHT = 48;
    private static final int SWITCH_MARGIN = 2;
    private static final int LABEL_MARGIN = 4;

    private final StyleContext style = WidgetParts.defaultStyle();
    private ScrollList<JobOption> optionList;

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
        this.add(new ItemIcon(bounds.x() + 8, bounds.y() + 26, this::axiomStack));
        this.add(new LabelWidget(bounds.x() + 28, bounds.y() + 30, 160, () -> GolemGuiText.jobName(this.view()))
                .withStyle(this.style).withMaxLines(1));

        this.add(new SpriteWidget(bounds.x() + 8, bounds.y() + 42, 188, 52, this.sprite("inset")));
        this.optionList = this.add(new ScrollList<JobOption>(bounds.x() + LIST_X, bounds.y() + LIST_Y, LIST_WIDTH, LIST_HEIGHT, ROW_HEIGHT,
                this::renderOption, this::toggleOption)
                .withStyle(this.style)
                .withTooltips(option -> Component.translatable(option.descriptionKey())));

        this.add(new SpriteWidget(bounds.x() + 8, bounds.y() + 95, 188, 3, this.sprite("divider")));

        this.add(new SpriteWidget(bounds.x() + 8, bounds.y() + 99, 188, 22, this.sprite("inset")));
        this.add(new SpriteWidget(bounds.x() + 12, bounds.y() + 102, 16, 16, this.sprite("icon_zone")));
        this.add(new LabelWidget(bounds.x() + 31, bounds.y() + 105, 48, this::zoneText).withStyle(this.style).withMaxLines(1));
        this.add(new SpriteWidget(bounds.x() + 80, bounds.y() + 102, 16, 16, this.sprite("icon_deposit")));
        this.add(new LabelWidget(bounds.x() + 99, bounds.y() + 105, 40, () -> this.linkText(ContainerRole::canDeposit, "gui.principiamagica.golem.no_deposit"))
                .withStyle(this.style).withMaxLines(1));
        this.add(new SpriteWidget(bounds.x() + 140, bounds.y() + 102, 16, 16, this.sprite("icon_supply")));
        this.add(new LabelWidget(bounds.x() + 159, bounds.y() + 105, 33, () -> this.linkText(ContainerRole::canSupply, "gui.principiamagica.golem.no_supply"))
                .withStyle(this.style).withMaxLines(1));
        this.add(new TooltipArea(bounds.x() + 8, bounds.y() + 99, 188, 22, Component.translatable("gui.principiamagica.golem.workplace.tooltip")));
    }

    @Override
    public void tick() {
        List<JobOption> options = this.view().job().map(job -> job.value().options()).orElse(List.of());
        this.optionList.setEntries(options);
        this.optionList.tick();
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
     * @return the name of the first linked container with the role, or the empty text if there is none
     */
    private Component linkText(Predicate<ContainerRole> role, String emptyKey) {
        Workplace workplace = this.view().workplace();
        Optional<ContainerLink> link = workplace.links().stream().filter(candidate -> role.test(candidate.role())).findFirst();
        Level level = Minecraft.getInstance().level;
        if (link.isEmpty() || level == null) {
            return Component.translatable(emptyKey);
        }

        return level.getBlockState(link.get().pos()).getBlock().getName();
    }

    private GuiSprite sprite(String name) {
        return this.style.sprite(GuiStyles.part(name), GuiStyles.sprite(name));
    }
}
