#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// texCoord0.x: distance along the ribbon in blocks, texCoord0.y: 0..1 across the ribbon.
// GameTime wraps every 24000 ticks, so all flow rates are chosen such that one wrap is a whole number of cycles.
void main() {
    float ticks = GameTime * 24000.0;

    float edge = 1.0 - abs(texCoord0.y * 2.0 - 1.0);
    float core = edge * edge * edge;
    float halo = edge * edge * 0.35;

    float pulse = smoothstep(0.75, 1.0, fract(texCoord0.x * 0.25 - ticks * 0.025));
    float shimmer = 0.85 + 0.15 * sin(6.2831853 * fract(ticks * 0.0375) + texCoord0.x * 2.0);

    vec3 rgb = mix(vertexColor.rgb, vec3(1.0), core * 0.6) * (1.0 + pulse);
    float alpha = (core * 0.9 + halo + pulse * edge * 0.8) * vertexColor.a * shimmer;

#ifdef DASHED
    float dash = step(fract(texCoord0.x * 0.5 - ticks * 0.05), 0.5);
    alpha *= mix(0.2, 1.0, dash);
#endif

    vec4 color = vec4(rgb, alpha) * ColorModulator;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
