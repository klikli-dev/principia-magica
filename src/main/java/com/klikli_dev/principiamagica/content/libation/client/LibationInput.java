// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.item.ClayPhialItem;
import com.klikli_dev.principiamagica.content.libation.item.NatronSpongeItem;
import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageSelectPrinciple;
import com.klikli_dev.principiamagica.registry.FluidRegistry;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles Crouch + Scroll when aiming at a principle container with an empty Clay Phial or Natron Sponge
 * to choose which principle to draw or blot.
 */
public final class LibationInput {

    private static @Nullable BlockPos selectedPos;
    private static @Nullable Holder<Principle> selectedPrinciple;

    private LibationInput() {
    }

    public static boolean isSelectingTool(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        return isSelectableStack(player.getMainHandItem()) || (player.getMainHandItem().isEmpty() && isSelectableStack(player.getOffhandItem()));
    }

    private static boolean isSelectableStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return (stack.getItem() instanceof ClayPhialItem && ClayPhialItem.isEmpty(stack))
                || stack.getItem() instanceof NatronSpongeItem;
    }

    /**
     * @return the currently highlighted principle for {@code pos} among {@code stacks}, or the first stack if none is tracked yet.
     * Whenever the highlighted principle changes, the selection is synced to the server.
     */
    public static @Nullable Holder<Principle> selectedFor(BlockPos pos, List<PrincipleStack> stacks) {
        if (stacks.isEmpty()) {
            return null;
        }
        if (pos.equals(selectedPos) && selectedPrinciple != null) {
            for (PrincipleStack stack : stacks) {
                if (stack.principle().value().equals(selectedPrinciple.value())) {
                    return stack.principle();
                }
            }
        }
        select(pos, stacks.getFirst().principle());
        return selectedPrinciple;
    }

    /**
     * Sets the tracked selection and tells the server, but only if the principle differs from the one the server has.
     * The server selection is per player, so moving to another container with the same principle needs no packet.
     */
    private static void select(BlockPos pos, Holder<Principle> principle) {
        boolean changed = selectedPrinciple == null || !selectedPrinciple.value().equals(principle.value());
        selectedPos = pos;
        selectedPrinciple = principle;
        if (changed) {
            Networking.sendToServer(new MessageSelectPrinciple(Optional.of(principle)));
        }
    }

    /**
     * Syncs the selection right before a use click, so the server never acts on a selection the HUD has not shown,
     * e.g. while the HUD is hidden with F1.
     */
    public static void onInteractionKeyMapping(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null || !isSelectingTool(player)) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, hit.getDirection());
        if (handler == null) {
            return;
        }
        List<PrincipleStack> stacks = snapshotPrinciples(handler);
        if (!stacks.isEmpty()) {
            selectedFor(pos, stacks);
        }
    }

    public static void onMouseScrolling(InputEvent.MouseScrollingEvent event) {
        if (event.getScrollDeltaY() == 0.0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null || !player.isShiftKeyDown() || !isSelectingTool(player)) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, hit.getDirection());
        if (handler == null) {
            return;
        }

        List<PrincipleStack> stacks = snapshotPrinciples(handler);
        if (stacks.size() <= 1) {
            return;
        }

        event.setCanceled(true);
        Holder<Principle> current = selectedFor(pos, stacks);
        int currentIndex = 0;
        for (int i = 0; i < stacks.size(); i++) {
            if (current != null && stacks.get(i).principle().value().equals(current.value())) {
                currentIndex = i;
                break;
            }
        }
        int step = event.getScrollDeltaY() > 0 ? -1 : 1;
        int nextIndex = Math.floorMod(currentIndex + step, stacks.size());
        select(pos, stacks.get(nextIndex).principle());
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundRegistry.PRINCIPLE_SELECT.get(), 1.0F, 1.0F));
    }

    private static List<PrincipleStack> snapshotPrinciples(ResourceHandler<FluidResource> handler) {
        List<PrincipleStack> stacks = new ArrayList<>();
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            FluidResource resource = handler.getResource(i);
            int amount = handler.getAmountAsInt(i);
            if (FluidRegistry.isPrincipleFluid(resource) && amount > 0) {
                Holder<Principle> principle = FluidRegistry.principleFor(resource.getFluid());
                if (principle != null) {
                    stacks.add(new PrincipleStack(principle, amount));
                }
            }
        }
        return stacks;
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        selectedPos = null;
        selectedPrinciple = null;
    }
}
