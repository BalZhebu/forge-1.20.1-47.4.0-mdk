#version 150

// 「翅膀虚影」几何 —— 玩家背后的一块竖直四边形。
//
// Java 侧给出中心 + 右向量 + 上向量 + 半宽 / 半高，顶点写「世界坐标 - 相机坐标」，
// 这里把顶点换算成这块面上的归一化坐标：
//   wingUV.x = 沿 Right 的偏移 / HalfWidth    ∈ [-1, 1]（0 = 身体中线）
//   wingUV.y = 沿 Up 的偏移    / HalfHeight   ∈ [-1, 1]
//
// 因为平面法线就是玩家的朝向，所以从第三人称背后看永远是"正对镜头的一片翼展"。

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

/** 平面中心（相机相对）。 */
uniform vec3 Center;
/** 右向量（单位，水平）。 */
uniform vec3 Right;
/** 上向量（单位，竖直）。 */
uniform vec3 Up;
uniform float HalfWidth;
uniform float HalfHeight;

out vec2 wingUV;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vec3 rel = Position - Center;
    wingUV = vec2(
        dot(rel, Right) / max(HalfWidth, 0.0001),
        dot(rel, Up) / max(HalfHeight, 0.0001)
    );
    vertexColor = Color;
}
