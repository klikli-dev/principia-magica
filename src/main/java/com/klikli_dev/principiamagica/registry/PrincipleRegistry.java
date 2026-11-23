// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.principle.Principle;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PrincipleRegistry {
    public static final DeferredRegister<Principle> PRINCIPLES = DeferredRegister.create(PrincipiaMagicaRegistries.Keys.PRINCIPLE, PrincipiaMagica.MODID);

    public static final DeferredHolder<Principle, Principle> SILT = primal("silt", 0xAA613D, "gleam");
    public static final DeferredHolder<Principle, Principle> GLEAM = primal("gleam", 0xEEC348, "silt");
    public static final DeferredHolder<Principle, Principle> SAP = primal("sap", 0x59A268, "ash");
    public static final DeferredHolder<Principle, Principle> ASH = primal("ash", 0x838991, "sap");

    public static final DeferredHolder<Principle, Principle> LOAM = compound("loam", 0x747D4E, "silt", "sap");
    public static final DeferredHolder<Principle, Principle> SHARD = compound("shard", 0xBA5747, "silt", "ash");
    public static final DeferredHolder<Principle, Principle> MYRRH = compound("myrrh", 0xD18034, "gleam", "sap");
    public static final DeferredHolder<Principle, Principle> GLAZE = compound("glaze", 0x487FB5, "gleam", "ash");

    public static final DeferredHolder<Principle, Principle> MIRE = compound("mire", 0x4C5C37, "silt", "sap");
    public static final DeferredHolder<Principle, Principle> BITUMEN = compound("bitumen", 0x35313D, "silt", "ash");
    public static final DeferredHolder<Principle, Principle> UNGUENT = compound("unguent", 0xB49A44, "gleam", "sap");
    public static final DeferredHolder<Principle, Principle> KOHL = compound("kohl", 0x4D5165, "gleam", "ash");

    public static final DeferredHolder<Principle, Principle> ALABASTER = compound("alabaster", 0xE8DEC2, "silt", "gleam");
    public static final DeferredHolder<Principle, Principle> MARROW = compound("marrow", 0xC29A8E, "sap", "ash");
    public static final DeferredHolder<Principle, Principle> FAIENCE = compound("faience", 0x46B19E, "glaze", "gleam");
    public static final DeferredHolder<Principle, Principle> KYPHI = compound("kyphi", 0xC79958, "myrrh", "gleam");
    public static final DeferredHolder<Principle, Principle> STELE = compound("stele", 0x9D9991, "shard", "silt");
    public static final DeferredHolder<Principle, Principle> SINEW = compound("sinew", 0xB0736A, "unguent", "sap");
    public static final DeferredHolder<Principle, Principle> BRINE = compound("brine", 0x3C7682, "bitumen", "ash");
    public static final DeferredHolder<Principle, Principle> VEIL = compound("veil", 0x242D4B, "kohl", "ash");

    private static DeferredHolder<Principle, Principle> primal(String name, int color, String opposite) {
        return PRINCIPLES.register(name, () -> Principle.primal(color, ref(opposite)));
    }

    private static DeferredHolder<Principle, Principle> compound(String name, int color, String first, String second) {
        return PRINCIPLES.register(name, () -> Principle.compound(color, ref(first), ref(second)));
    }

    private static DeferredHolder<Principle, Principle> ref(String name) {
        return DeferredHolder.create(PrincipiaMagicaRegistries.Keys.PRINCIPLE, PrincipiaMagica.loc(name));
    }
}
