package com.TovidY.kunluncontinent.special.lonebamboo;

import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.special.SpecialSoulRing;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * 孤竹 —— 娱乐魂环。
 *
 * <p>孤竹本身是主世界地表的一株景观竹子（不掉魂骨、魂环必掉），
 * 吸收它的魂环后，<b>该魂环占据的那个槽位</b>会被替换成「撑杆跳」这个娱乐魂技，
 * 而不是该武魂的正常魂技。魂环照常计入魂环列表与 9 环上限，
 * 也就是「拿一个战斗魂技槽换一个娱乐技能」。</p>
 */
public class LoneBambooSoulRing implements SpecialSoulRing {

    @Override
    public EntityType<?> entityType() {
        return EntityInit.LONE_BAMBOO.get();
    }

    /** 娱乐魂环：必定掉落，不掷年限概率骰。 */
    @Override
    public boolean alwaysDropSoulRing() {
        return true;
    }

    /** 娱乐魂环：必定不掉魂骨。 */
    @Override
    public boolean neverDropSoulBone() {
        return true;
    }

    /**
     * 吸收后该槽位的魂技固定为「撑杆跳」。
     *
     * <p>不看武魂也不看环数 —— 孤竹不是任何武魂的魂兽，没必要按武魂分池。
     * 以后如果想让某个特殊生物「按环数给不同技能」，在实现里按 {@code ringIndex}
     * 分支即可，接口已经带了这个参数。</p>
     */
    @Nullable
    @Override
    public BaseSkillItem createSkill(String wuhun, int ringIndex, RandomSource random) {
        return ModItems.SKILL_POLE_VAULT.get();
    }
}
