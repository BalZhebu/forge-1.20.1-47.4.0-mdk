package com.TovidY.kunluncontinent.advancement;

import com.TovidY.kunluncontinent.KlMain;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class LevelTrigger extends SimpleCriterionTrigger<LevelTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation(KlMain.MOD_ID, "level_up");

    @Override
    public ResourceLocation getId() { return ID; }

    public void trigger(ServerPlayer player, int currentLevel) {
        this.trigger(player, instance -> instance.matches(currentLevel));
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new Instance(player, GsonHelper.getAsInt(json, "min_level"));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final int minLevel;

        public Instance(ContextAwarePredicate player, int minLevel) {
            super(LevelTrigger.ID, player);
            this.minLevel = minLevel;
        }

        public static Instance levelReached(int level) {
            return new Instance(ContextAwarePredicate.ANY, level);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("min_level", minLevel);
            return json;
        }

        public boolean matches(int currentLevel) {
            return currentLevel >= this.minLevel;
        }
    }

}
