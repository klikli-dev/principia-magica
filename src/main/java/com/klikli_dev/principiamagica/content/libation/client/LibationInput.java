// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.content.libation.item.ClayPhialItem;
import com.klikli_dev.principiamagica.content.libation.item.NatronSpongeItem;
import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleResource;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.PrincipleTransferUtil;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageSelectPrinciple;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import org.jspecify.annotations.Nullable;

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
        selectedPos = pos;
        selectedPrinciple = stacks.getFirst().principle();
        return selectedPrinciple;
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
        ResourceHandler<PrincipleResource> handler = level.getCapability(PrincipiaMagicaCapabilities.Principle.BLOCK, pos, hit.getDirection());
        if (handler == null) {
            return;
        }

        List<PrincipleStack> stacks = PrincipleTransferUtil.snapshot(handler).stacks();
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
        selectedPos = pos;
        selectedPrinciple = stacks.get(nextIndex).principle();
        Networking.sendToServer(new MessageSelectPrinciple(Optional.of(selectedPrinciple)));
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        selectedPos = null;
        selectedPrinciple = null;
    }
}
