#version 150

// 八荒戟 · 第八魂技「降雷」—— 雷柱的逐像素形状。
//
// 一条持续折角的雷线 + 两条分叉 + 落点冲击光晕，全部程序化算出。
// 相比原来每个目标要打 ~60 个粒子（22 段主雷 + 3×16 段分叉），
// 这里每个目标只有一个四边形。

uniform float Time;
uniform float Age;
uniform float Fade;
uniform float Intensity;
uniform float Seed;
uniform vec3 CoreColor;
uniform vec3 EdgeColor;

in vec2 boltUV;
in vec4 vertexColor;

out vec4 fragColor;

// 把高度切成多少段 —— 段数越多折角越碎，雷电的"锯齿感"越强
const float SEGMENTS = 15.0;

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

// 雷线：在高度 v 处的横向中心偏移。
// 做法是把 v 切成 SEGMENTS 段，每段给一个伪随机偏移，段内线性插值 —— 于是得到折线。
//
// ⚠️ 乘子不能太大：u 的取值就是 [-1, 1]（四边形边界），摆幅 + 辉光一旦越界
//    就会被硬裁掉，表现为"雷柱边缘被切了一刀"。
//    a、b ∈ [-0.5, 0.5]，× 1.5 后折线在 [-0.75, 0.75]，
//    再加辉光半径 ~0.75，正好贴满 [-1, 1] 而不会被裁。
float boltLine(float v, float seed) {
    float f = v * SEGMENTS;
    float i = floor(f);
    float t = fract(f);
    float a = hash11(i * 1.7 + seed * 13.0) - 0.5;
    float b = hash11((i + 1.0) * 1.7 + seed * 13.0) - 0.5;
    return mix(a, b, t) * 1.5;
}

void main() {
    float u = boltUV.x;
    float v = boltUV.y;

    // 顶点之外的像素（插值误差）直接丢
    if (v < -0.02 || v > 1.02) {
        discard;
    }

    // ---- ① 主雷线：白热核心 + 周围辉光 ----
    float l1 = boltLine(v, Seed);
    float d1 = abs(u - l1);
    float core = smoothstep(0.100, 0.0, d1);
    float glow = exp(-d1 * 4.0) * 0.58;

    // ---- ② 分叉雷：频率更高、更暗，只在中下段出现（像侧枝）----
    float l2 = boltLine(v, Seed + 5.0) * 0.70 + 0.28;
    float d2 = abs(u - l2);
    float fork = exp(-d2 * 7.0) * 0.32 * smoothstep(0.85, 0.28, v);

    float l3 = boltLine(v, Seed + 31.0) * 0.55 - 0.48;
    float d3 = abs(u - l3);
    fork += exp(-d3 * 8.5) * 0.24 * smoothstep(0.72, 0.12, v);

    // ---- ③ 落点冲击：v 接近 0 处横向铺开的光晕 ----
    float impact = smoothstep(0.14, 0.0, v) * exp(-pow(u * 1.9, 2.0));

    // ---- ④ 竖向衰减：落点最亮，越往上越淡（雷从云里劈下来，越靠近地面越实）----
    float vert = mix(1.0, 0.26, v);

    // ---- ⑤ 高频闪烁：雷是"闪"的，不是恒定亮 ----
    float flicker = 0.76
            + 0.15 * sin(Time * 73.0 + v * 21.0)
            + 0.09 * sin(Time * 41.0 + u * 11.0);

    float energy = (core * 1.80 + glow + fork + impact * 0.95) * vert * flicker * Intensity;

    // 白热核心 → 青蓝 → 幽紫，向外过渡
    vec3 color = mix(EdgeColor, CoreColor, clamp(core + impact, 0.0, 1.0));
    color += vec3(0.85, 0.92, 1.00) * core * 0.75 * flicker;
    color += vec3(0.22, 0.42, 0.85) * (glow + fork);

    // 刚劈下来的那一瞬整体泛白
    color += vec3(0.30, 0.34, 0.44) * pow(max(1.0 - Age * 4.0, 0.0), 2.0);

    float alpha = clamp(energy, 0.0, 1.0) * Fade * vertexColor.a;
    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
