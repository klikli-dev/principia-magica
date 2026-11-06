// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting.GlyphHit;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageEraseGlyph;
import com.klikli_dev.principiamagica.network.messages.MessageShiftZone;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.UUID;

/**
 * Draws the workplace of the golem selected on a held stylus: footprints, carved glyphs, container links, anchor, work
 * target, and the pending glyph with a preview of the footprint under the cursor. Elements fade in and out when the stylus
 * is equipped, unequipped or the selection changes.
 * <p>
 * While sneaking with the stylus, the glyph under the crosshair is highlighted and sneak-right-click erases its footprint,
 * even from a distance.
 */
public class WorkplaceOverlayRenderer {

    private static final long SPARKLE_INTERVAL_TICKS = 4;
    private static final double SPARKLE_RANGE_SQR = 32.0 * 32.0;
    private static final int BUFFER_SIZE = 1536;
    private static final int HIGHLIGHT_COLOR = 0xFFFFFF;
    private static final float HIGHLIGHT_GLYPH_SIZE = 1.0F;

    private final FadingElementTracker<UUID> golemFade = new FadingElementTracker<>();
    private final FadingElementTracker<PendingGlyph> cornerFade = new FadingElementTracker<>();
    private final PendingZoneRenderer pending = new PendingZoneRenderer();
    private MultiBufferSource.@Nullable BufferSource buffers;
    @Nullable
    private Targeted targeted;

    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;

        ItemStack stylus = player == null || level == null ? null : heldStylus(player);
        UUID selected = stylus == null ? null : stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        PendingGlyph corner = stylus == null ? null : pendingGlyph(stylus, level);

        this.golemFade.tick(selected);
        this.cornerFade.tick(corner);
        this.pending.tick(corner == null ? null : corner.pos().pos(), hoveredBlock(minecraft));
        this.targeted = targetedGlyph(minecraft);

        if (player != null && level != null) {
            this.spawnSparkles(level, player);
        }
    }

    /**
     * Sneak-right-click on a glyph the stylus is targeting erases its footprint. The click is cancelled when a glyph is hit,
     * so the block behind it is not used and the server does not also run the stylus action for that block.
     */
    public void onInteractionKeyMapping(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }

        Targeted target = targetedGlyph(Minecraft.getInstance());
        if (target == null) {
            return;
        }

        event.setCanceled(true);
        event.setSwingHand(true);
        CarvedGlyph glyph = target.hit().glyph();
        Networking.sendToServer(new MessageEraseGlyph(target.golemId(), glyph.pos(), glyph.face()));
    }

    /**
     * Shift+scroll with a stylus that has a golem selected raises or lowers the footprint under the cursor, instead of changing the hotbar slot.
     */
    public void onMouseScrolling(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null || !player.isShiftKeyDown()) {
            return;
        }

        ItemStack stylus = heldStylus(player);
        UUID selected = stylus == null ? null : stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        GolemEntity golem = findGolem(level, selected);
        BlockPos hovered = hoveredBlock(minecraft);
        if (golem == null || hovered == null) {
            return;
        }

        int index = ClientGolemWorkplaceCache.latest(golem.getId())
                .map(entry -> entry.workplace().footprintIndexAt(hovered))
                .orElse(-1);
        if (index < 0) {
            return;
        }

        event.setCanceled(true);
        int delta = event.getScrollDeltaY() > 0 ? 1 : -1;
        Networking.sendToServer(new MessageShiftZone(golem.getId(), index, delta));
    }

    public void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentParticles event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        boolean golemVisible = this.golemFade.isVisible(partialTick);
        PendingGlyph corner = this.cornerFade.isVisible(partialTick) ? this.cornerFade.key() : null;
        if (!golemVisible && corner == null) {
            return;
        }

        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        MultiBufferSource.BufferSource buffers = this.buffers();
        WorkplaceGeometry geometry = new WorkplaceGeometry(buffers, event.getPoseStack().last(), camera);
        double time = level.getGameTime() + partialTick;

        if (golemVisible) {
            this.renderGolem(minecraft, geometry, level, this.golemFade.key(), this.golemFade.alpha(partialTick), partialTick, time);
        }

        if (corner != null) {
            this.pending.render(geometry, corner.glyph(), this.cornerFade.alpha(partialTick), partialTick, time);
        }

        // Draws in the insertion order of the fixed buffers: fill, beam, ribbon, glyph.
        buffers.endBatch();
    }

    /**
     * Buffer source with a fixed buffer per workplace render type. The geometry holds vertex consumers for all render types at once,
     * which the shared vanilla buffer source does not support: requesting a second non-fixed
     * render type ends the batch of the previous one.
     */
    private MultiBufferSource.BufferSource buffers() {
        if (this.buffers == null) {
            SequencedMap<RenderType, ByteBufferBuilder> fixed = new LinkedHashMap<>();
            fixed.put(WorkplaceRenderTypes.FILL, new ByteBufferBuilder(BUFFER_SIZE));
            fixed.put(WorkplaceRenderTypes.BEAM, new ByteBufferBuilder(BUFFER_SIZE));
            fixed.put(WorkplaceRenderTypes.RIBBON, new ByteBufferBuilder(BUFFER_SIZE));
            fixed.put(WorkplaceRenderTypes.GLYPH, new ByteBufferBuilder(BUFFER_SIZE));
            this.buffers = MultiBufferSource.immediateWithBuffers(fixed, new ByteBufferBuilder(BUFFER_SIZE));
        }
        return this.buffers;
    }

    private void spawnSparkles(ClientLevel level, Player player) {
        UUID golemId = this.golemFade.key();
        if (golemId == null || this.golemFade.alpha(1.0F) < 1.0F || level.getGameTime() % SPARKLE_INTERVAL_TICKS != 0) {
            return;
        }

        GolemEntity golem = findGolem(level, golemId);
        if (golem == null || golem.distanceToSqr(player) > SPARKLE_RANGE_SQR) {
            return;
        }

        int below = golem.getJob().map(GolemJob::below).orElse(WorkFootprint.DEFAULT_BELOW);
        int above = golem.getJob().map(GolemJob::above).orElse(WorkFootprint.DEFAULT_ABOVE);
        ClientGolemWorkplaceCache.latest(golem.getId()).ifPresent(entry -> WorkplaceSparkles.spawn(level, entry.workplace().zones(below, above)));
    }

    private void renderGolem(Minecraft minecraft, WorkplaceGeometry geometry, ClientLevel level, @Nullable UUID golemId, float alpha, float partialTick, double time) {
        GolemEntity golem = findGolem(level, golemId);
        if (golem == null) {
            return;
        }

        double range = minecraft.options.getEffectiveRenderDistance() * 16.0 + 16.0;
        double rangeSqr = range * range;
        Vec3 camera = geometry.camera();
        Vec3 feet = golem.getPosition(partialTick);

        if (!WorkplaceGeometry.isWithin(feet, camera, rangeSqr)) {
            return;
        }

        SelectedGolemRenderer.render(geometry, feet, golem.getBbWidth(), alpha, time);

        int below = golem.getJob().map(GolemJob::below).orElse(WorkFootprint.DEFAULT_BELOW);
        int above = golem.getJob().map(GolemJob::above).orElse(WorkFootprint.DEFAULT_ABOVE);

        ClientGolemWorkplaceCache.latest(golem.getId()).ifPresent(entry -> {
            entry.anchor().ifPresent(anchor -> AnchorRenderer.render(geometry, anchor.pos(), alpha, time));

            @Nullable
            Targeted highlight = this.targeted != null && this.targeted.golemId() == golem.getId() ? this.targeted : null;

            for (WorkFootprint footprint : entry.workplace().footprints()) {
                WorkZone zone = footprint.zone(below, above);
                if (WorkplaceGeometry.isWithin(zone.aabb().getCenter(), camera, rangeSqr)) {
                    ZoneRenderer.render(geometry, zone, alpha, time);
                }

                if (highlight != null && highlight.hit().footprint().equals(footprint)) {
                    geometry.boxEdges(zone.aabb().inflate(0.01), WorkplaceGeometry.argb(HIGHLIGHT_COLOR, alpha * WorkplaceGeometry.pulse(time, 0.25, 0.4F)));
                }

                for (CarvedGlyph glyph : footprint.glyphs()) {
                    if (!WorkplaceGeometry.isWithin(Vec3.atCenterOf(glyph.pos()), camera, rangeSqr)) {
                        continue;
                    }

                    if (highlight != null && highlight.hit().glyph().equals(glyph)) {
                        ZoneRenderer.renderGlyph(geometry, glyph, HIGHLIGHT_COLOR, HIGHLIGHT_GLYPH_SIZE, alpha, time);
                    } else {
                        ZoneRenderer.renderGlyph(geometry, glyph, ZoneRenderer.COLOR, alpha, time);
                    }
                }
            }

            for (ContainerLink link : entry.workplace().links()) {
                if (WorkplaceGeometry.isWithin(Vec3.atCenterOf(link.pos()), camera, rangeSqr)) {
                    LinkRenderer.render(geometry, link, alpha, time);
                }
            }

            entry.currentTarget().ifPresent(target -> TargetBeamRenderer.render(geometry,
                    feet.add(0.0, golem.getBbHeight() / 2.0, 0.0), target, alpha, time));
        });
    }

    private static ItemStack heldStylus(Player player) {
        if (player.getMainHandItem().getItem() instanceof StylusItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof StylusItem) {
            return player.getOffhandItem();
        }
        return null;
    }

    @Nullable
    private static PendingGlyph pendingGlyph(ItemStack stylus, ClientLevel level) {
        PendingGlyph glyph = stylus.get(DataComponentRegistry.STYLUS_PENDING_GLYPH.get());
        return glyph != null && glyph.pos().dimension().equals(level.dimension()) ? glyph : null;
    }

    @Nullable
    private static BlockPos hoveredBlock(Minecraft minecraft) {
        return minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : null;
    }

    /**
     * The glyph of the selected golem under the crosshair, while sneaking with a stylus. Blocks in front of the glyph win,
     * matching what the player sees, and the reach is the same as for the glyph render range.
     */
    @Nullable
    private static Targeted targetedGlyph(Minecraft minecraft) {
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null || !player.isShiftKeyDown()) {
            return null;
        }

        ItemStack stylus = heldStylus(player);
        UUID selected = stylus == null ? null : stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
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

    private static double blockDistance(ClientLevel level, Player player, Vec3 from, Vec3 to) {
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS ? Double.POSITIVE_INFINITY : from.distanceTo(hit.getLocation());
    }

    /**
     * @param golemId the entity id of the golem the glyph belongs to
     * @param hit     the targeted glyph
     */
    private record Targeted(int golemId, GlyphHit hit) {
    }

    @Nullable
    private static GolemEntity findGolem(ClientLevel level, @Nullable UUID selected) {
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
