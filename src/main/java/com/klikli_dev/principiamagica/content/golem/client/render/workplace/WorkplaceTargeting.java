// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting.CornerHit;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Client-side queries about what the player is looking at with a stylus held. Shared by the overlay renderer, the input handling
 * and the stylus HUD, so they all agree on the same targeted corner and sticky corner lock.
 */
public class WorkplaceTargeting {

    private static final double LOCK_VERTEX_SIZE = 0.65;
    private static final double LOCK_BLOCK_INFLATE = 0.05;

    @Nullable
    private static CornerLock cornerLock;

    /**
     * Remembers the original corner vertex/block where the player started Shift+scrolling a corner,
     * keeping the player tethered to that corner until they look away.
     */
    public record CornerLock(int golemId, int footprintIndex, int cornerIndex, Vec3 anchorVertex, BlockPos anchorBlock) {
    }

    /**
     * @param golemId        the entity id of the golem the corner belongs to
     * @param footprintIndex the index of the footprint in the golem's workplace
     * @param cornerIndex    the box corner index (0..7)
     * @param footprint      the footprint being targeted
     * @param vertex         the current 3D vertex of the corner
     * @param block          the current block coordinate of the corner
     * @param locked         whether this target is held via a sticky {@link CornerLock}
     * @param anchorVertex   the original vertex where the lock began (same as {@code vertex} when unlocked)
     * @param anchorBlock    the original block where the lock began (same as {@code block} when unlocked)
     */
    public record TargetedCorner(
            int golemId,
            int footprintIndex,
            int cornerIndex,
            WorkFootprint footprint,
            Vec3 vertex,
            BlockPos block,
            boolean locked,
            Vec3 anchorVertex,
            BlockPos anchorBlock
    ) {
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

    public static boolean isLookingAtGolem(Minecraft minecraft) {
        return minecraft.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof GolemEntity;
    }

    /**
     * Mirrors {@code StylusBlockInteraction.placeCorner} so the ghost preview snaps below replaceable ground cover like grass.
     */
    public static BlockPos snapReplaceable(ClientLevel level, BlockPos pos) {
        if (level.getBlockState(pos).canBeReplaced()) {
            BlockPos below = pos.below();
            if (!level.getBlockState(below).isAir() && !level.getBlockState(below).canBeReplaced()) {
                return below;
            }
        }
        return pos;
    }

    public static void lockCorner(TargetedCorner target) {
        if (cornerLock != null
                && cornerLock.golemId() == target.golemId()
                && cornerLock.footprintIndex() == target.footprintIndex()
                && cornerLock.cornerIndex() == target.cornerIndex()) {
            return;
        }
        cornerLock = new CornerLock(target.golemId(), target.footprintIndex(), target.cornerIndex(), target.vertex(), target.block());
    }

    public static void clearCornerLock() {
        cornerLock = null;
    }

    /**
     * The targeted box corner (0..7) of the selected golem under the crosshair, or the active sticky {@link CornerLock}
     * as long as the player is still looking at the original corner block/vertex where they began moving it.
     */
    @Nullable
    public static TargetedCorner targetedCorner(Minecraft minecraft) {
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null) {
            clearCornerLock();
            return null;
        }

        ItemStack stylus = heldStylus(player);
        if (stylus == null || pendingGlyph(stylus, level) != null) {
            clearCornerLock();
            return null;
        }

        UUID selected = stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        GolemEntity golem = findGolem(level, selected);
        if (golem == null) {
            clearCornerLock();
            return null;
        }

        List<WorkFootprint> footprints = ClientGolemWorkplaceCache.latest(golem.getId())
                .map(entry -> entry.workplace().footprints())
                .orElse(List.of());
        if (footprints.isEmpty()) {
            clearCornerLock();
            return null;
        }

        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 from = player.getEyePosition(partialTick);
        Vec3 to = from.add(player.getViewVector(partialTick).scale(GlyphTargeting.MAX_RANGE));

        if (cornerLock != null) {
            if (cornerLock.golemId() == golem.getId()
                    && cornerLock.footprintIndex() >= 0
                    && cornerLock.footprintIndex() < footprints.size()
                    && isRayHittingLock(from, to, cornerLock)) {
                WorkFootprint footprint = footprints.get(cornerLock.footprintIndex());
                return new TargetedCorner(
                        golem.getId(),
                        cornerLock.footprintIndex(),
                        cornerLock.cornerIndex(),
                        footprint,
                        footprint.cornerVertex(cornerLock.cornerIndex()),
                        footprint.cornerBlock(cornerLock.cornerIndex()),
                        true,
                        cornerLock.anchorVertex(),
                        cornerLock.anchorBlock()
                );
            }
            clearCornerLock();
        }

        double blockDist = blockDistance(level, player, from, to);
        Optional<CornerHit> hit = GlyphTargeting.pickCorner(from, to, footprints, blockDist);
        return hit.map(h -> new TargetedCorner(
                golem.getId(),
                h.footprintIndex(),
                h.cornerIndex(),
                h.footprint(),
                h.vertex(),
                h.block(),
                false,
                h.vertex(),
                h.block()
        )).orElse(null);
    }

    private static boolean isRayHittingLock(Vec3 from, Vec3 to, CornerLock lock) {
        AABB vertexBox = AABB.ofSize(lock.anchorVertex(), LOCK_VERTEX_SIZE, LOCK_VERTEX_SIZE, LOCK_VERTEX_SIZE);
        if (vertexBox.contains(from) || vertexBox.clip(from, to).isPresent()) {
            return true;
        }
        AABB blockBox = new AABB(lock.anchorBlock()).inflate(LOCK_BLOCK_INFLATE);
        return blockBox.contains(from) || blockBox.clip(from, to).isPresent();
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
