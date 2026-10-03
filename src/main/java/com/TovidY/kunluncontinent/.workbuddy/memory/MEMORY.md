# 昆仑大陆（kunluncontinent）项目长期备忘

## 构建与校验
- 根目录 `D:\xiangmu\forge-1.20.1-47.4.0-mdk`。本机 bash 环境残缺，一律走 PowerShell / gradlew.bat，
  **stdout 重定向到文件再读**。
- 编译：`./gradlew.bat compileJava --offline --console=plain "-Dnet.minecraftforge.gradle.check.certs=false"`（40~90s）。
  `-D` 参数**必须加引号**，否则被当任务名。输出是 GBK，读中文错误要先 `iconv -f GBK -t UTF-8`。
  偶发 `Failed to validate certificate for libraries.minecraft.net` = ForgeGradle 联网问题，与代码无关，重试。
- datagen：`runData --offline --console=plain`，2~3 min，产物在 `src/generated/resources/`。
- 查原版/Forge API 签名最可靠：解压 `~/.gradle/caches/forge_gradle/.../forge-1.20.1-47.4.0_*-sources.jar`
  读打补丁后的 MC 源码。
- ⚠️ 本版本没有 `ParticleTypes.DUST_PLUME`（1.21 才有）。

## ⚠️ 工具坑
- **同一文件的多处 `Edit` 绝不能并行发出** —— 会静默丢改动。逐个改，改完 grep 复查。
- ⚠️ **`old_string` 换成更短的片段时要带足上下文**：曾把
  `return value;\n}\n\npublic static float getJingshenli` 缩成 `return value;\n}`，
  结果**把下一个方法的签名一起吃掉**。改连续的同形方法（尤其聚合 getter）必查。
- bash 里用 python `-c` 追加中文日志，反引号会被吞 → 写文件用 Write 工具。

## ⭐ 属性加点系统（`capability/playerattributes/AttributePointSpec`）
- **唯一权威表 = `AttributePointSpec` 枚举**（11 条，ordinal 即网络包下标）：
  生命值/强健 → 攻击力/强攻 → 防御力/铁壁 → 暴击率/锐意 → 暴击伤害/破军 →
  生命恢复/回春 → 吸血/噬魂 → 闪避/游龙 → 命中/锁魂 → 物穿/破甲 → 抗暴/坚心。
  每条含 `key/displayName/desc/perPointPercent/mode/color`；**加属性只加一行枚举**。
  每点收益按用户给的表（攻/防/生命/恢复 1.5%/1.2%，暴伤 2%，物穿 1%，吸血 0.4%，
  暴击率/命中/抗暴 0.3%，闪避 0.25%），单条上限 99 点。
- **⚠️ 两种结算模式不能统一**：
  - `Mode.MULT`（数值类：攻防生命暴伤物穿吸血回春）→ `value * (1 + 每点% × 点数)`
  - `Mode.FLAT`（概率类：暴击率/闪避/命中/抗暴）→ `value + 每点% × 点数`
  原因：基础暴击率只有 5%，走乘算 99 点也只有 12.4%，"封顶 30%"的设计定位达不到；
  加算是 5% + 29.7% = 34.7%，才对得上。**别看到"都是百分比"就统一成一种。**
- 结算**只能用 `ModAttributeAPI.withPoints(entity, key, nakedValue)`**，
  且**必须夹在裸值上、在武魂/魂骨/装备/药水相加之前**。
  ⛔ **绝不能放在 getter 结尾对总和乘算** —— 那样等于"开武魂后同样的 99 点收益翻十几倍"
  （实测 99 级 + 9 枚十万年魂环：攻击力 66,123 → 157,053，武魂那 6 万被白嫖）。
  这就是用户提的"本体属性 ≠ 武魂属性，点数不该一起放大"。11 条 key 一个都不能漏。
- 存档：`PlayerAttributeCapability.allocatedPoints` 是 `int[]`（下标=ordinal），
  NBT key `"AllocatedPoints"` 走 `putIntArray`；读档按当前枚举长度 `System.arraycopy`
  裁剪/补 0 → **加属性不用改存档格式**。
- ⭐ **转生继承**：`PlayerHunhuanAPI#zhuansheng` 里调
  `AttributePoints.inheritOnZhuansheng(newCap, oldCap)` —— 转生是整体重置，
  11 条已加点**全部清零、不返还**，但按**转生前已加总数**的 **10%~30%**（`0.10 + rnd*0.20`）
  折算成新存档的可用点数。
  - **取整必须向下取整、绝不四舍五入**：`(int)(invested * rate)` 直接截断。
    用户明确要求"有小数点的不能按四舍五入，必须扣除小数点，不能进1"。
    投入 99 点 → 最低 9 点（0.10×99=9.9→9）、最高 29 点。
  - 传 0（没点过）→ `clearAllocatedPoints()` + `setAttributePoints(0)`，返回 0。
  - 播报走 `sendSystemMessage`："投入 N 点 → 继承 M 点（10%~30% 折算，不返还）"。
- 规则：**只能加不能减**（`AttributePoints.allocate` 只接受正数）。
- ⭐ **重置的两条路共用 `AttributePoints.resetWithScroll(player, cap)`**：
  ① 右键 `ModItems.RESET_SCROLL`（`item/klitem/ResetScrollItem`）
  ② 点面板右下角"重置属性点"（`CPacketResetPoints`）。两条路都扣 1 个重置卷轴。
  卷轴不足 → **不扣、不重置**，只提示。`reset()` 是内部裸调用（不含扣道具）。
- ⚠️ **扣道具别用 `Inventory.clearOrCountMatchingItems(predicate)`** ——
  它的三参数重载会额外扣 `containerMenu.getCarried()`（鼠标正抓着的那摞），语义不可控。
  `consumeResetScroll` 自己遍历 `inv.items` + `inv.offhand.get(0)`，扣完必须
  `player.inventoryMenu.broadcastChanges()`（`setChanged` 不同步客户端）。
- ⚠️ **把 `ModItems` 里的 `new Item(new Properties(){ appendHoverText })` 换成具名Item 类时**：
  `appendHoverText` 是 `Item` 的方法、**不在 `Properties` 里**，必须一起搬进新类；
  且匿名类少一层括号（`})` → `})`，`Properties` 的 `}` 之后还要 `))`）。
  用户的物品常写成这种匿名子类形式，改的时候别把他配的 tooltip 弄丢了。
- 网络：`CPacketAllocatePoint(specIndex, amount)` / `CPacketResetPoints`，
  成功后走 `SynsAPI.synsPlayerAttribute` 立刻回同步（不然要等 tick 刷 MAX_HEALTH）。
- 面板 `screen/attribute/point/PointScreen`：两列 6+5，每行
  色条 + 别名 + `x/99` + 进度条 + 最右 `+`；右下角"重置属性点"。面板 400×236。
  ⚠️ **行内只显示别名 + 点数，不要加"当前值 → 加满后"之类的实时数值**（用户明确要求移除）。
  画色条高度要和文字对齐（文字 `y+2` 高 9px → 色条也 `y+2`~`y+11`），别比文字高一截。
- ⚠️ **`HandDrawnButton.renderTooltip` 内部判 `isHoveredOrFocused()`，点击后按钮会一直 focused
  → 点过 A 再移到 B，两个 tooltip 同时画出来、文本重叠。**
  `PointScreen.render` 里改成**自己按矩形过滤、只画鼠标真正压着的那个**，别依赖内部判断。
- 每条属性有 `displayName`（别名：强攻/铁壁…）和 `attrName`（原属性名：攻击力/防御力…）两个名字，
  tooltip 第一行是「别名 · 原属性名」，行内灰色文字是「当前值 → 点满后」。

## ⭐ 属性系面板页签（`screen/attribute/AttributeTabs`）
- **页签的唯一权威定义 = `AttributeTabs` 注册表**，各 Screen 的 `init()` 只调一行
  `AttributeTabs.buildTabs(this, AttributeTabs.PAGE_XXX);`（另有 `buildConfigButton(this)`）。
  **新增面板只改 1 处注册表**（以前 4 个 Screen 各抄 40 行）。
- API：`tab(id, label, iconSupplier, MenuProvider, visibleCondition)` + `ensureRegistered()`（幂等）
  + `buildTabs(host, pageId)`；页签 id 常量 `PAGE_ATTRIBUTE/HUNGU/HUNHUAN/SHENKAO/CONFIG/POINT`；
  尺寸常量也收在这里。
- `network/server/PacketSyncPage` **没有 if-else 链**，靠 `AttributeTabs.byId(id)` 查表分发；
  服务端也校验 `tab.isVisible()`。另有 `openPage(ServerPlayer,int)` 供指令调用。
- ⚠️ **`leftPos/topPos/imageWidth/font/addRenderableWidget` 在 Screen 里全是 protected**，
  静态工具类访问不到 → `AttributeTabs.Host` 接口让各 Screen 转发。
  `font()` 返回 `net.minecraft.client.gui.Font` 不是 int。

## ⭐ UI 提示优先用原版，不要自绘
- **"在物品栏上方显示一行字" = 原版 ActionBar**：
  `serverPlayer.displayClientMessage(Component.literal("§c提示"), true);`
  服务端直接调，原版内部自动发包，**不需要自定义包/客户端类**。（曾白写一个 HUD 类 + S2C 包，已删。）
- 真正需要多行/自定义位置/自定义动画时才自己画，钩子用 `RenderGuiEvent.Post`
  （**不要用 `CustomizeGuiOverlayEvent.DebugText`**，那个只在 F3 打开时触发）。
- 聊天栏 = `sendSystemMessage`；物品栏上方 = ActionBar。两者别混。
- 项目风格用 `screen/KunlunGuiHelper`（圆角面板/按钮/动画）。

## 渲染事件选择（血泪教训）
- ⚠️ **第一人称下 `RenderPlayerEvent` / `RenderLivingEvent` 不为本地玩家触发**
  （`LevelRenderer` 实体循环里 `entity != pCamera.getEntity() || ...` 整条 false）。
  想让"自己身上/脚下的东西"第一人称可见，必须换 `RenderLevelStageEvent`
  （`AFTER_ENTITIES`）自己补。范例：`PWRenderPlayerEvent.renderFirstPersonRings`。
- ⭐ **`RenderPlayerEvent.Post` 与 `RenderLivingEvent.Post` 空间不同（二者勿混！曾因混用翻车）**：
  - `RenderPlayerEvent.Post`：`super.render()`（已 popPose）之后触发
    ⇒ **相机相对、原点在脚底、1 单位 = 1 格**。
  - `RenderLivingEvent.Post`：**仍在模型变换链内**（16 单位 = 1 格、Y 翻转 scale -1,-1,1、朝向已旋转）。
    证据：词条牌按"1 格"假设画 `bbHeight+0.4` 实际 microscopic 糊在怪脚上。
  - 画"世界坐标、以格为单位"的东西（头顶名牌/立牌）→ 用 `RenderLevelStageEvent`
    （AFTER_PARTICLES）+ `translate(lerp(pos) - cam)`，别用 `RenderLivingEvent.Post`。
  - **不要在补渲染时再叠 `180-bodyYaw / scale(-1,-1,1) / 0.9375 / -1.501`** ——
    那是模型层的变换，叠上去会整体推高约 1.2 格并让"背后"反向。（曾踩过，已修）
- `RenderLevelStageEvent`（AFTER_ENTITIES）的 PoseStack 与上面同一套空间，
  `translate(pos.x - cam.x, ...)` 把原点移到任意实体处即等于 Post 的空间。
- ⚠️ **位置必须自己做 tick 插值** `Mth.lerp(partialTick, entity.xOld, entity.getX())`，
  否则小尺寸物件每 tick 跳一格 = "疯狂抽动"。第一人称 `bobView` 已含在
  `camera.getPosition()` 里，**不需要额外补偿**。

## ⭐ 击杀掉落全表（2026-10-03 梳理，13 类）
主入口 `KLivingDeathEvent#livingDeathEvent` → 必须 `source.getEntity() instanceof Player`
+ `entity instanceof PlayerNpcEntity` 直接 return（NPC 不走这套）+ `TowerSpawned` 塔怪也 return。

| # | 掉落 | 实现处 | 触发条件 |
|---|---|---|---|
| 1 | **魂环**（生成 HunhuanEntity） | `tryGenerateHunhuan` | 按 `KLConfig.TIER*_PROB` |
| 2 | **魂骨** | `handleHunguDrop` | `KLConfig.dropChanceTier1~7` |
| 3 | **魂币 / 钱袋** | `CoinDropHandler.tryDropCoins` | 基础10%+年限加成，封顶45%；钱袋 1%~15% |
| 4 | **内丹** | `NeidanDropHandler.tryDropNeidan` | 20% 基础，8 档 tier × 6 品质（NBT `Quality`） |
| 5 | **返气草种子** | `handleFanqicaoSeedDrop` | 年限 ≥500，1%~8%，1~5 个 |
| 6 | **深海精华** | `ModDropHandler.tryExtraDrops` | `isMarine()`（水生/fish/ocean/whale/shark 关键词）6% |
| 7 | **经验** | `handleExperience` | `addJingyan`，非物品 |
| 8 | 恶魔鲸专属 | `DemonWhaleEntity.dropCustomDeathLoot` | 必掉：内丹6(5~30, ZHEN) + 恶魔鲸徽章 + 冷酷铁锭(3~10)，全发光 |
| 9 | 雪怪专属 | `SnowDemonEntity.dropCustomDeathLoot` | 10% 凛铁锭 |
| 10 | 冰晶专属 | `IceCrystalEntity` | 极寒雪花碎片 |
| 11 | 神考触发 | `handleGodGlimpse` | 非物品，Lv75+/99+ 概率触发 |
| 12 | 神考任务进度 | `handleGodKillTask` | 非物品 |
| 13 | 塔怪奖励 | `PlayerNpcEntity` / `TowerSpawnerEngine` | 塔怪的 drop 在 `onMobDrops` 被 `clear()` 清空 |

- ⚠️ `ModDropHandler` 里的 `isMarine` 用了 `entity.isInWaterOrBubble()` 兜底 ——
  **站在水里的任何怪都算水生**，包括被引入水里的陆怪。
- 魂币/内丹都有**调试作弊开关**（读 `player.getPersistentData()`）：
  `KL_Neidan_Prob_Cheat`、`KL_Neidan_Quality_Cheat`、魂币走 `CoinDropHandler.setTestMode(uuid,...)`。

## ⭐ 声明式掉落表（`drop/` 包，2026-10-03）
**新增任何"击杀掉 X"的东西，只改 `drop/DropRegistry.java` 一个文件。**
- `DropRule` = 不可变规则 + 链式 Builder：
  `of(id, item).filter(...).chance(c).minNianxian(n).maxNianxian(n)
   .perYear(1000, 0.001).capChance(0.08).count(min,max).glowing().build()`
  - `perYear` + `capChance` = **概率随年限递增并封顶**（返气草靠它保住了原有行为）
  - `roll()` 只做判定+产出 ItemStack，**不spawn**（表现层交给 `DropExecutor`）
- `DropFilter` = 生物筛选器：`all()` / `mobType(WATER)` / `nameContains("fish",...)`
  / `is(EntityType...)` / `inWater()` / `of(predicate,desc)`，可 `.and()/.or()` 组合。
- `DropExecutor.apply(entity)` = 遍历所有规则，**各规则独立判定**（同一次击杀可命中多条）。
  年限从 `MobAttributeCapability` 读，无 capability 按 0 → 带年限门槛的规则自动跳过。
- 调试指令 `/kunluncontinent droptable`（列全表）/ `droptable test 50000`（模拟某年限）。
- ⚠️ **深海精华的判定已改严**：旧 `isMarine` 用 `isInWaterOrBubble()`兜底，
  会把"被推进水里的陆怪"也算水生。现在只认 `MobType.WATER` + 明确关键词。
- 已迁入表内：深海精华(6%)、返气草种子(500年以上，1%→8%随年限递增，1~5个)。
- 已删：`event/server/ModDropHandler.java`（整文件）和 `KLivingDeathEvent#handleFanqicaoSeedDrop`。
- 魂环/魂骨/魂币/内丹**不在这个表里**（各有独立机制：年限配置 / 品质权重 / 作弊调试开关）。

## 玩家配置开关（ConfigScreen）
- `PlayerAttributeCapability.configFlags` 是 **long 位掩码**，默认 `0xFFL`（bit0~7 开），
  `isConfigOpen(i)` 读位 / `toggleConfig(i)` 异或位。**新增高位索引默认就是关的。**
- 已占用：1 生物魂环 / 2 玩家魂环 / 3 吸收经验提示 / 4 魂环实体 / 5 魂核实体 /
  6 屏幕UI / 7 屏幕UI位置 / 8 聚灵物品 / 9 NPC交易品级 / 10 粒子优化 /
  11 NPC魂环 / 12 第一人称魂环（默认关）。
- 加开关只改 `ConfigScreen`：`rawItems` 加一行 `new ConfigItemData("名字", 索引)`
  + `getToggleTooltip` 加一个 case。

## 特效：优先用项目已有贴图做「自定义粒子」
- **不要用原版粒子拼形状模仿具体物件**（如"用一圈 glow 假装魂环"）—— 已被用户明确否决。
  正确做法：`SimpleParticleType` + `TextureSheetParticle`，把贴图本身当粒子渲染。
  范例：`ModParticles` + `HunhuanRingParticle` + `particles/*.json`
  + `KlMain.ClientModEvents#registerParticleProviders`。
- 接入链路（缺一步就看不到贴图）：注册名 == `assets/<modid>/particles/<name>.json` 文件名；
  贴图放 `textures/particle/<name>.png`；JSON `{"textures":["<modid>:<name>"]}`；
  客户端 `RegisterParticleProvidersEvent#registerSpriteSet`。
- 自己写粒子时：**不要调 `super.tick()`**（父类会自动回收 + 自己动位置）；
  **必须手写 `this.xo/yo/zo = this.x/y/z`**（和 `oRoll = roll`），否则一跳一跳；
  `getLightColor()` 返回 `0xF000F0` 恒全亮；脉动用 `pow(0.5+0.5*sin(t*w),3)`，纯 sin 会抽搐。
- **直接生成粒子不需要网络包**：Provider 缓存 `SpriteSet` → 自定义构造器 + 静态 spawn
  → `Minecraft.getInstance().particleEngine.add(...)`。
- 粒子贴图必须带 alpha。无 PIL 时用纯 python 处理 PNG（zlib 解 IDAT → 手动反滤波 → 重编码 → 回读验证）。

### ⭐ 粒子默认是公告板，想躺平必须重写 render
```java
Quaternionf rot = new Quaternionf().rotateY(spin).rotateX((float)(Math.PI/2));
```
- JOML `rotateX/Y` 是**右乘**（`q = q*R`），后调用的先作用于顶点。
- 翻平用 **+π/2**（法线是局部 -Z，+π/2 才转到世界 +Y 朝上）；-π/2 会朝下。
- 平移量照抄父类：`Mth.lerp(partialTick, xo, x) - camera.getPosition().x()` 等。
- **平躺四边形正反各画一遍**（cull 状态不确定）；**`quadSize` 是半宽**，边长 = 2×quadSize。
- 重写 render 后包围盒还是原来的小盒子 → 要 `setSize(...)` 放大 + `shouldCull()` 返回 `false`。

## 魂环年限 → 视觉（唯一权威表 `HunhuanAbsorbFx.Tier`）
- 魂环贴图 `particletext.png` / `shenhuan.png` 是**纯白图形**（平均色 244，饱和度恒 0）
  → **染色 = 乘什么色得什么色**，不要"留白打底"/兑水。
- 8 档配色（与 `PWRenderLivingEvent.renderHunhuanAttribute` 的 `setShaderColor` 口径一致）：
  白(十年 `0xFFFFFF`) / 黄(百年 `0xFFFF00`) / 紫(千年 `0xCC00CC`) / 黑(万年 `0x1A1A1A`) /
  红(十万年 `0xFF0000`) / 橙金(百万年 `0xFF8A00`) / 蓝(千万年 `0x2E7BFF`) / 金绿(亿年 `0x22FF55`)。

## ⭐ NPC 亡语 + NPC 魂骨掉落
- **NPC 的 `soulCapability` 就是 `PlayerAttributeCapability`** —— 和玩家同一个类，
  天然带 7 个魂骨槽 `hunguInventory` 和 `boneOnlyStats`，所以"给 NPC 装魂骨"不用任何适配。
- ⭐⭐ **重大 BUG（已修）**：`PlayerNpcEntity` 继承 `PathfinderMob`，
  所以 `ModAttributeAPI` 11 个 getter 的 `instanceof Mob` 分支**全都会命中它** →
  去读 `MobAttributeCapability`（NPC 从不写它）→ **战斗中 NPC 的攻防暴闪全是 0**。
  修法：`ModAttributeAPI.npcAttr(entity, key, rawGetter, boneKey)` 私有辅助，
  在每个 getter 开头 NPC 短路返回「soulCapability 裸值+ boneOnlyStats」，
  命中返回 `NO_NPC`(NaN) 则继续原逻辑。**11 处一个都不能漏。**
- `entity/playernpc/NpcBoneGenerator` = 魂骨生成/生效/掉落唯一实现处。
  - 年限**档位对齐**：1~39→1~1万 / 40~79→百~十万 / 80~94→万~百万 /
    **95~99→万~999.9万（最高，NPC 等级上限 99）**
  - 第 1 枚自带概率：1~39 级 **0.01%** / 40~79 级 **0.05%** / 80 级以上 **0.1%**
  - **多骨链式概率** `CHAIN = {60%, 32%, 14%, 5%, 1.5%, 0.4%}`，
    每跳再乘 `levelChainBonus`（1~99 级 = 1.0×~2.0× 线性，高等级略容易出多枚）
    ⚠️ 原先定 40/15/5/1/0.2/0.04，算出来 7 枚总概率低到 Lv99 约 1/6500 亿
    ——"理论上存在、实际永远见不到"，等于没做。现整体抬升约 1.5 倍并保持
    递减比例，落在 **Lv99 约 1/2.3 亿 ~ Lv50 约 1/21 亿**（"稀有传闻"区间）。
  - ⭐ **掉率恒 100%**：`dropChance(lv)` 直接 `return 1.0`，不再按等级分档
    （原 55%/65%/85%/90%）。稀有度全部由"生成时是否带骨"承担——
    玩家看到带骨 NPC 时不会怀疑"这次会不会掉不出来"。
    `tryDropBone(npc)` 签名也去掉了 `guarantee` 参数（原来用它做调试强掉）。
  - 各枚数梯度（Lv99）：1 枚 **1/1000** / 3 枚 1/1562 / 7 枚 **1/2.3 亿**；
    Lv50：1/2000、7 枚 1/21 亿；Lv39：1/10000、7 枚 1/173 亿。
  - ⭐ 词条数 `MAX_ENTRIES = 6`（不含必带的 `maxshengming`，即总共最多 7 条）。
    **别设成 10/11** —— 可选池才 11 个，每次塞满等于"固定满词条"，词条系统就没意义了。
    实际落点（含必带那条）：Lv1~39 约 3~4 / Lv40~79 约 3~5 / Lv80~94 约 4~6 / Lv95~99 约 5~7。
  - ⭐ **多枚魂骨的词条必须"全局轮转"分配，不能每枚各自 shuffle**：
    `equipBones` 里先把 `ENTRY_POOL`（10 个）整体洗牌成一个 `rotation` 列表，
    每枚从队首领走自己的词条、**用过的挪到队尾**，下一枚接着从后面取
    → 同一只 NPC 的 7 枚词条互不重复。（原来每枚各自 shuffle，7 枚大量重复，
    看起来像"每枚词条都一样"。）
  - NBT：`NpcHasBone` / `NpcBoneCount` / `NpcBoneNianxian` / `NpcBoneDropped`
  - 掉落时**一次性把 7 个槽全丢出来**（带词条，不是重新 roll）
- ⭐ **`PlayerAttributeCapability.getAttrValueByKey` / `collectBoneAttributes` 已抽成
  public static**，玩家和 NPC 共用 —— 玩家原 `refreshBoneAttributes(Player)` 末尾会发网络包，
  NPC 不能发，所以只抽"汇总"那半段。**改魂骨取值口径时两边自动同步。**
- `PlayerNpcEntity.recalculateNpcStats()` = 裸值 + `boneOnlyStats`（生命/攻击），
  并调 `applyVanillaAttributes()` 存 7 个 `boneXxx` 快照字段（给面板/调试看，战斗不读它）。
  `refreshNpcAttributes()` 已改成直接调 `recalculateNpcStats()`（原来是一份重复的旧拷贝）。
- 亡语在 `PlayerNpcEntity.die(DamageSource)` 里：`resolveKiller(cause)` 取
  `getDirectEntity` → `getEntity` → `getLastHurtByMob()`（含召唤物 `getVehicle()`），
  **非玩家击杀一律不触发**。文案 `NpcDeathWhisper` 按等级分 LOW/MID/HIGH 三池，
  用原版 ActionBar + `ENDERMAN_SCREAM`（1.20.1 **没有 `WRAITH_*` 常量**）。
- ⭐ NPC 生成指令只有**一条**：`/kunluncontinent npc <level> <name...> [nianxian]`（`NpcSpawnCommand`）
  - 给年限 → 必定带骨；不给 → 走自然概率（0.01%/0.05%/0.1%）。
    `npcdebug` 里不要再写第二份 spawnwithbone（用户明确要求合并，曾被抱怨）。
  - 格式 `<level> <name...> [nianxian] [count]`：给年限 → 必定带骨；
    再给 count(1~7) → **强制装满该枚数**（`equipBones` 第 4 参`exactCount`，>0 时跳链式概率）。
  - ⚠️ **`name` 是 `greedyString`，会把后面所有内容吞掉** ——
    `npc 99 暮雨 1000000` 里 brigadier 只认 name = "暮雨 1000000" → 报"未找到皮肤"。
    修法：`resolveSkin(raw)` 先整串匹配皮肤 → 再整串当数字 ID →
    **再从末尾逐个剥纯数字**（最多 2 个：先剥的当年限、后剥的当枚数），剩下的再匹配皮肤。
    名字含数字也 OK（"4th 1300000" 能正确拆）。
  - ⚠️ **消歧规则：单数字一律当年限，不当枚数**（年限更常用）。
    要"多枚 + 随机年限"必须写两个数：`暮雨 0 7`（0 = 年限随机）。
  - 辅助子命令 `/kunluncontinent npcdebug`（`NpcDebugCommand`）：
    `bone <nianxian>`（给自己魂骨）/ `boneinfo`（附近 NPC 魂骨一览）/ `odds <level>`（概率速查）。
- ⚠️ `java.util.function` 里**没有 `ToFloatFunction`**（那是 Guava 的），用 `Function<..., Float>`。

## NPC 交易系统（`entity/playernpc/NpcTradeCatalog`）
- 交易池 6 品质（白/蓝/紫/黑/红/金），`Quality` 含 `priority`/`color`/等级区间/`TradeEntry` 池；
  NPC 等级由 `soulCapability.getDengji()` 决定。
- ⭐ **交易结果 ItemStack 绝不能挂 NBT**（旧版挂了 `KlNpcQuality`）：NBT 参与堆叠判定，
  挂上就永远无法与正常途径的同名物品合并。
  **品级展示改成按交易内容反查** `NpcTradeCatalog.getOfferQualityColor(offer)`，
  主键 =「货币 + 总价 + 物品」（68 条 TradeEntry 零冲突），物品级反查只做兜底。
  - 反查用 `offer.getBaseCostA()`（原始价），不要用 `getCostA()`（会被 demand 修正）；
    总价 = `costA.count + costB.count`（>64 会拆两格）。
  - `baseCostA/costB/result` 在网络和 NBT 两条路上都完整保留 → 客户端和读档后都成立。
  - 两张反查表**必须声明在 `QUALITIES` 之后**（静态字段按声明顺序初始化）。
- 旧存档清理 `stripLegacyQualityTag`；配方持久化在 NPC 自身 NBT，
  `PlayerNpcEntity.readAdditionalSaveData` 读档时洗一遍（每次读档约 12 次循环，开销可忽略）。
  ⛔ **不要加"登录时遍历玩家背包"之类的补漏**（用户明确否决过）。
  ⚠️ **删完 key 若tag 变空，必须 `stack.setTag(null)`**（空 CompoundTag 一样 `hasTag()==true`）。
  - 顺带确认：**`Inventory.setChanged()` 不同步客户端**，改完物品 NBT 必须
    `serverPlayer.inventoryMenu.broadcastChanges()`。
- 交易列表渲染在 `ModUiRenderHandler.onGuiRender`（`ScreenEvent.Render.Post`），配置 9 切染色样式。

## 技能体系结构
- 基类 `item/baseskillist/BaseSkillItem`：抽象 `getBaseCost / getDamageMultiplier /
  getCastTime / getCooldownTicks`；`executeEffect` 有 3 参（自动算伤害）与 4 参（带 finalDamage）
  两个重载，部分技能同时重写 —— 改特效时两个入口都要覆盖或抽公共私有方法。
- 4 武魂：`pohunqiang`(破魂枪,器) / `bahuangji`(八荒戟,器) / `liejinhu`(裂金虎,兽) /
  `juyuan`(磐石巨猿,兽)，各 1~9 魂技；第 7 魂技统一在 `zhenshen/`（用户禁止改动）。
  器/兽分类在 `capability/playerattributes/Wuhunname.java`。
- 技能随机分配表 `SkillLibrary.java`：`register(武魂, 环序, List<RegistryObject>)`，
  `getRandomSkill` 在同环序池子里**等权随机**（同一槽位天然支持多条魂技）。

## ⭐ 变体魂技（声明式，一条 spec = 一条新魂技）
- 工具类：`SkillSpec`（descKey/消耗/倍率/吟唱/冷却/`SkillEffect` lambda）+ `LambdaSkillItem`。
- 注册：`ModItems.skillVariant(id, 图标来源, spec)` —— 记入 `SKILL_VARIANTS`，
  datagen（`ModItemModelsProvider`）自动**复用图标来源的贴图**（零新增美术）。
- 完整步骤：ModItems 一条 `skillVariant(...)` → SkillLibrary 对应槽位池子加一项 →
  语言文件加 `add(Item, 名字)` + `add(descKey, ...)`。**不写技能类、不加贴图。**
- 现状：兽武魂第一魂技各 5 条（原 1 + 变体 4，等权 20%）；器武魂未加。存档按注册名序列化。
- ⚠️ 多段伤害会被 `CombatEventHandler` 每次**再加一份攻击力**，多段数值要比单段调低。
- ⚠️ 纯增益技能 `damageMultiplier` 填 0 时 `finalDamage=0`，特效 lambda 别消费它。

## ⭐ 技能强度 = 魂环年限（链路与坑）
- 分档表 `BaseSkillItem.getPowerMultiplier(long nianxian)`：
  10年1.0 / 百年1.5 / 千年2.5 / 万年4 / 十万年8 / 百万年15 / 千万年25 / 亿年50（阶梯，有悬崖）。
  消耗 `getCostMultiplier` 1.0→3.5。伤害 = `ModAttributeAPI.getGongji(player) × getDamageMultiplier() × powerMultiplier`
  ——攻击力本身已含魂环加成，年限**双重**生效。
- **年限取"当前槽位对应的那枚环"**：第 N 魂技 ↔ 第 N 枚魂环。
  吸收时 `handleAbsorbed` 用 `rings.size()` 定环序；释放时三条入口都取
  `rings.get(selectedSlot).getNianxian()`。
- ⛔ **`getDamageMultiplier()` 返回 0 的技能 `finalDamage` 就是 0**。凡是把 finalDamage 存进 NBT
  交给 Tick 事件当伤害的延迟型技能，倍率写 0 等于完全没伤害（八荒寂灭曾踩过，已修成 1.0）。
- 5 个技能用**固定基数**而非玩家攻击力：`SkillLeijinhu1/Leijinhu2/Pohun2/Panshijuyuan1/Bahuang2`。
  要统一成 `gongji × 倍率 × power` 是 5 行改动，但会改平衡，改前先问用户。
- **延迟型技能**的后续表现不在技能类里，而在 `event/server/PWPlayerTickEvent.java`
  （碎星领域/裂地/不周倾/裂天/八荒寂灭/虚空云）。改这类技能要同时看技能类和 Tick 事件。
- 粒子优化开关 `isConfigOpen(10)`，各处据此降密。

## 粒子特效规范（统一走 `effect/ParticleFx`）
- 服务端专用：`ParticleFx fx = ParticleFx.of(level, player); if (fx == null) return;`
- 三档控制：`.lod(...)` 细节倍率 / `.size(...)` 全局半径缩放 / `.budget(n)` 预算上限。
- 形状一律用工具类方法，不要手写 `for (int i = 0; i < 360; i += 10)` 这类散装循环。
- 求横向单位向量用 `ParticleFx.ortho(dir)`，不要手写叉积（垂直视角会 NaN）。
- 需要复用的局部变量必须声明在 `if (fx != null)` 之外。

## 魂环"开启方式"动画（3 种样式）
- 全部差异收在 `PWRenderPlayerEvent.openAnim(style, progress, count, restY, entity)`，
  返回 `OpenAnim(x,y,z,spinDeg,scaleMul,alphaMul)`；`progress >= 1` 返回 `IDLE`（全零偏移），
  **静止状态与改动前完全一致**。
- 样式下标即配置值：`0 逐环展开` / `1 天降落位` / `2 魂环升腾`（自脚下破地而出 →
  冲天过环位 1.15 格 → easeOutBack 落定）。名字数组 `ANIM_STYLE_NAMES` 在渲染侧，配置界面直接取。
- **加第 4 种样式**：`openAnim` 加 case + `ANIM_STYLE_NAMES` 加一项
  + `PlayerAttributeCapability.HUNHUAN_ANIM_COUNT` 加一，其它地方不用动。
- 配置值 `hunhuanOpenAnim` 走 `PacketChangeHunhuanAnim` 持久化 + `SynsAPI.synsPlayerAttribute` 回同步。
- ⚠️ 样式存在 `EntityWuhunCache.animStyle`，由 **`SyncWuhunDataPacket` 携带**
  → **别人的客户端也按你的样式播动画**。渲染时不要直接读本地 capability。
- ⚠️ `restY` 必须由调用方传入（魂环 = `0.25+count*0.02`，神环 = `1.2`），
  否则算不出正确的"地面之下"起点。

## 神考（神位传承）系统
- 数据在 `godclass/GodRegistry`（海神/修罗神/天使神），任务类型 `GodTaskType`。
- 提交入口 `C2SCheckTaskPacket`；KILL 由 `KLivingDeathEvent.handleGodKillTask` 自动推进。
- ⭐ **奖励只走 `GodInfo.grantAttr(player, key, value)` 这一个出口**，`dengji` 走 `grantLevels` 真升级。
- **第 9 考奖励只在 `finalizeAscension`（飞升动画结束后）发一次**，`executeRewards` 里绝不能再发。
- 等级上限（`PlayerUpgradeSystem.isTupoDengji`）：**99 级在封神前是硬上限**（"请封神后再突破"），
  封神后才允许 99→100；100~198 需魂环数 ≥ level/10；199 真满级。
  → `grantLevels` 判据：`currentLevel >= 99 && !cap.isGod()` 才折算成属性补偿。

## ⭐ 武魂永久基础属性（`capability/playerattributes/WuhunPermanent`）
把魂环的一部分"抽成"出来永久加给玩家，解决"关武魂弱 99.3%"。
- **比例按等级线性，绝不写死**：`0.10 + (lv-1)/98 × 0.40` → Lv1 10%、Lv50 30%、Lv99 **50%**。
- **只在升级时重算**（`PlayerUpgradeSystem.processSuccessfulUpgrade` 里，
  紧跟 `AttributePoints.grantOnLevelUp` 之后），单向递增、只补差额。
- 独立字段 `PlayerAttributeCapability.wuhunPermanentStats`（`Map<String,Float>`），
  NBT key `"WuhunPermanentStats"`；11 个 `ModAttributeAPI` getter 里
  `getBoneBonus` 之后紧跟一行 `getPermanentBonus(player, key)`。
- ⭐ **四道防"左脚踩右脚"的锁**（用户明确要求，实测不做会指数爆炸）：
  1. **独立存储**，绝不回写 `getGongji()` 等裸值（裸值是基线的一部分）
  2. 抽成基线只取 `HunhuanAttributeHelper.getWuhunBonus`（**纯魂环**，不含永久属性）→ 天然无环
  3. 写入用 `merge(key, v, max)` 只增不减，重复调用幂等
  4. `WeakHashMap<cap, Boolean> SETTLED` 防重入（**不能用 configFlags 的位**：
     那12 个 bit 已被玩家配置开关占满；`isConfigOpen` 返回 boolean 不能位运算）
  另有 `HARD_CAP = 1e9` + `Float.isFinite` 净化 + 读档时丢弃非正数。
- **转生：`zhuanshengKeep` 按 5% 打折**（`/20`，与其它属性同口径），**绝不保留全部**。
- ⚠️ **已知待优化（用户提出后搁置）**：未开武魂就转生很亏 —— 本属性是 0，打折后仍 0，
  玩家感受上等于白存。要解决得另设规则（如按"曾经达到过的最高值"继承）。
- ⚠️ 数字的 getter 在 `CapabilityAttributeBase` 里是 **`getShengminghuifu`（小写 h）**，
  不是 `getShengmingHuifu`。NPC/魂兽那套才叫 `getWugong/getWufang`。

## ⚠️ 玩家裸值 vs 武魂加成：量级差 100~10000 倍（2026-10-03 核算）
用户问"关武魂是不是弱 70% 以上" —— **实际是弱 99% 以上，方向完全相反**。
- 玩家裸值（`applyGrowthAndBonus` 逐级累加 `i*0.4`）：Lv50=510 / Lv99=**1980** 攻击。
- 魂环加成（`HunhuanAttributeHelper.getWuhunBonus` 把当前武魂 9 枚魂环**全部相加**）：
  9 枚千年 ≈ 9,450、**十万年 ≈ 293,508**、百万年 ≈ 1,805,400。
- 所以关武魂后 99 级玩家攻击从 295,488 → 1,980，**只保留 0.7%**（弱化 99.3%）。
- 衰减：Lv50 弱 99.7%、Lv80 弱 99.2%、Lv99 弱 99.3%。**等级越高越依赖武魂**。
- 关闭路径：`CPacketQiehuanWuhun` / `PWPlayerTickEvent` → `setHunhuankuaiguan(-1)`
  → `getWuhunBonus` 的 `activeIndex >= 0` 不成立 → 加成归零，**裸值天然还在**。
- 结论：裸值成长公式（`i*0.4`）与魂环公式（`g*gMult`，十万年 gMult=3 但 g 本身上千）
  **量级完全不匹配**。要平衡得动其中一边 —— 后续用户会提需求，暂未改。

## 属性取数：三种口径，别用错
| 口径 | 取法 | 用途 |
|---|---|---|
| 裸值 | `cap.getXxx()` | 存档、等级成长、发奖励时累加 |
| 聚合值 | `ModAttributeAPI.getXxx(entity)` | 结算伤害/防御；含魂环 + 魂骨 + 药水 + 装备 |
| **面板值** | `ModAttributeAPI.getPanelAttributeValue(player, key)` | **一切"按面板数值判定"的检查**（神考等） |
- ⚠️ 神考数值考核曾用裸值导致"面板达标却判定不通过"，该方法已删除。
- 面板口径：**生命 = `player.getMaxHealth()`**，其余 = `ModAttributeAPI.getXxx(player)`。
- `getMaxHealth()` 能到百万级，是因为 `KlMain.changeAttributesIO()` 用反射把
  `Attributes.MAX_HEALTH` 的 maxValue 改成 `Float.MAX_VALUE`；base 由 `PWPlayerTickEvent`
  每 20 tick 同步成 `ModAttributeAPI.getMaxshengming()`。

## 成就（进度）系统
- **只有一个触发器** `advancement/KunlunTrigger`（`kunluncontinent:kunlun_event`），
  靠 JSON 的 `conditions.event` 字符串区分事件 → 新增成就**不用写新 Trigger 类**。
- 玩法侧只调 `advancement/AchievementAPI` 的语义化方法 + 事件 ID 常量。
- datagen 侧只调 `datagen/advancement/AdvancementHelper`
  （`task/goal/challenge/root` + `event/hasItems/levelReached/onJoin`），语言键
  `adv.kunlun.<路径>.title/.desc`，路径 `kunluncontinent:main/<name>`，
  文案在 `datagen/lang/ModZhCnLangProvider` 的「成就类」区块。
- 能用「背包里有某物品」表达的不要埋点；只有行为类才埋点。
- 无玩家上下文的场景（如炼丹炉 `serverTick`）用 `level.getNearestPlayer(x, y, z, 8.0, false)`。

## ⭐ 核心范式：先查"原版是否已经同步"
- **判断"某玩家正在做某事"前，先看这件事原版有没有同步**：
  吸收魂环 = 玩家骑在 `HunhuanEntity` 上（**乘客同步** `getVehicle()`）；
  年限 = `HunhuanEntity.getNianxianSync()`（**SynchedEntityData**）。
  两者原版都自动同步给所有客户端 → 动画**一个自定义包都不用发**，
  客户端遍历 `ClientLevel#players()` 自己判断即可。范例：`ClientHunhuanRingFx`。
  教训：曾为它写了 2 个 S2C 包 + 1 个客户端状态管理器，全被删掉。
- 只有原版**确实没同步**的状态才自己发包：`network/server/PacketXxxSync`
  + `broadcast()` 按半径遍历 `serverLevel.players()`。
- **"吸收成功"vs"玩家中途下车"**（不用发包）：成功 = 服务端 `handleAbsorbed()` 里`discard()` 掉实体；
  中断 = 实体还在、只是玩家不骑了。两个包到达顺序不确定 → 留几 tick 宽限期（现用 4 tick）。
- **想让实体在"被骑乘时"不渲染**：`EntityRenderer.render` 里`if (!entity.getPassengers().isEmpty()) return;`。
  **不要用 `isInvisible()`** —— `EntityRenderer` 本身不检查该标志。
- ⚠️ **给实体挂"客户端缓存快照"的渲染，改数据时必须主动推一次**：
  `PWRenderPlayerEvent.entityWuhunCacheMap` 只在登录/换维度/手动开关魂环/收到
  `SyncWuhunDataPacket` 时刷新 → 否则新魂环不显示。统一入口
  `PlayerHunhuanAPI.broadcastWuhunRings(ServerPlayer, boolean playAnimation)`。
- `PlayerAttributeCapability.hunhuankuaiguan` = **当前显示的武魂下标**，`-1` = 关闭。
  `getWuhunList()` 在关闭时返回 `null`，广播空列表正好保持隐藏。
- 吸收流程：`interact()` → `startRiding(this)`；`tick()` 中 `player.getVehicle() == this` 时
  提前 return（`livetime` 冻结）；进度在 `secondtick()`（每 20 tick）累加，
  阈值 `v = log10(年限)*10 + 10` 秒；到点 `handleAbsorbed()` → `discard()`；中途下车则归零。
- 魂环本体渲染 `HunhuanRender`（注册于 `EntityInit.HUNHUAN`），绘制在
  `PWRenderLivingEvent.renderHunhuan / renderAnimation / renderHunhuanAttribute`，
  且只有观看者自己的 `isConfigOpen(4)` 打开时才渲染。
  `KLRenderApi.renderStart/End` 内部成对 `pushPose/popPose`。