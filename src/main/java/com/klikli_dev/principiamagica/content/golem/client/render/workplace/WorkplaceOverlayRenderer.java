// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.content.golem.client.ClientGolemWorkplaceCache;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.GolemBatonItem;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.UUID;

/**
 * Draws the workplace of the golem selected on a held baton: zones, container links, anchor, work target, and the
 * pending zone corner with a preview of the zone under the cursor. Elements fade in and out when the baton is equipped,
 * unequipped or the selection changes.
 */
public class WorkplaceOverlayRenderer {

    private static final long SPARKLE_INTERVAL_TICKS = 4;
    private static final double SPARKLE_RANGE_SQR = 32.0 * 32.0;
    private static final int BUFFER_SIZE = 1536;

    private final FadingElementTracker<UUID> golemFade = new FadingElementTracker<>();
    private final FadingElementTracker<GlobalPos> cornerFade = new FadingElementTracker<>();
    private final PendingZoneRenderer pending = new PendingZoneRenderer();
    private MultiBufferSource.@Nullable BufferSource buffers;

    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;

        ItemStack baton = player == null || level == null ? null : heldBaton(player);
        UUID selected = baton == null ? null : baton.get(DataComponentRegistry.BATON_SELECTION.get());
        GlobalPos corner = baton == null ? null : pendingCorner(baton, level);

        this.golemFade.tick(selected);
        this.cornerFade.tick(corner);
        this.pending.tick(corner == null ? null : corner.pos(), hoveredBlock(minecraft));

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
        GlobalPos corner = this.cornerFade.isVisible(partialTick) ? this.cornerFade.key() : null;
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
            this.pending.render(geometry, corner.pos(), this.cornerFade.alpha(partialTick), partialTick, time);
        }

        // Draws in the insertion order of the fixed buffers: fill, beam, ribbon.
        buffers.endBatch();
    }

    /**
     * Buffer source with a fixed buffer per workplace render type. The geometry holds vertex consumers for all three
     * render types at once, which the shared vanilla buffer source does not support: requesting a second non-fixed
     * render type ends the batch of the previous one.
     */
    private MultiBufferSource.BufferSource buffers() {
        if (this.buffers == null) {
            SequencedMap<RenderType, ByteBufferBuilder> fixed = new LinkedHashMap<>();
            fixed.put(WorkplaceRenderTypes.FILL, new ByteBufferBuilder(BUFFER_SIZE));
            fixed.put(WorkplaceRenderTypes.BEAM, new ByteBufferBuilder(BUFFER_SIZE));
            fixed.put(WorkplaceRenderTypes.RIBBON, new ByteBufferBuilder(BUFFER_SIZE));
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

        ClientGolemWorkplaceCache.latest(golem.getId()).ifPresent(entry -> WorkplaceSparkles.spawn(level, entry.workplace().zones()));
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

        ClientGolemWorkplaceCache.latest(golem.getId()).ifPresent(entry -> {
            entry.anchor().ifPresent(anchor -> AnchorRenderer.render(geometry, anchor.pos(), alpha, time));

            for (WorkZone zone : entry.workplace().zones()) {
                if (WorkplaceGeometry.isWithin(zone.aabb().getCenter(), camera, rangeSqr)) {
                    ZoneRenderer.render(geometry, zone, alpha, time);
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

    private static ItemStack heldBaton(Player player) {
        if (player.getMainHandItem().getItem() instanceof GolemBatonItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof GolemBatonItem) {
            return player.getOffhandItem();
        }
        return null;
    }

    @Nullable
    private static GlobalPos pendingCorner(ItemStack baton, ClientLevel level) {
        GlobalPos corner = baton.get(DataComponentRegistry.BATON_PENDING_CORNER.get());
        return corner != null && corner.dimension().equals(level.dimension()) ? corner : null;
    }

    @Nullable
    private static BlockPos hoveredBlock(Minecraft minecraft) {
        return minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : null;
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
