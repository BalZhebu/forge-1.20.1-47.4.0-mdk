---
name: particle-fx
description: >-
  在昆仑大陆（kunluncontinent，Forge 1.20.1）模组里编写/修改任意技能的粒子特效时使用。
  提供 ParticleFx 工具类的完整 API、各武魂配色母题、必须遵守的项目规范，以及编译校验流程。
  触发场景：新增魂技、美化技能特效、调整粒子密度、修改 PWPlayerTickEvent 里的持续型特效。
agent_created: true
---

# 昆仑大陆 技能粒子特效开发

## 何时用

- 新增一个魂技类（`item/baseskillist/<武魂>/<序号>/`）需要写视觉表现
- 觉得某个技能「特效难看」要重做
- 要调整粒子密度 / 性能 / 配色
- 要改延迟型技能（碎星、裂地、不周倾、裂天、八荒寂灭、虚空云）的表现

## 铁律

1. **只改视觉层**。数值（`getBaseCost` / `getDamageMultiplier` / `getCastTime` /
   `getCooldownTicks` / `applyPenalty`）与伤害判定逻辑一律不动。
2. `BaseSkillItem.executeEffect` 有 **3 参**（自动算伤害）和 **4 参**（带 finalDamage）
   两个重载，部分技能同时重写两者。改特效时把视觉抽成一个私有方法，两个入口都调它。
   例：`SkillPohun3.runSkillLogic(level, player, powerMultiplier, finalDamage)`。
3. **延迟型技能的后续特效不在技能类里**。技能类只负责「起手式」，
   持续脉冲 / 落地冲击在 `event/server/PWPlayerTickEvent.java`。
   涉及 NBT 键：`SuiXingTimer`、`LieDiActive`、`BuzhouQing_Active`、
   `Liejinhu8_Active`、`Bahuang9_Active`、tag `PohunVoidZone`。
4. 第 7 魂技（`zhenshen/` 包）只做轻量美化，不要堆量。

## 标准写法

```java
if (!level.isClientSide) {
    ServerLevel serverLevel = (ServerLevel) level;

    // ...原有伤害/召唤/增益逻辑...

    ParticleFx fx = ParticleFx.of(level, player);   // 自动读粒子优化开关 → lod 0.5
    Vec3 origin = player.getEyePosition();          // 需要复用的变量提到 if 之外！
    double rot = serverLevel.getGameTime() * 0.4;   // 同上
    if (fx != null) {
        fx.budget(1800);
        fx.magicCircle(ParticleTypes.END_ROD, ParticleTypes.ENCHANT,
                player.position(), ParticleFx.Axis.Y, 4.0, rot, 2, 0.35);
    }
}
```

- `ParticleFx.of(level)` 只在服务端返回非 null；客户端返回 null，所以要判空。
- 延迟回调里（`serverLevel.getServer().tell(new TickTask(...))`）重新 `ParticleFx.of(level, player)`。
- Tick 事件里已有 `cap`/`particleOpt` 时，用
  `ParticleFx.of(level).lod(particleOpt ? 0.5 : 1.0)` 更省。

## ParticleFx API 速查

包：`com.TovidY.kunluncontinent.effect.ParticleFx`

链式控制：`lod(double)` 细节倍率(0.15~4) · `size(double)` 全局半径缩放 · `budget(int)` 粒子上限

| 分类 | 方法 |
|---|---|
| 基础 | `dot(type, pos[, vel])` · `bloom(type, pos, count, spread, speed)` · `line(from, to, step[, jitter, speed, drift])` · `polyline` · `burst(type, center, count, speed, outward)` |
| 圆环 | `circle(type, c, Axis, r, pts, phase, jitter)` · `circle(c, u, v, ...)` 任意朝向 · `dashedRing(...segments, fillRatio, phase, jitter)` **打散环** · `ringStack(base, Axis, r, height, layers, pts, phase, taper, twist)` **自定义高度环柱** · `shockRing` |
| 多边形 | `polygon(c, Axis, r, sides, rot, jitter)` · `star(c, Axis, rOut, rIn, tips, rot, jitter)` · `roseCurve(...)` |
| 魔法阵 | `magicCircle(ring, glyph, c, Axis, r, rot)` · `magicCircle(..., layers, layerGap)` **多层立体阵** · `magicPillar(ring, glyph, core, base, r, height, rot)` |
| 立体 | `spiral(base, Axis, r0, r1, height, turns, phase, pts, jitter)` · `helix(base, Axis, r, height, turns, strands, phase, pts)` · `cone(apex, dir, len, baseR, pts, phase, ribs)` · `vortex` · `sphere(c, r, count, coverage, jitter)` · `dome(base, r, rings, seg)` · `cylinder` · `column(shell, core, base, r, height, strands, phase, twist, taper)` **自定义高度光柱** · `pillars(c, ringR, count, height, phase)` |
| 任意方向 | `ringAround(c, dir, r, pts, phase, jitter)` · `spiralAround(origin, dir, r0, r1, len, turns, phase, pts, jitter)` · `helixAround(origin, dir, r, len, turns, strands, phase, pts)` |
| 战斗 | `slash(origin, dir, rotationAxis, r, arcDeg, thickness, phase)` 月牙剑气 · `fanBlades(...blades, r, spreadDeg, thickness)` · `crossSlash` · `sphereBlades(edge, tip, c, r, seeds, bladeLen, spin)` 剑气球面 |
| 曲线 | `wave(origin, fwd, side, len, amp, cycles, phase, pts, yOffset)` · `lightning(from, to, segments, jag, phase)` |
| 数学 | `rotate(v, axis, angle)` 罗德里格 · `ortho(dir)` · `map` · `easeOut/easeIn/smooth/lerp/frac/clamp` · `TAU` |

## 各武魂配色母题

| 武魂 | 粒子类型 |
|---|---|
| 破魂枪 | `SOUL` `SOUL_FIRE_FLAME` `CRIMSON_SPORE` `REVERSE_PORTAL` `SCULK_SOUL` |
| 八荒戟 | `FLAME` `LAVA` `SOUL_FIRE_FLAME` `CAMPFIRE_COSY_SMOKE` `ELECTRIC_SPARK` |
| 裂金虎 | `CRIT` `GLOW` `ELECTRIC_SPARK` `WAX_ON` `END_ROD`（啸月：昼=FLAME 系 / 夜=END_ROD 系） |
| 磐石巨猿 | `CAMPFIRE_COSY_SMOKE` `LARGE_SMOKE` `WHITE_ASH` `SMOKE` `BLOCK(STONE)` |

## 反复踩过的坑

1. **精确落点必须用 `count = 0`**：`sendParticles(type,x,y,z,0,vx,vy,vz,1.0)` →
   客户端在精确坐标生成 1 个粒子，速度 = `(vx,vy,vz)*speed`。
   `count > 0` 会被高斯噪声打散，只能用于「体积感」（`bloom` 已封装）。
2. **本版本没有 `ParticleTypes.DUST_PLUME`**（1.21 才加）。岩土尘土用
   `WHITE_ASH` / `ASH` / `CAMPFIRE_COSY_SMOKE`。
3. 求横向单位向量**不要**写 `look.cross(new Vec3(0,1,0)).normalize()` ——
   垂直视角叉积为零向量，normalize 得 NaN。用 `ParticleFx.ortho(dir)`。
4. **作用域**：在 `if (fx != null) { ... }` 里声明的 `double rot` / `Vec3 origin`，
   块外引用不到会直接编译失败。要复用的变量声明在 `if` 之前。
5. `label:` 这种写法不要用；一次 Write 只写一个完整文件，避免半截文件被覆盖。

## 校验流程（必做）

本机 bash 缺 `dirname`/`ls`，PowerShell 的 stdout 常被吞，所以：

```powershell
Set-Location 'D:\xiangmu\forge-1.20.1-47.4.0-mdk'
& .\gradlew.bat compileJava --offline --console=plain 2>&1 |
    Out-File -Encoding utf8 'D:\xiangmu\forge-1.20.1-47.4.0-mdk\compile_log.txt'
```

然后用 Read 读 `compile_log.txt`，确认 `BUILD SUCCESSFUL`，最后删掉这个临时日志。
