// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.WorkJob;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingJob;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.LumberjackJob;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GolemJobRegistry {
    public static final DeferredRegister<GolemJob> GOLEM_JOBS = DeferredRegister.create(PrincipiaMagicaRegistries.Keys.GOLEM_JOB, PrincipiaMagica.MODID);

    public static final DeferredHolder<GolemJob, WorkJob> FARMING = GOLEM_JOBS.register("farming", FarmingJob::create);

    public static final DeferredHolder<GolemJob, WorkJob> LUMBERJACK = GOLEM_JOBS.register("lumberjack", LumberjackJob::create);
}
