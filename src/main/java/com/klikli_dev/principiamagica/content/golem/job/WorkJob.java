// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * A job described by data: name, working zone, options, required tool, which items it keeps and how it finds and performs
 * its work. Jobs such as farming or lumberjacking are defined as instances of this record, so a new job only needs its
 * targets and behaviours.
 *
 * @param name            the job's name, used in the registry key's translation and as the description id suffix
 * @param below           how far below the footprint's working level the job works
 * @param above           how far above the footprint's working level the job works
 * @param requiredTool    the tag the main hand tool must match for the job to work, empty if no tool is needed
 * @param toolTag        the tag of items the golem accepts as its tool, see {@link GolemJob#toolTag()}
 * @param options         the options this job declares, shown in the GUI and stored per workplace
 * @param keeps           whether the golem keeps items of this stack for the workplace, see {@link GolemJob#keepAmount}
 * @param targetFinder    finds the blocks the job works on, see {@link WorkTargetFinder}
 * @param behaviours      creates the job's behaviours, called once per golem brain since behaviours hold per-golem state
 */
public record WorkJob(
        String name,
        int below,
        int above,
        Optional<TagKey<Item>> requiredTool,
        Optional<TagKey<Item>> toolTag,
        List<JobOption> options,
        BiPredicate<Workplace, ItemStack> keeps,
        WorkTargetFinder targetFinder,
        Supplier<List<BehaviorControl<? super GolemEntity>>> behaviours
) implements GolemJob {

    /**
     * How many items of a kept stack the golem holds on to, the rest is deposited.
     */
    public static final int KEEP_AMOUNT = 64;

    @Override
    public String getDescriptionId() {
        return "golem_job." + PrincipiaMagica.MODID + "." + this.name;
    }

    @Override
    public int keepAmount(GolemEntity golem, ItemStack stack) {
        if (!(golem.level() instanceof ServerLevel level)) {
            return 0;
        }

        return golem.getWorkplace(level)
                .filter(workplace -> this.keeps.test(workplace, stack))
                .map(workplace -> KEEP_AMOUNT)
                .orElse(0);
    }

    @Override
    public List<BehaviorControl<? super GolemEntity>> createJobBehaviours() {
        return this.behaviours.get();
    }
}
