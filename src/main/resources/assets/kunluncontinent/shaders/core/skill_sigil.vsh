#version 150

// 通用「地面符文阵」几何 —— 一个水平四边形，中心 Base，边长 2 × Radius。
//
// 顶点写「世界坐标 - 相机坐标」，ModelViewMat / ProjMat 由 BufferUploader.drawWithShader 填充。
// 这里只把顶点换算成相对阵心的归一化坐标：中心 (0,0)，边缘长度 1。
// 具体的符文图案（多边形 / 星芒 / 爪痕 / 冲击环）全部由 fragment 逐像素算，
// 参数走 uniform —— 于是同一套着色器能画出好几个技能各自不同的阵法。

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
    localPos = (Position.xz - Center) / max(Radius, 0.0001);
    vertexColor = Color;
}
