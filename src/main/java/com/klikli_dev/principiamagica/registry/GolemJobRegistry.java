// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.GolemJob;
import com.klikli_dev.principiamagica.content.golem.job.farming.FarmingJob;
import com.klikli_dev.principiamagica.content.golem.job.lumberjack.LumberjackJob;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GolemJobRegistry {
    public static final DeferredRegister<GolemJob> GOLEM_JOBS = DeferredRegister.create(PrincipiaMagicaRegistries.Keys.GOLEM_JOB, PrincipiaMagica.MODID);

    public static final DeferredHolder<GolemJob, FarmingJob> FARMING = GOLEM_JOBS.register("farming", FarmingJob::new);

    public static final DeferredHolder<GolemJob, LumberjackJob> LUMBERJACK = GOLEM_JOBS.register("lumberjack", LumberjackJob::new);
}
