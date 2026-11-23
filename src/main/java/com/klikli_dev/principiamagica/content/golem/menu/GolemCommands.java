// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.item.StylusActions;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.content.golem.job.JobOption;
import com.klikli_dev.principiamagica.content.golem.workplace.CarvedGlyph;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.content.golem.workplace.GlyphTargeting;
import com.klikli_dev.principiamagica.content.golem.workplace.WorkFootprint;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.MemoryModuleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Server-side edits requested by the golem GUI. Pure workplace edits are static so they can be unit tested.
 */
public final class GolemCommands {

    public static final double MAX_DISTANCE = 8.0;

    private GolemCommands() {
    }

    /**
     * @return the golem with the given id if it is alive, owned by the player and within {@link #MAX_DISTANCE}
     */
    public static Optional<GolemEntity> findOwnedGolem(ServerPlayer player, int entityId) {
        Entity entity = player.level().getEntity(entityId);
        if (!(entity instanceof GolemEntity golem) || !golem.isAlive()) {
            return Optional.empty();
        }

        if (!player.getUUID().equals(golem.getOwnerUUID()) || golem.distanceToSqr(player) > MAX_DISTANCE * MAX_DISTANCE) {
            return Optional.empty();
        }

        return Optional.of(golem);
    }

    /**
     * Finds the glyph whose 3D corner is at cornerPos, if the player is in reach of it.
     *
     * @return the stored glyph with its lift, or empty if no footprint carries a corner there or the player is out of reach
     */
    public static Optional<CarvedGlyph> findReachableGlyph(ServerPlayer player, Workplace workplace, BlockPos cornerPos) {
        return workplace.findGlyphAtCorner(cornerPos)
                .filter(glyph -> GlyphTargeting.isInReach(player.getEyePosition(), glyph.corner()));
    }

    /**
     * Applies a workplace edit to the golem and re-syncs the workplace to the player, if the edit was valid.
     */
    public static void editWorkplace(ServerPlayer player, GolemEntity golem, Function<Workplace, Optional<Workplace>> edit) {
        ServerLevel level = (ServerLevel) golem.level();
        Workplace current = golem.getWorkplace(level).orElse(Workplace.EMPTY);
        edit.apply(current).ifPresent(updated -> {
            golem.updateWorkplace(ignored -> updated);
            golem.getBrain().eraseMemory(MemoryModuleTypeRegistry.RESTOCK_COOLDOWN.value());
            golem.getBrain().eraseMemory(MemoryModuleTypeRegistry.UNREACHABLE_TARGETS.value());
            StylusItem.syncWorkplace(player, golem);
        });
    }

    /**
     * @return the workplace with the option set, or empty if the key is not one of the job's options
     */
    public static Optional<Workplace> setJobOption(Workplace workplace, List<JobOption> options, String key, boolean value) {
        return options.stream()
                .filter(option -> option.key().equals(key))
                .findFirst()
                .map(option -> workplace.withOptions(workplace.options().with(option, value)));
    }

    /**
     * @return the workplace without the footprint at the index, or empty if the index is out of range
     */
    public static Optional<Workplace> removeFootprint(Workplace workplace, int index) {
        if (index < 0 || index >= workplace.footprints().size()) {
            return Optional.empty();
        }

        List<WorkFootprint> footprints = new ArrayList<>(workplace.footprints());
        footprints.remove(index);
        return Optional.of(workplace.withFootprints(footprints).markInitialized());
    }

    /**
     * @return the workplace with the footprint at the index moved up or down by delta, clamped so its corners stay in range, or empty if rejected
     */
    public static Optional<Workplace> shiftFootprint(Workplace workplace, int index, int delta) {
        return StylusActions.shiftFootprint(workplace, index, delta).toOptional();
    }

    /**
     * @return the workplace with the link at the index set to the role, or empty if the index is out of range
     */
    public static Optional<Workplace> setLinkRole(Workplace workplace, int index, ContainerRole role) {
        if (index < 0 || index >= workplace.links().size()) {
            return Optional.empty();
        }

        List<ContainerLink> links = new ArrayList<>(workplace.links());
        links.set(index, links.get(index).withRole(role));
        return Optional.of(workplace.withLinks(links).markInitialized());
    }

    /**
     * @return the workplace without any container links, or empty if it has none to clear
     */
    public static Optional<Workplace> clearLinks(Workplace workplace) {
        if (workplace.links().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(workplace.withLinks(List.of()).markInitialized());
    }

    /**
     * @return the workplace without the link at the index, or empty if the index is out of range
     */
    public static Optional<Workplace> removeLink(Workplace workplace, int index) {
        if (index < 0 || index >= workplace.links().size()) {
            return Optional.empty();
        }

        List<ContainerLink> links = new ArrayList<>(workplace.links());
        links.remove(index);
        return Optional.of(workplace.withLinks(links).markInitialized());
    }
}
