package net.dodogang.bigrat.client.renderer.entity;

import net.dodogang.bigrat.client.model.entity.BigRatEntityModel;
import net.dodogang.bigrat.entity.BigRatEntity;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib3.renderer.geo.GeoEntityRenderer;

public class BigRatEntityRenderer extends GeoEntityRenderer<BigRatEntity> {
    @SuppressWarnings("unused")
    public BigRatEntityRenderer(EntityRenderDispatcher dispatcher, EntityRendererRegistry.Context ctx) {
        super(dispatcher, new BigRatEntityModel());
    }

    @Override
    protected void applyRotations(BigRatEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        EntityPose entityPose = entity.getPose();
        if (entityPose != EntityPose.SLEEPING) {
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0f - bodyYaw));
        }

        if (entity.isBaby()) {
            matrices.scale(0.5f, 0.5f, 0.5f);
        }

        if (entity.deathTime > 0) {
            float f = ((float)entity.deathTime + tickDelta - 1.0f) / 20.0f * 1.6f;
            f = MathHelper.sqrt(f);
            if (f > 1.0f) {
                f = 1.0f;
            }

            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(f * 90.0f));
        } else if (entity.isUsingRiptide()) {
            matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90.0f - entity.pitch));
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(((float)entity.age + tickDelta) * -75.0f));
        } else if (entityPose == EntityPose.SLEEPING) {
            Direction direction = entity.getSleepingDirection();
            float g = direction != null ? getYaw(direction) : bodyYaw;
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(g));
            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90.0f));
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270.0f));
        } else if (entity.hasCustomName()) {
            String string = Formatting.strip(entity.getName().getString());
            if (("Dinnerbone".equals(string) || "Grumm".equals(string))) {
                matrices.translate(0.0d, entity.getHeight() + 0.1f, 0.0d);
                matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0f));
            }
        }
    }

    private static float getYaw(Direction direction) {
        switch(direction) {
            case SOUTH:
                return 90.0F;
            /*case WEST:
                return 0.0F;*/
            case NORTH:
                return 270.0F;
            case EAST:
                return 180.0F;
            default:
                return 0.0F;
        }
    }
}
