#version 150

// 天使之翼的「翅膀虚影」（天使 2）。
//
// 目标不是画一对精细翅膀，而是**一眼能认出是翅膀**的发光轮廓：
//   · 每侧 6 条羽毛，从根部向外上方展开，长度 / 角度错开
//   · 根部一团光晕把羽毛连起来，避免看起来像 6 根独立的光棍
//   · 整体强度故意压低（Intensity ≈ 0.45）→ "虚影"而不是实体建模
//
// ⚠️ fsh 里用到 vsh 的 out 必须自己声明 in。

uniform float Time;
uniform float Age;
uniform float Fade;
uniform float Intensity;
uniform vec3 CoreColor;
uniform vec3 EdgeColor;

in vec2 wingUV;
in vec4 vertexColor;

out vec4 fragColor;

const float PI = 3.14159265;

/**
 * 一条羽毛：根部在 ROOT，沿 ang 方向伸出 len，中段最宽。
 *
 * @param p   已取绝对值后的坐标（x ≥ 0，所以左右两片对称）
 * @param ang 羽毛朝向（弧度，0 = 水平向外，正 = 向上）
 * @param len 羽毛长度
 * @param wid 羽毛最大半宽
 */
float feather(vec2 p, float ang, float len, float wid) {
    vec2 d = vec2(cos(ang), sin(ang));
    vec2 nrm = vec2(-d.y, d.x);
    vec2 q = p - vec2(0.07, -0.03);

    float along = dot(q, d);
    float across = dot(q, nrm);

    if (along < 0.0 || along > len) {
        return 0.0;
    }
    float t = along / len;
    // 根细 → 中段最宽 → 尖端收
    // ⚠️ 必须给 w 一个下限：t 贴近 0/1 时 sin()≈0，w=0 会让 smoothstep(0,0,x) 除零
    float w = max(wid * sin(pow(clamp(t, 0.0, 1.0), 0.55) * PI), 0.0015);
    // edge0 > edge1：反向 smoothstep，|across| 小于 w*0.30 时是 1
    return smoothstep(w, w * 0.30, abs(across));
}

void main() {
    // 左右对称：取 x 的绝对值
    vec2 p = vec2(abs(wingUV.x), wingUV.y);
    if (p.x > 1.0 || abs(p.y) > 1.0) {
        discard;
    }

    float mask = 0.0;
    mask = max(mask, feather(p, radians(62.0), 0.80, 0.070));
    mask = max(mask, feather(p, radians(48.0), 0.92, 0.085));
    mask = max(mask, feather(p, radians(33.0), 1.00, 0.095));
    mask = max(mask, feather(p, radians(18.0), 0.98, 0.095));
    mask = max(mask, feather(p, radians(3.0),  0.88, 0.085));
    mask = max(mask, feather(p, radians(-13.0), 0.72, 0.075));

    // 根部光晕：把 6 条羽毛连成一片
    float root = smoothstep(0.46, 0.0, length(vec2(p.x * 1.5, p.y * 0.85)));

    // 极轻微的呼吸，避免完全静态像贴图
    float pulse = 0.86 + 0.14 * sin(Time * 2.2);

    float energy = (mask * 1.0 + root * 0.60) * Intensity * pulse;
    // 出场淡入
    float grow = clamp(Age * 4.0, 0.0, 1.0);

    vec3 color = mix(EdgeColor, CoreColor, clamp(mask, 0.0, 1.0));
    color += vec3(1.0, 1.0, 0.94) * root * 0.35;

    float alpha = clamp(energy, 0.0, 1.0) * Fade * grow * vertexColor.a;
    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(color * vertexColor.rgb, alpha);
}
