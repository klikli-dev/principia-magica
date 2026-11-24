// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.KeyCombo;
import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceTargeting.TargetedCorner;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceTargeting.TargetedContainer;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions.EditFailure;
import com.klikli_dev.principiamagica.content.golem.item.StylusClick;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
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
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

        TargetedContainer container = WorkplaceTargeting.targetedContainer(minecraft);
        if (container != null) {
            Component status = container.role() == null
                    ? Component.translatable("hud.principiamagica.stylus.not_linked")
                    : roleName(container.role());
            graphics.text(font, status, x, y, TEXT_COLOR, true);
        }

        int hintY = y + (container != null ? LINE_HEIGHT : 0);
        List<HintLine> hints = workplaceHints(minecraft, stylus, level, container);
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
     * Context hints for the crosshair target while the stylus has a golem selected. They describe what the click does, in the order the
     * click resolves: the pending corner, a locked corner, a container, a corner, then the targeted zone box.
     */
    private static List<HintLine> workplaceHints(Minecraft minecraft, ItemStack stylus, ClientLevel level, @Nullable TargetedContainer container) {
        List<HintLine> hints = new ArrayList<>();
        UUID selected = stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        GolemEntity golem = WorkplaceTargeting.findGolem(level, selected);

        PendingGlyph pending = WorkplaceTargeting.pendingGlyph(stylus, level);
        if (pending != null) {
            boolean isMoving = pending.original().isPresent();
            hints.add(HintLine.neutral(Component.translatable(isMoving
                    ? "hud.principiamagica.stylus.moving_corner"
                    : "hud.principiamagica.stylus.pending_corner")));
            boolean unreachable = golem == null || minecraft.player == null
                    || golem.distanceToSqr(minecraft.player) > StylusItem.MAX_DISTANCE_SQR;
            if (isMoving && unreachable) {
                // The move cannot be placed or cancelled until the golem is reachable again
                hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.move_closer")));
                hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.discard_move", KeyCombo.CROUCH_RIGHT_CLICK.component())));
                return hints;
            }

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
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.lift_next", KeyCombo.CROUCH_SCROLL.component())));
            hints.add(HintLine.neutral(Component.translatable(isMoving
                    ? "hud.principiamagica.stylus.hint.place_moved_corner"
                    : "hud.principiamagica.stylus.hint.carve_opposite", KeyCombo.RIGHT_CLICK.component())));
            if (container != null && container.linked()) {
                // With a pending corner, a plain click still carves, but a sneak-click on a linked container unlinks it
                hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.unlink", KeyCombo.CROUCH_RIGHT_CLICK.component())));
            }
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.cancel", KeyCombo.RIGHT_CLICK.component())));
            return hints;
        }

        if (golem == null) {
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.select_golem", KeyCombo.RIGHT_CLICK.component())));
            return hints;
        }

        TargetedCorner corner = WorkplaceTargeting.targetedCorner(minecraft);
        if (corner != null && (corner.locked() || container == null)) {
            if (corner.locked()) {
                hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.corner_locked")));
            }
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.lift_corner", KeyCombo.CROUCH_SCROLL.component())));
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.move_corner", KeyCombo.RIGHT_CLICK.component())));
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.erase", KeyCombo.CROUCH_RIGHT_CLICK.component())));
            return hints;
        }

        if (container != null) {
            return containerHints(container);
        }

        if (WorkplaceTargeting.targetedZone(minecraft) != null) {
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.erase", KeyCombo.CROUCH_RIGHT_CLICK.component())));
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.corner_options")));
        }
        return hints;
    }

    /**
     * The hints of a container under the crosshair, in the order of the click: the missing warning if it is gone, the plain click
     * (sets the next role, or unlinks after both), and the sneak-click that unlinks if it is linked.
     */
    private static List<HintLine> containerHints(TargetedContainer container) {
        List<HintLine> hints = new ArrayList<>();
        if (container.missing()) {
            hints.add(HintLine.problem(Component.translatable("gui.principiamagica.golem.missing_container")));
        }

        ContainerRole next = StylusClick.nextRole(container.role());
        if (next == null) {
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.unlink", KeyCombo.RIGHT_CLICK.component())));
        } else {
            String key = container.linked() ? "hud.principiamagica.stylus.hint.set_role" : "hud.principiamagica.stylus.hint.link_as";
            hints.add(HintLine.neutral(Component.translatable(key, KeyCombo.RIGHT_CLICK.component(), roleName(next))));
        }

        if (container.linked()) {
            hints.add(HintLine.neutral(Component.translatable("hud.principiamagica.stylus.hint.unlink", KeyCombo.CROUCH_RIGHT_CLICK.component())));
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
        graphics.text(font, Component.translatable(hintKey, KeyCombo.RIGHT_CLICK.component()), x + DOT_SIZE + DOT_GAP, nextY, NEUTRAL_COLOR, true);
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
