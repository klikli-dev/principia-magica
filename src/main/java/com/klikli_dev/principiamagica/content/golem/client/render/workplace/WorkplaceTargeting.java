// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting.CornerHit;
import com.klikli_dev.principiamagica.content.golem.ai.behaviour.ContainerAccess;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting.ZoneHit;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * and the stylus HUD, so they all agree on the same targeted corner, targeted zone box, and sticky corner lock.
 */
public class WorkplaceTargeting {

    private static final double LOCK_BLOCK_INFLATE = 0.08;

    @Nullable
    private static CornerLock cornerLock;

    /**
     * Remembers the original virtual corner block where the player started Shift+scrolling a corner,
     * keeping the player tethered to that corner until they look away—even when crossing past the opposite Y bound.
     */
    public record CornerLock(
            int golemId,
            int footprintIndex,
            int cornerIndex,
            BlockPos anchorBlock,
            int movingY,
            int fixedY
    ) {
    }

    /**
     * @param golemId        the entity id of the golem the corner belongs to
     * @param footprintIndex the index of the footprint in the golem's workplace
     * @param cornerIndex    the box corner index (0..7)
     * @param footprint      the footprint being targeted
     * @param block          the current virtual block coordinate of the corner
     * @param locked         whether this target is held via a sticky {@link CornerLock}
     * @param anchorBlock    the original virtual block where the lock began (same as {@code block} when unlocked)
     */
    public record TargetedCorner(
            int golemId,
            int footprintIndex,
            int cornerIndex,
            WorkFootprint footprint,
            BlockPos block,
            boolean locked,
            BlockPos anchorBlock
    ) {
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

    /**
     * Engages (or advances) the sticky corner lock for a Shift+scroll step, returning the corner index to send for
     * this step so rapid scrolling stays in sync even when crossing past {@code fixedY}.
     */
    public static int lockAndAdvanceCorner(TargetedCorner target, int delta) {
        if (cornerLock == null
                || cornerLock.golemId() != target.golemId()
                || cornerLock.footprintIndex() != target.footprintIndex()) {
            int startCorner = target.cornerIndex();
            int movingY = WorkFootprint.isTopCorner(startCorner) ? target.footprint().maxY() : target.footprint().minY();
            int fixedY = WorkFootprint.isTopCorner(startCorner) ? target.footprint().minY() : target.footprint().maxY();
            cornerLock = new CornerLock(target.golemId(), target.footprintIndex(), startCorner, target.block(), movingY, fixedY);
        }

        int cornerToSend = cornerLock.cornerIndex();
        int nextMovingY = cornerLock.movingY() + delta;
        int nextMinY = Math.min(nextMovingY, cornerLock.fixedY());
        int nextMaxY = Math.max(nextMovingY, cornerLock.fixedY());
        if (nextMinY >= WorkFootprint.MIN_Y && nextMaxY <= WorkFootprint.MAX_Y && (nextMaxY - nextMinY + 1) <= WorkFootprint.MAX_SIZE) {
            int nextCornerIndex = nextMovingY > cornerLock.fixedY()
                    ? (cornerToSend | 2)
                    : (nextMovingY < cornerLock.fixedY() ? (cornerToSend & ~2) : cornerToSend);
            cornerLock = new CornerLock(
                    cornerLock.golemId(),
                    cornerLock.footprintIndex(),
                    nextCornerIndex,
                    cornerLock.anchorBlock(),
                    nextMovingY,
                    cornerLock.fixedY()
            );
        }
        return cornerToSend;
    }

    public static void clearCornerLock() {
        cornerLock = null;
    }

    /**
     * The targeted virtual corner block (0..7) of the selected golem under the crosshair, or the active sticky
     * {@link CornerLock} as long as the player is still looking at the original corner block where they began moving it.
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
                    && footprints.get(cornerLock.footprintIndex()).isInDimension(level.dimension())
                    && isRayHittingLock(from, to, cornerLock)) {
                WorkFootprint footprint = footprints.get(cornerLock.footprintIndex());
                return new TargetedCorner(
                        golem.getId(),
                        cornerLock.footprintIndex(),
                        cornerLock.cornerIndex(),
                        footprint,
                        footprint.cornerBlock(cornerLock.cornerIndex()),
                        true,
                        cornerLock.anchorBlock()
                );
            }
            clearCornerLock();
        }

        Optional<CornerHit> hit = GlyphTargeting.pickCorner(from, to, footprints, level.dimension());
        return hit.map(h -> new TargetedCorner(
                golem.getId(),
                h.footprintIndex(),
                h.cornerIndex(),
                h.footprint(),
                h.block(),
                false,
                h.block()
        )).orElse(null);
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

        return GlyphTargeting.pickZone(from, to, footprints, blockDist, level.dimension())
                .map(hit -> new TargetedZone(golem.getId(), hit))
                .orElse(null);
    }

    private static boolean isRayHittingLock(Vec3 from, Vec3 to, CornerLock lock) {
        AABB blockBox = new AABB(lock.anchorBlock()).inflate(LOCK_BLOCK_INFLATE);
        return blockBox.contains(from) || blockBox.clip(from, to).isPresent();
    }

    /**
     * The container link of the selected golem under the crosshair. Found like a corner: a ray test against the link's box, not the
     * crosshair block, so a link whose container is gone can be targeted in the air too.
     *
     * @param golemId the entity id of the golem the link belongs to
     * @param link    the targeted link
     */
    public record TargetedLink(int golemId, ContainerLink link) {
    }

    /**
     * A container the stylus would act on, found the way the server finds it: a container link in the air, or a block under the
     * crosshair with an item container. The click acts on it before corners and zones.
     *
     * @param golemId the entity id of the selected golem
     * @param pos     the container position
     * @param face    the clicked face, used for a new link
     * @param role    the role of the link, or null if the position is not linked
     * @param missing whether the linked container is gone
     */
    public record TargetedContainer(int golemId, BlockPos pos, Direction face, @Nullable ContainerRole role, boolean missing) {
        public boolean linked() {
            return this.role != null;
        }
    }

    /**
     * @return the container the stylus would act on at the crosshair, or null. Linked positions are found by the link's box (see
     * {@link #targetedLink}), unlinked containers by the crosshair block having an item capability, like the server checks.
     */
    @Nullable
    public static TargetedContainer targetedContainer(Minecraft minecraft) {
        return findContainer(minecraft, false);
    }

    /**
     * The container under the crosshair while a corner is pending too. The click does not act on it then, but the HUD shows what the
     * sneak-click does, so it uses this. Clicks use {@link #targetedContainer}.
     */
    @Nullable
    public static TargetedContainer containerAtCrosshair(Minecraft minecraft) {
        return findContainer(minecraft, true);
    }

    @Nullable
    private static TargetedContainer findContainer(Minecraft minecraft, boolean allowPending) {
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null) {
            return null;
        }

        ItemStack stylus = heldStylus(player);
        if (stylus == null || (!allowPending && pendingGlyph(stylus, level) != null)) {
            return null;
        }

        GolemEntity golem = findGolem(level, stylus.get(DataComponentRegistry.STYLUS_SELECTION.get()));
        if (golem == null) {
            return null;
        }

        BlockHitResult hovered = hoveredHit(minecraft);
        TargetedLink link = findLink(minecraft, allowPending);
        if (link != null) {
            BlockPos pos = link.link().pos();
            Direction face = hovered != null && hovered.getBlockPos().equals(pos) ? hovered.getDirection() : Direction.UP;
            return new TargetedContainer(golem.getId(), pos, face, link.link().role(), LinkRenderer.isMissing(level, pos));
        }

        if (hovered != null && ContainerAccess.hasItemCapability(level, hovered.getBlockPos())) {
            BlockPos pos = hovered.getBlockPos();
            @Nullable
            ContainerRole role = ClientGolemWorkplaceCache.latest(golem.getId())
                    .flatMap(entry -> entry.workplace().links().stream()
                            .filter(candidate -> candidate.pos().equals(pos))
                            .map(ContainerLink::role)
                            .findFirst())
                    .orElse(null);
            return new TargetedContainer(golem.getId(), pos, hovered.getDirection(), role, false);
        }

        return null;
    }

    @Nullable
    public static TargetedLink targetedLink(Minecraft minecraft) {
        return findLink(minecraft, false);
    }

    @Nullable
    private static TargetedLink findLink(Minecraft minecraft, boolean allowPending) {
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null) {
            return null;
        }

        ItemStack stylus = heldStylus(player);
        if (stylus == null || (!allowPending && pendingGlyph(stylus, level) != null)) {
            return null;
        }

        UUID selected = stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        GolemEntity golem = findGolem(level, selected);
        if (golem == null) {
            return null;
        }

        List<ContainerLink> links = ClientGolemWorkplaceCache.latest(golem.getId())
                .map(entry -> entry.workplace().links())
                .orElse(List.of());
        if (links.isEmpty()) {
            return null;
        }

        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 from = player.getEyePosition(partialTick);
        Vec3 to = from.add(player.getViewVector(partialTick).scale(GlyphTargeting.MAX_RANGE));

        return GlyphTargeting.pickLink(from, to, links, blockDistance(level, player, from, to))
                .map(hit -> new TargetedLink(golem.getId(), hit.link()))
                .orElse(null);
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
