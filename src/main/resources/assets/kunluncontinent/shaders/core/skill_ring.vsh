#version 150

// 「锁链环」几何 —— 一个平面四边形，用**顶点色**携带局部 UV。
//
// 为什么不用 uniform 传中心：一个被禁锢的生物要画好几个环，一场战斗可能十几个生物，
// 若每个环都要单独设 uniform、单独 draw，帧率会很难看。
// 这里的取巧办法：Java 侧把「四边形内的归一化坐标 (u,v)」写进顶点色的 R/G，
// alpha 写进 A —— 于是所有环可以塞进**同一个 buffer、同一次 draw**，
// uniform 只有全局的 Time / Intensity。
//
//   ringUV      = Color.rg * 2 - 1   ∈ [-1, 1]²（0,0 = 环心）
//   vertexColor = (1,1,1, Color.a)   （颜色交给 fsh，这里只传 alpha）

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 ringUV;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    ringUV = Color.rg * 2.0 - 1.0;
    vertexColor = vec4(1.0, 1.0, 1.0, Color.a);
}
