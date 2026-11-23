// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleFluid;
import com.klikli_dev.principiamagica.content.principle.PrincipleFluidType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FluidRegistry {

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, PrincipiaMagica.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, PrincipiaMagica.MODID);

    private static final Map<ResourceKey<Principle>, DeferredHolder<Fluid, PrincipleFluid>> BY_PRINCIPLE = new LinkedHashMap<>();
    private static final List<DeferredHolder<Fluid, PrincipleFluid>> ALL = new ArrayList<>();

    public static final DeferredHolder<Fluid, PrincipleFluid> SILT = registerPrinciple("silt", PrincipleRegistry.SILT);
    public static final DeferredHolder<Fluid, PrincipleFluid> GLEAM = registerPrinciple("gleam", PrincipleRegistry.GLEAM);
    public static final DeferredHolder<Fluid, PrincipleFluid> SAP = registerPrinciple("sap", PrincipleRegistry.SAP);
    public static final DeferredHolder<Fluid, PrincipleFluid> ASH = registerPrinciple("ash", PrincipleRegistry.ASH);

    public static final DeferredHolder<Fluid, PrincipleFluid> LOAM = registerPrinciple("loam", PrincipleRegistry.LOAM);
    public static final DeferredHolder<Fluid, PrincipleFluid> SHARD = registerPrinciple("shard", PrincipleRegistry.SHARD);
    public static final DeferredHolder<Fluid, PrincipleFluid> MYRRH = registerPrinciple("myrrh", PrincipleRegistry.MYRRH);
    public static final DeferredHolder<Fluid, PrincipleFluid> GLAZE = registerPrinciple("glaze", PrincipleRegistry.GLAZE);

    public static final DeferredHolder<Fluid, PrincipleFluid> MIRE = registerPrinciple("mire", PrincipleRegistry.MIRE);
    public static final DeferredHolder<Fluid, PrincipleFluid> BITUMEN = registerPrinciple("bitumen", PrincipleRegistry.BITUMEN);
    public static final DeferredHolder<Fluid, PrincipleFluid> UNGUENT = registerPrinciple("unguent", PrincipleRegistry.UNGUENT);
    public static final DeferredHolder<Fluid, PrincipleFluid> KOHL = registerPrinciple("kohl", PrincipleRegistry.KOHL);

    public static final DeferredHolder<Fluid, PrincipleFluid> ALABASTER = registerPrinciple("alabaster", PrincipleRegistry.ALABASTER);
    public static final DeferredHolder<Fluid, PrincipleFluid> MARROW = registerPrinciple("marrow", PrincipleRegistry.MARROW);
    public static final DeferredHolder<Fluid, PrincipleFluid> FAIENCE = registerPrinciple("faience", PrincipleRegistry.FAIENCE);
    public static final DeferredHolder<Fluid, PrincipleFluid> KYPHI = registerPrinciple("kyphi", PrincipleRegistry.KYPHI);
    public static final DeferredHolder<Fluid, PrincipleFluid> STELE = registerPrinciple("stele", PrincipleRegistry.STELE);
    public static final DeferredHolder<Fluid, PrincipleFluid> SINEW = registerPrinciple("sinew", PrincipleRegistry.SINEW);
    public static final DeferredHolder<Fluid, PrincipleFluid> BRINE = registerPrinciple("brine", PrincipleRegistry.BRINE);
    public static final DeferredHolder<Fluid, PrincipleFluid> VEIL = registerPrinciple("veil", PrincipleRegistry.VEIL);

    private static DeferredHolder<Fluid, PrincipleFluid> registerPrinciple(String name, DeferredHolder<Principle, Principle> principle) {
        DeferredHolder<FluidType, PrincipleFluidType> type = FLUID_TYPES.register(name, () -> new PrincipleFluidType(
                principle,
                FluidType.Properties.create()
                        .descriptionId("principle." + PrincipiaMagica.MODID + "." + name)));
        DeferredHolder<Fluid, PrincipleFluid> fluid = FLUIDS.register(name, () -> new PrincipleFluid(type, principle));
        BY_PRINCIPLE.put(principle.getKey(), fluid);
        ALL.add(fluid);
        return fluid;
    }

    public static PrincipleFluid fluidFor(Holder<Principle> principle) {
        ResourceKey<Principle> key = principle.unwrapKey().orElseThrow(
                () -> new IllegalArgumentException("Principle holder has no key: " + principle));
        DeferredHolder<Fluid, PrincipleFluid> holder = BY_PRINCIPLE.get(key);
        if (holder == null) {
            throw new IllegalArgumentException("No fluid registered for principle: " + key);
        }
        return holder.get();
    }

    public static FluidResource resourceFor(Holder<Principle> principle) {
        return FluidResource.of(fluidFor(principle));
    }

    public static @Nullable Holder<Principle> principleFor(Fluid fluid) {
        return fluid instanceof PrincipleFluid principleFluid ? principleFluid.principle() : null;
    }

    public static boolean isPrincipleFluid(FluidResource resource) {
        return !resource.isEmpty() && resource.isComponentsPatchEmpty() && resource.getFluid() instanceof PrincipleFluid;
    }

    public static Collection<DeferredHolder<Fluid, PrincipleFluid>> all() {
        return Collections.unmodifiableList(ALL);
    }
}
