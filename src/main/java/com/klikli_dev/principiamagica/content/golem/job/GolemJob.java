// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

import com.google.common.collect.ImmutableList;
import com.klikli_dev.principiamagica.content.golem.ai.JobBehaviours;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public interface GolemJob {

    String getDescriptionId();

    default Component getName() {
        return Component.translatable(this.getDescriptionId());
    }

    /**
     * Options this job declares. Shown in the golem GUI and stored per workplace.
     */
    default List<JobOption> options() {
        return List.of();
    }

    /**
     * How far below the footprint's working level the job works, e.g. the soil under a crop.
     */
    int below();

    /**
     * How far above the footprint's working level the job works, e.g. crops and tall plants.
     */
    int above();

    /**
     * Tag the golem's main hand tool must match for the job to work. Empty if no tool is needed.
     */
    default Optional<TagKey<Item>> requiredTool() {
        return Optional.empty();
    }

    /**
     * How many of this item the golem keeps in its inventory. Items beyond this are deposited.
     * Items with a value above zero are also restocked from SUPPLY containers when lacking.
     */
    default int keepAmount(GolemEntity golem, ItemStack stack) {
        return 0;
    }

    WorkTargetFinder targetFinder();

    /**
     * Job-specific behaviours, which run at the job priority after the shared deposit/restock behaviours.
     */
    List<BehaviorControl<? super GolemEntity>> createJobBehaviours();

    default ImmutableList<Pair<Integer, BehaviorControl<? super GolemEntity>>> createWorkBehaviours() {
        return JobBehaviours.compose(this);
    }
}
