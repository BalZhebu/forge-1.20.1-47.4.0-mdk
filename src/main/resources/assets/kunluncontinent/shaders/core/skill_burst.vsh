#version 150

// 通用「屏幕对齐广告牌」几何 —— 体内爆裂 / 黑色旋涡 / 旋涡金光共用。
//
// 与 skill_bolt.vsh 的区别：那个是"竖直广告牌"（沿世界 Y 展开，正俯视会退化成一条线），
// 这个是**真正的屏幕对齐广告牌**：沿相机的两个正交轴（Right / Up）各展开 ±HalfSize，
// 所以从任何角度（包括正上方往下看）都是一个正对镜头的正方形。
//
// 用途是"贴在生物身上的效果"：Center 传的是**该生物的身体中心**，四边形包住它。
// 顶点仍然写「世界坐标 - 相机坐标」，ModelViewMat / ProjMat 由 BufferUploader 填。
//
//   Right : 相机右向量（世界空间）
//   Up    : 相机上向量（世界空间，= Right × Look）
//   (u,v) = 相对中心、沿 Right / Up 的偏移 / HalfSize  ∈ [-1, 1]²

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 Center;
uniform vec3 Right;
uniform vec3 Up;
uniform float HalfSize;

out vec2 burstUV;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vec3 rel = Position - Center;
    burstUV = vec2(dot(rel, Right), dot(rel, Up)) / max(HalfSize, 0.0001);
    vertexColor = Color;
}
