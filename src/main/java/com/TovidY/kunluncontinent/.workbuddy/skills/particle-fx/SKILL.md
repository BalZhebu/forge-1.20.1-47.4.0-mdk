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

## 第一原则：能贴图就贴图，不要用原版粒子拼形状

**用户明确否决过**"用一圈 glow/end_rod 粒子假装魂环"这种做法。原版粒子拼出来只是
一圈光点，看不出是什么东西。项目里已经有魂环贴图（`textures/picture/particletext.png`、
`shenhuan.png`），**应该把贴图本身做成自定义粒子渲染**。

判断标准：如果要表现的是一个**具体物件**（魂环、符文、图案），走自定义粒子；
只有在表现"氛围"（火星、烟、灰尘、光点）时才用原版粒子。

自定义粒子的完整接入链路（缺一步就看不到贴图）：

| 步骤 | 位置 | 要点 |
|---|---|---|
| 1 注册类型 | `Init/ModParticles.java` | `DeferredRegister<ParticleType<?>>` + `SimpleParticleType`；**注册名必须与 JSON 文件名一致** |
| 2 放贴图 | `assets/<modid>/textures/particle/<name>.png` | 粒子图集只收这个目录；**必须带 alpha**，否则渲染成不透明方块 |
| 3 贴图定义 | `assets/<modid>/particles/<name>.json` | `{"textures":["<modid>:<name>"]}` |
| 4 注册 Provider | `KlMain.ClientModEvents`（`Dist.CLIENT`） | `RegisterParticleProvidersEvent#registerSpriteSet` |
| 5 粒子类 | `extends TextureSheetParticle` | Provider 里把 `SpriteSet` 存静态字段 |

带参数的行为（跟随实体、自定义缩放曲线）**不能用原版 `addParticle`** ——
它只有 6 个 double。做法是 Provider 里缓存 `SpriteSet`，然后自己写构造器 +
静态 spawn 方法，直接 `Minecraft.getInstance().particleEngine.add(particle)`。
**这条路径完全不需要网络包，`ParticleType` 注册只是给 `registerSpriteSet` 一个 key。**
范例见 `HunhuanRingParticle` + `ClientHunhuanRingFx`。

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

## 让特效对其他玩家也可见

**先查这件事是不是原版已经在同步了 —— 90% 的情况都不需要自己发包。**

| 要同步的信息 | 原版现成机制 | 自定义包 |
|---|---|---|
| 粒子 / 音效 | `ServerLevel#sendParticles`、`level.playSound` 天然广播给附近所有人 | 不需要 |
| "某玩家正在做某事" | **乘客同步**（`player.getVehicle()`） | 不需要 |
| "某实体的某个数值" | **`SynchedEntityData`**（`defineSynchedData` + `get/set`） | 不需要 |

范例：吸收魂环动画。
玩家吸魂环 = `player.getVehicle() instanceof HunhuanEntity`（乘客同步），
年限 = `HunhuanEntity.getNianxianSync()`（`EntityDataAccessor<Long> NIANXIAN`）。
两个都是原版自动同步的，所以：

```
客户端每 tick 遍历 ClientLevel#players()
  → 谁骑在 HunhuanEntity 上，就给他 spawn 一枚魂环粒子；下车就放手
entity/hunhuan/ClientHunhuanRingFx.java   ← 唯一的驱动，零网络包
```

好处：不会因为包丢失而卡住，代码量是"两包一管理器"方案的 1/5。
**教训**：上一版为此写了 `PacketHunhuanAbsorbSync` + `PacketHunhuanParticles` +
`ClientHunhuanAbsorbManager`，全被删了。

### 派生技巧：区分"成功"与"中断"、以及"被骑乘时不渲染"

**区分两种收场**（同样零发包）：

| 情况 | 原版能观察到的差异 |
|---|---|
| 成功 | 服务端 `discard()` 掉那个实体 → 客户端查不到它 |
| 中断 | 实体还在，只是玩家不再骑它 |

⚠️ 但"乘客变更包"和"实体移除包"**到达顺序不确定**，所以不能立刻下结论 ——
让粒子进一个 `SETTLING` 阶段，宽限几 tick（现用 4）再判。

**长动画建议改成阶段机**（`enum Phase { FOLLOW, SETTLING, CONVERGE, FADE }` +
`phaseTicks`），比一堆布尔标志好维护得多。小技巧：`phaseTicks` 允许从**负数**起步，
就能表达"先原地停 60 tick 再开始淡出"。

**想让某实体在被骑乘时不渲染**：在它的 `EntityRenderer.render` 里

```java
if (!entity.getPassengers().isEmpty()) return;
```

- **不要**用 `isInvisible()`/`setInvisible()` —— `EntityRenderer` 本身不检查那个标志，
  还得自己加判断，而且要多一份服务端状态。
- 乘客是原版同步的（`ClientboundSetPassengersPacket`），所以别人骑上去本地也正确；
  **玩家一下车自动恢复显示**，零状态管理。范例：`HunhuanRender`。

### ⚠️ 改了游戏数据后，记得刷新客户端的"渲染缓存"

`PWRenderPlayerEvent.entityWuhunCacheMap` 是**按 UUID 缓存的一份快照**，
只在「登录 / 换维度 / 手动开关魂环 / 收到 `SyncWuhunDataPacket`」时刷新，
而 `renderPlayerEventPost` 只在 `cache == null` 时懒加载一次 ——
**缓存建起来之后就再也不会自动更新**。

症状：数据明明改了（比如刚吸收完一个魂环），玩家身上却看不到，
要手动关开一次武魂才出现。

修法：任何"魂环列表变化"的地方都补一次广播，
统一走 `PlayerHunhuanAPI.broadcastWuhunRings(ServerPlayer, boolean playAnimation)`。
推而广之：**这个项目里凡是给实体挂"客户端缓存快照"的渲染，改数据时都要主动推一次。**

### ⚠️ 第一人称下 `RenderPlayerEvent` / `RenderLivingEvent` 不触发

`LevelRenderer` 的实体循环里有这么一条判定：

```java
(entity != pCamera.getEntity() || pCamera.isDetached()
 || pCamera.getEntity() instanceof LivingEntity && ((LivingEntity)pCamera.getEntity()).isSleeping())
```

第一人称时摄像机**贴在自己身上**（`isDetached()` = false），整条为 false
→ **本地玩家实体被整个跳过** → `PlayerRenderer.render` 不跑 → 事件不触发。
所以"自己身上/脚下的东西"在第一人称默认必然看不见。

要补渲染就换 `RenderLevelStageEvent`（`Stage.AFTER_ENTITIES`），
它的 PoseStack 是**相机相对、单位 = 格**：

```java
poseStack.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
```

⚠️ **但位置必须自己做 tick 插值**，这个事件不会帮你插：

```java
double px = Mth.lerp(partialTick, entity.xOld, entity.getX());   // y / z 同理
```

直接取 `getX()` 是"当前 tick 的快照"，而画面里其它一切（相机、实体、粒子）都在插值 ——
差多少就每 tick 抖多少。尺寸小的物件（比如 0.2 格的魂环）看起来就是"疯狂抽动"。
`LevelRenderer.renderEntity` 也是这么插值的，照抄即可。

（第一人称的走路视角晃动 `bobView` 已经包含在 `camera.getPosition()` 里，
用 camPos 反推位置会跟着一起晃、与世界同相，不用额外补偿。）

**关键：不要猜 poseStack 空间用的是什么单位，把原路径的变换链一字不差抄一遍。**
以玩家为例（对应 `LivingEntityRenderer.render`）：

```java
float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));   // setupRotations
poseStack.scale(-1.0F, -1.0F, 1.0F);                            // 翻转 Y
poseStack.scale(0.9375F, 0.9375F, 0.9375F);                     // PlayerRenderer.scale
poseStack.translate(0.0F, -1.501F, 0.0F);
// 之后直接复用第三人称那套绘制调用
```

照抄就一定和第三人称同一个空间 —— 比推导"1 单位是 1 格还是 1/16 格"省事得多。

### 加一个玩家配置开关

- `PlayerAttributeCapability.configFlags` 是 **long 位掩码**，默认 `0xFFL`（bit0~7 开）；
  `isConfigOpen(i)` 读位 / `toggleConfig(i)` 异或位。**新索引只要 ≥8，默认就是关的。**
- 只改 `screen/attribute/config/ConfigScreen`：
  `rawItems` 加 `new ConfigItemData("名字", 索引)`、`getToggleTooltip` 加一个 case
  （想要多行说明就多塞几个 `Component.literal`）。按钮行为、发包、消息都是循环统一处理的。

只有原版**确实没有**同步的状态（纯客户端表现、自定义 UI 开关）才自己发包：
- 包注册：`NetworkHandler.register(X.class, X::encode, X::decode, X::handle)`；
  包体用 `FriendlyByteBuf`，字段顺序 encode/decode 必须一致。
- 广播：遍历 `serverLevel.players()`，用 `distanceToSqr` 卡半径（如 64 格）逐个
  `NetworkHandler.sendToClient(packet, sp)`。吸收者本人距离为 0，自然包含在内。
- 客户端类上标 `@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)`，
  包体里引用它只写在 `ctx.get().enqueueWork(() -> ...)` 的 lambda 里 ——
  这样专用服务器上永远不会加载到 `Minecraft` 类。

## 自己写粒子（`extends TextureSheetParticle`）的坑

- **不要调 `super.tick()`**：父类会按 `age >= lifetime` 自动回收，还会自己乱动位置。
  自己接管生死，`lifetime` 设个很大的值（`Integer.MAX_VALUE / 2`）防误回收。
- **必须手写 `this.xo/yo/zo = this.x/y/z`**（以及 `oRoll = roll`），
  否则渲染层拿不到上一帧位置、粒子会一跳一跳。
- 跟随实体：每 tick `level.getEntity(anchorId)` 取锚点；锚点没了 / 不再满足条件就
  转入**淡出**再 `remove()`，别突然消失。
- 用 `getLightColor()` 返回 `0xF000F0` 让粒子恒全亮（发光物件不该受环境光影响）。
- 缩放脉动想要"平时稳稳的、偶尔鼓一下"，用 `pow(0.5+0.5*sin(t*w), 3)` 这种曲线，
  纯 sin 会变成一直抽搐。

### 想让粒子躺平 / 有固定朝向 → 重写 `render`

**粒子默认是公告板（billboard）—— 永远正对摄像机**。所以"环形贴图"必然竖着立起来，
不管内容是什么。要让它像呼啦圈一样横着，必须重写
`Particle.render(VertexConsumer, Camera, float)`：

```java
Quaternionf rot = new Quaternionf()
        .rotateY(spin)                       // 绕世界 Y 自转
        .rotateX((float) (Math.PI / 2));     // 翻平：局部 +Z 法线 → 世界 +Y
```

坑：

1. **JOML 的 `rotateX/rotateY` 是右乘**（`q = q * R`），后调用的**先**作用于顶点。
   所以上面这种写法 = `Ry(a) · Rx(π/2) · v` → 先翻平再自转，自转轴保持竖直 ✓
2. **翻平要用 `+π/2`**。四边形按 `(-1,-1)→(-1,1)→(1,1)→(1,-1)` 绕序时法线是**局部 -Z**；
   绕 X 轴 θ 把 (0,0,-1) 映射到 (0, sinθ, -cosθ)，θ=+π/2 才得到 (0,1,0) 朝上。
3. **平躺的四边形要正反各画一遍**。`PARTICLE_SHEET_TRANSLUCENT.begin()` 不设置面剔除状态，
   cull 是上一段渲染遗留的、不确定。只有一片四边形，双面输出开销可忽略但能保证看得见。
4. 平移量照抄父类：`Mth.lerp(partialTick, xo, x) - camera.getPosition().x()`。
5. **`quadSize` 是半宽**，四边形边长 = `2 × quadSize`。
   想知道贴图里的图形占多少：算一下非透明像素的 bbox（魂环贴图占满 96%
   → 显示直径 ≈ `1.9 × quadSize`）。
6. 重写 render 后**包围盒还是原来那个 0.2 的小盒子**，会被裁掉大半 →
   构造时 `setSize(quadSize * 2.2F, ...)` 放大，并 `shouldCull()` 返回 `false`。

### 染色：先确认贴图底色，别盲目"留白打底"

粒子颜色 `rCol/gCol/bCol` 是**乘**在贴图上的。所以：

- 贴图偏白（魂环 `particletext.png` 实测平均 RGB=(244,244,244)、**饱和度恒为 0**）
  → 直接 `setColor(主题色)` 就能得到纯正颜色。
  **千万不要"往白色兑水"**（`1-(1-c)*0.6` 之类），那只会把颜色冲淡 —— 已被用户指出过一次。
- 贴图本身有颜色 → 只能做偏色（tint），换不了色；想换色得另做一张白底贴图。

判断方法（无 PIL 也能做）：python 解 PNG，统计 alpha>32 的像素的平均色与饱和度中位数。

## 注意：不要轻易改魂环本体的渲染

`PWRenderLivingEvent.renderHunhuan → renderAnimation/renderHunhuanAttribute` 里画的是
**裸顶点（±6 单位）**，世界尺度未确认（`KLRenderApi.renderStart` 没有做 1/16 缩放），
且 `renderHunhuanAttribute` 会原地 `matrix4f.scale(...)`。
想给魂环本体加"多环堆叠 / 位移"很容易做出尺寸错乱，
优先在**粒子层**做环绕（半径以方块为单位，完全可控）。

## 校验流程（必做）

本机 bash 缺 `dirname`/`ls`，PowerShell 的 stdout 常被吞，所以：

```powershell
Set-Location 'D:\xiangmu\forge-1.20.1-47.4.0-mdk'
& .\gradlew.bat compileJava --offline --console=plain 2>&1 |
    Out-File -Encoding utf8 'D:\xiangmu\forge-1.20.1-47.4.0-mdk\compile_log.txt'
```

然后用 Read 读 `compile_log.txt`，确认 `BUILD SUCCESSFUL`，最后删掉这个临时日志。
