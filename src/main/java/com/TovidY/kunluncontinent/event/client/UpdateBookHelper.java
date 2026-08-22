package com.TovidY.kunluncontinent.event.client;

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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UpdateBookHelper {

    private static final String CHANGELOG_BASE_URL = "https://gitee.com/balzhebu/kunlun_continent_release/raw/master/Changelog/";

    // 原版成书单页尺寸标准（像素）
    private static final int PAGE_WIDTH_PX = 114;
    private static final int LINES_PER_PAGE = 14;

    public static ItemStack fetchAndBuildUpdateBook(String version) {
        String requestUrl = CHANGELOG_BASE_URL + version + ".json";
        HttpURLConnection conn = null;

        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (KunlunContinentMod)");

            if (conn.getResponseCode() == 200) {
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                    String title = json.has("title") ? json.get("title").getAsString() : version + "更新日志";
                    String fullUrl = json.has("full_url") ? json.get("full_url").getAsString() : requestUrl;

                    List<String> rawSections = new ArrayList<>();
                    if (json.has("content")) {
                        JsonArray array = json.getAsJsonArray("content");
                        for (JsonElement element : array) {
                            rawSections.add(element.getAsString());
                        }
                    }

                    return buildWrittenBook(title, version, rawSections, fullUrl);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }

        return createFallbackBook(version);
    }

    private static ItemStack buildWrittenBook(String bookTitle, String version, List<String> sections, String fullUrl) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();

        tag.putString("title", bookTitle);
        tag.putString("author", "昆仑大陆");
        tag.putInt("generation", 0);

        ListTag pages = new ListTag();
        MutableComponent page1 = Component.literal("§1§l【昆仑大陆版本更新】§r\n\n")
                .append("§0更新内容告示：\n当前版本：§1§l" + version + "§r\n\n")
                .append("§8详细请看下一页...§r");
        pages.add(StringTag.valueOf(Component.Serializer.toJson(page1)));
        StringBuilder cleanContent = new StringBuilder();
        for (String section : sections) {
            if (section == null || section.trim().isEmpty()) continue;
            String normalized = section.replace("\r\n", "\n").replace("\r", "\n");
            normalized = normalized.replaceAll("(?m)^(\\s*\\d+\\.|[一二三四五六七八九十]+[、.])\\s*\\n+", "$1 ");
            cleanContent.append(normalized.trim()).append("\n\n");
        }

        List<String> calculatedPages = splitByPixelWidth(cleanContent.toString(), PAGE_WIDTH_PX, LINES_PER_PAGE);
        for (String pageText : calculatedPages) {
            MutableComponent pageComponent = Component.literal("§0" + pageText.trim());
            pages.add(StringTag.valueOf(Component.Serializer.toJson(pageComponent)));
        }

        MutableComponent lastPage = Component.literal("§1§l【历代更新日志】§r\n\n")
                .append("§0文本太多，请点击下方蓝色字体前往查看：\n\n");

        MutableComponent linkBtn = Component.literal("§1§n[ 点击查看全卷更新文档 ]§r")
                .withStyle(style -> style
                        .withColor(ChatFormatting.BLUE)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, fullUrl)));

        lastPage.append(linkBtn);
        pages.add(StringTag.valueOf(Component.Serializer.toJson(lastPage)));

        tag.put("pages", pages);
        return book;
    }

    /**
     * 服务端模拟 Minecraft Font 像素感应切行算法
     */
    private static List<String> splitByPixelWidth(String text, int maxPixelWidth, int maxLinesPerPage) {
        List<String> pages = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        String[] rawParagraphs = text.split("\n");

        for (String paragraph : rawParagraphs) {
            if (paragraph.isEmpty()) {
                lines.add(""); // 保留空行
                continue;
            }

            StringBuilder currentLine = new StringBuilder();
            int currentLineWidth = 0;

            for (int i = 0; i < paragraph.length(); i++) {
                char c = paragraph.charAt(i);

                // 忽略 MC 样式代码 (§0-§f, §k-§r) 的宽度计算
                if (c == '§' && i + 1 < paragraph.length()) {
                    currentLine.append(c).append(paragraph.charAt(i + 1));
                    i++;
                    continue;
                }

                int charWidth = getCharPixelWidth(c);

                if (currentLineWidth + charWidth > maxPixelWidth) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                    currentLineWidth = 0;
                }

                currentLine.append(c);
                currentLineWidth += charWidth;
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }

        // 按照 LINES_PER_PAGE (14行) 组合成页
        StringBuilder currentPage = new StringBuilder();
        int lineCount = 0;

        for (String line : lines) {
            if (lineCount >= maxLinesPerPage) {
                pages.add(currentPage.toString());
                currentPage.setLength(0);
                lineCount = 0;
            }
            currentPage.append(line).append("\n");
            lineCount++;
        }

        if (currentPage.length() > 0) {
            pages.add(currentPage.toString());
        }

        return pages;
    }

    /**
     * Minecraft 默认字体像素宽度映射
     */
    private static int getCharPixelWidth(char c) {
        if (c >= 0x4E00 && c <= 0x9FA5) {
            return 9;
        }
        if (c >= 'a' && c <= 'z') return 6;
        if (c >= 'A' && c <= 'Z') return 6;
        if (c >= '0' && c <= '9') return 6;
        if (c == ' ' || c == '.' || c == '!' || c == ':') return 4;
        if (c == ',' || c == ';') return 4;
        return 7; // 其余符号默认 7 像素
    }

    private static ItemStack createFallbackBook(String version) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", version + "更新日志");
        tag.putString("author", "昆仑大陆");

        ListTag pages = new ListTag();
        MutableComponent p = Component.literal("§1§l【昆仑大陆更新】§r\n\n")
                .append("当前版本：" + version + "\n\n")
                .append("§8网络连接异常，未能获取远程详细日志。请前往官方群查看。§r");
        pages.add(StringTag.valueOf(Component.Serializer.toJson(p)));

        tag.put("pages", pages);
        return book;
    }
}