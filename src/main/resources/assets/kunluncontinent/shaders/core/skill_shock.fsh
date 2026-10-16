#version 150

// 扩散光波 —— 不周倾落地时从中心向外推开的冲击环。
//
// 几何由 skill_sigil.vsh 提供（一个水平四边形），但归一化用的是「最大半径」，
// 所以 localPos 的取值恒为 [-1, 1]，与时间无关。真正在动的是 ShockRadius：
// 光波前沿在归一化空间里的位置，由 Java 侧按推进度算好传进来。
//
// 这样做的好处是几何完全静态 —— 每帧只改一个 uniform，不用重建顶点。

uniform float ShockRadius;
uniform float Time;
uniform float Age;
uniform float Fade;
uniform float Intensity;
uniform float TickCount;
uniform vec3 CoreColor;
uniform vec3 EdgeColor;

in vec2 localPos;
// ⚠️ 必须声明：vsh 里 out 了 vertexColor，fsh 侧不声明就是「未定义标识符」→ 编译失败。
//    失败时 SkillShaders 会吃掉异常返回 null，表现是「特效整个不画、还不报错」。
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float r = length(localPos);

    // ---- ① 光波前沿：在 ShockRadius 处的一圈亮带 ----
    //     内外两侧都平滑过渡，避免出现"被切一刀"的硬边
    float front = smoothstep(0.11, 0.0, abs(r - ShockRadius));

    // ---- ② 环上的等分刻度：让冲击波有"符文"的质感 ----
    float ang = atan(localPos.y, localPos.x);
    float ticks = step(0.82, cos(ang * TickCount + Time * 1.6)) * front * 0.65;

    // ---- ③ 内侧余辉：能量扫过之后留下的余韵 ----
    float trail = smoothstep(ShockRadius, ShockRadius * 0.25, r) * 0.22;

    // ---- ④ 阵心残能：光波刚离开中心时那里还有一团亮 ----
    float core = pow(max(1.0 - r * 2.6, 0.0), 2.0) * 0.55;

    float energy = (front * 1.85 + ticks + trail + core) * Intensity;

    vec3 color = mix(EdgeColor, CoreColor, clamp(front + core, 0.0, 1.0));
    // 前沿内侧偏白热，像空气被推开的瞬间
    color += vec3(0.85, 0.78, 0.58) * front * 0.55;

    float alpha = clamp(energy, 0.0, 1.0) * Fade * vertexColor.a;
    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
