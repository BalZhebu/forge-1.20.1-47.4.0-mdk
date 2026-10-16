#version 150

// 旋涡金光 —— 猛虎破界（裂金虎 9）：黑色旋涡打出的那一道光。
//
// 加法混合（发光），所以"光束"能亮到发白。
// 出招时刻由服务端控制 —— 它和伤害结算在同一 tick 发出，做到"光到即伤"。
//
// 画面构成：
//   · 白热核心 —— 光从旋涡里涌出的那一点
//   · 放射光束 —— 12 道向外打的光刺
//   · 十字星芒 —— UV 是相机对齐的，所以这两条看起来像屏幕上的镜头光晕

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

// 光束条数
const float RAYS = 12.0;

void main() {
    vec2 p = burstUV;
    float r = length(p);
    if (r > 1.0) {
        discard;
    }

    float ang = atan(p.y, p.x);

    // 极快的起手 + 较长的余辉
    float env = min(1.0, Age * 9.0) * pow(max(1.0 - Age, 0.0), 1.7);

    // ---- ① 白热核心：一开始铺满，迅速收缩成一个亮点 ----
    float core = smoothstep(mix(0.50, 0.06, min(1.0, Age * 2.0)), 0.0, r);

    // ---- ② 放射光束 ----
    float seg = TAU / RAYS;
    float a = mod(ang + Seed * 5.7 + seg * 0.5, seg) - seg * 0.5;
    float arc = abs(a) * max(r, 0.10);
    float ray = smoothstep(0.055, 0.0, arc) * smoothstep(0.05, 1.0, r);

    // ---- ③ 十字星芒（屏幕对齐，因此看起来像镜头光晕）----
    float crossH = smoothstep(0.030, 0.0, abs(p.y)) * smoothstep(1.0, 0.05, abs(p.x));
    float crossV = smoothstep(0.030, 0.0, abs(p.x)) * smoothstep(1.0, 0.05, abs(p.y));

    float energy = (core * 1.50 + ray * 1.25 + (crossH + crossV) * 0.80) * env * Intensity;

    vec3 color = mix(EdgeColor, CoreColor, clamp(core + ray * 0.5, 0.0, 1.0));
    color += vec3(1.00, 0.96, 0.86) * core * 0.90;

    float alpha = clamp(energy, 0.0, 1.0) * Fade * vertexColor.a;
    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
