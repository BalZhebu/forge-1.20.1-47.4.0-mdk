package com.TovidY.kunluncontinent.mixin;

import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * 昆仑大陆的 Mixin 配置插件 —— 用来让「只针对某个可选 mod 的适配 Mixin」
 * 在该 mod 不存在时自动跳过。
 *
 * <h3>为什么需要</h3>
 * <p>{@code MixinEntityDisplayName} 是为了绕开 Alex's Mobs 宇宙鳕鱼的
 * 「hasCustomName() → 自定义持久化 → 不计入刷怪上限」问题而写的补丁。
 * 它注入的是原版 {@code Entity#getDisplayName()}、<b>没有引用任何 alexsmobs 的类</b>，
 * 所以缺 mod 时加载也不会崩；但那样它就是个纯负担（每个实体每次都多跑一遍判定），
 * 而且对没装 Alex's Mobs 的整合包毫无意义。</p>
 *
 * <h3>做法</h3>
 * <p>在 {@code shouldApplyMixin} 里按 modid 判断，未安装就返回 {@code false}
 * —— Mixin 根本不会被应用，等价于「这个补丁不存在」。
 * 其余 Mixin 一律照常应用，不受影响。</p>
 *
 * <h3>注意</h3>
 * <ul>
 *   <li>判断走 {@code FMLLoader.getLoadingModList()}：这个阶段 mod 列表已经扫完，
 *       而 Forge 的类加载器也可见，不需要（也不能）碰 {@code ModList}；</li>
 *   <li>判断本身出任何异常都 <b>放行</b> —— 宁可多应用一次也绝不因为判断失败而漏掉补丁，
 *       反正这个 Mixin 不引用目标 mod 的类，应用了也不会崩；</li>
 *   <li>新增「可选 mod 适配 Mixin」时，把类名加进 {@link #OPTIONAL_MIXINS} 与对应 modid 即可。</li>
 * </ul>
 */
public class KunlunMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("KunlunMixin");

    /** 只针对可选 mod 的适配 Mixin → 所需的 modid。 */
    private static final String MIXIN_ENTITY_DISPLAY_NAME =
            "com.TovidY.kunluncontinent.mixin.MixinEntityDisplayName";
    private static final String MOD_ALEXS_MOBS = "alexsmobs";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        // 沿用 kunluncontinent.mixins.json 里声明的 refmap
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (MIXIN_ENTITY_DISPLAY_NAME.equals(mixinClassName)) {
            boolean present = isModPresent(MOD_ALEXS_MOBS);
            if (!present) {
                LOGGER.info("[Kunlun] skipped {} (mod '{}' not installed)",
                        "MixinEntityDisplayName", MOD_ALEXS_MOBS);
            }
            return present;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
    }

    /** mod 是否存在于当前加载列表里；任何异常都按「存在」处理。 */
    private static boolean isModPresent(String modId) {
        try {
            var modList = FMLLoader.getLoadingModList();
            return modList == null || modList.getModFileById(modId) != null;
        } catch (Throwable t) {
            return true;
        }
    }
}
