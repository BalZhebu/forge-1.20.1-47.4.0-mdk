package com.TovidY.kunluncontinent.entity.playernpc;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PlayerNpcRenderer extends MobRenderer<PlayerNpcEntity, PlayerModel<PlayerNpcEntity>> {

    // 史蒂夫默认备用皮肤路径
    private static final ResourceLocation DEFAULT_SKIN = new ResourceLocation("textures/entity/player/wide/steve.png");

    public PlayerNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(PlayerNpcEntity entity) {
        int index = entity.getSkinIndex();
        ResourceLocation customTexture = new ResourceLocation(KlMain.MOD_ID, "textures/entity/player_npc/skin_" + index + ".png");
        return customTexture;
    }
}