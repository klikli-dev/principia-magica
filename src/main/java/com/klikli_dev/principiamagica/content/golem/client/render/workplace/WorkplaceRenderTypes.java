// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class WorkplaceRenderTypes {

    private static final Identifier RIBBON_VERTEX_SHADER = PrincipiaMagica.loc("core/workplace_ribbon");
    private static final Identifier RIBBON_FRAGMENT_SHADER = PrincipiaMagica.loc("core/workplace_ribbon");
    private static final Identifier FILL_FRAGMENT_SHADER = PrincipiaMagica.loc("core/workplace_fill");

    /**
     * Glowing camera-facing ribbons (zone edges, link frames, rune rings). Additive, depth tested, no depth write.
     */
    public static final RenderPipeline RIBBON_PIPELINE = pipeline("ribbon", RIBBON_FRAGMENT_SHADER, BlendFunction.ADDITIVE, false);

    /**
     * Same ribbon shader with dashes flowing along the ribbon, used for the work target beam.
     */
    public static final RenderPipeline BEAM_PIPELINE = pipeline("beam", RIBBON_FRAGMENT_SHADER, BlendFunction.ADDITIVE, true);

    /**
     * Very faint, static translucent zone faces.
     */
    public static final RenderPipeline FILL_PIPELINE = pipeline("fill", FILL_FRAGMENT_SHADER, BlendFunction.TRANSLUCENT, false);

    public static final RenderType RIBBON = create("principiamagica_workplace_ribbon", RIBBON_PIPELINE);
    public static final RenderType BEAM = create("principiamagica_workplace_beam", BEAM_PIPELINE);
    public static final RenderType FILL = create("principiamagica_workplace_fill", FILL_PIPELINE);

    private WorkplaceRenderTypes() {
    }

    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(RIBBON_PIPELINE);
        event.registerPipeline(BEAM_PIPELINE);
        event.registerPipeline(FILL_PIPELINE);
    }

    private static RenderPipeline pipeline(String name, Identifier fragmentShader, BlendFunction blend, boolean dashed) {
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(PrincipiaMagica.loc("pipeline/workplace_" + name))
                .withVertexShader(RIBBON_VERTEX_SHADER)
                .withFragmentShader(fragmentShader)
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("Fog", UniformType.UNIFORM_BUFFER)
                .withUniform("Globals", UniformType.UNIFORM_BUFFER)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withColorTargetState(new ColorTargetState(blend))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false));

        if (dashed) {
            builder.withShaderDefine("DASHED");
        }

        return builder.build();
    }

    private static RenderType create(String name, RenderPipeline pipeline) {
        RenderSetup setup = RenderSetup.builder(pipeline)
                .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .createRenderSetup();
        return RenderType.create(name, setup);
    }
}
