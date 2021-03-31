package net.dodogang.bigrat;

import net.dodogang.bigrat.client.renderer.entity.BigRatEntityRenderer;
import net.dodogang.bigrat.init.BigRatEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class BigRatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BigRat.log("Initializing client");

        EntityRendererRegistry errInstance = EntityRendererRegistry.INSTANCE;
        errInstance.register(BigRatEntities.BIG_RAT, BigRatEntityRenderer::new);

        BigRat.log("Initialized client");
    }
}
