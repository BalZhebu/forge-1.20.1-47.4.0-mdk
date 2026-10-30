#version 150

// 「横向刀光」几何 —— 施法者身前一段**水平环带**（很像拔刀斩划过的那道弧）。
//
// Java 侧按角度把环带切成若干四边形（顶点写「世界坐标 - 相机坐标」），
// 这里把顶点换算成两个归一化坐标：
//   arcUV.x = 相对朝向的角度 / HalfAngle           ∈ [-1, 1]   （0 = 正前方）
//   arcUV.y = (半径 - Inner) / (Radius - Inner)    ∈ [ 0, 1]   （0 = 刀根、1 = 刀尖）
//
// ⚠️ 为什么 v 是半径而不是高度：
//    第一版把 v 当成「高度」、几何做成竖直柱面墙 —— 结果是一根竖条绕着玩家转，
//    看起来像"横向一堵墙"，完全不是横斩。横斩的展开方向必须是**半径**，
//    高度只留一点点（见 ArcEffect 里内外圈的 y 差），够在平视时不至于退化成零。

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

/** 刃心（相机相对的 xz）。 */
uniform vec2 Center;
/** 施法者的水平朝向（单位向量，xz）。 */
uniform vec2 Dir;
/** 刀尖所在半径（格）。 */
uniform float Radius;
/** 刀根所在半径（格）。 */
uniform float Inner;
/** 单侧张角（弧度），例如 60° = 1.0472。 */
uniform float HalfAngle;

out vec2 arcUV;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vec2 p = Position.xz - Center;
    float len = length(p);
    vec2 n = p / max(len, 0.0001);

    // atan(side, forward)：0 = 正前方，±PI = 背后
    float c = dot(n, Dir);
    float s = dot(n, vec2(-Dir.y, Dir.x));
    float ang = atan(s, c);

    arcUV = vec2(
        ang / max(HalfAngle, 0.0001),
        (len - Inner) / max(Radius - Inner, 0.0001)
    );
    vertexColor = Color;
}
