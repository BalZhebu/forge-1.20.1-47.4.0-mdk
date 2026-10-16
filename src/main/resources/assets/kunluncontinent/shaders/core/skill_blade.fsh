#version 150

// 光刃 —— 弑神（破魂枪 9）。
//
// 不再是"一根细射线"，而是一道劈出去的能量刃：
//   · 月牙轮廓 —— 两端收成尖、中段最厚，像挥出去的剑光
//   · 上下两条锐利的刃口亮边
//   · 中间一条白热核心线
//   · 内部有沿长度流动的能量纹
//
// ⚠️ 刃口位置是 `abs(u) == halfW`。几何给的 HalfWidth 是**最大**半宽，
//    实际轮廓由 taper 沿长度收窄，所以顶点出来的四边形比刃本身宽 —— 这是有意的，
//    留出余量给外围辉光，同时保证轮廓不会碰到边界被硬裁。

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

const float TAU = 6.28318531;
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

    // ---- ① 月牙轮廓：两端尖、中段厚 ----
    float taper = pow(sin(clamp(v, 0.0, 1.0) * PI), 0.55);

    // 轻微起伏 —— 光刃要比虚空裂隙规整得多，只给一点点
    float wobble = (hash11(floor(v * 18.0) * 1.3 + Seed) - 0.5) * 0.10;
    float halfW = max(taper * (1.0 + wobble), 0.0001);

    float d = abs(u);
    float n = u / halfW;

    // ---- ② 刃体：中间偏厚的能量填充 ----
    float body = exp(-pow(n * 1.7, 2.0)) * taper;

    // ---- ③ 刃口：上下两条锐利亮边 ----
    float rim = smoothstep(0.16, 0.0, abs(d - halfW)) * taper;

    // ---- ④ 白热核心线：沿中轴的一条极亮线 ----
    float coreLine = smoothstep(0.10, 0.0, d) * taper;

    // ---- ⑤ 沿长度流动的能量纹 ----
    float flow = 0.70 + 0.30 * sin((v * 5.0 - Time * 2.6) * TAU);

    float energy = (body * 1.10 + rim * 1.50 + coreLine * 0.85) * flow * Intensity;

    vec3 color = mix(EdgeColor, CoreColor, clamp(body + rim, 0.0, 1.0));
    color += vec3(1.00, 0.96, 0.88) * coreLine * 0.90;
    // 刚劈出的那一瞬整体泛白
    color += vec3(0.45, 0.36, 0.28) * pow(max(1.0 - Age * 3.0, 0.0), 2.0);

    float alpha = clamp(energy, 0.0, 1.0) * Fade * vertexColor.a;
    if (alpha < 0.005) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
