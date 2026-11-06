// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStatus;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
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
import org.jspecify.annotations.Nullable;

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

        ItemStack stylus = WorkplaceOverlayRenderer.heldStylus(player);
        if (stylus == null) {
            return;
        }

        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + OFFSET_X;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + OFFSET_Y;
        Font font = minecraft.font;

        if (minecraft.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof GolemEntity golem
                && player.getUUID().equals(golem.getOwnerUUID())) {
            this.renderGolem(graphics, font, golem, x, y);
            return;
        }

        if (minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            linkRole(stylus, level, hit.getBlockPos()).ifPresent(role -> graphics.text(font, roleName(role), x, y, TEXT_COLOR, true));
        }
    }

    private void renderGolem(GuiGraphicsExtractor graphics, Font font, GolemEntity golem, int x, int y) {
        GolemStatus status = golem.getStatus();
        graphics.fill(x, y + (LINE_HEIGHT - DOT_SIZE) / 2, x + DOT_SIZE, y + (LINE_HEIGHT + DOT_SIZE) / 2, statusColor(status));
        graphics.text(font, golem.getName(), x + DOT_SIZE + DOT_GAP, y, TEXT_COLOR, true);

        if (status != GolemStatus.OK) {
            graphics.text(font, hintOf(status), x + DOT_SIZE + DOT_GAP, y + LINE_HEIGHT, TEXT_COLOR, true);
        }
    }

    /**
     * @return the role of the container at the position, if it is linked to the golem selected on the held stylus
     */
    private static Optional<ContainerRole> linkRole(ItemStack stylus, ClientLevel level, BlockPos pos) {
        UUID selected = stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        GolemEntity golem = WorkplaceOverlayRenderer.findGolem(level, selected);
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
