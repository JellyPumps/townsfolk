package com.bouncyjelly.townsfolk.client.render;

import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class CitizenRenderer
        extends BipedEntityRenderer<CitizenEntity, PlayerEntityModel<CitizenEntity>> {
    private static final Identifier TEXTURE =
            Identifier.ofVanilla("textures/entity/player/wide/steve.png");

    public CitizenRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public Identifier getTexture(CitizenEntity entity) {
        return TEXTURE;
    }
}
