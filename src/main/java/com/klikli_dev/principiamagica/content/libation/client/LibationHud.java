// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.client.principle.PrincipleRenderer;
import com.klikli_dev.principiamagica.content.KeyCombo;
import com.klikli_dev.principiamagica.content.libation.client.LibationHudStatus.Status;
import com.klikli_dev.principiamagica.content.libation.drying.DryingSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.spout.SpoutBlockEntity;
import com.klikli_dev.principiamagica.content.libation.trough.CatchTroughBlockEntity;
import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleContainer;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.BACKGROUND_COLOR;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.BAR_GAP;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.BAR_HEIGHT;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.BAR_WIDTH;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.LINE_HEIGHT;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.PADDING;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.ROW_HEIGHT;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.STATUS_GAP;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.TEXT_COLOR;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.TEXT_GAP;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.barHeight;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.principleRowWidth;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.renderBar;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.renderItemRow;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.renderPrincipleRow;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.renderStatuses;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.statusHeight;
import static com.klikli_dev.principiamagica.content.libation.client.LibationHudPanel.statusWidth;

/**
 * Crosshair panel for the principle containers (catch trough, Wabet Basin, Steeping Jar) and the drying slab. Containers list
 * their principles with amounts, vessels show the curing status and progress, the catch trough shows its item with the steeping
 * status and progress, the drying slab shows its input and output with the drying status and progress.
 * <p>
 * Status lines come from {@link LibationHudStatus}, drawing primitives from {@link LibationHudPanel}.
 * Hidden with F1 and while a screen is open.
 */
public class LibationHud {

    private static final int OFFSET_X = 10;
    private static final int OFFSET_Y = 6;

    public void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(PrincipiaMagica.loc("libation_hud"), this::render);
    }

    private void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (minecraft.player == null || level == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + OFFSET_X;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + OFFSET_Y;
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        boolean selecting = LibationInput.isSelectingTool(minecraft.player);
        if (level.getBlockEntity(pos) instanceof DryingSlabBlockEntity drying) {
            renderDrying(graphics, minecraft.font, level, drying, partialTick, x, y);
        } else if (level.getBlockEntity(pos) instanceof LibationSlabBlockEntity slab) {
            renderSlab(graphics, minecraft.font, level, pos, slab, partialTick, selecting, x, y);
        } else if (level.getBlockEntity(pos) instanceof SpoutBlockEntity) {
            renderSpout(graphics, minecraft.font, level, pos, x, y);
        } else if (level.getBlockEntity(pos) instanceof CatchTroughBlockEntity trough) {
            renderTrough(graphics, minecraft.font, level, pos, trough, partialTick, selecting, x, y);
        } else if (level.getBlockEntity(pos) instanceof PrincipleContainer container) {
            renderContainer(graphics, minecraft.font, level, pos, container, selecting, x, y);
        }
    }

    private static void renderContainer(GuiGraphicsExtractor graphics, Font font, ClientLevel level, BlockPos pos, PrincipleContainer container, boolean selecting, int x, int y) {
        List<PrincipleStack> stacks = container.getPrinciples().stacks();
        Holder<Principle> selected = selecting ? LibationInput.selectedFor(pos, stacks) : null;
        List<Status> statuses = new ArrayList<>(LibationHudStatus.container(level, pos, container));
        if (selecting && stacks.size() > 1) {
            statuses.add(selectHintStatus());
        }
        Optional<Float> progress = LibationHudStatus.curingProgress(level, pos, container);

        int rowsHeight = stacks.isEmpty() ? LINE_HEIGHT : stacks.size() * ROW_HEIGHT;
        int width = stacks.isEmpty() ? font.width(Component.translatable("hud.principiamagica.libation.empty")) : 0;
        for (PrincipleStack stack : stacks) {
            width = Math.max(width, principleRowWidth(font, stack));
        }
        int height = rowsHeight + PADDING * 2;
        width = Math.max(width, statusWidth(font, statuses));
        if (!statuses.isEmpty()) {
            height += STATUS_GAP + statuses.size() * LINE_HEIGHT;
        }
        if (progress.isPresent()) {
            width = Math.max(width, BAR_WIDTH);
            height += BAR_GAP + BAR_HEIGHT;
        }
        int innerWidth = width;
        width += PADDING * 2;

        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);

        if (stacks.isEmpty()) {
            graphics.text(font, Component.translatable("hud.principiamagica.libation.empty"), x + PADDING, y + PADDING, LibationHudStatus.NEUTRAL_COLOR, true);
        }
        for (int i = 0; i < stacks.size(); i++) {
            PrincipleStack stack = stacks.get(i);
            boolean isSelected = selected != null && stack.principle().value().equals(selected.value());
            renderPrincipleRow(graphics, font, stack, x + PADDING, y + PADDING + i * ROW_HEIGHT, isSelected, innerWidth);
        }

        int statusY = y + PADDING + rowsHeight + STATUS_GAP;
        renderStatuses(graphics, font, statuses, x + PADDING, statusY);
        if (progress.isPresent()) {
            int barY = statusY + statuses.size() * LINE_HEIGHT + BAR_GAP;
            renderBar(graphics, x + PADDING, barY, progress.get());
        }
    }

    /**
     * Lists the water of the libation slab, the state of its spout, the principles stored in it by type and amount and, while an
     * item dissolves, its progress.
     */
    private static void renderSlab(GuiGraphicsExtractor graphics, Font font, ClientLevel level, BlockPos pos, LibationSlabBlockEntity slab, float partialTick, boolean selecting, int x, int y) {
        Component water = Component.translatable("hud.principiamagica.libation.water", slab.getWater(), LibationSlabBlockEntity.WATER_CAPACITY);
        Status spout = LibationHudStatus.hostSpout(level, pos);
        List<PrincipleStack> stacks = slab.getStored().stacks();
        Holder<Principle> selected = selecting ? LibationInput.selectedFor(pos, stacks) : null;
        boolean showSelectHint = selecting && stacks.size() > 1;
        Status selectHint = showSelectHint ? selectHintStatus() : null;
        boolean dissolving = slab.isDissolving();
        Component status = Component.translatable("hud.principiamagica.libation.status.dissolving");
        float progress = Mth.clamp(slab.getDissolveProgress(level.getGameTime(), partialTick) / (float) LibationSlabBlockEntity.DISSOLVE_TIME, 0.0F, 1.0F);

        int width = Math.max(font.width(water), font.width(spout.text()));
        int rowsHeight = stacks.isEmpty() ? LINE_HEIGHT : stacks.size() * ROW_HEIGHT;
        if (stacks.isEmpty()) {
            width = Math.max(width, font.width(Component.translatable("hud.principiamagica.libation.empty")));
        }
        for (PrincipleStack stack : stacks) {
            width = Math.max(width, principleRowWidth(font, stack));
        }
        if (selectHint != null) {
            width = Math.max(width, font.width(selectHint.text()));
        }
        int rowsY = y + PADDING + LINE_HEIGHT + STATUS_GAP + LINE_HEIGHT + STATUS_GAP;
        int bottom = rowsY + rowsHeight;
        if (dissolving) {
            width = Math.max(width, Math.max(font.width(status), BAR_WIDTH));
            bottom += STATUS_GAP + LINE_HEIGHT + BAR_GAP + BAR_HEIGHT;
        }
        if (selectHint != null) {
            bottom += STATUS_GAP + LINE_HEIGHT;
        }
        int innerWidth = width;
        width += PADDING * 2;
        int height = bottom + PADDING - y;

        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
        graphics.text(font, water, x + PADDING, y + PADDING, TEXT_COLOR, true);
        graphics.text(font, spout.text(), x + PADDING, y + PADDING + LINE_HEIGHT + STATUS_GAP, spout.color(), true);
        if (stacks.isEmpty()) {
            graphics.text(font, Component.translatable("hud.principiamagica.libation.empty"), x + PADDING, rowsY, LibationHudStatus.NEUTRAL_COLOR, true);
        }
        for (int i = 0; i < stacks.size(); i++) {
            PrincipleStack stack = stacks.get(i);
            boolean isSelected = selected != null && stack.principle().value().equals(selected.value());
            renderPrincipleRow(graphics, font, stack, x + PADDING, rowsY + i * ROW_HEIGHT, isSelected, innerWidth);
        }
        if (dissolving) {
            int lineY = rowsY + rowsHeight + STATUS_GAP;
            graphics.text(font, status, x + PADDING, lineY, LibationHudStatus.CURING_COLOR, true);
            int barY = lineY + LINE_HEIGHT + BAR_GAP;
            renderBar(graphics, x + PADDING, barY, progress);
        }
        if (selectHint != null) {
            int hintY = bottom - LINE_HEIGHT;
            graphics.text(font, selectHint.text(), x + PADDING, hintY, selectHint.color(), true);
        }
    }

    /**
     * Shows whether the spout is open, and whether there is a container below it to receive the drain.
     */
    private static void renderSpout(GuiGraphicsExtractor graphics, Font font, ClientLevel level, BlockPos pos, int x, int y) {
        List<Status> statuses = LibationHudStatus.spout(level, pos);

        int width = statusWidth(font, statuses);
        int height = PADDING * 2 + statuses.size() * LINE_HEIGHT;
        width += PADDING * 2;

        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
        renderStatuses(graphics, font, statuses, x + PADDING, y + PADDING);
    }

    /**
     * Lists the input and the output of the drying slab, each with its item icon. While the input has a recipe, the status
     * of the drying and the progress of the first input item are shown below the rows.
     */
    private static void renderDrying(GuiGraphicsExtractor graphics, Font font, ClientLevel level, DryingSlabBlockEntity drying, float partialTick, int x, int y) {
        List<ItemStack> stacks = new ArrayList<>(2);
        List<Component> labels = new ArrayList<>(2);
        ItemStack input = drying.getInput();
        if (!input.isEmpty()) {
            stacks.add(input);
            labels.add(Component.literal(input.getCount() + "x"));
        }
        ItemStack output = drying.getOutput();
        if (!output.isEmpty()) {
            stacks.add(output);
            labels.add(outputLabel(output));
        }
        if (stacks.isEmpty()) {
            return;
        }

        Optional<DryingRecipe> recipe = input.isEmpty() ? Optional.empty() : LibationRecipes.drying(input);
        List<Status> statuses = LibationHudStatus.drying(level, drying, recipe);
        boolean hasBar = recipe.isPresent();
        float progress = recipe.isPresent() ? drying.getDisplayProgress(level.getGameTime(), partialTick) / recipe.get().time() : 0.0F;

        int rowsHeight = stacks.size() * ROW_HEIGHT;
        int width = 0;
        for (Component label : labels) {
            width = Math.max(width, PrincipleRenderer.ICON_SIZE + TEXT_GAP + font.width(label));
        }
        width = Math.max(width, statusWidth(font, statuses));
        if (hasBar) {
            width = Math.max(width, BAR_WIDTH);
        }
        width += PADDING * 2;
        int height = rowsHeight + statusHeight(statuses) + barHeight(hasBar) + PADDING * 2;
        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);

        for (int row = 0; row < stacks.size(); row++) {
            renderItemRow(graphics, font, stacks.get(row), labels.get(row), x + PADDING, y + PADDING + row * ROW_HEIGHT);
        }
        renderStatuses(graphics, font, statuses, x + PADDING, y + PADDING + rowsHeight + STATUS_GAP);
        if (hasBar) {
            renderBar(graphics, x + PADDING, y + PADDING + rowsHeight + statusHeight(statuses) + BAR_GAP, Mth.clamp(progress, 0.0F, 1.0F));
        }
    }

    /**
     * Lists the principles of the catch trough, then the item it holds with the steeping status and progress. The principles
     * are listed as in the other containers.
     */
    private static void renderTrough(GuiGraphicsExtractor graphics, Font font, ClientLevel level, BlockPos pos, CatchTroughBlockEntity trough, float partialTick, boolean selecting, int x, int y) {
        List<PrincipleStack> stacks = trough.getPrinciples().stacks();
        Holder<Principle> selected = selecting ? LibationInput.selectedFor(pos, stacks) : null;
        ItemStack item = trough.getItem();
        List<Status> statuses = new ArrayList<>(LibationHudStatus.trough(trough));
        if (selecting && stacks.size() > 1) {
            statuses.add(selectHintStatus());
        }
        Optional<SteepingRecipe> recipe = item.isEmpty() ? Optional.empty() : LibationRecipes.steeping(item, trough.getPrinciples());
        boolean hasBar = recipe.isPresent();
        float progress = recipe.isPresent() ? trough.getDisplayProgress(level.getGameTime(), partialTick) / recipe.get().time() : 0.0F;

        int principlesHeight = stacks.isEmpty() ? LINE_HEIGHT : stacks.size() * ROW_HEIGHT;
        int itemHeight = item.isEmpty() ? 0 : ROW_HEIGHT;
        int rowsHeight = principlesHeight + itemHeight;
        int width = stacks.isEmpty() ? font.width(Component.translatable("hud.principiamagica.libation.empty")) : 0;
        for (PrincipleStack stack : stacks) {
            width = Math.max(width, principleRowWidth(font, stack));
        }
        if (!item.isEmpty()) {
            width = Math.max(width, PrincipleRenderer.ICON_SIZE + TEXT_GAP + font.width(item.getHoverName()));
        }
        width = Math.max(width, statusWidth(font, statuses));
        if (hasBar) {
            width = Math.max(width, BAR_WIDTH);
        }
        int innerWidth = width;
        width += PADDING * 2;
        int height = rowsHeight + statusHeight(statuses) + barHeight(hasBar) + PADDING * 2;
        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);

        if (stacks.isEmpty()) {
            graphics.text(font, Component.translatable("hud.principiamagica.libation.empty"), x + PADDING, y + PADDING, LibationHudStatus.NEUTRAL_COLOR, true);
        }
        for (int i = 0; i < stacks.size(); i++) {
            PrincipleStack stack = stacks.get(i);
            boolean isSelected = selected != null && stack.principle().value().equals(selected.value());
            renderPrincipleRow(graphics, font, stack, x + PADDING, y + PADDING + i * ROW_HEIGHT, isSelected, innerWidth);
        }
        if (!item.isEmpty()) {
            renderItemRow(graphics, font, item, item.getHoverName(), x + PADDING, y + PADDING + principlesHeight);
        }
        renderStatuses(graphics, font, statuses, x + PADDING, y + PADDING + rowsHeight + STATUS_GAP);
        if (hasBar) {
            renderBar(graphics, x + PADDING, y + PADDING + rowsHeight + statusHeight(statuses) + BAR_GAP, Mth.clamp(progress, 0.0F, 1.0F));
        }
    }

    private static Status selectHintStatus() {
        return new Status(Component.translatable("hud.principiamagica.libation.hint.select_principle", KeyCombo.CROUCH_SCROLL.component()), LibationHudStatus.NEUTRAL_COLOR);
    }

    private static Component outputLabel(ItemStack stack) {
        return Component.literal(stack.getCount() + "x ")
                .append(Component.translatable("hud.principiamagica.libation.status.dried").withColor(LibationHudStatus.OK_COLOR));
    }
}
