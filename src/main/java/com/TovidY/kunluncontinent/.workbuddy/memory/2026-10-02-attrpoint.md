# 2026-10-02 属性加点系统

## 做了什么
把之前只有"可用点数"显示的空壳 `PointScreen` 做成完整的 11 属性加点面板。

### 新增文件
- `capability/playerattributes/AttributePointSpec.java` — **唯一权威表**，11 条属性枚举。
  顺序：生命值/强健 → 攻击力/强攻 → 防御力/铁壁 → 暴击率/锐意 → 暴击伤害/破军 →
  生命恢复/回春 → 吸血/噬魂 → 闪避/游龙 → 命中/锁魂 → 物穿/破甲 → 抗暴/坚心。
  每条含 key / displayName / desc / perPointPercent / mode / color / colorCode。
- `network/client/CPacketAllocatePoint.java` — (specIndex, amount) 加点。
- `network/client/CPacketResetPoints.java` — 重置全部。

### 修改
- `PlayerAttributeCapability`：加 `int[] allocatedPoints` + `getAllocatedPoints/setAllocatedPoints/
  getAllocatedPointsTotal/clearAllocatedPoints`；NBT `"AllocatedPoints"` putIntArray，
  读档按枚举长度 arraycopy 裁剪。
- `ModAttributeAPI`：11 个聚合 getter 末尾统一
  `AttributePointSpec.applyBonusByKey(key, value, entity)`。
- `AttributePoints`：新增 `allocate()`（只加不减）/ `reset()`（清零+全额退还，
  卷轴限制预留位在开头注释里）。
- `NetworkHandler`：注册两个新包。
- `PointScreen`：重写，400×236，两列 6+5。

## 数值方案（用户给表，已核对无超标）
每级 1 点、99 级共 99 点、单条上限 99 点 → 一个 build 最多点满一条，数值安全。
关键决策：**概率类属性走加算、数值类走乘算**。
- MULT（乘算）：生命/攻击/防御/暴伤/生命恢复/吸血/物穿
- FLAT（加算）：暴击率/闪避/命中/抗暴
  → 暴击率基础只有 5%，乘算 99 点也才 12.4%，达不到"封顶 30%"的定位；
  加算是 5+29.7 = 34.7%，正好对上。闪避同理（基础 1% → 25.75%）。

## 交互
- 每行：色条 + 显示名（强攻/铁壁…，不是攻击力/防御力）+ 灰色定位 + `x/99` + 进度条 + `+` / `满`。
- tooltip：定位 / 每点收益 + 结算方式 / 当前值 → 加满后 / 已加点数。
- 右下角"重置属性点"按钮，点数全额退还。**只能加不能减**是硬规则。
- 加点成功后服务端立刻 `SynsAPI.synsPlayerAttribute` 回同步（否则 MAX_HEALTH 要等 tick）。

## 踩的坑
`Edit` 把 `return value;\n}\n\npublic static float getJingshenli` 缩成 `return value;\n}`，
把下一个方法签名一起吃掉了。改连续的同形方法（尤其 ModAttributeAPI 的聚合 getter）
必须带足够上下文。
gradle 输出是 GBK，读中文错误要 `iconv -f GBK -t UTF-8`。

## 状态
`compileJava` BUILD SUCCESSFUL。
## 复盘：用户反馈的 6 个 UI 问题及修法
1. 移除每行的「满」一键加满按钮（只留 `+`）。
2. **tooltip 重叠**：根因是 `KunlunGuiHelper.HandDrawnButton.renderTooltip` 内部判
   `isHoveredOrFocused()` —— 点击后按钮**一直保持 focused**，移到别的按钮时两个 tooltip
   同时渲染。修法：`PointScreen.render` 里自己按矩形过滤，只画鼠标真正压着的那一个。
3. tooltip 里删掉「当前 X → 加满后 X」（面板行里已经有了）。
4. tooltip 第一行右侧改成**原属性名**（生命值/攻击力/…），不再是定位短语。
   → 枚举里 `desc` 字段换成 `attrName`（getDesc() 已删除）。
5. 被淘汰的定位短语位置换成「当前值 → 点满后」的实时数值。
6. `x/99` 被按钮挡住 → 删掉「满」按钮后 `+` 右移到行末（BTN_X = COL_W - BTN_W - 6），
   `x/99` 改为右对齐到 POINTS_RIGHT=150。

## 第二轮：移除行内数值 + 色条对齐
用户明确要求**移除行内的"当前值 → 加满后"数值**（上一轮加错了他没要）。
连带清理：`fullValue()` / `fmt()` / `ellipsis()` 三个私有方法、
`VALUE_DX` / `VALUE_MAX_W` 常量、`renderRows` 的 `Player` 参数、
`ModAttributeAPI` 与 `Player` 两个 import —— 全部删干净，编译无警告。
另外色条从 `y~y+12`（12px）改成 `y+2~y+11`（9px），与别名文字同高同起点。

## ⚠️ 第三轮修正：加点不能放大武魂属性（用户提出的隐患，确实存在）
用户担心"属性加点是加给玩家本体的，开武魂时武魂会直接赋予玩家，会不会爆炸增长"。

**结论：没有无限复利（加点不写回 capability，每次读取重算），但放大隐患是真的。**
原因：`ModAttributeAPI.getXongji` 结构是「裸值+武魂+魂骨+武器+药水」全部相加，
我最初把 `applyBonusByKey` 接在 getter 结尾 → 乘的是整个总和。
实测 99 级 + 9 枚十万年魂环，攻击力 63181 × 2.485 = 157053；
只放大裸值则是 1981×2.485 + 61200 = 66123 → **差 9 万，同 99 点收益放大 18 倍**。

**修法**：`ModAttributeAPI` 新增私有 `withPoints(entity, key, nakedValue)`，
11 个 getter 全部改成 `裸值 → withPoints → 再 +=武魂/魂骨/装备/药水`，
结尾的 `applyBonusByKey` 全部删掉。`armor_piercing` 的 `*=0.7` 仍在最后（位置不变）。
已 grep 确认 11 条 key 无遗漏。编译通过。

## 第四轮：重置卷轴（两条路都能重置）
用户新增了 `ModItems.RESET_SCROLL`（配置和贴图他都做好了，只要写功能）。

### 结构
- 新增 `item/klitem/ResetScrollItem`：`use()` 里只调
  `AttributePoints.resetWithScroll`，**不自己写扣卷轴**。
- `ModItems.RESET_SCROLL` 注册改成 `new ResetScrollItem(new Item.Properties())`。
- ⛔ **别用 `Inventory.clearOrCountMatchingItems(predicate)` 扣道具** ——
  查源码发现它的三参数重载会额外扣 `containerMenu.getCarried()`（鼠标正抓着的摞）。
  改成自己遍历 `inv.items` + `inv.offhand.get(0)`，扣完显式
  `player.inventoryMenu.broadcastChanges()`。
- `AttributePoints`：`resetWithScroll`（扣卷轴 + 重置，玩家主动行为都走这个）
  / `reset`（裸重置不扣道具）/ `consumeResetScroll`（只扣）。
- `CPacketResetPoints` 从 `reset` 改成 `resetWithScroll`。
- 面板重置按钮 tooltip 加了一行"需要消耗 1 个重置卷轴"。

### 踩的坑
把 `ModItems` 里 `new Item(new Item.Properties(){ appendHoverText(...) })`
换成具名类时：`appendHoverText` 是 `Item` 的方法**不在 Properties 里**，
要一起搬进新类；括号也少一层（`})` → `}))`。用户配的 tooltip 不能丢。
`compileJava` + `runData` 均通过。
