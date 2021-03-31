package net.dodogang.bigrat.init;

import net.dodogang.bigrat.BigRat;
import net.dodogang.bigrat.entity.BigRatEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BigRatSoundEvents {
    public static final SoundEvent ENTITY_BIG_RAT_AMBIENT = createBigRatSound("ambient");
    public static final SoundEvent ENTITY_BIG_RAT_AMBIENT_BABY = createBigRatSound("ambient_baby");
    public static final SoundEvent ENTITY_BIG_RAT_HURT = createBigRatSound("hurt");
    public static final SoundEvent ENTITY_BIG_RAT_STEP = createBigRatSound("step");
    public static final SoundEvent ENTITY_BIG_RAT_WARNING = createBigRatSound("warning");
    public static final SoundEvent ENTITY_BIG_RAT_DEATH = createBigRatSound("death");
    private static SoundEvent createBigRatSound(String id) {
        return createEntitySound(BigRatEntity.id, id);
    }

    private static SoundEvent createEntitySound(String entity, String id) {
        return register("entity." + entity + "." + id);
    }

    private static SoundEvent register(String id) {
        Identifier identifier = new Identifier(BigRat.MOD_ID, id);
        return Registry.register(Registry.SOUND_EVENT, identifier, new SoundEvent(identifier));
    }
}
