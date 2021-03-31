package net.dodogang.bigrat.client.model.entity;

import net.dodogang.bigrat.BigRat;
import net.dodogang.bigrat.entity.BigRatEntity;
import net.dodogang.bigrat.init.BigRatEntities;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BigRatEntityModel extends AnimatedGeoModel<BigRatEntity> {
    @Override
    public Identifier getModelLocation(BigRatEntity entity) {
        return new Identifier(BigRat.MOD_ID, "geo/" + BigRatEntity.id + ".json");
    }

    @Override
    public Identifier getTextureLocation(BigRatEntity entity) {
        return BigRatEntities.texture(BigRatEntity.id + "/" + BigRatEntity.id);
    }

    @Override
    public Identifier getAnimationFileLocation(BigRatEntity entity) {
        return new Identifier(BigRat.MOD_ID, "animations/entity/" + BigRatEntity.id + ".json");
    }
}
