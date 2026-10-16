#version 150

// 黑色旋涡 —— 猛虎破界（裂金虎 9）在目标身后撕开的暗口。
//
// ⚠️ 这条 fsh 对应的 json 用的是**普通 alpha 混合**（`one_minus_srcalpha`），
//    不是加法混合 —— 加法混合下黑色等于"什么都没画"（黑×α + 背景 = 背景），
//    永远画不出"把背景吃掉一块"的效果。和 skill_rift 是同一个道理。
//
// 画面构成：
//   · 涡心 —— 近纯黑（EdgeColor，几乎不透光的虚空）
//   · 旋臂 —— 对数螺旋，随时间缓慢转动
//   · 边缘 —— 一圈金色亮边（它是随后那道金光的源头）
//   · 开合 —— 先张开，保持，最后收拢

uniform float Time;
uniform float Age;
uniform float Fade;
uniform float Intensity;
uniform float Seed;
uniform vec3 CoreColor;
uniform vec3 EdgeColor;

in vec2 burstUV;
in vec4 vertexColor;

out vec4 fragColor;

const float TAU = 6.28318531;

// 旋臂条数
const float ARMS = 3.0;

void main() {
    vec2 p = burstUV;
    float r = length(p);

    // 张开 → 保持 → 收拢
    float opening = smoothstep(0.0, 0.22, Age);
    float closing = smoothstep(1.0, 0.80, Age);
    float radius = mix(0.10, 1.0, opening) * closing;
    if (radius <= 0.02) {
        discard;
    }

    float rr = r / radius;
    if (rr > 1.05) {
        discard;
    }

    float ang = atan(p.y, p.x);

    // 螺旋臂：角度 - 对数半径 → 几条往涡心卷进去的带
    float swirl = fract(ang / TAU * ARMS - log(max(rr, 0.06)) * 1.35 + Time * 0.75 + Seed);
    float arm = smoothstep(0.62, 0.08, abs(swirl - 0.5) * 2.0);

    float disc = smoothstep(1.00, 0.78, rr);
    float rim = smoothstep(0.16, 0.0, abs(rr - 0.93));

    // 涡心近黑、旋臂比涡心略亮一点、边缘最亮（金）
    vec3 color = mix(EdgeColor, CoreColor, arm * 0.22);
    color = mix(color, CoreColor, rim);

    // 涡心几乎不透明，边缘略透 —— 这样才像"吃掉背景的一块"
    float alpha = disc * (0.92 - 0.40 * rim) + rim * 0.80;
    alpha *= Fade * vertexColor.a * Intensity;

    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, clamp(alpha, 0.0, 1.0));
}
