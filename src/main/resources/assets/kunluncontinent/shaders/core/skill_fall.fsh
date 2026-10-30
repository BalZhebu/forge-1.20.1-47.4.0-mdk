#version 150

// 天降巨物（天使 9「终焉救赎」）。
//
// 复用 skill_beam.vsh 的几何（Start / Dir / Side / Length / HalfWidth），
// 但不用它画"柱子"，而是画一个**巨大的钝头能量体**：
//   · 中心实心（不是空管）
//   · 两侧一条亮轮廓，说明它有体积
//   · 底部白热（撞击前的预兆）
//   · 顶部渐隐（伸进云层里，看不到头 → 显得很高）
//
// 下落是 Java 侧挪 Start 的 y 做的（FallEffect），这里只管图案。

uniform float Time;
uniform float Age;
uniform float Fade;
uniform float Intensity;
uniform vec3 CoreColor;
uniform vec3 EdgeColor;

in vec2 beamUV;
in vec4 vertexColor;

out vec4 fragColor;

const float TAU = 6.28318531;

void main() {
    float u = beamUV.x;
    float v = beamUV.y;

    if (v < 0.0 || v > 1.0 || abs(u) > 1.0) {
        discard;
    }

    // 实心主体（中心亮、向外缓降）
    float core = smoothstep(0.66, 0.0, abs(u));
    // 两侧轮廓线
    float rim = smoothstep(0.085, 0.0, abs(abs(u) - 0.80));
    // 底部撞击预兆
    float bottom = smoothstep(0.28, 0.0, v);
    // 顶部渐隐
    float topFade = smoothstep(1.0, 0.50, v);
    // 纵向流纹（快速向上流动，强调"在冲下来"）
    float flow = 0.72 + 0.28 * sin((v * 7.0 - Time * 3.2) * TAU);

    float energy = (core * 0.60 + rim * 1.35 + bottom * 1.70) * topFade * flow * Intensity;

    vec3 color = mix(EdgeColor, CoreColor, clamp(core + rim, 0.0, 1.0));
    color += vec3(1.00, 0.98, 0.88) * bottom * 0.85;

    float alpha = clamp(energy, 0.0, 1.0) * Fade * vertexColor.a;
    if (alpha < 0.005) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
