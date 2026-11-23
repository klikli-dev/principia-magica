// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting.GlyphHit;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting.ZoneHit;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Client-side queries about what the player is looking at with a stylus held. Shared by the overlay renderer, the input handling
 * and the stylus HUD, so they all agree on the same targeted glyph.
 */
public class WorkplaceTargeting {

    /**
     * @param golemId the entity id of the golem the glyph belongs to
     * @param hit     the targeted glyph
     */
    public record Targeted(int golemId, GlyphHit hit) {
    }

    /**
     * @param golemId the entity id of the golem the footprint belongs to
     * @param hit     the targeted footprint box
     */
    public record TargetedZone(int golemId, ZoneHit hit) {
    }

    @Nullable
    public static ItemStack heldStylus(Player player) {
        if (player.getMainHandItem().getItem() instanceof StylusItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof StylusItem) {
            return player.getOffhandItem();
        }
        return null;
    }

    @Nullable
    public static PendingGlyph pendingGlyph(ItemStack stylus, ClientLevel level) {
        PendingGlyph glyph = stylus.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
        return glyph != null && glyph.pos().dimension().equals(level.dimension()) ? glyph : null;
    }

    @Nullable
    public static BlockHitResult hoveredHit(Minecraft minecraft) {
        return minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK ? hit : null;
    }

    @Nullable
    public static BlockPos hoveredBlock(Minecraft minecraft) {
        BlockHitResult hit = hoveredHit(minecraft);
        return hit == null ? null : hit.getBlockPos();
    }

    /**
     * The glyph of the selected golem under the crosshair, with a stylus held. Blocks in front of the glyph win,
     * matching what the player sees, and the reach is the same as for the glyph render range.
     */
    @Nullable
    public static Targeted targetedGlyph(Minecraft minecraft) {
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null) {
            return null;
        }

        ItemStack stylus = heldStylus(player);
        if (stylus == null || pendingGlyph(stylus, level) != null) {
            return null;
        }

        UUID selected = stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        GolemEntity golem = findGolem(level, selected);
        if (golem == null) {
            return null;
        }

        List<WorkFootprint> footprints = ClientGolemWorkplaceCache.latest(golem.getId())
                .map(entry -> entry.workplace().footprints())
                .orElse(List.of());
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 from = player.getEyePosition(partialTick);
        Vec3 to = from.add(player.getViewVector(partialTick).scale(GlyphTargeting.MAX_RANGE));

        Optional<GlyphHit> hit = GlyphTargeting.pick(from, to, footprints);
        if (hit.isEmpty() || !GlyphTargeting.winsOverBlock(hit.get(), blockDistance(level, player, from, to))) {
            return null;
        }

        return new Targeted(golem.getId(), hit.get());
    }

    /**
     * The footprint box of the selected golem under the crosshair, with a stylus held and no pending corner.
     */
    @Nullable
    public static TargetedZone targetedZone(Minecraft minecraft) {
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null) {
            return null;
        }

        ItemStack stylus = heldStylus(player);
        if (stylus == null || pendingGlyph(stylus, level) != null) {
            return null;
        }

        UUID selected = stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        GolemEntity golem = findGolem(level, selected);
        if (golem == null) {
            return null;
        }

        List<WorkFootprint> footprints = ClientGolemWorkplaceCache.latest(golem.getId())
                .map(entry -> entry.workplace().footprints())
                .orElse(List.of());
        if (footprints.isEmpty()) {
            return null;
        }

        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 from = player.getEyePosition(partialTick);
        Vec3 to = from.add(player.getViewVector(partialTick).scale(GlyphTargeting.MAX_RANGE));
        double blockDist = blockDistance(level, player, from, to);

        return GlyphTargeting.pickZone(from, to, footprints, blockDist)
                .map(hit -> new TargetedZone(golem.getId(), hit))
                .orElse(null);
    }

    /**
     * @return whether the crosshair block is currently linked as a container to the given golem
     */
    public static boolean isHoveredLinkedContainer(Minecraft minecraft, int golemId) {
        BlockPos hovered = hoveredBlock(minecraft);
        if (hovered == null) {
            return false;
        }

        return ClientGolemWorkplaceCache.latest(golemId)
                .map(entry -> entry.workplace().links().stream().anyMatch(link -> link.pos().equals(hovered)))
                .orElse(false);
    }

    private static double blockDistance(ClientLevel level, Player player, Vec3 from, Vec3 to) {
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS ? Double.POSITIVE_INFINITY : from.distanceTo(hit.getLocation());
    }

    @Nullable
    public static GolemEntity findGolem(ClientLevel level, @Nullable UUID selected) {
        if (selected == null) {
            return null;
        }

        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof GolemEntity golem && selected.equals(golem.getUUID())) {
                return golem;
            }
        }
        return null;
    }
}
