#version 150

// 金色横向刀光（天使 1「圣光斩」）。
//
// 展开坐标 (u = 角度, v = 半径)：
//   · 刀身：v 0.35~0.85 最厚，刀根收细
//   · 刀尖：v ≈ 0.90 一条白热亮线（刀锋划过的轨迹）
//   · 扫过：Sweep 从 -1.08 推到 +1.08，前沿最亮，身后只留很短的拖尾
//
// ⚠️ 拖尾必须短。第一版用的是"整段弧持续发光 + 长拖尾"，
//    视觉上就变成"一堵发光的墙在移动"，而不是"一道刀光划过"。

uniform float Time;
uniform float Age;
uniform float Fade;
uniform float Intensity;
uniform vec3 CoreColor;
uniform vec3 EdgeColor;

in vec2 arcUV;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float u = arcUV.x;
    float v = arcUV.y;

    if (abs(u) > 1.0 || v < 0.0 || v > 1.0) {
        discard;
    }

    // ---- 扫过进度：前 55% 的时间斩完这一刀，剩下留给余辉 ----
    float sweep = mix(-1.08, 1.08, clamp(Age / 0.55, 0.0, 1.0));
    float passed = sweep - u;
    if (passed < -0.02) {
        // 还没扫到这个角度
        discard;
    }
    // 前沿极亮，往后迅速衰减（短拖尾）
    float trail = exp(-max(passed, 0.0) * 7.0);

    // ---- 刀身：刀根细、中段厚 ----
    float body = smoothstep(0.10, 0.45, v) * smoothstep(1.00, 0.86, v);
    // ---- 刀锋：外缘那条白热亮线 ----
    float tip = smoothstep(0.11, 0.0, abs(v - 0.90));
    // ---- 刀根的一点余辉，让弧看起来是"从手里甩出去的" ----
    float root = smoothstep(0.30, 0.0, v) * 0.35;

    float energy = (body * 0.95 + tip * 1.90 + root) * trail * Intensity;

    vec3 color = mix(EdgeColor, CoreColor, clamp(body + tip, 0.0, 1.0));
    // 刀锋打到发白
    color += vec3(1.00, 0.97, 0.86) * tip * 1.30;

    float alpha = clamp(energy, 0.0, 1.0) * Fade * vertexColor.a;
    if (alpha < 0.005) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
