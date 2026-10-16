#version 150

// 世界坐标（相机相对）→ 裁剪空间。
// ModelViewMat / ProjMat 由 VertexBuffer#drawWithShader 自动填充为
// RenderSystem 的相机视图矩阵与投影矩阵，所以顶点位置必须写成「世界坐标 - 相机坐标」。

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 Center;
uniform float Radius;

out vec2 localPos;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // 把顶点位置换算成相对阵心的归一化坐标：中心 (0,0)，边缘长度 1
    localPos = (Position.xz - Center) / max(Radius, 0.0001);
    vertexColor = Color;
}
