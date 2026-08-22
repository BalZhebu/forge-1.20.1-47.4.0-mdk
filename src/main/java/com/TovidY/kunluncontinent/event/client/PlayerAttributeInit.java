package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.Init.KLConfig;
import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.ModItems;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

//设置玩家初始属性

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class PlayerAttributeInit {

    private static final String CURRENT_VERSION = ModList.get()
            .getModContainerById(KlMain.MOD_ID)
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse("0.0.1");

    private static final String VERSION_CHECK_URL = "https://gitee.com/balzhebu/kunlun_continent_release/raw/master/version.json";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        checkUpdateAndNotify(player);
        // 2. 检查是否刚刚更新了 MOD，若是则调用 UpdateBookHelper 发更新日志书
        checkFirstLoginAfterUpdate(player);
        // 3. 玩家 Capability 初始化
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attributes -> {
            if (!attributes.isInitialized()) {
                if (attributes.isBrandNewConfig()) {
                    attributes.initDefaultAttributes();
                    ItemStack book = new ItemStack(ModItems.GUIDE_BOOK.get());
                    if (!player.getInventory().add(book)) {
                        player.drop(book, false);
                    }
                }
                attributes.setInitialized(true);
            }
            syncMaxHealthToPlayer(player, attributes.getMaxshengming());
            syncAllAttributesToClient(player, attributes);
        });
    }

    /**
     * 判断是否为刚升级 MOD 后的首次登录
     */
    private static void checkFirstLoginAfterUpdate(ServerPlayer player) {
        String lastSeenVersion = KLConfig.LAST_SEEN_VERSION.get();

        if (!CURRENT_VERSION.equals(lastSeenVersion)) {
            CompletableFuture.supplyAsync(() -> UpdateBookHelper.fetchAndBuildUpdateBook(CURRENT_VERSION))
                    .thenAcceptAsync(updateBook -> {
                        player.getServer().execute(() -> {
                            if (!player.getInventory().add(updateBook)) {
                                player.drop(updateBook, false);
                            }
                            player.sendSystemMessage(
                                    Component.literal("[昆仑大陆] 检测到版本升级！本次《" + CURRENT_VERSION + "更新日志》已放入您的背包。")
                                            .withStyle(ChatFormatting.GOLD)
                            );
                        });
                    });

            KLConfig.LAST_SEEN_VERSION.set(CURRENT_VERSION);
            KLConfig.LAST_SEEN_VERSION.save();
        }
    }

    private static void checkUpdateAndNotify(ServerPlayer player) {
        CompletableFuture.runAsync(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(VERSION_CHECK_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (KunlunContinentMod)");

                if (conn.getResponseCode() == 200) {
                    try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                        String latestVersion = json.has("latest_version") ? json.get("latest_version").getAsString() : "";
                        String downloadUrl = json.has("download_url") ? json.get("download_url").getAsString() : "";

                        if (!latestVersion.isEmpty() && isVersionOutdated(CURRENT_VERSION, latestVersion)) {
                            player.getServer().execute(() -> sendUpdateMessage(player, latestVersion, downloadUrl));
                        }
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private static void sendUpdateMessage(ServerPlayer player, String latestVersion, String downloadUrl) {
        MutableComponent prefix = Component.literal("[昆仑大陆] ").withStyle(ChatFormatting.AQUA);
        MutableComponent msg = Component.literal("检测到新版本 ")
                .append(Component.literal("v" + latestVersion).withStyle(ChatFormatting.GREEN))
                .append("\n当前版本: ")
                .append(Component.literal("v" + CURRENT_VERSION).withStyle(ChatFormatting.RED))
                .append("\n");
        MutableComponent linkBtn = Component.literal("[ 点击前往下载更新 ]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.YELLOW)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击在浏览器中打开更新下载页面"))));

        player.sendSystemMessage(prefix.append(msg).append(linkBtn));
    }

    private static boolean isVersionOutdated(String current, String latest) {
        if (current == null || latest == null) return false;
        if (current.equalsIgnoreCase(latest)) return false;

        try {
            String cleanCurrent = cleanVersionString(current);
            String cleanLatest = cleanVersionString(latest);
            String[] currParts = cleanCurrent.split("[^0-9]+");
            String[] lateParts = cleanLatest.split("[^0-9]+");
            int length = Math.max(currParts.length, lateParts.length);
            for (int i = 0; i < length; i++) {
                int currNum = i < currParts.length && !currParts[i].isEmpty() ? Integer.parseInt(currParts[i]) : 0;
                int lateNum = i < lateParts.length && !lateParts[i].isEmpty() ? Integer.parseInt(lateParts[i]) : 0;
                if (currNum < lateNum) return true;
                if (currNum > lateNum) return false;
            }
            if (current.contains("beta") && !latest.contains("beta")) {
                return true;
            }
        } catch (Exception e) {
            return !current.equalsIgnoreCase(latest);
        }
        return false;
    }

    private static String cleanVersionString(String ver) {
        if (ver.startsWith("1.20.1_")) {
            return ver.substring("1.20.1_".length());
        }
        return ver;
    }

    public static void syncMaxHealthToPlayer(Player player, float targetMaxHealth) {
        if (player == null) return;
        var maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null && (float) maxHealthAttr.getBaseValue() != targetMaxHealth) {
            maxHealthAttr.setBaseValue(targetMaxHealth);
        }
    }

    private static void syncAllAttributesToClient(ServerPlayer player, PlayerAttributeCapability attr) {
        CompoundTag nbtData = attr.serializeNBT();
        com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute packet =
                new com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute(player.getId(), nbtData);
        com.TovidY.kunluncontinent.network.NetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                packet
        );
    }
}