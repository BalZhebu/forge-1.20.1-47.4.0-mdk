#version 150

// 通用「地面符文阵」—— 磐石巨猿 / 裂金虎 系列的第 8、9 魂技共用。
//
// 一套公式画出所有阵法，靠 uniform 参数区分：
//   PolySides  外圈等分刻度数        PolyRadius 外圈半径
//   StarPoints 星芒角数              StarInner / StarOuter 星芒内外半径
//   RuneRadius 内圈法阵半径
//   SlashCount 爪痕条数（0 = 无）    SlashAngle 爪痕整体朝向
//   ShockCount 冲击环数量（最多 3）
//   CoreColor / EdgeColor 中心色 / 边缘色
//
// localPos : 相对阵心的归一化坐标，中心 (0,0)、边缘长度 1
// Time     : 存活秒数（驱动自转）
// Age      : 0 → 1 的推进度
// Pulse    : 0 → 1 循环相位（驱动冲击环外推）

uniform float Time;
uniform float Age;
uniform float Fade;
uniform float Intensity;
uniform float Pulse;
uniform float PolySides;
uniform float PolyRadius;
uniform float StarPoints;
uniform float StarInner;
uniform float StarOuter;
uniform float RuneRadius;
uniform float SlashCount;
uniform float SlashAngle;
uniform float ShockCount;
uniform vec3 CoreColor;
uniform vec3 EdgeColor;

in vec2 localPos;
in vec4 vertexColor;

out vec4 fragColor;

const float TAU = 6.28318531;

void main() {
    float r = length(localPos);
    if (r > 1.0) {
        discard;
    }

    float ang = atan(localPos.y, localPos.x);

    // ---- ① 外圈符文环：PolySides 等分刻度 + 一道连续细环 ----
    float band = smoothstep(0.075, 0.0, abs(r - PolyRadius));
    float ring = smoothstep(0.016, 0.0, abs(r - min(PolyRadius + 0.055, 0.995)));
    float ticks = step(0.86, cos(ang * PolySides + Time * 0.45)) * band;

    // ---- ② 星芒：StarPoints 个尖角，从 StarInner 伸到 StarOuter ----
    float starShape = smoothstep(0.62, 0.98, cos(ang * StarPoints - Time * 0.30));
    float starRadial = smoothstep(StarInner, StarInner + 0.08, r)
            * smoothstep(StarOuter, StarOuter - 0.10, r);
    float star = starShape * starRadial;

    // ---- ③ 内圈法阵：密集刻度，反向自转 ----
    float rune = smoothstep(0.055, 0.0, abs(r - RuneRadius));
    float runeTicks = step(0.90, cos(ang * (StarPoints * 2.0) + Time * 0.9)) * rune;

    // ---- ④ 爪痕：SlashCount 条从中心射出的直线（裂天 / 猛虎破界）----
    //      mod 把角度折进一个扇区，到扇区中线的弧长得越近越亮
    float slash = 0.0;
    if (SlashCount > 0.5) {
        float seg = TAU / SlashCount;
        float a = mod(ang - SlashAngle + seg * 0.5, seg) - seg * 0.5;
        float arc = abs(a) * max(r, 0.15);
        slash = smoothstep(0.045, 0.0, arc) * smoothstep(0.30, 0.92, r);
    }

    // ---- ⑤ 冲击环：最多 3 道，相位错开地从中心推向边缘 ----
    float shock = 0.0;
    for (int i = 0; i < 3; i++) {
        if (float(i) >= ShockCount - 0.5) {
            break;
        }
        float ph = fract(Pulse + float(i) * 0.30);
        shock += smoothstep(0.085, 0.0, abs(r - ph)) * (1.0 - ph);
    }

    // ---- ⑥ 阵心能量 ----
    float core = pow(max(1.0 - r, 0.0), 2.6);

    // 外缘柔和收口，避免硬边
    float border = smoothstep(1.0, 0.90, r);

    float energy = (core * 0.55
            + ticks * 0.75
            + ring * 1.10
            + star * 0.65
            + runeTicks * 0.50
            + slash * 1.25
            + shock * 1.60) * border * Intensity;

    vec3 color = mix(CoreColor, EdgeColor, smoothstep(0.0, 1.0, r));
    color += vec3(0.55, 0.42, 0.18) * slash;
    color += vec3(0.35, 0.28, 0.12) * shock;
    // 起手瞬间整体泛白，像刚砸下来
    color += vec3(0.22, 0.19, 0.14) * pow(max(1.0 - Age * 3.0, 0.0), 2.0);

    float alpha = clamp(energy, 0.0, 1.0) * Fade * vertexColor.a;
    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
