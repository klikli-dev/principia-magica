// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceTargeting.TargetedCorner;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions.EditFailure;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Crosshair HUD while a stylus is held. Looking at a container linked to the selected golem shows its role. Looking at one of
 * the player's own golems shows its name, a status dot and the status hint if it has a problem. Hidden with F1 and while a screen is open.
 */
public class StylusHud {

    private static final int OFFSET_X = 10;
    private static final int OFFSET_Y = 6;
    private static final int LINE_HEIGHT = 9;
    private static final int DOT_SIZE = 4;
    private static final int DOT_GAP = 4;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int OK_COLOR = 0xFF55DD55;
    private static final int NEUTRAL_COLOR = 0xFFAAAAAA;
    private static final int PROBLEM_COLOR = 0xFFFF5555;

    public void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(PrincipiaMagica.loc("stylus_hud"), this::render);
    }

    private void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }

        ItemStack stylus = WorkplaceTargeting.heldStylus(player);
        if (stylus == null) {
            return;
        }

        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + OFFSET_X;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + OFFSET_Y;
        Font font = minecraft.font;

        if (minecraft.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof GolemEntity golem
                && player.getUUID().equals(golem.getOwnerUUID())) {
            boolean isSelected = golem.getUUID().equals(stylus.get(DataComponentRegistry.STYLUS_SELECTION.get()));
            this.renderGolem(graphics, font, golem, isSelected, x, y);
            return;
        }

        Optional<ContainerRole> role = minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK
                ? linkRole(stylus, level, hit.getBlockPos())
                : Optional.empty();
        role.ifPresent(found -> graphics.text(font, roleName(found), x, y, TEXT_COLOR, true));

        int hintY = y + (role.isPresent() ? LINE_HEIGHT : 0);
        List<HintLine> hints = workplaceHints(minecraft, stylus, level, role.isPresent());
        for (int i = 0; i < hints.size(); i++) {
            HintLine line = hints.get(i);
            graphics.text(font, line.text(), x, hintY + i * LINE_HEIGHT, line.color(), true);
        }
    }

    private record HintLine(Component text, int color) {
        static HintLine neutral(Component text) {
            return new HintLine(text, NEUTRAL_COLOR);
        }

        static HintLine problem(Component text) {
            return new HintLine(text, PROBLEM_COLOR);
        }
    }

    /**
     * Context hints for the crosshair target while the stylus has a golem selected: the pending corner, the targeted glyph,
     * a hovered linked container, or the targeted zone box.
     */
    private static List<HintLine> workplaceHints(Minecraft minecraft, ItemStack stylus, ClientLevel level, boolean hoveringLinkedContainer) {
        List<HintLine> hints = new ArrayList<>();
        UUID selected = stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        GolemEntity golem = WorkplaceTargeting.findGolem(level, selected);

        PendingGlyph pending = WorkplaceTargeting.pendingGlyph(stylus, level);
        if (pending != null) {
            boolean isMoving = pending.original().isPresent();
            hints.add(HintLine.neutral(Component.translatable(isMoving
                    ? "hud.principiamagica.stylus.moving_corner"
                    : "hud.principiamagica.stylus.pending_corner")));
            BlockHitResult hovered = WorkplaceTargeting.hoveredHit(minecraft);
            if (hovered != null && golem != null) {
                Workplace workplace = ClientGolemWorkplaceCache.latest(golem.getId())
                        .map(ClientGolemWorkplaceCache.Entry::workplace)
                        .orElse(Workplace.EMPTY);
                BlockPos snapped = WorkplaceTargeting.snapReplaceable(level, hovered.getBlockPos());
                EditFailure failure = StylusActions.placeFootprint(workplace, level.dimension(), pending.pos().pos(), snapped.offset(0, pending.ghostLift(), 0)).failure();
                if (failure != null) {
                    hints.add(HintLine.problem(failureMessage(failure)));
                }
            }
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.lift_next")));
            hints.add(HintLine.neutral(Component.translatable(isMoving
                    ? "hud.principiamagica.stylus.hint.place_moved_corner"
                    : "hud.principiamagica.stylus.hint.carve_opposite")));
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.cancel")));
            return hints;
        }

        if (golem == null) {
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.select_golem")));
            return hints;
        }

        TargetedCorner corner = WorkplaceTargeting.targetedCorner(minecraft);
        if (corner != null && (corner.locked() || !hoveringLinkedContainer)) {
            if (corner.locked()) {
                hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.corner_locked")));
            }
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.lift_corner")));
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.move_corner")));
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.erase")));
            return hints;
        }

        if (hoveringLinkedContainer) {
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.cycle_link")));
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.unlink")));
            return hints;
        }

        if (WorkplaceTargeting.targetedZone(minecraft) != null) {
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.erase")));
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.corner_options")));
        }
        return hints;
    }

    private static Component failureMessage(EditFailure failure) {
        return switch (failure) {
            case CORNER_OCCUPIED -> Component.translatable("message.principiamagica.stylus.corner_occupied");
            case OUT_OF_BOUNDS -> Component.translatable("message.principiamagica.stylus.out_of_bounds");
            case TOO_LARGE, NOT_FOUND -> Component.translatable("message.principiamagica.stylus.footprint_too_large",
                    WorkFootprint.MAX_SIZE, WorkFootprint.MAX_SIZE);
        };
    }

    private void renderGolem(GuiGraphicsExtractor graphics, Font font, GolemEntity golem, boolean selected, int x, int y) {
        GolemStatus status = golem.getStatus();
        graphics.fill(x, y + (LINE_HEIGHT - DOT_SIZE) / 2, x + DOT_SIZE, y + (LINE_HEIGHT + DOT_SIZE) / 2, statusColor(status));
        graphics.text(font, golem.getName(), x + DOT_SIZE + DOT_GAP, y, TEXT_COLOR, true);

        if (status != GolemStatus.OK) {
            graphics.text(font, hintOf(status), x + DOT_SIZE + DOT_GAP, y + LINE_HEIGHT, TEXT_COLOR, true);
        }

        int nextY = y + (status != GolemStatus.OK ? 2 : 1) * LINE_HEIGHT;
        String hintKey = selected ? "hud.principiamagica.stylus.hint.deselect" : "hud.principiamagica.stylus.hint.select";
        graphics.text(font, Component.translatable(hintKey), x + DOT_SIZE + DOT_GAP, nextY, NEUTRAL_COLOR, true);
    }

    /**
     * @return the role of the container at the position, if it is linked to the golem selected on the held stylus
     */
    private static Optional<ContainerRole> linkRole(ItemStack stylus, ClientLevel level, BlockPos pos) {
        UUID selected = stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        GolemEntity golem = WorkplaceTargeting.findGolem(level, selected);
        if (golem == null) {
            return Optional.empty();
        }

        return ClientGolemWorkplaceCache.latest(golem.getId()).flatMap(entry -> entry.workplace().links().stream()
                .filter(link -> link.pos().equals(pos))
                .map(ContainerLink::role)
                .findFirst());
    }

    private static Component roleName(ContainerRole role) {
        return Component.translatable("hud.principiamagica.stylus.role." + role.name().toLowerCase(Locale.ROOT));
    }

    private static Component hintOf(GolemStatus status) {
        return Component.translatable("gui.principiamagica.golem.status_hint." + status.name().toLowerCase(Locale.ROOT));
    }

    private static int statusColor(GolemStatus status) {
        if (status == GolemStatus.OK) {
            return OK_COLOR;
        }

        return status.isProblem() ? PROBLEM_COLOR : NEUTRAL_COLOR;
    }
}
