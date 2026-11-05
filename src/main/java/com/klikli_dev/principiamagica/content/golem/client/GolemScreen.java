// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.klikli_dev.codedefinedgui.api.layout.LayoutResolveContext;
import com.klikli_dev.codedefinedgui.api.layout.LayoutResolverRegistry;
import com.klikli_dev.codedefinedgui.api.layout.LayoutScreenView;
import com.klikli_dev.codedefinedgui.api.layout.LayoutSpec;
import com.klikli_dev.codedefinedgui.api.layout.ScreenLayoutController;
import com.klikli_dev.codedefinedgui.api.screen.GuiHost;
import com.klikli_dev.codedefinedgui.api.screen.GuiRootWidget;
import com.klikli_dev.codedefinedgui.api.style.BuiltinGuiParts;
import com.klikli_dev.codedefinedgui.api.style.BuiltinGuiStyles;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleContext;
import com.klikli_dev.codedefinedgui.api.style.GuiStyleRegistry;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprites;
import com.klikli_dev.codedefinedgui.api.widget.GuiBackgroundWidget;
import com.klikli_dev.codedefinedgui.api.widget.GuiSpriteWidget;
import com.klikli_dev.codedefinedgui.api.widget.GuiTextWidget;
import com.klikli_dev.codedefinedgui.api.widget.IconButtonWidget;
import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache.Entry;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemInventory;
import com.klikli_dev.principiamagica.content.golem.entity.GolemMode;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.JobOption;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenu;
import com.klikli_dev.principiamagica.content.golem.menu.GolemMenuLayout;
import com.klikli_dev.principiamagica.content.golem.upgrade.GolemUpgrade;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.network.Message;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageCycleLinkRole;
import com.klikli_dev.principiamagica.network.messages.MessageRemoveGolemCore;
import com.klikli_dev.principiamagica.network.messages.MessageRemoveGolemUpgrade;
import com.klikli_dev.principiamagica.network.messages.MessageRemoveLink;
import com.klikli_dev.principiamagica.network.messages.MessageRemoveZone;
import com.klikli_dev.principiamagica.network.messages.MessageSetGolemMode;
import com.klikli_dev.principiamagica.network.messages.MessageSetJobOption;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Optional;

/**
 * Golem GUI: header, mode, job options and workplace list (scrollable), core removal, and the golem and player inventory.
 * Layout and slot backgrounds come from CodeDefinedGui. Text buttons are vanilla buttons, because CodeDefinedGui has no text button.
 */
public class GolemScreen extends AbstractContainerScreen<GolemMenu> implements GuiHost, LayoutScreenView {

    private static final int TEXT_COLOR = 0xFF404040;
    private static final int HINT_COLOR = 0xFF6A6A6A;
    private static final int OK_COLOR = 0xFF1B5E20;
    private static final int PROBLEM_COLOR = 0xFFA01818;

    private static final int MODE_Y = 40;
    private static final int MODE_WIDTH = 60;
    private static final int MODE_GAP = 6;
    private static final int BUTTON_HEIGHT = 16;

    private static final int LIST_X = 8;
    private static final int LIST_Y = 62;
    private static final int LIST_WIDTH = 192;
    private static final int LIST_ROWS = 6;
    private static final int ROW_HEIGHT = 18;
    private static final int ROLE_BUTTON_X = 108;
    private static final int ROLE_BUTTON_WIDTH = 62;
    private static final int REMOVE_X = LIST_X + LIST_WIDTH - 18;
    private static final int LINK_TEXT_WIDTH = 96;

    private static final int CORE_BUTTON_Y = LIST_Y + LIST_ROWS * ROW_HEIGHT + 4;
    private static final int CORE_BUTTON_WIDTH = 96;
    private static final int HINT_Y = CORE_BUTTON_Y + BUTTON_HEIGHT + 4;

    private final GuiRootWidget root;
    private final ScreenLayoutController layoutController;
    private int scroll;
    private @Nullable Snapshot builtSnapshot;

    public GolemScreen(GolemMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GolemMenuLayout.WIDTH, GolemMenuLayout.HEIGHT);
        this.root = new GuiRootWidget(this);
        this.layoutController = new ScreenLayoutController(this, this, this.root, new GuiStyleContext(GuiStyleRegistry.get(BuiltinGuiStyles.DEFAULT)));
        this.inventoryLabelY = GolemMenuLayout.PLAYER_MAIN_Y - 10;
        this.inventoryLabelX = GolemMenuLayout.PLAYER_INVENTORY_X;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(this.root);
        this.rebuild();
    }

    private void rebuild() {
        this.root.clearChildren();
        this.layoutController.init();

        Snapshot snapshot = this.snapshot();
        Workplace workplace = snapshot.workplace().map(Entry::workplace).orElse(Workplace.EMPTY);
        GolemMode mode = snapshot.mode();

        for (int i = 0; i < GolemMode.values().length; i++) {
            GolemMode target = GolemMode.values()[i];
            Button button = Button.builder(Component.translatable("golem_mode.principiamagica." + target.name().toLowerCase(Locale.ROOT)),
                            ignored -> this.send(new MessageSetGolemMode(this.menu.getGolemId(), target)))
                    .bounds(this.leftPos + LIST_X + i * (MODE_WIDTH + MODE_GAP), this.topPos + MODE_Y, MODE_WIDTH, BUTTON_HEIGHT)
                    .build();
            button.active = target != mode;
            this.root.addChild(button);
        }

        List<Row> rows = this.buildRows(workplace, snapshot.job(), snapshot.upgrades(), snapshot.upgradeSlots());
        this.scroll = Math.max(0, Math.min(this.scroll, rows.size() - LIST_ROWS));
        for (int i = this.scroll; i < Math.min(rows.size(), this.scroll + LIST_ROWS); i++) {
            this.addRow(rows.get(i), this.topPos + LIST_Y + (i - this.scroll) * ROW_HEIGHT, workplace);
        }

        Button removeCore = Button.builder(Component.translatable("gui.principiamagica.golem.remove_core"),
                        ignored -> this.send(new MessageRemoveGolemCore(this.menu.getGolemId())))
                .bounds(this.leftPos + LIST_X, this.topPos + CORE_BUTTON_Y, CORE_BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        removeCore.active = snapshot.job().isPresent();
        this.root.addChild(removeCore);

        this.root.syncWithHost();
        this.builtSnapshot = snapshot;
    }

    private List<Row> buildRows(Workplace workplace, Optional<Holder<GolemJob>> job, List<Holder<GolemUpgrade>> upgrades, int upgradeSlots) {
        List<Row> rows = new ArrayList<>();

        job.ifPresent(holder -> {
            List<JobOption> options = holder.value().options();
            if (!options.isEmpty()) {
                rows.add(Row.heading(Component.translatable("gui.principiamagica.golem.options")));
                for (JobOption option : options) {
                    rows.add(Row.option(option, workplace.options().get(option)));
                }
            }
        });

        rows.add(Row.heading(Component.translatable("gui.principiamagica.golem.upgrades", upgrades.size(), upgradeSlots)));
        if (upgrades.isEmpty()) {
            rows.add(Row.heading(Component.translatable("gui.principiamagica.golem.no_upgrades")));
        }
        for (int i = 0; i < upgrades.size(); i++) {
            rows.add(Row.upgrade(i, upgrades.get(i).value().getName()));
        }

        rows.add(Row.heading(Component.translatable("gui.principiamagica.golem.zones", workplace.zones().size())));
        if (workplace.zones().isEmpty()) {
            rows.add(Row.heading(Component.translatable("gui.principiamagica.golem.no_zones")));
        }
        for (int i = 0; i < workplace.zones().size(); i++) {
            rows.add(Row.zone(i));
        }

        rows.add(Row.heading(Component.translatable("gui.principiamagica.golem.containers", workplace.links().size())));
        if (workplace.links().isEmpty()) {
            rows.add(Row.heading(Component.translatable("gui.principiamagica.golem.no_containers")));
        }
        for (int i = 0; i < workplace.links().size(); i++) {
            rows.add(Row.link(i));
        }

        return rows;
    }

    private void addRow(Row row, int y, Workplace workplace) {
        int golemId = this.menu.getGolemId();
        switch (row.kind()) {
            case HEADING -> this.root.addChild(new GuiTextWidget(this.leftPos + LIST_X, y + 5, () -> row.text(), () -> TEXT_COLOR, false));
            case UPGRADE -> {
                this.root.addChild(new GuiTextWidget(this.leftPos + LIST_X + 2, y + 5, () -> row.text(), () -> TEXT_COLOR, false));
                this.root.addChild(new IconButtonWidget(this.leftPos + REMOVE_X, y + 1, GuiSprites.FILTER_ICON_RESET,
                        Component.translatable("gui.principiamagica.golem.remove"), () -> this.send(new MessageRemoveGolemUpgrade(golemId, row.index())))
                        .withTooltip(Component.translatable("gui.principiamagica.golem.remove")));
            }
            case OPTION -> {
                Component state = Component.translatable(row.value() ? "gui.principiamagica.golem.on" : "gui.principiamagica.golem.off");
                Button button = Button.builder(Component.translatable("gui.principiamagica.golem.option_state", Component.translatable(row.option().translationKey()), state),
                                ignored -> this.send(new MessageSetJobOption(golemId, row.option().key(), !row.value())))
                        .bounds(this.leftPos + LIST_X, y + 1, LIST_WIDTH, BUTTON_HEIGHT)
                        .build();
                button.setTooltip(Tooltip.create(Component.translatable(row.option().descriptionKey())));
                this.root.addChild(button);
            }
            case ZONE -> {
                WorkZone zone = workplace.zones().get(row.index());
                Component text = Component.translatable("gui.principiamagica.golem.zone_row",
                        zone.width(), zone.height(), zone.depth(), zone.min().getX(), zone.min().getY(), zone.min().getZ());
                this.root.addChild(new GuiTextWidget(this.leftPos + LIST_X + 2, y + 5, () -> text, () -> TEXT_COLOR, false));
                this.root.addChild(new IconButtonWidget(this.leftPos + REMOVE_X, y + 1, GuiSprites.FILTER_ICON_RESET,
                        Component.translatable("gui.principiamagica.golem.remove"), () -> this.send(new MessageRemoveZone(golemId, row.index())))
                        .withTooltip(Component.translatable("gui.principiamagica.golem.remove")));
            }
            case LINK -> {
                ContainerLink link = workplace.links().get(row.index());
                Component text = Component.literal(this.font.plainSubstrByWidth(this.linkName(link.pos()), LINK_TEXT_WIDTH));
                this.root.addChild(new GuiTextWidget(this.leftPos + LIST_X + 2, y + 5, () -> text, () -> TEXT_COLOR, false));

                Button role = Button.builder(roleName(link.role()), ignored -> this.send(new MessageCycleLinkRole(golemId, row.index())))
                        .bounds(this.leftPos + ROLE_BUTTON_X, y + 1, ROLE_BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build();
                this.root.addChild(role);

                this.root.addChild(new IconButtonWidget(this.leftPos + REMOVE_X, y + 1, GuiSprites.FILTER_ICON_RESET,
                        Component.translatable("gui.principiamagica.golem.remove"), () -> this.send(new MessageRemoveLink(golemId, row.index())))
                        .withTooltip(Component.translatable("gui.principiamagica.golem.remove")));
            }
        }
    }

    private String linkName(BlockPos pos) {
        String blockName = this.minecraft.level != null ? this.minecraft.level.getBlockState(pos).getBlock().getName().getString() : "";
        return blockName + " " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    private static Component roleName(ContainerRole role) {
        return switch (role) {
            case SUPPLY -> Component.translatable("gui.principiamagica.golem.role_supply");
            case DEPOSIT -> Component.translatable("gui.principiamagica.golem.role_deposit");
            case BOTH -> Component.translatable("gui.principiamagica.golem.role_both");
        };
    }

    private void send(Message message) {
        Networking.sendToServer(message);
    }

    private @Nullable GolemEntity golem() {
        return this.minecraft.level != null && this.minecraft.level.getEntity(this.menu.getGolemId()) instanceof GolemEntity golem ? golem : null;
    }

    private Snapshot snapshot() {
        GolemEntity golem = this.golem();
        return new Snapshot(
                ClientGolemWorkplaceCache.latest(this.menu.getGolemId()),
                golem != null ? golem.getMode() : GolemMode.WORK,
                golem != null ? golem.getSpec().job() : Optional.empty(),
                golem != null ? golem.getSpec().upgrades() : List.of(),
                golem != null ? golem.getSpec().material().value().upgradeSlots() : 0,
                this.menu.getUsableGolemSlots()
        );
    }

    @Override
    protected void containerTick() {
        if (!this.snapshot().equals(this.builtSnapshot)) {
            this.rebuild();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }

        boolean inList = mouseX >= this.leftPos + LIST_X && mouseX < this.leftPos + LIST_X + LIST_WIDTH
                && mouseY >= this.topPos + LIST_Y && mouseY < this.topPos + LIST_Y + LIST_ROWS * ROW_HEIGHT;
        if (!inList || scrollY == 0) {
            return false;
        }

        this.scroll -= (int) Math.signum(scrollY);
        this.rebuild();
        return true;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        GolemEntity golem = this.golem();
        Snapshot snapshot = this.snapshot();

        Component name = this.golemName(golem);
        Component job = snapshot.job().map(holder -> holder.value().getName()).orElse(Component.translatable("gui.principiamagica.golem.no_core"));
        Component material = golem != null ? Component.translatable(golem.getSpec().material().value().getDescriptionId()) : Component.empty();
        Component info = Component.translatable("gui.principiamagica.golem.info", material, job);

        boolean problem = golem != null && golem.getStatus().isProblem();
        Component status = golem != null
                ? Component.translatable(golem.getStatus().translationKey())
                : Component.empty();

        graphics.text(this.font, name, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        graphics.text(this.font, info, this.titleLabelX, 17, TEXT_COLOR, false);
        graphics.text(this.font, status, this.titleLabelX, 27, problem ? PROBLEM_COLOR : OK_COLOR, false);
        graphics.text(this.font, Component.translatable("gui.principiamagica.golem.hint_line_1"), LIST_X, HINT_Y, HINT_COLOR, false);
        graphics.text(this.font, Component.translatable("gui.principiamagica.golem.hint_line_2"), LIST_X, HINT_Y + 10, HINT_COLOR, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TEXT_COLOR, false);
    }

    private Component golemName(@Nullable GolemEntity golem) {
        if (golem == null) {
            return this.title;
        }

        if (golem.getCustomName() != null) {
            return golem.getCustomName();
        }

        return Component.translatable("gui.principiamagica.golem.title", Component.translatable(golem.getSpec().material().value().getDescriptionId()));
    }

    @Override
    public LayoutSpec layoutSpec() {
        return this.menu.layoutSpec();
    }

    @Override
    public void registerResolvers(LayoutResolverRegistry registry) {
        registry.resolve("main.background", ctx -> ctx.addWidget(new GuiBackgroundWidget(this, ctx.node().x(), ctx.node().y(), ctx.node().widthOrThrow(), ctx.node().heightOrThrow())));
        registry.add("main.tool", 10, ctx -> ctx.addWidget(slotBackground(ctx)));

        int usableGolemSlots = this.menu.getUsableGolemSlots();
        for (int i = 0; i < GolemInventory.MAX_SLOTS; i++) {
            int slot = i;
            registry.add("golem.slot_" + i, 10, ctx -> {
                if (slot < usableGolemSlots) {
                    ctx.addWidget(slotBackground(ctx));
                }
            });
        }

        for (int i = 0; i < GolemMenuLayout.PLAYER_MAIN_ROWS * GolemMenuLayout.PLAYER_COLUMNS; i++) {
            registry.add("player.slot_" + (i + GolemMenuLayout.PLAYER_COLUMNS), 10, ctx -> ctx.addWidget(slotBackground(ctx)));
        }
        for (int column = 0; column < GolemMenuLayout.PLAYER_COLUMNS; column++) {
            registry.add("player.hotbar_" + column, 10, ctx -> ctx.addWidget(slotBackground(ctx)));
        }
    }

    private static GuiSpriteWidget slotBackground(LayoutResolveContext ctx) {
        return new GuiSpriteWidget(ctx.node().x() - 1, ctx.node().y() - 1, ctx.style().sprite(BuiltinGuiParts.PLAYER_SLOT, GuiSprites.INVENTORY_SLOT));
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

    private record Snapshot(Optional<Entry> workplace, GolemMode mode, Optional<Holder<GolemJob>> job, List<Holder<GolemUpgrade>> upgrades, int upgradeSlots, int usableSlots) {
    }

    private record Row(Kind kind, int index, @Nullable JobOption option, boolean value, Component text) {
        static Row heading(Component text) {
            return new Row(Kind.HEADING, -1, null, false, text);
        }

        static Row option(JobOption option, boolean value) {
            return new Row(Kind.OPTION, -1, option, value, Component.empty());
        }

        static Row upgrade(int index, Component name) {
            return new Row(Kind.UPGRADE, index, null, false, name);
        }

        static Row zone(int index) {
            return new Row(Kind.ZONE, index, null, false, Component.empty());
        }

        static Row link(int index) {
            return new Row(Kind.LINK, index, null, false, Component.empty());
        }
    }

    private enum Kind {
        HEADING,
        OPTION,
        UPGRADE,
        ZONE,
        LINK
    }
}
