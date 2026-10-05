package com.TovidY.kunluncontinent.screen.guide;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GuidBookScreen extends AbstractContainerScreen<GuideBookMenu> {

    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation(KlMain.MOD_ID, "textures/gui/guide_book.png");
    private static final ResourceLocation LEFT_BUTTON = new ResourceLocation(KlMain.MOD_ID, "textures/gui/right_book.png");
    private static final ResourceLocation LEFT_BUTTON_HOVER = new ResourceLocation(KlMain.MOD_ID, "textures/gui/right_book_max.png");
    private static final ResourceLocation RIGHT_BUTTON = new ResourceLocation(KlMain.MOD_ID, "textures/gui/left_book.png");
    private static final ResourceLocation RIGHT_BUTTON_HOVER = new ResourceLocation(KlMain.MOD_ID, "textures/gui/left_book_max.png");

    private final List<GuideArticle> articles = new ArrayList<>();
    private int selectedArticleIndex = 0;
    private int currentPageInArticle = 0;

    private float scrollAmount = 0.0F;
    private boolean isScrolling = false;
    private static final int LEFT_PANEL_X = 20;
    private static final int LEFT_PANEL_Y = 18;
    private static final int LEFT_PANEL_WIDTH = 105;
    private static final int LEFT_PANEL_HEIGHT = 142;
    private static final int ITEM_HEIGHT = 16;

    private List<FormattedCharSequence> cachedArticleLines = new ArrayList<>();
    private static final int RIGHT_TEXT_X = 156;
    private static final int RIGHT_TEXT_Y = 22;
    private static final int RIGHT_TEXT_WIDTH = 116;
    private static final int LINES_PER_PAGE = 12;

    public GuidBookScreen(GuideBookMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 292;
        this.imageHeight = 180;
        parseAndBuildArticles();
    }

    private void parseAndBuildArticles() {

        addArticle("欢迎来到昆仑",
                "§1§l《欢迎来到昆仑大陆！祝您玩的愉快 ~ 》§r\n"
                        + "[本引导书更新于’§b1.0§r‘版本]\n\n"
                        + "§c§l请各位玩家务必认真仔细查看§r，基本收录了正常游玩时遇到的§b所有常见攻略§r。");

        addArticle("属性面板系统",
                "§l属性面板系统：§r\n"
                        + "默认按 §1§lO键§r 打开§b属性面板§r，在属性面板下按住§bShift键§r可查询§b具体数值§r（例如玩家攻击力为10000时自动缩进变为1万，按Shift即可查看具体数值）。");

        addArticle("屏幕GUI解释",
                "§l屏幕GUI解释：§r\n"
                        + "屏幕左上角四个属性条分别为：\n"
                        + "• §c生命值(红色)§r\n"
                        + "• §6饱和度(橙色)§r\n"
                        + "• §9精神力(蓝色)§r\n"
                        + "• §a经验值(绿色)§r\n"
                        + "经验条左下角数字代表§b当前等级§r。\n\n"
                        + "§8注意：跨维度等数值变异常时等待数秒即可恢复。");

        addArticle("技能系统",
                "§l技能系统：§r\n"
                        + "默认长按 §1§lR键§r 打开§b技能栏§r，按 §1§lV键§r §b释放选中技能§r。\n"
                        + "用鼠标滑动选择，松开R键选择。\n"
                        + "点按R可§b顺序快切§r。\n"
                        + "§b魂技威力与消耗§r随§6魂环年限§r增加。");

        addArticle("觉醒武魂",
                "§l觉醒武魂：§r\n"
                        + "击杀生物、炼制丹药、蒲团修炼均可获得§b经验§r，经验值满了后自动§b突破下一个等级§r。\n"
                        + "§b首次突破成功时觉醒武魂§r并分配天赋。");

        addArticle("天赋系统",
                "§l天赋系统：§r\n"
                        + "玩家§b觉醒武魂§r时出现，基本出现在玩家等级从0进行升级时会触发§b天赋赋予§r，§7普通天赋最为常见§r，不同天赋略微影响数值，但后期均可通过§6更高年限的魂环§r提升。");

        addArticle("武魂系统",
                "§l武魂系统：§r\n"
                        + "默认按 §1§lK键§r 打开§b武魂§r，打开时消耗§9精神力§r，在此状态下方可§b吸收魂环§r。\n"
                        + "在§b开启武魂§r的状态下可长按默认R键选择技能，而在§c未开启武魂§r的情况下长按R键是没反应的。");

        addArticle("魂环吸收",
                "§l魂环吸收：§r\n"
                        + "§c逢10级(10/20/30...)§b锁定等级§r，必须吸收魂环才可突破！\n"
                        + "吸收方法：§b打开武魂§r后右键生物掉落魂环§b坐上去§r，消耗§9精神力§r吸收。\n"
                        + "§c精神力不足将停止且重置进度。");

        addArticle("魂核系统",
                "§l魂核系统：§r\n"
                        + "魂环分解（玩家蹲下后对着魂环实体按下右键即可分解）或自然消失后生成的产物，随时间流逝消失，可用§b聚魂瓶§r收集。");

        addArticle("属性点数系统",
                "§l属性点数系统：§r\n"
                        + "玩家每升一级可获得一个点数。"
                        + "\n点数用于打开属性面板后，属性面板的右侧就是点数面板。"
                        + "\n一条属性可一次性点满99个属性点。"
                        + "\n§c注意！点数所增加的属性是玩家本体的基础属性而不是武魂属性，也就是说玩家开启武魂时获得的属性不计入点数增幅的百分比中。"
                        + "\n例如玩家基础属性为100，点数增加10%，但打开武魂时玩家属性变为200即使有10%的加持，计算也是玩家的基础属性(100)×这个点数的增幅");

        addArticle("挑战系统",
                "§l挑战系统：§r\n"
                        + "制作§b挑战石碑§r右键进入维度，右键建筑中心§b战斗石碑§r刷怪。\n"
                        + "通关层数越高，获得§d神位§r概率越高。\n\n"
                        + "§c注意：通过挑战塔获得的神位无法自己选择，是§b随机§r从神位库中任选一个神位赋予玩家。");

        addArticle("重修与转世",
                "§l重修&转世系统：§r\n"
                        + "1.地狱门遗迹采§d彼岸花§r炼制【§d忘川渡§r】并喝下获得‘§d彼岸§r’buff。\n"
                        + "2.用【§b重修之眼§r】找到§b重修遗迹§r，在玩家拥有‘§d彼岸§r’buff时对着§b重修台§r右键就会开启§c雷劫§r，总需遭受§c99道雷劫§r，雷劫数量不是固定99道，而是看玩家当前§c生命值§r情况，玩家的生命值§c低于20§r后方可完成转世重修。\n"
                        + "3.§4§l切勿用任何手段回血！§r血量降至§c20以内§r才可转世成功，若99道天雷无法降低你的生命值那你的转生就会失败。\n"
                        + "重修会§b重置所有武魂属性§r并给予对应【§d武魂果实§r】。\n\n"
                        + "§c注意：雷劫不会劈死玩家！不需要恐惧被雷劈死！尽量不要去回血！");

        addArticle("重修遗迹",
                "§l重修遗迹：§r\n"
                        + "通过【§b重修之眼§r】寻找，使用方法与§b末影之眼§r一致。 \n\n可用此眼寻找重修台 \n当拥有彼岸花buff时右键重修台可进行转世重修");

        addArticle("深海遗迹",
                "§l深海遗迹：§r\n"
                        + "通过【§b深海之眼§r】寻找，使用方法与§b末影之眼§r一致。 \n\n可用此眼寻找海底祭坛的遗迹。 \n使用物品【深海祭品】右键海底祭坛可召唤【魔鲸】");

        addArticle("聚灵台阵",
                "§l聚灵台阵：§r\n"
                        + "§b聚灵台§r居中，半径§b2格(5*5*2)§r范围内放置§b聚魂基石柱§r解锁槽位。\n"
                        + "根据强度恢复能量，最多吃§b8个基石柱§r增幅，放多了基石柱会根据当前场上最高的基石柱来定。");

        addArticle("武魂果实",
                "§l武魂果实：§r\n"
                        + "转生后根据转生的玩家拥有的武魂给予，吃下即可觉醒对应武魂（§b最多觉醒3个§r），玩家吃下武魂果实时等级必须§c大于1级§r。");

        addArticle("NPC实体系统",
                "§lNPC实体系统：§r\n"
                        + "可在世界上随机找到NPC的生成。"
                        + "\n当玩家右键NPC实体时可打开对话面板，右侧为选择栏，选择栏可选择NPC的对话内容。"
                        + "\n可和NPC进行交易和售卖，这点不多说，主要的内容是："
                        + "\n§c玩家选择和NPC切磋时，NPC会主动攻击你，此时打败NPC后NPC会给予你§b魂币。"
                        + "\n§c但是，若玩家不选择切磋而是直接击杀NPC则必定获得它的魂骨，但前提是NPC生成时本身就带有魂骨，NPC的实力越强，携带魂骨的概率更高，运气好时甚至可以掉落全套高年限魂骨。");


        addArticle("内丹与丹药",
                "§l内丹系统：§r\n"
                        + "击杀不同年限的生物将掉落指定等阶但不同品质的§b内丹§r，内丹可炼制为§b经验丹§r，等级越高的炼丹炉炼制低阶丹药的速度越快，反之低等级炼丹炉炼制高阶内丹时效率越慢。\n"
                        + "§b炼丹品质介绍§r：当内丹放入炼丹炉的内丹槽位时，炼丹炉UI下方或玩家物品栏上方会出现当前等级内丹可炼制的§b丹药品质§r，炼制品质越高增幅越高，炼制品质想要更改就需要更好品质的内丹，每次击杀生物掉落内丹时有概率掉落更高品质的内丹，更高品质的内丹炼出高品质的丹药概率成正比。\n"
                        + "炼丹时有概率获得‘§7丹渣§r’品质的丹药，‘§7丹渣§r’品质丹药无法服用只能当做消耗材料，1个丹渣品质单独放置合成台可合成1个丹渣，9个丹渣可合成§7丹渣块§r，丹渣块放入炼丹炉中的丹渣槽位可让出现丹渣的概率降低为§b0§r，每次放入丹渣块炼制一个丹药就会扣除一个丹渣块。");

        addArticle("年限生成系统",
                "§l年限生成：§r\n"
                        + "玩家当前坐标距离当前维度世界原点（§b0,0§r）坐标越远生成§6高年限生物§r概率越高，最远§b10000格§r达到极限。\n"
                        + "所有的维度都遵从这点法则。");

        addArticle("魂骨系统",
                "§l魂骨系统：§r\n"
                        + "击杀生物时有§c极低概率§r掉落（§a1000万年以上的生物将100%掉落魂骨§r）。\n"
                        + "魂骨掉落时会生成§b词条§r，词条分为§b1~10属性§r随机生成，词条越多生成概率越低。");

        addArticle("蒲团修炼",
                "§l蒲团修炼：§r\n"
                        + "前期修满一周期(§b10分钟§r)§a直升2级§r。\n"
                        + "修炼耗尽后强制停止，除修炼外的行为均可恢复时间。\n"
                        + "耗尽后强行修炼仅恢复§9精神力§r。");

        addArticle("飞行系统",
                "§l飞行系统：§r\n"
                        + "最大§9精神力≥5000§r解锁，飞行时加快消耗§9精神力§r。\n"
                        + "飞行时伤害§c削弱40%§r(§b70级后递减至10%§r)。\n"
                        + "玩家等级越高，消耗的§9精神力§r越低。");

        addArticle("维度传送门的建造",
                "§l维度传送门的建造：§r\n"
                        + "特定生物掉落物品合成§b传送门框架§r，前往生成高年限生物的维度。\n"
                        + "§b极寒冰域传送门：§r将方块'§b极寒冰域传送门框架§r'按照原版地狱门的形状建造然后使用§b极寒石§r右键传送门方块的内面即可点亮传送门。\n"
                        + "§b万雷天域传送门：§r将方块'§b万雷天域传送门框架§r'按照原版地狱门的形状建造然后使用§b雷域石§r右键传送门方块的内面即可点亮传送门。");

        addArticle("神位获取方式",
                "§l神位获取：§r\n"
                        + "§b海神神位：§r击杀特定的海洋生物：§b溺尸、守卫者、远古守卫者§r，每次击杀有§b0.5%§r的概率获得神位。\n"
                        + "§c修罗神神位：§r玩家击杀任意生物时，当前玩家自身的攻击力§b>50000§r，有§b0.4%§r的概率获得。\n"
                        + "§a天使神神位：§r击杀类型为§a亡灵生物§r（如僵尸、骷髅、凋灵等任意的被设定为亡灵）的生物，有§b0.5%§r的概率获得。\n\n"
                        + "§r提示：当玩家等级等于§b99级§r时所有神位的获取设定概率为§b1%§r。\n"
                        + "并且打挑战塔每次通关的关卡越高，获得神位的概率越高，具体请看引导书内的“§b挑战系统§r”章节。");

        addArticle("神考任务解析",
                "§l神考任务解析：§r\n"
                        + "关于生命值的检查：根据玩家当前面板上的§c生命值§r进行检查，所以无论玩家用什么手段提升生命值，只要玩家打开面板时查看的§c最大生命值§r的属性满足条件即可点击神考的检查按钮就能提交神考。\n"
                        + "§b击杀生物考核§r：根据玩家击杀的生物数量自动提交，若在以前未触发此任务的情况下击杀再多的生物不计入考核数量，玩家当前神考的考核内容必须是对应击杀条件的任务才会开始计算击杀生物。\n"
                        + "§b提交物品考核§r：将指定物品放入背包后点击检查按钮后扣除玩家背包内的指定物品，扣除成功则为通过。");

        addArticle("特殊攻击",
                "§l特殊攻击：§r\n"
                        + "攻击概率触发§c撕裂、燃烧、震撼、湮灭§r等效果，增伤并附加§bdebuff§r（怪物也可以对你触发）。");

        addArticle("维度-极寒冰域",
                "§l极寒冰域：§r\n"
                        + "常年下雪，生成§6万年至百万年§r生物。\n"
                        + "无§b御寒魂导器§r将依据等级承受§c百分比真实伤害§r。");

        addArticle("维度-万雷天域",
                "§l万天雷域：§r\n"
                        + "常年降雷，生成§6万年至百万年§r生物。\n"
                        + "无§b电流防御魂器§r有极高概率遭受§c高额雷击§r。");
    }

    private void addArticle(String title, String content) {
        articles.add(new GuideArticle(title, content));
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        refreshCurrentArticle();

        this.addRenderableWidget(new ImageButton(x + 158, y + 155, 18, 10, 0, 0, 0, LEFT_BUTTON, 18, 10, b -> {
            if (currentPageInArticle > 0) {
                currentPageInArticle--;
                playPageSound();
            }
        }) {
            @Override
            public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                if (currentPageInArticle > 0) {
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                    RenderSystem.enableBlend();
                    ResourceLocation texture = this.isHovered ? LEFT_BUTTON_HOVER : LEFT_BUTTON;

                    graphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, 18, 10);
                    RenderSystem.disableBlend();
                }
            }
        });
        this.addRenderableWidget(new ImageButton(x + 252, y + 155, 18, 10, 0, 0, 0, RIGHT_BUTTON, 18, 10, b -> {
            if ((currentPageInArticle + 1) * LINES_PER_PAGE < cachedArticleLines.size()) {
                currentPageInArticle++;
                playPageSound();
            }
        }) {
            @Override
            public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                if ((currentPageInArticle + 1) * LINES_PER_PAGE < cachedArticleLines.size()) {
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                    RenderSystem.enableBlend();
                    ResourceLocation texture = this.isHovered ? RIGHT_BUTTON_HOVER : RIGHT_BUTTON;
                    graphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, 18, 10);
                    RenderSystem.disableBlend();
                }
            }
        });
    }

    private void refreshCurrentArticle() {
        if (selectedArticleIndex >= 0 && selectedArticleIndex < articles.size()) {
            GuideArticle article = articles.get(selectedArticleIndex);
            this.cachedArticleLines = this.font.split(Component.literal(article.content), RIGHT_TEXT_WIDTH);
            this.currentPageInArticle = 0;
        }
    }
    private void playPageSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 1. 绘制左侧目录列表 (带严密的裁剪区域，防越界)
        int maxScroll = Math.max(0, articles.size() * ITEM_HEIGHT - LEFT_PANEL_HEIGHT);
        int currentScrollY = (int) (scrollAmount * maxScroll);

        graphics.enableScissor(this.leftPos + LEFT_PANEL_X, this.topPos + LEFT_PANEL_Y,
                this.leftPos + LEFT_PANEL_X + LEFT_PANEL_WIDTH, this.topPos + LEFT_PANEL_Y + LEFT_PANEL_HEIGHT);

        for (int i = 0; i < articles.size(); i++) {
            int itemY = LEFT_PANEL_Y + (i * ITEM_HEIGHT) - currentScrollY;
            if (itemY + ITEM_HEIGHT < LEFT_PANEL_Y || itemY > LEFT_PANEL_Y + LEFT_PANEL_HEIGHT) continue;

            boolean isSelected = (i == selectedArticleIndex);
            String title = (isSelected ? "§1§l> " : "§8") + articles.get(i).title;

            graphics.drawString(this.font, title, LEFT_PANEL_X, itemY + 3, isSelected ? 0x0000AA : 0x404040, false);
        }
        graphics.disableScissor();

        // 2. 绘制右侧文本内容 (精确放置在 RIGHT_TEXT_X 以东)
        int lineY = RIGHT_TEXT_Y;
        int startLine = currentPageInArticle * LINES_PER_PAGE;

        for (int i = 0; i < LINES_PER_PAGE; i++) {
            int lineIndex = startLine + i;
            if (lineIndex >= cachedArticleLines.size()) break;

            FormattedCharSequence line = cachedArticleLines.get(lineIndex);
            graphics.drawString(this.font, line, RIGHT_TEXT_X, lineY + (i * 10), 0x303030, false);
        }

        // 3. 右页底部页码 (居中计算在 RIGHT_TEXT_X 与 RIGHT_TEXT_WIDTH 中间)
        int totalPages = Math.max(1, (int) Math.ceil((double) cachedArticleLines.size() / LINES_PER_PAGE));
        String pageCounter = (currentPageInArticle + 1) + " / " + totalPages;
        int pageCounterX = RIGHT_TEXT_X + (RIGHT_TEXT_WIDTH / 2) - (this.font.width(pageCounter) / 2);
        graphics.drawString(this.font, pageCounter, pageCounterX, 156, 0x808080, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(BOOK_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 292, 180);

        // 左侧滚动条
        int maxScroll = Math.max(0, articles.size() * ITEM_HEIGHT - LEFT_PANEL_HEIGHT);
        if (maxScroll > 0) {
            int scrollBarX = x + LEFT_PANEL_X + LEFT_PANEL_WIDTH - 2;
            int scrollBarY = y + LEFT_PANEL_Y + (int) (scrollAmount * (LEFT_PANEL_HEIGHT - 18));
            graphics.fill(scrollBarX, y + LEFT_PANEL_Y, scrollBarX + 2, y + LEFT_PANEL_Y + LEFT_PANEL_HEIGHT, 0x30000000); // 轨道
            graphics.fill(scrollBarX - 1, scrollBarY, scrollBarX + 3, scrollBarY + 18, 0xFF8B5A2B); // 棕色滑块
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int relativeX = (int) (mouseX - this.leftPos);
        int relativeY = (int) (mouseY - this.topPos);

        // 监听左侧目录点击
        if (relativeX >= LEFT_PANEL_X && relativeX <= LEFT_PANEL_X + LEFT_PANEL_WIDTH &&
                relativeY >= LEFT_PANEL_Y && relativeY <= LEFT_PANEL_Y + LEFT_PANEL_HEIGHT) {

            int maxScroll = Math.max(0, articles.size() * ITEM_HEIGHT - LEFT_PANEL_HEIGHT);
            int currentScrollY = (int) (scrollAmount * maxScroll);
            int clickedIndex = (relativeY - LEFT_PANEL_Y + currentScrollY) / ITEM_HEIGHT;

            if (clickedIndex >= 0 && clickedIndex < articles.size()) {
                this.selectedArticleIndex = clickedIndex;
                refreshCurrentArticle();
                playPageSound();
                return true;
            }
        }

        // 监听滑块区域点击
        if (relativeX >= LEFT_PANEL_X + LEFT_PANEL_WIDTH - 4 && relativeX <= LEFT_PANEL_X + LEFT_PANEL_WIDTH + 4 &&
                relativeY >= LEFT_PANEL_Y && relativeY <= LEFT_PANEL_Y + LEFT_PANEL_HEIGHT) {
            this.isScrolling = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isScrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isScrolling) {
            int maxScroll = Math.max(0, articles.size() * ITEM_HEIGHT - LEFT_PANEL_HEIGHT);
            if (maxScroll > 0) {
                float delta = (float) dragY / (LEFT_PANEL_HEIGHT - 18);
                this.scrollAmount = Mth.clamp(this.scrollAmount + delta, 0.0F, 1.0F);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxScroll = Math.max(0, articles.size() * ITEM_HEIGHT - LEFT_PANEL_HEIGHT);
        if (maxScroll > 0) {
            float scrollStep = 1.0F / (articles.size() - 5);
            this.scrollAmount = Mth.clamp((float) (this.scrollAmount - delta * scrollStep), 0.0F, 1.0F);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        if (this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.BOOK_PUT, 1.0F, 1.0F);
        }
        super.onClose();
    }

    private static class GuideArticle {
        String title;
        String content;

        public GuideArticle(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }
}