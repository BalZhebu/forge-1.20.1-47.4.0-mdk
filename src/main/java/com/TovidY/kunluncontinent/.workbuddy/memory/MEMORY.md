# 昆仑大陆（kunluncontinent）项目长期备忘

## 构建与校验
- 根目录：`D:\xiangmu\forge-1.20.1-47.4.0-mdk`
- 编译校验命令（本机 PATH 里 bash 缺 `dirname`/`ls`，必须走 PowerShell）：
  `Set-Location 'D:\xiangmu\...'; & .\gradlew.bat compileJava --offline --console=plain 2>&1 | Out-File -Encoding utf8 <log>`
  PowerShell 的 stdout 常被吞，务必重定向到文件再读。
  实测 `--offline` 可用（依赖已在 gradle 缓存里），单次约 40~90s。
- 本版本 `ParticleTypes.DUST_PLUME` **不存在**（1.21 才有），不要使用。

## 技能体系结构
- 基类 `item/baseskillist/BaseSkillItem`：抽象方法 `getBaseCost / getDamageMultiplier /
  getCastTime / getCooldownTicks`；`executeEffect` 有 3 参（自动算伤害）与 4 参（带 finalDamage）
  两个重载，部分技能同时重写两者 —— 改特效时两个入口都要覆盖或抽公共私有方法。
- 4 武魂目录：`pohunqiang`(破魂枪) / `bahuangji`(八荒戟) / `liejinhu`(裂金虎) / `juyuan`(磐石巨猿)，
  每个武魂 1~9 魂技；第 7 魂技统一在 `zhenshen/` 包下。
- 技能随机分配表：`item/baseskillist/SkillLibrary.java`。
- **延迟型技能**的后续表现不在技能类里，而在 `event/server/PWPlayerTickEvent.java`：
  `SuiXingTimer`(碎星领域) / `LieDiActive`(裂地) / `BuzhouQing_Active`(不周倾) /
  `Liejinhu8_*`(裂天) / `Bahuang9_*`(八荒寂灭) / `PohunVoidZone` tag 的虚空云。
  改这类技能要同时看技能类（起手）和 Tick 事件（持续/落地）。
- 玩家「粒子优化」开关：`PlayerAttributeCapability.isConfigOpen(10)`，各处都据此降密。

## 粒子特效规范（统一走 effect/ParticleFx）
- 服务端专用：`ParticleFx fx = ParticleFx.of(level, player); if (fx == null) return;`
- 三档控制：`.lod(...)` 细节倍率 / `.size(...)` 全局半径缩放 / `.budget(n)` 粒子预算上限
- 形状一律用工具类方法，不要再手写 `for (int i = 0; i < 360; i += 10)` 这类散装循环
- 求横向单位向量用 `ParticleFx.ortho(dir)`，不要手写叉积（垂直视角会 NaN）
- 需要复用的 `rot` / `origin` 等局部变量必须声明在 `if (fx != null)` 之外

## 配色母题（各武魂视觉区分）
- 破魂枪：`SOUL` / `SOUL_FIRE_FLAME` / `CRIMSON_SPORE` / `REVERSE_PORTAL`（暗红+幽魂）
- 八荒戟：`FLAME` / `LAVA` / `SOUL_FIRE_FLAME` / `CAMPFIRE_COSY_SMOKE`（金红+岩浆）
- 裂金虎：`CRIT` / `GLOW` / `ELECTRIC_SPARK` / `WAX_ON` / `END_ROD`（金色+雷光；啸月分日月两态）
- 磐石巨猿：`CAMPFIRE_COSY_SMOKE` / `LARGE_SMOKE` / `WHITE_ASH` / `BLOCK(STONE)` / `SMOKE`（岩土灰）
