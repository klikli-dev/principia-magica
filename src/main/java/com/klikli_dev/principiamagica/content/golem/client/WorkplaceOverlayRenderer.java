// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.GolemBatonItem;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkZone;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Draws the workplace of the golem selected on a held baton: zones, container links, anchor, work target, and the
 * pending zone corner with a preview of the zone under the cursor.
 */
public class WorkplaceOverlayRenderer {

    private static final int ZONE_COLOR = 0xFF55FFFF;
    private static final int SUPPLY_COLOR = 0xFF3366FF;
    private static final int DEPOSIT_COLOR = 0xFFFF8800;
    private static final int BOTH_COLOR = 0xFF33FF55;
    private static final int ANCHOR_COLOR = 0xFFFFFFFF;
    private static final int GOLEM_COLOR = 0xFFFFFFFF;
    private static final int TARGET_COLOR = 0xFFFFFF00;
    private static final int PENDING_COLOR = 0xFFFF55FF;

    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentParticles event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        ItemStack baton = heldBaton(player);
        if (baton == null) {
            return;
        }

        Optional<GolemEntity> golem = findGolem(minecraft, baton.get(DataComponentRegistry.BATON_SELECTION.get()));
        GlobalPos corner = baton.get(DataComponentRegistry.BATON_PENDING_CORNER.get());
        if (golem.isEmpty() && corner == null) {
            return;
        }

        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffer.getBuffer(RenderTypes.lines());
        float lineWidth = minecraft.getWindow().getAppropriateLineWidth();

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        PoseStack.Pose pose = poseStack.last();

        golem.ifPresent(entity -> {
            drawBox(consumer, pose, entity.getBoundingBox().inflate(0.02), GOLEM_COLOR, lineWidth);
            ClientGolemWorkplaceCache.get(entity.getId()).ifPresent(entry -> drawWorkplace(consumer, pose, entity, entry, lineWidth));
        });

        if (corner != null && corner.dimension().equals(minecraft.level.dimension())) {
            drawBox(consumer, pose, new AABB(corner.pos()).inflate(0.02), PENDING_COLOR, lineWidth);
            if (minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
                WorkZone preview = WorkZone.of(corner.pos(), hit.getBlockPos());
                drawBox(consumer, pose, preview.aabb().inflate(0.01), PENDING_COLOR, lineWidth);
            }
        }

        poseStack.popPose();
        buffer.endBatch(RenderTypes.lines());
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

    private static Optional<GolemEntity> findGolem(Minecraft minecraft, UUID selected) {
        if (selected == null) {
            return Optional.empty();
        }

        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof GolemEntity golem && selected.equals(golem.getUUID())) {
                return Optional.of(golem);
            }
        }
        return Optional.empty();
    }

    private static void drawWorkplace(VertexConsumer consumer, PoseStack.Pose pose, GolemEntity golem, ClientGolemWorkplaceCache.Entry entry, float lineWidth) {
        entry.anchor().ifPresent(anchor -> drawBox(consumer, pose, new AABB(anchor.pos()).inflate(0.02), ANCHOR_COLOR, lineWidth));

        for (WorkZone zone : entry.workplace().zones()) {
            drawBox(consumer, pose, zone.aabb().inflate(0.01), ZONE_COLOR, lineWidth);
        }

        for (ContainerLink link : entry.workplace().links()) {
            drawBox(consumer, pose, new AABB(link.pos()).inflate(0.02), roleColor(link.role()), lineWidth);
        }

        entry.currentTarget().ifPresent(target -> drawLine(consumer, pose,
                golem.position().add(0, golem.getBbHeight() / 2.0, 0), Vec3.atCenterOf(target), TARGET_COLOR, lineWidth));
    }

    private static int roleColor(ContainerRole role) {
        return switch (role) {
            case SUPPLY -> SUPPLY_COLOR;
            case DEPOSIT -> DEPOSIT_COLOR;
            case BOTH -> BOTH_COLOR;
        };
    }

    private static void drawBox(VertexConsumer consumer, PoseStack.Pose pose, AABB box, int color, float lineWidth) {
        Vec3[] corners = new Vec3[8];
        for (int i = 0; i < 8; i++) {
            corners[i] = new Vec3(
                    (i & 1) == 0 ? box.minX : box.maxX,
                    (i & 2) == 0 ? box.minY : box.maxY,
                    (i & 4) == 0 ? box.minZ : box.maxZ
            );
        }

        for (int i = 0; i < 8; i++) {
            for (int bit : new int[]{1, 2, 4}) {
                if ((i & bit) == 0) {
                    drawLine(consumer, pose, corners[i], corners[i | bit], color, lineWidth);
                }
            }
        }
    }

    private static void drawLine(VertexConsumer consumer, PoseStack.Pose pose, Vec3 from, Vec3 to, int color, float lineWidth) {
        Vec3 normal = to.subtract(from).normalize();
        Vec3 reverse = normal.scale(-1);

        consumer.addVertex(pose, (float) from.x, (float) from.y, (float) from.z)
                .setColor(color)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z)
                .setLineWidth(lineWidth);

        consumer.addVertex(pose, (float) to.x, (float) to.y, (float) to.z)
                .setColor(color)
                .setNormal(pose, (float) reverse.x, (float) reverse.y, (float) reverse.z)
                .setLineWidth(lineWidth);
    }
}
