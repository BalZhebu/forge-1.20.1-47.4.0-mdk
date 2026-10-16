#version 150

// 体内爆裂 —— 裂天（裂金虎 8）。
//
// 设计要点：这是「生物体内炸开」的效果，所以**故意不做任何穿透处理**：
// Java 侧把四边形中心放在该生物的身体中心，渲染时深度测试是开的，
// 于是核心会被生物自己的模型挡住，只有撑出轮廓的那部分露出来 ——
// 看起来就是"光从它体内炸出来"，而不是"贴在身上的一层贴图"。
// （对比：地面法阵要 polygonOffset 把自己拉到地面之上，这里恰恰相反。）
//
// 动画：白热内核迅速收缩 + 放射裂纹被撑开 + 亮壳向外推。

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

// 裂纹条数
const float CRACKS = 7.0;

void main() {
    vec2 p = burstUV;
    float r = length(p);
    if (r > 1.0) {
        discard;
    }

    float ang = atan(p.y, p.x);

    // 生命包络：起手极快（"啪"那一下），之后衰减
    float env = min(1.0, Age * 7.0) * pow(max(1.0 - Age, 0.0), 1.35);

    // ---- ① 白热内核：一开始铺满，随后迅速收缩 ----
    float coreR = mix(0.82, 0.10, min(1.0, Age * 2.2));
    float core = smoothstep(coreR, 0.0, r);

    // ---- ② 放射裂纹：几道从中心被撑开的口子 ----
    float seg = TAU / CRACKS;
    float a = mod(ang + Seed * 3.1 + seg * 0.5, seg) - seg * 0.5;
    float arc = abs(a) * max(r, 0.18);
    float crack = smoothstep(0.050, 0.0, arc) * smoothstep(0.18, 0.95, r);

    // ---- ③ 亮壳：一圈由内向外推的冲击面 ----
    float shell = smoothstep(0.14, 0.0, abs(r - mix(0.12, 1.05, min(1.0, Age * 1.15))));

    // ---- ④ 内部底光：让"体内"有一点实感，不至于只有几条线 ----
    float inner = smoothstep(1.0, 0.0, r) * 0.32;

    float energy = (core * 1.70 + crack * 1.05 + shell * 0.85 + inner) * env * Intensity;

    vec3 color = mix(EdgeColor, CoreColor, clamp(core + shell * 0.6, 0.0, 1.0));
    color += vec3(1.00, 0.97, 0.88) * core * 0.85;   // 内核泛白热
    color += vec3(0.55, 0.42, 0.18) * crack;         // 裂纹偏橙金

    float alpha = clamp(energy, 0.0, 1.0) * Fade * vertexColor.a;
    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
