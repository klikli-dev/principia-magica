// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.client.principle.PrincipleRenderer;
import com.klikli_dev.principiamagica.content.libation.LibationUtils;
import com.klikli_dev.principiamagica.content.libation.drying.DryingSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.klikli_dev.principiamagica.content.libation.spout.SpoutBlockEntity;
import com.klikli_dev.principiamagica.content.libation.spout.VesselTapBlock;
import com.klikli_dev.principiamagica.content.libation.trough.CatchTroughBlockEntity;
import com.klikli_dev.principiamagica.content.libation.vessel.CuringVesselBlockEntity;
import com.klikli_dev.principiamagica.content.libation.vessel.VesselProperties;
import com.klikli_dev.principiamagica.content.principle.PrincipleContainer;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringEnvironment;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Crosshair panel for the principle containers (catch trough, Wabet Basin, Steeping Jar) and the drying slab. Containers list
 * their principles with amounts, vessels show the curing status and progress, the catch trough shows its item with the steeping
 * status and progress, the drying slab shows its input and output with the drying status and progress.
 * Hidden with F1 and while a screen is open.
 */
public class LibationHud {

    private static final int OFFSET_X = 10;
    private static final int OFFSET_Y = 6;
    private static final int PADDING = 3;
    private static final int ROW_HEIGHT = 18;
    private static final int TEXT_GAP = 4;
    private static final int LINE_HEIGHT = 9;
    private static final int STATUS_GAP = 3;
    private static final int BAR_GAP = 3;
    private static final int BAR_WIDTH = 60;
    private static final int BAR_HEIGHT = 3;
    private static final int BACKGROUND_COLOR = 0xA0000000;
    private static final int BAR_TRACK_COLOR = 0xFF303030;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int NEUTRAL_COLOR = 0xFFAAAAAA;
    private static final int CURING_COLOR = 0xFFFFD36B;
    private static final int OK_COLOR = 0xFF55DD55;
    private static final int PROBLEM_COLOR = 0xFFFF5555;

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

        if (level.getBlockEntity(pos) instanceof DryingSlabBlockEntity drying) {
            renderDrying(graphics, minecraft.font, level, drying, partialTick, x, y);
        } else if (level.getBlockEntity(pos) instanceof LibationSlabBlockEntity slab) {
            renderSlab(graphics, minecraft.font, level, pos, slab, x, y);
        } else if (level.getBlockEntity(pos) instanceof SpoutBlockEntity) {
            renderSpout(graphics, minecraft.font, level, pos, x, y);
        } else if (level.getBlockEntity(pos) instanceof CatchTroughBlockEntity trough) {
            renderTrough(graphics, minecraft.font, level, trough, partialTick, x, y);
        } else if (level.getBlockEntity(pos) instanceof PrincipleContainer container) {
            renderContainer(graphics, minecraft.font, level, pos, container, x, y);
        }
    }

    private static void renderContainer(GuiGraphicsExtractor graphics, Font font, ClientLevel level, BlockPos pos, PrincipleContainer container, int x, int y) {
        List<PrincipleStack> stacks = container.getPrinciples().stacks();
        List<Status> statuses = status(level, pos, container);
        Optional<Float> progress = curingProgress(level, pos, container);

        int rowsHeight = stacks.isEmpty() ? LINE_HEIGHT : stacks.size() * ROW_HEIGHT;
        int width = stacks.isEmpty() ? font.width(Component.translatable("hud.principiamagica.libation.empty")) : 0;
        for (PrincipleStack stack : stacks) {
            width = Math.max(width, PrincipleRenderer.ICON_SIZE + TEXT_GAP + font.width(principleRow(stack)));
        }
        int height = rowsHeight + PADDING * 2;
        for (Status status : statuses) {
            width = Math.max(width, font.width(status.text()));
        }
        if (!statuses.isEmpty()) {
            height += STATUS_GAP + statuses.size() * LINE_HEIGHT;
        }
        if (progress.isPresent()) {
            width = Math.max(width, BAR_WIDTH);
            height += BAR_GAP + BAR_HEIGHT;
        }
        width += PADDING * 2;

        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);

        if (stacks.isEmpty()) {
            graphics.text(font, Component.translatable("hud.principiamagica.libation.empty"), x + PADDING, y + PADDING, NEUTRAL_COLOR, true);
        }
        for (int i = 0; i < stacks.size(); i++) {
            PrincipleStack stack = stacks.get(i);
            int rowY = y + PADDING + i * ROW_HEIGHT;
            PrincipleRenderer.renderIcon(graphics, stack.principle(), x + PADDING, rowY);
            graphics.text(font, principleRow(stack), x + PADDING + PrincipleRenderer.ICON_SIZE + TEXT_GAP,
                    rowY + (PrincipleRenderer.ICON_SIZE - LINE_HEIGHT) / 2, TEXT_COLOR, true);
        }

        int statusY = y + PADDING + rowsHeight + STATUS_GAP;
        for (int i = 0; i < statuses.size(); i++) {
            Status status = statuses.get(i);
            graphics.text(font, status.text(), x + PADDING, statusY + i * LINE_HEIGHT, status.color(), true);
        }
        if (progress.isPresent()) {
            int barY = statusY + statuses.size() * LINE_HEIGHT + BAR_GAP;
            renderBar(graphics, x + PADDING, barY, progress.get());
        }
    }

    /**
     * @return the row text of a principle: its name in its colour, followed by the amount in mB.
     */
    private static Component principleRow(PrincipleStack stack) {
        return Component.empty().append(stack.principle().value().prefixedName()).append(" ").append(stack.amountText());
    }

    /**
     * Lists the water of the libation slab, the state of its spout, the principles stored in it by type and amount and, while an
     * item dissolves, its progress.
     */
    private static void renderSlab(GuiGraphicsExtractor graphics, Font font, ClientLevel level, BlockPos pos, LibationSlabBlockEntity slab, int x, int y) {
        Component water = Component.translatable("hud.principiamagica.libation.water", slab.getWater(), LibationSlabBlockEntity.WATER_CAPACITY);
        Status spout = spoutStatus(level, pos);
        List<PrincipleStack> stacks = slab.getStored().stacks();
        boolean dissolving = slab.isDissolving();
        Component status = Component.translatable("hud.principiamagica.libation.status.dissolving");
        float progress = Mth.clamp(slab.getDissolveProgress() / (float) LibationSlabBlockEntity.DISSOLVE_TIME, 0.0F, 1.0F);

        int width = Math.max(font.width(water), font.width(spout.text()));
        int rowsHeight = stacks.isEmpty() ? LINE_HEIGHT : stacks.size() * ROW_HEIGHT;
        if (stacks.isEmpty()) {
            width = Math.max(width, font.width(Component.translatable("hud.principiamagica.libation.empty")));
        }
        for (PrincipleStack stack : stacks) {
            width = Math.max(width, PrincipleRenderer.ICON_SIZE + TEXT_GAP + font.width(principleRow(stack)));
        }
        int rowsY = y + PADDING + LINE_HEIGHT + STATUS_GAP + LINE_HEIGHT + STATUS_GAP;
        int bottom = rowsY + rowsHeight;
        if (dissolving) {
            width = Math.max(width, Math.max(font.width(status), BAR_WIDTH));
            bottom += STATUS_GAP + LINE_HEIGHT + BAR_GAP + BAR_HEIGHT;
        }
        width += PADDING * 2;
        int height = bottom + PADDING - y;

        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
        graphics.text(font, water, x + PADDING, y + PADDING, TEXT_COLOR, true);
        graphics.text(font, spout.text(), x + PADDING, y + PADDING + LINE_HEIGHT + STATUS_GAP, spout.color(), true);
        if (stacks.isEmpty()) {
            graphics.text(font, Component.translatable("hud.principiamagica.libation.empty"), x + PADDING, rowsY, NEUTRAL_COLOR, true);
        }
        for (int i = 0; i < stacks.size(); i++) {
            PrincipleStack stack = stacks.get(i);
            int rowY = rowsY + i * ROW_HEIGHT;
            PrincipleRenderer.renderIcon(graphics, stack.principle(), x + PADDING, rowY);
            graphics.text(font, principleRow(stack), x + PADDING + PrincipleRenderer.ICON_SIZE + TEXT_GAP,
                    rowY + (PrincipleRenderer.ICON_SIZE - LINE_HEIGHT) / 2, TEXT_COLOR, true);
        }
        if (dissolving) {
            int lineY = bottom - LINE_HEIGHT - BAR_GAP - BAR_HEIGHT;
            graphics.text(font, status, x + PADDING, lineY, CURING_COLOR, true);
            int barY = lineY + LINE_HEIGHT + BAR_GAP;
            renderBar(graphics, x + PADDING, barY, progress);
        }
    }

    /**
     * Shows whether the spout is open, and whether there is a container below it to receive the drain.
     */
    private static void renderSpout(GuiGraphicsExtractor graphics, Font font, ClientLevel level, BlockPos pos, int x, int y) {
        List<Status> statuses = new ArrayList<>();
        boolean open = level.getBlockState(pos).getValue(BlockStateProperties.OPEN);
        statuses.add(openStatus(level.getBlockState(pos).getBlock() instanceof VesselTapBlock, open));
        if (open && LibationUtils.drainTarget(level, pos) == null) {
            statuses.add(new Status(Component.translatable("hud.principiamagica.libation.status.no_target"), PROBLEM_COLOR));
        }

        int width = 0;
        for (Status status : statuses) {
            width = Math.max(width, font.width(status.text()));
        }
        int height = PADDING * 2 + statuses.size() * LINE_HEIGHT;
        width += PADDING * 2;

        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
        for (int i = 0; i < statuses.size(); i++) {
            Status status = statuses.get(i);
            graphics.text(font, status.text(), x + PADDING, y + PADDING + i * LINE_HEIGHT, status.color(), true);
        }
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
        List<Status> statuses = dryingStatus(level, drying, recipe);
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
            int rowY = y + PADDING + row * ROW_HEIGHT;
            graphics.item(stacks.get(row), x + PADDING, rowY);
            graphics.text(font, labels.get(row), x + PADDING + PrincipleRenderer.ICON_SIZE + TEXT_GAP,
                    rowY + (PrincipleRenderer.ICON_SIZE - LINE_HEIGHT) / 2, TEXT_COLOR, true);
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
    private static void renderTrough(GuiGraphicsExtractor graphics, Font font, ClientLevel level, CatchTroughBlockEntity trough, float partialTick, int x, int y) {
        List<PrincipleStack> stacks = trough.getPrinciples().stacks();
        ItemStack item = trough.getItem();
        List<Status> statuses = troughStatus(trough);
        Optional<SteepingRecipe> recipe = item.isEmpty() ? Optional.empty() : LibationRecipes.steeping(item, trough.getPrinciples());
        boolean hasBar = recipe.isPresent();
        float progress = recipe.isPresent() ? trough.getDisplayProgress(level.getGameTime(), partialTick) / recipe.get().time() : 0.0F;

        int principlesHeight = stacks.isEmpty() ? LINE_HEIGHT : stacks.size() * ROW_HEIGHT;
        int itemHeight = item.isEmpty() ? 0 : ROW_HEIGHT;
        int rowsHeight = principlesHeight + itemHeight;
        int width = stacks.isEmpty() ? font.width(Component.translatable("hud.principiamagica.libation.empty")) : 0;
        for (PrincipleStack stack : stacks) {
            width = Math.max(width, PrincipleRenderer.ICON_SIZE + TEXT_GAP + font.width(principleRow(stack)));
        }
        if (!item.isEmpty()) {
            width = Math.max(width, PrincipleRenderer.ICON_SIZE + TEXT_GAP + font.width(item.getHoverName()));
        }
        width = Math.max(width, statusWidth(font, statuses));
        if (hasBar) {
            width = Math.max(width, BAR_WIDTH);
        }
        width += PADDING * 2;
        int height = rowsHeight + statusHeight(statuses) + barHeight(hasBar) + PADDING * 2;
        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);

        if (stacks.isEmpty()) {
            graphics.text(font, Component.translatable("hud.principiamagica.libation.empty"), x + PADDING, y + PADDING, NEUTRAL_COLOR, true);
        }
        for (int i = 0; i < stacks.size(); i++) {
            PrincipleStack stack = stacks.get(i);
            int rowY = y + PADDING + i * ROW_HEIGHT;
            PrincipleRenderer.renderIcon(graphics, stack.principle(), x + PADDING, rowY);
            graphics.text(font, principleRow(stack), x + PADDING + PrincipleRenderer.ICON_SIZE + TEXT_GAP,
                    rowY + (PrincipleRenderer.ICON_SIZE - LINE_HEIGHT) / 2, TEXT_COLOR, true);
        }
        if (!item.isEmpty()) {
            int itemY = y + PADDING + principlesHeight;
            graphics.item(item, x + PADDING, itemY);
            graphics.text(font, item.getHoverName(), x + PADDING + PrincipleRenderer.ICON_SIZE + TEXT_GAP,
                    itemY + (PrincipleRenderer.ICON_SIZE - LINE_HEIGHT) / 2, TEXT_COLOR, true);
        }
        renderStatuses(graphics, font, statuses, x + PADDING, y + PADDING + rowsHeight + STATUS_GAP);
        if (hasBar) {
            renderBar(graphics, x + PADDING, y + PADDING + rowsHeight + statusHeight(statuses) + BAR_GAP, Mth.clamp(progress, 0.0F, 1.0F));
        }
    }

    /**
     * @return the status of the drying: the reason it does not progress, or drying. Empty when the slab holds no input.
     */
    private static List<Status> dryingStatus(ClientLevel level, DryingSlabBlockEntity drying, Optional<DryingRecipe> recipe) {
        List<Status> statuses = new ArrayList<>(1);
        ItemStack input = drying.getInput();
        if (input.isEmpty()) {
            return statuses;
        }

        if (recipe.isEmpty()) {
            statuses.add(new Status(Component.translatable("hud.principiamagica.libation.status.no_recipe"), PROBLEM_COLOR));
        } else if (!drying.canMergeOutput(recipe.get().assemble(new SingleRecipeInput(input.copyWithCount(1))))) {
            statuses.add(new Status(Component.translatable("hud.principiamagica.libation.status.output_full"), PROBLEM_COLOR));
        } else if (level.getMaxLocalRawBrightness(drying.getBlockPos().above()) <= 0) {
            statuses.add(new Status(Component.translatable("hud.principiamagica.libation.status.too_dark"), PROBLEM_COLOR));
        } else {
            statuses.add(new Status(Component.translatable("hud.principiamagica.libation.status.drying"), CURING_COLOR));
        }
        return statuses;
    }

    /**
     * @return the status of the item in the trough: steeping, or the reason it does not steep. Empty when the trough holds no item.
     */
    private static List<Status> troughStatus(CatchTroughBlockEntity trough) {
        List<Status> statuses = new ArrayList<>(1);
        ItemStack item = trough.getItem();
        if (item.isEmpty()) {
            return statuses;
        }

        if (LibationRecipes.steepingFor(item).isEmpty()) {
            statuses.add(new Status(Component.translatable("hud.principiamagica.libation.status.no_recipe"), PROBLEM_COLOR));
        } else if (LibationRecipes.steeping(item, trough.getPrinciples()).isEmpty()) {
            statuses.add(new Status(Component.translatable("hud.principiamagica.libation.status.missing_principles"), PROBLEM_COLOR));
        } else {
            statuses.add(new Status(Component.translatable("hud.principiamagica.libation.status.steeping"), CURING_COLOR));
        }
        return statuses;
    }

    private static Component outputLabel(ItemStack stack) {
        return Component.literal(stack.getCount() + "x ")
                .append(Component.translatable("hud.principiamagica.libation.status.dried").withColor(OK_COLOR));
    }

    /**
     * @return the height of the status lines, including the gap above them. Zero without status lines.
     */
    private static int statusHeight(List<Status> statuses) {
        return statuses.isEmpty() ? 0 : STATUS_GAP + statuses.size() * LINE_HEIGHT;
    }

    /**
     * @return the height of the progress bar, including the gap above it. Zero without a bar.
     */
    private static int barHeight(boolean hasBar) {
        return hasBar ? BAR_GAP + BAR_HEIGHT : 0;
    }

    private static int statusWidth(Font font, List<Status> statuses) {
        int width = 0;
        for (Status status : statuses) {
            width = Math.max(width, font.width(status.text()));
        }
        return width;
    }

    private static void renderStatuses(GuiGraphicsExtractor graphics, Font font, List<Status> statuses, int x, int y) {
        for (int i = 0; i < statuses.size(); i++) {
            Status status = statuses.get(i);
            graphics.text(font, status.text(), x, y + i * LINE_HEIGHT, status.color(), true);
        }
    }

    /**
     * Draws a progress bar of the curing color on its track.
     *
     * @param fraction the progress in [0, 1].
     */
    private static void renderBar(GuiGraphicsExtractor graphics, int x, int y, float fraction) {
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BAR_TRACK_COLOR);
        graphics.fill(x, y, x + Math.round(BAR_WIDTH * fraction), y + BAR_HEIGHT, CURING_COLOR);
    }

    /**
     * @return the curing progress in [0, 1] of a vessel that is curing, or empty.
     */
    private static Optional<Float> curingProgress(ClientLevel level, BlockPos pos, PrincipleContainer container) {
        if (!(container instanceof CuringVesselBlockEntity vessel) || !vessel.isCuring()) {
            return Optional.empty();
        }

        VesselProperties properties = vessel.properties();
        Optional<CuringRecipe> recipe = LibationRecipes.curing(vessel.getPrinciples(), properties.environment());
        if (recipe.isEmpty()) {
            return Optional.empty();
        }

        int light = level.getMaxLocalRawBrightness(pos);
        if (!properties.canCure(light)) {
            return Optional.empty();
        }
        int time = properties.requiredTime(recipe.get().time(), light);
        if (time <= 0) {
            return Optional.empty();
        }
        return Optional.of(Mth.clamp(vessel.getProgress() / (float) time, 0.0F, 1.0F));
    }

    /**
     * @return the status of the spout of a host: open, closed or missing. Shown for the host, which only drains through an open spout.
     */
    private static Status spoutStatus(ClientLevel level, BlockPos host) {
        boolean vessel = level.getBlockEntity(host) instanceof CuringVesselBlockEntity;
        Direction side = LibationUtils.spoutSide(level, host);
        if (side == null) {
            return new Status(Component.translatable(vessel ? "hud.principiamagica.libation.status.no_tap" : "hud.principiamagica.libation.status.no_spout"), PROBLEM_COLOR);
        }
        boolean open = level.getBlockState(host.relative(side)).getValue(BlockStateProperties.OPEN);
        return openStatus(vessel, open);
    }

    /**
     * @return the status of a spout or tap: "Tap" for the vessels, "Spout" for the slab.
     */
    private static Status openStatus(boolean vessel, boolean open) {
        if (vessel) {
            return open
                    ? new Status(Component.translatable("hud.principiamagica.libation.status.tap_open"), OK_COLOR)
                    : new Status(Component.translatable("hud.principiamagica.libation.status.tap_closed"), NEUTRAL_COLOR);
        }
        return open
                ? new Status(Component.translatable("hud.principiamagica.libation.status.spout_open"), OK_COLOR)
                : new Status(Component.translatable("hud.principiamagica.libation.status.spout_closed"), NEUTRAL_COLOR);
    }

    /**
     * @return the status lines of a vessel: curing, its spout and the reason it does not cure. Empty for containers without status.
     */
    private static List<Status> status(ClientLevel level, BlockPos pos, PrincipleContainer container) {
        List<Status> statuses = new ArrayList<>();
        if (!(container instanceof CuringVesselBlockEntity vessel)) {
            return statuses;
        }

        if (vessel.isCuring()) {
            statuses.add(new Status(Component.translatable("hud.principiamagica.libation.status.curing"), CURING_COLOR));
        }
        statuses.add(spoutStatus(level, pos));
        VesselProperties properties = vessel.properties();
        if (properties.environment() == CuringEnvironment.DARK) {
            if (!vessel.getPrinciples().isEmpty() && LibationRecipes.curing(vessel.getPrinciples(), properties.environment()).isEmpty()) {
                statuses.add(new Status(Component.translatable("hud.principiamagica.libation.status.no_recipe"), PROBLEM_COLOR));
            }
        } else if (!properties.canCure(level.getMaxLocalRawBrightness(pos))) {
            statuses.add(new Status(Component.translatable("hud.principiamagica.libation.status.too_dark"), PROBLEM_COLOR));
        }
        return statuses;
    }

    private record Status(Component text, int color) {
    }
}
