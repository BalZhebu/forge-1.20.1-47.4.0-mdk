#version 150

// 八荒戟 · 第八魂技「降雷」—— 雷柱。
//
// 几何是一个「广告牌」四边形：底边中心在 Base，沿相机右向量 Right 展开 ±HalfWidth，
// 沿世界 Y 方向抬高 Height。这样无论从哪个角度看，雷柱都正对镜头。
//
// 顶点写的是「世界坐标 - 相机坐标」（相机相对），ModelViewMat / ProjMat
// 由 BufferUploader.drawWithShader 填充为相机视图矩阵与投影矩阵。
//
// 本工程的顶点格式统一是 DefaultVertexFormat.POSITION_COLOR（位置 + 颜色，不带 UV），
// 所以 (u, v) 在这里从世界坐标反算：
//   u = 沿 Right 的偏移 / HalfWidth  ∈ [-1, 1]
//   v = 离底面的高度 / Height        ∈ [ 0, 1]

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 Base;
uniform vec3 Right;
uniform float HalfWidth;
uniform float Height;

out vec2 boltUV;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vec3 rel = Position - Base;
    boltUV = vec2(
        dot(rel, Right) / max(HalfWidth, 0.0001),
        rel.y / max(Height, 0.0001)
    );
    vertexColor = Color;
}
