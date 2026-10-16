#version 150

// 通用「带状能量体」—— 岩柱 / 巨柱 / 虚空裂隙 / 死神射线共用。
//
// 一套公式画出所有带子，靠 uniform 区分：
//   Core   > 0 → 中心亮、边缘暗（实心光柱）
//          < 0 → 中心暗、两侧亮（虚空裂隙：中间是撕开的口子）
//   Jagged     折线抖动强度（0 = 平直光柱；>0 = 锯齿裂隙）
//   Flow       沿长度方向的能量流动速度
//   CoreColor / EdgeColor 剖面内 / 剖面外的颜色

uniform float Time;
uniform float Age;
uniform float Fade;
uniform float Intensity;
uniform float Core;
uniform float Jagged;
uniform float Flow;
uniform float Seed;
uniform vec3 CoreColor;
uniform vec3 EdgeColor;

in vec2 beamUV;
in vec4 vertexColor;

out vec4 fragColor;

const float TAU = 6.28318531;

// 折线抖动最多切成多少段 —— 段数越多折角越碎
const float SEGMENTS = 18.0;

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

// 在长度位置 v 处的横向偏移（折线）。
// ⚠️ 摆幅受 u ∈ [-1, 1] 这个边界约束：a、b ∈ [-0.5, 0.5]，
//    × Jagged（最大 0.6）后落在 ±0.3，加上辉光扩散也不会碰到四边形边缘。
float jag(float v, float seed) {
    if (Jagged < 0.001) {
        return 0.0;
    }
    float f = v * SEGMENTS;
    float i = floor(f);
    float t = fract(f);
    float a = hash11(i * 1.7 + seed * 13.0) - 0.5;
    float b = hash11((i + 1.0) * 1.7 + seed * 13.0) - 0.5;
    return mix(a, b, t) * Jagged;
}

void main() {
    float u = beamUV.x;
    float v = beamUV.y;

    if (v < -0.02 || v > 1.02) {
        discard;
    }

    float d = abs(u - jag(v, Seed));

    // ---- ① 横向剖面 ----
    // band 是以中线为中心的钟形；Core 为负时取反 → 中心变成一个"洞"
    float band = exp(-pow(d * 2.4, 2.0));
    float profile = Core > 0.0 ? band : (1.0 - band);

    // ---- ② 沿长度的能量流动：脉动的亮斑由起点推向末端 ----
    float flow = 0.5 + 0.5 * sin((v * 6.0 - Time * Flow) * TAU);
    flow = 0.66 + 0.34 * flow;

    // ---- ③ 端头收口：起点和末端都柔和淡出，避免出现硬切面 ----
    float head = smoothstep(0.0, 0.06, v) * smoothstep(1.0, 0.88, v);

    // ---- ④ 起点冲击：贴地/贴手那一端横向铺开的光晕 ----
    float impact = smoothstep(0.13, 0.0, v) * exp(-pow(u * 2.0, 2.0));

    // ---- ⑤ 末端爆发：射线/裂隙的"落点" ----
    float tip = smoothstep(0.90, 1.0, v) * exp(-pow(u * 1.6, 2.0));

    float energy = (profile * 1.45 + band * 0.50 + impact * 0.90 + tip * 1.10)
            * flow * head * Intensity;

    vec3 color = mix(EdgeColor, CoreColor, clamp(profile, 0.0, 1.0));
    color += vec3(0.70, 0.80, 1.00) * tip * 0.50;
    // 起手瞬间整体提亮
    color += vec3(0.25, 0.18, 0.30) * pow(max(1.0 - Age * 3.5, 0.0), 2.0);

    float alpha = clamp(energy, 0.0, 1.0) * Fade * vertexColor.a;
    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
