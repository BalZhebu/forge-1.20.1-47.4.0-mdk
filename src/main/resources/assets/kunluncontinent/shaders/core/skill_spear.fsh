#version 150

// 金色长枪（天使 4「审判之矛」）。
//
// 复用 skill_beam.vsh 的几何（沿 Dir 的一条带子，beamUV = (u 横向, v 纵向)），
// 图案换成一把枪：
//   · 枪杆：细长的一条，占 0.05 ~ 0.84
//   · 枪头：0.84 ~ 1.0 收成三角，是整把枪最亮的部分
//   · 尾缨：0 ~ 0.10 散开的短须
//   · 推进：TipPos 从 0.12 推到 1.06 —— 看起来就是"投出去、飞出去"
//
// ⚠️ 不做精细枪刃纹路：用户要的是"大致金色轮廓"，细节越多越容易糊成一团。

uniform float Time;
uniform float Age;
uniform float Fade;
uniform float Intensity;
uniform float Flow;
uniform vec3 CoreColor;
uniform vec3 EdgeColor;

in vec2 beamUV;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float u = beamUV.x;
    float v = beamUV.y;

    if (v < 0.0 || v > 1.0) {
        discard;
    }

    // ---- 飞行推进：枪尖从手里射到尽头 ----
    float tipPos = mix(0.12, 1.06, clamp(Age / 0.75, 0.0, 1.0));
    if (v > tipPos) {
        discard;
    }

    float au = abs(u);

    // ---- 枪杆：细长，靠两端微微收窄 ----
    float shaftW = 0.145;
    float shaft = smoothstep(shaftW, shaftW * 0.30, au)
            * step(0.04, v) * smoothstep(0.88, 0.82, v);

    // ---- 枪头：三角，越靠尖端越窄 ----
    float t = clamp((v - 0.80) / 0.20, 0.0, 1.0);
    float headW = 0.36 * (1.0 - t);
    float head = smoothstep(headW, headW * 0.18, au) * step(0.78, v) * step(v, 1.0);

    // ---- 中轴白热芯线 ----
    float coreLine = smoothstep(0.055, 0.0, au) * step(v, 0.94);

    // ---- 尾缨：几根散开的短须 ----
    float tassel = smoothstep(0.30, 0.12, au) * smoothstep(0.12, 0.0, v) * 0.75;

    // ---- 沿枪身流动的金纹 ----
    float flow = 0.78 + 0.22 * sin((v * 6.0 - Time * Flow) * 6.28318531);

    float energy = (shaft * 1.00 + head * 1.45 + coreLine * 1.55 + tassel) * flow * Intensity;

    vec3 color = mix(EdgeColor, CoreColor, clamp(shaft + head + tassel, 0.0, 1.0));
    color += vec3(1.00, 0.98, 0.90) * coreLine * 1.15;

    float alpha = clamp(energy, 0.0, 1.0) * Fade * vertexColor.a;
    if (alpha < 0.005) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
