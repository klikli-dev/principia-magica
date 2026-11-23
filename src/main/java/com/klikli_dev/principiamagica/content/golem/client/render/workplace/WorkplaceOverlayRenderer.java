// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.client.render.workplace.WorkplaceTargeting.Targeted;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.PendingGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.UUID;

/**
 * Draws the workplace of the golem selected on a held stylus: footprints, carved glyphs, container links, anchor, work
 * target, and the pending glyph with a preview of the footprint under the cursor. Elements fade in and out when the stylus
 * is equipped, unequipped or the selection changes.
 * <p>
 * The glyph under the crosshair is highlighted, even from a distance. Input handling lives in {@link WorkplaceInput}.
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

        ItemStack stylus = player == null || level == null ? null : WorkplaceTargeting.heldStylus(player);
        UUID selected = stylus == null ? null : stylus.get(DataComponentRegistry.STYLUS_SELECTION.get());
        PendingGlyph corner = stylus == null ? null : WorkplaceTargeting.pendingGlyph(stylus, level);

        this.golemFade.tick(selected);
        this.cornerFade.tick(corner);
        this.pending.tick(corner, WorkplaceTargeting.hoveredHit(minecraft));
        this.targeted = WorkplaceTargeting.targetedGlyph(minecraft);

        if (player != null && level != null) {
            this.spawnSparkles(level, player);
        }
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
            this.pending.render(geometry, corner, this.cornerFade.alpha(partialTick), partialTick, time);
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

        GolemEntity golem = WorkplaceTargeting.findGolem(level, golemId);
        if (golem == null || golem.distanceToSqr(player) > SPARKLE_RANGE_SQR) {
            return;
        }

        int below = golem.getJob().map(GolemJob::below).orElse(WorkFootprint.DEFAULT_BELOW);
        int above = golem.getJob().map(GolemJob::above).orElse(WorkFootprint.DEFAULT_ABOVE);
        ClientGolemWorkplaceCache.latest(golem.getId()).ifPresent(entry -> WorkplaceSparkles.spawn(level, entry.workplace().zones(below, above)));
    }

    private void renderGolem(Minecraft minecraft, WorkplaceGeometry geometry, ClientLevel level, @Nullable UUID golemId, float alpha, float partialTick, double time) {
        GolemEntity golem = WorkplaceTargeting.findGolem(level, golemId);
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
                AABB box = footprint.zone(0, 0).aabb();
                if (WorkplaceGeometry.isWithin(box.getCenter(), camera, rangeSqr)) {
                    ZoneRenderer.render(geometry, footprint, below, above, alpha, time);
                }

                if (highlight != null && highlight.hit().footprint().equals(footprint)) {
                    geometry.boxEdges(box.inflate(0.01), WorkplaceGeometry.argb(HIGHLIGHT_COLOR, alpha * WorkplaceGeometry.pulse(time, 0.25, 0.4F)));
                }

                for (CarvedGlyph glyph : footprint.glyphs()) {
                    if (!WorkplaceGeometry.isWithin(Vec3.atCenterOf(glyph.corner()), camera, rangeSqr)) {
                        continue;
                    }

                    if (highlight != null && highlight.hit().glyph().isSameGlyph(glyph)) {
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
}
