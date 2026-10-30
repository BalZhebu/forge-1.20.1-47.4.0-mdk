#version 150

// 禁锢的锁链环 —— 绕在被禁锢生物身上的几道金色锁环。
//
// 图案很克制：一道主环 + 一道内细环，主环上按角度做明暗起伏，
// 看起来像「一节一节的链环」，不用真的去建锁链的几何。

uniform float Time;
uniform float Intensity;

in vec2 ringUV;
in vec4 vertexColor;

out vec4 fragColor;

const float TAU = 6.28318531;

void main() {
    float r = length(ringUV);
    if (r > 1.0) {
        discard;
    }

    // ---- 主环 ----
    float ring = smoothstep(0.085, 0.0, abs(r - 0.80));
    // ---- 内侧细环 ----
    float inner = smoothstep(0.040, 0.0, abs(r - 0.64)) * 0.55;
    // ---- 链节感：沿角度做周期明暗，并缓慢流动 ----
    float ang = atan(ringUV.y, ringUV.x);
    float links = 0.50 + 0.50 * abs(sin(ang * 6.0 + Time * 1.7));

    float energy = (ring * 1.10 + inner) * links * Intensity;

    vec3 color = vec3(1.00, 0.86, 0.46);
    // 环的内侧压暗一点，金色更"金属"而不是纯发光
    color = mix(color * 0.72, color, ring);

    float alpha = clamp(energy, 0.0, 1.0) * vertexColor.a;
    if (alpha < 0.01) {
        discard;
    }
    fragColor = vec4(color, alpha);
}
