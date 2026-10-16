#version 150

// 虚空裂缝 —— 裂空（破魂枪 8）。
//
// 与 skill_beam 的区别：这里画的是「一条裂开的口子」，不是「一根能量带」：
//   · 梭形轮廓 —— 两端收成尖，中段最宽
//   · 边缘不规则 —— 沿长度和横向都有锯齿抖动，像被硬生生撕开
//   · 内部是**近纯黑**（虚空），不是发光
//   · 只有边缘和外围薄雾发光
//
// ⚠️ 这条 shader 的 json 用的是**普通 alpha 混合**（`one_minus_srcalpha`），
//    不是加法混合 —— 加法混合下画不出"黑洞"：黑色加任何东西还是背景色本身，
//    只有普通混合才能把背景真的压暗。

uniform float Time;
uniform float Age;
uniform float Fade;
uniform float Intensity;
uniform float Seed;
uniform vec3 CoreColor;
uniform vec3 EdgeColor;

in vec2 beamUV;
// ⚠️ 必须声明：vsh 里 out 了 vertexColor，fsh 侧不声明就是「未定义标识符」→ 编译失败。
//    失败时 SkillShaders 会吃掉异常返回 null，表现是「特效整个不画、还不报错」。
in vec4 vertexColor;

out vec4 fragColor;

const float PI = 3.14159265;

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

void main() {
    float u = beamUV.x;
    float v = beamUV.y;

    if (v < -0.01 || v > 1.01) {
        discard;
    }

    // ---- ① 梭形轮廓：两端收成尖，中段最宽 ----
    float taper = pow(sin(clamp(v, 0.0, 1.0) * PI), 0.5);

    // ---- ② 沿长度的锯齿抖动：让裂口的宽窄起伏，不是一条规整的带 ----
    float seg = floor(v * 26.0);
    float wobble = (hash11(seg * 1.7 + Seed) - 0.5) * 0.45;
    float halfW = max(taper * (1.0 + wobble) * 0.86, 0.0);

    // ---- ③ 横向边缘毛刺：撕口不会是平滑的 ----
    float burr = (hash11(floor(v * 44.0) * 3.1 + floor(abs(u) * 9.0) * 7.7 + Seed) - 0.5) * 0.11;
    float d = abs(u) + burr * taper;

    // ---- ④ 三个区域：内部虚空 / 边缘裂口 / 外围薄雾 ----
    float inside = step(d, halfW);
    float rim = smoothstep(0.085, 0.0, abs(d - halfW)) * taper;
    float haze = smoothstep(halfW + 0.32, halfW + 0.02, d) * taper
            * exp(-max(d - halfW, 0.0) * 7.0);

    // 内部留一丝极暗的紫 —— 全黑在暗环境里会完全看不见，也就没了"裂缝"的形状
    vec3 voidCol = EdgeColor;
    float voidAlpha = inside * taper * (0.93 - 0.08 * sin(Time * 2.2 + v * 9.0));

    // 边缘：撕裂处的高能紫光
    vec3 rimCol = CoreColor + vec3(0.30, 0.22, 0.45) * pow(max(1.0 - Age * 3.0, 0.0), 2.0);
    float rimAlpha = clamp(rim * 1.6, 0.0, 1.0);

    float hazeAlpha = haze * 0.32;

    vec3 color = mix(voidCol, rimCol, clamp(rim * 2.2, 0.0, 1.0));
    float alpha = max(max(voidAlpha, rimAlpha), hazeAlpha) * Fade * vertexColor.a * Intensity;

    if (alpha < 0.006) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
