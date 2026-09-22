package com.TovidY.kunluncontinent.advancement;

import com.TovidY.kunluncontinent.KlMain;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/**
 * 通用成就触发器。
 *
 * <p>整套成就只用这一个触发器：JSON 里的 {@code "event"} 字段是一个事件 ID 字符串，
 * 游戏里通过 {@link AchievementAPI} 触发指定 ID 即可完成对应成就。
 * 这样新增成就时不需要再单独写一个 Trigger 类。</p>
 *
 * <p>生成的 JSON 形如：</p>
 * <pre>
 * "criteria": {
 *   "criterion": {
 *     "trigger": "kunluncontinent:kunlun_event",
 *     "conditions": { "event": "hunhuan_first" }
 *   }
 * }
 * </pre>
 */
public class KunlunTrigger extends SimpleCriterionTrigger<KunlunTrigger.Instance> {

    private static final ResourceLocation ID = new ResourceLocation(KlMain.MOD_ID, "kunlun_event");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    /** 触发指定事件：所有声明了该 event 的成就都会被判定。 */
    public void trigger(ServerPlayer player, String eventId) {
        this.trigger(player, instance -> instance.matches(eventId));
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new Instance(player, GsonHelper.getAsString(json, "event"));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final String eventId;

        public Instance(ContextAwarePredicate player, String eventId) {
            super(KunlunTrigger.ID, player);
            this.eventId = eventId;
        }

        public static Instance of(String eventId) {
            return new Instance(ContextAwarePredicate.ANY, eventId);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("event", this.eventId);
            return json;
        }

        public boolean matches(String currentEventId) {
            return this.eventId.equals(currentEventId);
        }
    }
}
