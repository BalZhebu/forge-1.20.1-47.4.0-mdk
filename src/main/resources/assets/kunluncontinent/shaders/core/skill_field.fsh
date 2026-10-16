#version 150

// 八荒寂灭 · 寂灭地阵
// 全部形状由程序化公式逐像素算出，不再依赖粒子堆出多边形。
//
// localPos : 相对阵心的归一化坐标，中心 (0,0)、边缘长度 1
// Time     : 特效存活秒数（驱动自转）
// Age      : 0 → 1 的整体推进度（驱动淡入淡出）
// Pulse    : 0 → 1 的单次脉冲相位（每 1.5 秒一轮，从中心推向边缘）
// Fade     : 总体透明度包络（Java 侧按 Age 算好）
// Intensity: 整体亮度倍率，便于调参不改代码

uniform float Time;
uniform float Age;
uniform float Pulse;
uniform float Fade;
uniform float Intensity;
uniform vec3 CoreColor;
uniform vec3 EdgeColor;

in vec2 localPos;
in vec4 vertexColor;

out vec4 fragColor;

const float PI = 3.14159265;

void main() {
    float r = length(localPos);
    if (r > 1.0) {
        discard;
    }

    float ang = atan(localPos.y, localPos.x);

    // ---- ① 寂灭核心：中心最浓，向外迅速衰减 ----
    float core = pow(max(1.0 - r, 0.0), 2.2);

    // ---- ② 外圈符文环：24 等分刻度 + 连续细环，缓慢自转 ----
    float outerBand = smoothstep(0.075, 0.0, abs(r - 0.930));
    float outerRing = smoothstep(0.018, 0.0, abs(r - 0.985));
    float ticks24 = step(0.86, cos(ang * 24.0 + Time * 0.55)) * outerBand;

    // ---- ③ 内圈符文环：12 等分刻度，反向自转 ----
    float innerBand = smoothstep(0.060, 0.0, abs(r - 0.575));
    float innerRing = smoothstep(0.014, 0.0, abs(r - 0.630));
    float ticks12 = step(0.90, cos(ang * 12.0 - Time * 0.85)) * innerBand;

    // ---- ④ 脉冲冲击环：每轮从中心推到边缘，越靠外越淡 ----
    float shock = smoothstep(0.105, 0.0, abs(r - Pulse)) * (1.0 - Pulse);

    // ---- ⑤ 湮灭裂隙：斜向旋转的细纹，制造"空间被撕开"的质感 ----
    float glyph = smoothstep(0.90, 1.0, sin(ang * 8.0 + r * 9.0 - Time * 1.4))
            * smoothstep(0.98, 0.30, r);

    // ---- ⑥ 阵体裁剪：外缘柔和收口，避免硬边 ----
    float border = smoothstep(1.0, 0.90, r);

    float energy = core * 0.85
            + outerBand * 0.55
            + outerRing * 1.15
            + ticks24 * 0.60
            + innerBand * 0.40
            + innerRing * 0.85
            + ticks12 * 0.45
            + shock * 1.70
            + glyph * 0.22;

    energy = energy * border * Intensity;

    // 中心偏猩红、边缘偏幽紫 —— "八荒皆寂灭"
    vec3 color = mix(CoreColor, EdgeColor, smoothstep(0.0, 1.0, r));
    color += vec3(0.60, 0.20, 0.06) * shock;
    color += vec3(0.10, 0.05, 0.18) * glyph;
    // 起手瞬间整体泛白，像刚砸下来
    color += vec3(0.22, 0.16, 0.20) * pow(max(1.0 - Age * 3.0, 0.0), 2.0);

    float alpha = clamp(energy, 0.0, 1.0) * Fade * vertexColor.a;
    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
