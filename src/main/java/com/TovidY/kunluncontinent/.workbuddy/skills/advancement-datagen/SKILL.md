---
name: advancement-datagen
description: >-
  在昆仑大陆（kunluncontinent，Forge 1.20.1）模组里新增或修改成就（进度）时使用。
  提供 KunlunTrigger / AchievementAPI / AdvancementHelper 的完整用法、埋点位置规范、
  语言键约定，以及 compileJava + runData 的校验流程。
  触发场景：加成就、改成就条件、加里程碑奖励、调整成就分支结构。
agent_created: true
---

# 昆仑大陆 成就（进度）开发

## 架构：三层，各改一处

```
玩法代码 ──> AchievementAPI.onXxx(player)          语义化入口（advancement/AchievementAPI）
             └─ KunlunTrigger（单触发器 + 事件 ID）
datagen  ──> AdvancementHelper.task/goal/challenge 一行一个成就（datagen/advancement/）
             └─ ModAdvancementProvider              分支结构
文案     ──> ModZhCnLangProvider 「成就类」区块       adv.kunlun.<路径>.title/.desc
```

**只有一个触发器**：`kunluncontinent:kunlun_event`。JSON 里靠 `conditions.event` 字符串区分，
所以新增成就**不需要**写新的 `SimpleCriterionTrigger` 子类。

## 加一个新成就：4 步

### 1. 事件 ID 常量（`AchievementAPI`）

```java
public static final String MY_EVENT = "my_event";
```

### 2. 语义化入口（`AchievementAPI`）

```java
/** 完成某件大事。 */
public static void onSomethingDone(Player player) {
    trigger(player, MY_EVENT);
}
```
带阈值的用现成模式——加一行到分档表就行，运行时会自动多触发：

```java
public static final List<NianxianTier> NIANXIAN_TIERS = List.of(
        new NianxianTier(1_000, HUNHUAN_THOUSAND),
        new NianxianTier(10_000, HUNHUAN_MYRIAD),   // ← 新增档位只加这一行
        ...
);
```

### 3. datagen 条目（`ModAdvancementProvider`）

```java
Advancement myNode = AdvancementHelper.task(saver, helper, parent, "my_path",
        ModItems.MY_ICON.get(), AdvancementHelper.event(AchievementAPI.MY_EVENT));
```
- `task` / `goal` / `challenge` 对应普通 / 目标 / 挑战框
- 需要「解锁条件即父节点」就传已有 `Advancement` 变量做 parent
- 用「背包里有某物品」表达时用 `AdvancementHelper.hasItems(...)` —— **零埋点**，优先选它

### 4. 文案（`ModZhCnLangProvider`）

```java
add("adv.kunlun.my_path.title","标题");
add("adv.kunlun.my_path.desc","描述");
```

## 埋点规范

- 只在**行为发生的那一刻**触发，紧跟玩家可见的提示信息之后，例如：
  ```java
  player.sendSystemMessage(Component.literal("成功吸收...魂环！"));
  AchievementAPI.onAbsorbHunhuan(player, monsterCap.getNianxian(), listForActive.size());
  ```
- 非行为类的（拥有某物品、等级）**不要埋点**，用 `hasItems` / `levelReached` 条件。
- 没有玩家上下文的场景（炼丹炉 `serverTick` 等）：
  ```java
  Player crafter = entity.level.getNearestPlayer(x, y, z, 8.0D, false);
  AchievementAPI.onAlchemySuccess(crafter, finalQuality);   // 入口内部已做 null / 客户端判空
  ```
- 持续型场景（踏入维度）在 tick 里每 20 tick 探一次即可，成就一旦拿到，
  触发器监听器会被移除，重复调用是 O(1)，不用担心开销。

## 已埋点的位置（新增前先看这里，别重复埋）

| 玩法 | 文件 | 方法 |
|---|---|---|
| 觉醒武魂 | `capability/playerattributes/PlayerHunhuanAPI` | `addWuHun(player)` ×2 |
| 吸收魂环 | 同上 | `addHunhuan(player, entity)` |
| 炼丹成功 | `block/blockentity/LiandanluBlockEntity` | `executeCraft(...)` |
| 转生 | `item/klitem/ZhuanShengTestItem` | `completeReincarnation(...)` |
| 爬塔开始 | `tower/block/SummonTowerBlock` | `use(...)` |
| 爬塔通关 | `event/server/KLivingDeathEvent` | 通关分支 |
| 开启神考 | `event/server/KLivingDeathEvent` | `triggerGodExam(...)`（唯一入口） |
| 进入雷界 | `event/server/PWPlayerTickEvent` | `onPlayerTick` 维度探测 |

## 踩过的坑

1. **`CriterionTriggerInstance` 在 `net.minecraft.advancements`，不在 `net.minecraft.advancements.critereon`。**
   写错包会报「找不到符号 类 CriterionTriggerInstance」。
2. `Advancement.Builder.addCriterion` 有**两个重载**：
   `(String, CriterionTriggerInstance)` 与 `(String, Criterion)`。
   传 `KunlunTrigger.Instance.of(id)` 走前者，最省事，不需要 `createCriterion`。
3. 顶部 `show_toast` 只在**玩家在线时**触发；离线补发不会补提示，这是原版行为。
4. 改旧成就的显示键会连带 lang：本项目旧键是
   `advancements.kunluncontinent.<name>.title/.description`，
   新统一为 `adv.kunlun.<path>.title/.desc`。改前缀时两边一起改。
5. 不知道某原版 API 的签名时，直接解压
   `~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.0_mapped_parchment_2023.09.03-1.20.1/forge-1.20.1-47.4.0_mapped_parchment_2023.09.03-1.20.1-sources.jar`
   读源码，比猜快得多。

## 校验流程（必做，两步）

```powershell
Set-Location 'D:\xiangmu\forge-1.20.1-47.4.0-mdk'
# 1) 编译
& .\gradlew.bat compileJava --offline --console=plain 2>&1 |
    Out-File -Encoding utf8 'D:\xiangmu\forge-1.20.1-47.4.0-mdk\compile_log.txt'
# 2) 生成数据（约 2~3 分钟）
& .\gradlew.bat runData --offline --console=plain 2>&1 |
    Out-File -Encoding utf8 'D:\xiangmu\forge-1.20.1-47.4.0-mdk\rundata_log.txt'
```

用 Read 读日志确认 `BUILD SUCCESSFUL`，再 Glob
`src/generated/resources/data/kunluncontinent/advancements/main/*.json` 确认新 JSON 落地，
最后删掉这两个临时日志。PowerShell 的 stdout 常被吞，务必重定向到文件再读。
