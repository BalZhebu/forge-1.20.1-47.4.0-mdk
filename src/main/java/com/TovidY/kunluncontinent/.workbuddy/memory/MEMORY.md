# 昆仑大陆（kunluncontinent）项目长期备忘

## 构建与校验
- 根目录：`D:\xiangmu\forge-1.20.1-47.4.0-mdk`
- 编译校验命令（本机 PATH 里 bash 缺 `dirname`/`ls`，必须走 PowerShell）：
  `Set-Location 'D:\xiangmu\...'; & .\gradlew.bat compileJava --offline --console=plain 2>&1 | Out-File -Encoding utf8 <log>`
  PowerShell 的 stdout 常被吞，务必重定向到文件再读。
  实测 `--offline` 可用（依赖已在 gradle 缓存里），单次约 40~90s。
- 本版本 `ParticleTypes.DUST_PLUME` **不存在**（1.21 才有），不要使用。
- 查原版 API 签名（拿不准时最可靠）：解压
  `~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.0_mapped_parchment_2023.09.03-1.20.1/forge-1.20.1-47.4.0_mapped_parchment_2023.09.03-1.20.1-sources.jar`
  就能读到打补丁后的完整 MC 源码。
- 跑数据生成：`& .\gradlew.bat runData --offline --console=plain`，约 2~3 分钟，
  产物落在 `src/generated/resources/`。

## 成就（进度）系统规范
- **只有一个触发器**：`advancement/KunlunTrigger`（注册名 `kunluncontinent:kunlun_event`），
  靠 JSON 里的 `conditions.event` 字符串区分事件，新增成就不用写新 Trigger 类。
- 玩法侧只调 `advancement/AchievementAPI` 的语义化方法（`onAwakenWuhun` /
  `onAbsorbHunhuan` / `onAlchemySuccess` / `onReincarnate` / `onTowerStart` /
  `onTowerCleared` / `onGodExamStart` / `onEnterThunderRealm`），事件 ID 常量也在这里。
- datagen 侧只调 `datagen/advancement/AdvancementHelper`：
  `task/goal/challenge/root` + `event/hasItems/levelReached/onJoin`。
  一行一个成就，语言键默认 `adv.kunlun.<路径>.title/.desc`。
- 成就路径统一 `kunluncontinent:main/<name>`，显示键在
  `datagen/lang/ModZhCnLangProvider` 的「成就类」区块。
- 能用「背包里有某物品」表达的成就（`hasItems`）不要埋点；只有行为类才埋点。
- 无玩家上下文的场景（如炼丹炉 `serverTick`）用
  `level.getNearestPlayer(x, y, z, 8.0, false)` 找到最近的玩家再触发。

## 魂环系统与"全服可见特效"范式
- 吸收魂环：`HunhuanEntity.interact()` 里 `player.startRiding(this)` 开始；
  `tick()` 中 `player.getVehicle() == this` 时提前 return（`livetime` 冻结）；
  进度在 `secondtick()`（每 20 tick）累加 `existenceTime`，
  阈值 `v = log10(年限) * 10 + 10` 秒；到点 `handleAbsorbed()` → `discard()`；
  玩家中途下来则 `existenceTime` 归零（中断）。
- 魂环本体渲染：`HunhuanRender`（注册于 `EntityInit.HUNHUAN`），
  绘制逻辑在 `PWRenderLivingEvent.renderHunhuan / renderAnimation / renderHunhuanAttribute`，
  且**只有观看者自己的 `isConfigOpen(4)` 打开时才渲染**。
- ⭐ **判断"某玩家正在做某事"前，先查这件事是不是原版已经在同步**：
  吸收魂环 = 玩家骑在 `HunhuanEntity` 上（**乘客同步** `getVehicle()`），
  年限 = `HunhuanEntity.getNianxianSync()`（**SynchedEntityData**）。
  两者都是原版自动同步给所有客户端的 → 吸收动画**一个自定义包都不用发**，
  每个客户端遍历 `ClientLevel#players()` 自己判断即可，天然全服可见、丢包也不会卡住。
  范例：`ClientHunhuanRingFx`（唯一驱动）+ `HunhuanRingParticle`。
  教训：上一版为它写了两个 S2C 包 + 一个客户端状态管理器，全被删掉了。
- **"吸收成功"与"玩家中途下车"的判别**（不用发包）：
  成功 = 服务端 `handleAbsorbed()` 里 `discard()` 掉魂环实体 → 客户端看不到该实体；
  中断 = 魂环实体还在、只是玩家不骑了。
  但两个包到达顺序不确定 → 必须留几 tick 宽限期再判定（现用 4 tick）。
- **想让某个实体在"被骑乘时"不渲染**：在它的 `EntityRenderer.render` 里
  `if (!entity.getPassengers().isEmpty()) return;` 即可。
  **不要**去用 `isInvisible()`/`setInvisible()` —— `EntityRenderer` 本身不检查那个标志，
  还得自己加判断；用乘客判断还能自动恢复，零状态同步。
  范例：`HunhuanRender`（`HunhuanEntity` 的实体渲染器）。
- 只有原版**确实没同步**的状态（纯客户端表现）才自己发包：
  `network/server/PacketXxxSync` + `broadcast()` 按半径遍历 `serverLevel.players()`。
- ⚠️ **玩家/怪物身上的魂环渲染走客户端"永不失效"的缓存**：
  `event/client/PWRenderPlayerEvent.entityWuhunCacheMap`（`Map<UUID, EntityWuhunCache>`）。
  它**只在登录 / 换维度 / 手动开关魂环 / 收到 `SyncWuhunDataPacket` 时刷新**，
  `renderPlayerEventPost` 只在 `cache == null` 时懒加载一次。
  → **任何"魂环列表变化"都必须广播一次**，否则新魂环不会显示、要手动关开魂环才出来。
  统一入口：`PlayerHunhuanAPI.broadcastWuhunRings(ServerPlayer, boolean playAnimation)`
  （吸收完成由 `HunhuanEntity.handleAbsorbed` 调用，`playAnimation=false`）。
- `PlayerAttributeCapability.hunhuankuaiguan` = **当前显示的武魂下标**，`-1` = 关闭
  （开关逻辑在 `network/client/CPacketQiehuanWuhun`）。
  `getWuhunList()` / `getWuhunName()` 在关闭时返回 `null`，
  所以广播空列表正好保持隐藏，不用额外判空逻辑。
- `KLRenderApi.renderStart/End` 内部成对 `pushPose/popPose`；
  `renderAnimation/renderHunhuanAttribute` 原地改 matrix。改这两个渲染方法时注意尺度未知。

## 渲染事件选择
- ⚠️ **第一人称下 `RenderPlayerEvent` / `RenderLivingEvent` 不会为本地玩家触发**：
  `LevelRenderer` 的实体循环里有 `entity != pCamera.getEntity() || pCamera.isDetached() || ...`，
  第一人称摄像机贴在自己身上 → 整条为 false → **本地玩家实体被整个跳过**。
  想让"自己身上/脚下的东西"在第一人称可见，必须换 `RenderLevelStageEvent`
  （`Stage.AFTER_ENTITIES`）自己补一份。范例：`PWRenderPlayerEvent.renderFirstPersonRings`。
- **复刻 poseStack 变换链时不要猜单位**，直接把 `LivingEntityRenderer.render` 的
  `mulPose(Y, 180 - bodyYaw)` → `scale(-1,-1,1)` → `scale(0.9375)` → `translate(0,-1.501,0)`
  一字不差抄一遍，结果必然与原路径同一套空间。
- `RenderLevelStageEvent` 的 PoseStack 是**相机相对、单位=格**：
  `translate(pos.x - cam.x, ...)` 就能把原点移到任意实体处。
- ⚠️ **但位置必须自己做 tick 插值**：
  `Mth.lerp(partialTick, entity.xOld, entity.getX())`。
  直接取 `getX()` 是"当前 tick 快照"，而画面里其它东西都在插值 → 这个视觉每 tick 跳一格，
  小尺寸物件看起来就是"疯狂抽动"（`LevelRenderer.renderEntity` 也是这么插值的，照抄）。
- 第一人称的走路视角晃动（`bobView`）已经包含在 `camera.getPosition()` 里，
  用 camPos 反推位置就会跟着一起晃、与世界同相，**不需要额外补偿**。

## 玩家配置开关（ConfigScreen）
- `PlayerAttributeCapability.configFlags` 是 **long 位掩码**，默认 `0xFFL`（bit0~7 开），
  `isConfigOpen(i)` 读位 / `toggleConfig(i)` 异或位。**新增高位索引默认就是关的。**
- 已占用索引：1 生物魂环 / 2 玩家魂环 / 3 吸收经验提示 / 4 魂环实体 / 5 魂核实体 /
  6 屏幕UI / 7 屏幕UI位置（子页）/ 8 聚灵物品 / 9 NPC交易品级 / 10 粒子优化 /
  11 NPC魂环 / **12 第一人称魂环（默认关）**。
- 加开关只改 `screen/attribute/config/ConfigScreen`：`rawItems` 加一行
  `new ConfigItemData("名字", 新索引)` + `getToggleTooltip` 加一个 case。按钮行为是循环统一处理的。

## 特效铁律：优先用项目已有贴图做「自定义粒子」
- **不要用原版粒子拼形状来模仿某个具体物件**（例如"用一圈 glow 粒子假装魂环"）。
  原版粒子拼出来只是一圈光点，看不出是什么东西，观感很差 —— 已被用户明确否决过一次。
- 正确做法：`SimpleParticleType` + `TextureSheetParticle`，把贴图本身作为粒子渲染。
  现成范例：`ModParticles` + `HunhuanRingParticle` + 两个 `particles/*.json`
  + `KlMain.ClientModEvents#registerParticleProviders`。
- 接入链路（缺一步就看不到贴图）：
  1. 注册名必须与 `assets/<modid>/particles/<name>.json` **文件名一致**；
  2. 贴图放 `assets/<modid>/textures/particle/<name>.png`（粒子图集只收这个目录）；
  3. JSON `{"textures":["<modid>:<name>"]}`；
  4. 客户端 `RegisterParticleProvidersEvent#registerSpriteSet` 注册 Provider。
- 自己写粒子（`extends TextureSheetParticle`）时：
  - **不要调 `super.tick()`**（父类会按 `age >= lifetime` 自动回收、还会自己动位置），
    自己接管生死；
  - **必须手写 `this.xo/yo/zo = this.x/y/z`**（和 `oRoll = roll`），
    否则渲染层拿不到上一帧位置、粒子会一跳一跳；
  - 跟随实体就每 tick `level.getEntity(anchorId)` 重定位，锚点没了就**淡出**再 remove；
  - `getLightColor()` 返回 `0xF000F0` 让发光物件恒全亮；
  - "平时稳、偶尔动"的脉动用 `pow(0.5+0.5*sin(t*w), 3)`，纯 sin 会一直抽搐。
- ⭐ **粒子默认是公告板（永远正对摄像机）**，贴图必然"竖着面对你"。
  想让粒子**躺平 / 有固定朝向**，必须重写 `Particle.render(VertexConsumer, Camera, float)`：
  ```java
  Quaternionf rot = new Quaternionf().rotateY(spin).rotateX((float)(Math.PI/2));
  ```
  - JOML 的 `rotateX/rotateY` 是**右乘**（`q = q * R`），所以后调用的先作用于顶点：
    `rotateY(a).rotateX(π/2)` = 先翻平、再绕竖直轴自转。
  - 翻平要用 **+π/2**：四边形按 `(-1,-1)→(-1,1)→(1,1)→(1,-1)` 绕序时法线是**局部 -Z**，
    θ=+π/2 才把它转到世界 **+Y（朝上）**；用 -π/2 会朝下。
  - 平移量照抄父类：`Mth.lerp(partialTick, xo, x) - camera.getPosition().x()` 等。
  - **平躺四边形要正反各画一遍**：`PARTICLE_SHEET_TRANSLUCENT.begin()` 不设置面剔除状态，
    cull 是上一段渲染遗留的、不确定。反正只有一片四边形，开销可忽略。
  - **`quadSize` 是半宽**，四边形边长 = `2 × quadSize`。
    （实测魂环贴图图形占满整张图 96% → 显示直径 ≈ 1.9 × quadSize。）
  - 重写 render 后包围盒还是原来的小盒子，要 `setSize(...)` 放大 + `shouldCull()` 返回 `false`，
    否则环会被裁掉大半。
- **直接生成粒子不需要网络包**：Provider 缓存 `SpriteSet` → 自己写构造器 +
  静态 spawn → `Minecraft.getInstance().particleEngine.add(particle)`。
  `ParticleType` 注册只是为了给 `registerSpriteSet` 一个 key。
- 粒子贴图必须带 alpha（否则渲染成不透明方块）。
  无 PIL 时可用纯 python 处理 PNG：zlib 解 IDAT → 手动反滤波 → 重编码，改完回读验证。

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

## 魂环年限 → 视觉（唯一权威表在 `HunhuanAbsorbFx.Tier`）
- **魂环贴图 `particletext.png` / `shenhuan.png` 是纯白图形**（实测平均色 244/238，
  饱和度恒为 0，只有 alpha 变化）→ **染色 = 乘什么色得什么色**，
  不要再"留白打底"/往白色兑水，那样只会把颜色冲淡。
- 8 档配色，与 `PWRenderLivingEvent.renderHunhuanAttribute` 的 `setShaderColor` 口径一致：
  白(十年,`0xFFFFFF`) / 黄(百年,`0xFFFF00`) / 紫(千年,`0xCC00CC`) / 黑(万年,`0x1A1A1A`) /
  红(十万年,`0xFF0000`) / 橙金(百万年,`0xFF8A00`) / 蓝(千万年,`0x2E7BFF`) / 金绿(亿年,`0x22FF55`)。
- `Tier.size` 是粒子 **quadSize（四边形半宽）**；贴图图形占满 96%，**显示直径 ≈ 1.9 × size**。

## 神考（神位传承）系统
- 数据在 `godclass/GodRegistry`（海神/修罗神/天使神），任务类型 `GodTaskType`
  （KILL / ATTRIBUTE / ITEM_CHECK / ITEM_CONSUME / HUNHUAN_NIANXIAN）。
- 提交入口 `network/client/C2SCheckTaskPacket`（检查按钮），
  KILL 类型由 `event/server/KLivingDeathEvent.handleGodKillTask` 自动推进。
  两者完成时都走 `PlayerAttributeCapability.checkTaskCompletion`。
- ⭐ **奖励只走 `GodInfo.grantAttr(player, key, value)` 这一个出口**
  （`addAttrReward` / `addFinalAttrReward` 都只是注册它）。
  `dengji` 是特殊键，走 `grantLevels` 真升级而不是加属性。
- **第 9 考的奖励只在 `finalizeAscension`（飞升动画结束后）发一次**，
  `executeRewards` 里绝不能再发 —— 否则会双倍（积攒型神考会 1~9 考全部双倍）。
- 等级上限语义（`PlayerUpgradeSystem.isTupoDengji`）：
  **99 级在封神前是硬上限**（"请封神后再突破"），封神后才允许 99 → 100；
  100~198 突破需要魂环数 ≥ level/10；199 才是真满级。
  → `grantLevels` 判据：`currentLevel >= 99 && !cap.isGod()` 才折算成属性补偿。

## 属性取数：三种口径，别用错
| 口径 | 取法 | 用途 |
|---|---|---|
| 裸值 | `cap.getXxx()` | 存档、等级成长、发奖励时累加 |
| 聚合值 | `ModAttributeAPI.getXxx(entity)` | 结算伤害/防御；含魂环 `getWuhunBonus` + 魂骨 `getBoneOnlyStats` + 药水 `PotionAttribute` + 装备/套装 |
| **面板值** | `ModAttributeAPI.getPanelAttributeValue(player, key)` | **一切"按面板数值判定"的检查**（神考等） |

- ⚠️ 神考数值考核曾经用 `PlayerAttributeCapability.getGodAttributeValue(key)`（= 裸值），
  导致**面板达标却判定不通过**；该方法已删除，改用 `getPanelAttributeValue`。
- 面板口径（`AttributeScreen`）：**生命 = `player.getMaxHealth()`**，
  其余 = `ModAttributeAPI.getXxx(player)`。
- `player.getMaxHealth()` 能到百万级是因为 `KlMain.changeAttributesIO()` 用反射把
  `Attributes.MAX_HEALTH` 的 maxValue 改成了 `Float.MAX_VALUE`（原版上限 1024）；
  base 由 `PWPlayerTickEvent` 每 20 tick 同步成 `ModAttributeAPI.getMaxshengming()`。

## 配色母题（各武魂视觉区分）
- 破魂枪：`SOUL` / `SOUL_FIRE_FLAME` / `CRIMSON_SPORE` / `REVERSE_PORTAL`（暗红+幽魂）
- 八荒戟：`FLAME` / `LAVA` / `SOUL_FIRE_FLAME` / `CAMPFIRE_COSY_SMOKE`（金红+岩浆）
- 裂金虎：`CRIT` / `GLOW` / `ELECTRIC_SPARK` / `WAX_ON` / `END_ROD`（金色+雷光；啸月分日月两态）
- 磐石巨猿：`CAMPFIRE_COSY_SMOKE` / `LARGE_SMOKE` / `WHITE_ASH` / `BLOCK(STONE)` / `SMOKE`（岩土灰）
