#version 150

// 通用「带状能量体」几何 —— 一根光柱 / 一道裂隙 / 一束射线共用。
//
// 几何是一个沿 Dir 展开 Length、沿 Side 展开 ±HalfWidth 的四边形：
//   Start : 带子起点（相机相对坐标）
//   Dir   : 单位方向（竖直光柱用 (0,1,0)，射线用视线方向）
//   Side  : 单位宽度方向 —— Java 侧取「垂直于 Dir 且垂直于相机视线」的向量，
//           这样无论从哪个角度看，带子在屏幕上都有可见宽度
//
// (u, v) 在这里反算，所以顶点格式继续用统一的 POSITION_COLOR（没有 UV 属性）：
//   u = 沿 Side 的偏移 / HalfWidth   ∈ [-1, 1]
//   v = 沿 Dir 的距离  / Length      ∈ [ 0, 1]

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 Start;
uniform vec3 Dir;
uniform vec3 Side;
uniform float Length;
uniform float HalfWidth;

out vec2 beamUV;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vec3 rel = Position - Start;
    beamUV = vec2(
        dot(rel, Side) / max(HalfWidth, 0.0001),
        dot(rel, Dir) / max(Length, 0.0001)
    );
    vertexColor = Color;
}
