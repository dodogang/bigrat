package net.dodogang.bigrat.init;

import net.dodogang.bigrat.BigRat;
import net.dodogang.bigrat.entity.BigRatEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;

public class BigRatEntities {
    public static final EntityType<BigRatEntity> BIG_RAT = register(BigRatEntity.id, FabricEntityTypeBuilder.createMob()
        .entityFactory(BigRatEntity::new)
        .defaultAttributes(() -> MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D))
        .dimensions(EntityDimensions.changing(1.4f, 1.4f))
        .spawnGroup(SpawnGroup.MONSTER)
        .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BigRatEntity::canSpawn)
        ,createSpawnEggColors(0xe87422, 0xf5d7a6)
    );

    @SuppressWarnings("deprecation")
    public BigRatEntities() {
        BiomeModifications.addSpawn(BiomeSelectors.all(), SpawnGroup.MONSTER, BigRatEntities.BIG_RAT, 10, 1, 3);
    }

    private static <T extends Entity> EntityType<T> register(String id, FabricEntityTypeBuilder<T> entityType, int[] spawnEggColors) {
        EntityType<T> builtEntityType = entityType.build();

        if (spawnEggColors != null) {
            Registry.register(Registry.ITEM, new Identifier(BigRat.MOD_ID, id + "_spawn_egg"), new SpawnEggItem(builtEntityType, spawnEggColors[0], spawnEggColors[1], new Item.Settings().maxCount(64).group(BigRat.ITEM_GROUP)));
        }

        return Registry.register(Registry.ENTITY_TYPE, new Identifier(BigRat.MOD_ID, id), builtEntityType);
    }

    public static Identifier texture(String path) {
        return BigRat.texture("entity/" + path);
    }
    protected static int[] createSpawnEggColors(int primary, int secondary) {
        return new int[]{ primary, secondary };
    }
}
