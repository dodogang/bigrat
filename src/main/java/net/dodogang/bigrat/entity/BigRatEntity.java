package net.dodogang.bigrat.entity;

import net.dodogang.bigrat.init.BigRatSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.Durations;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IntRange;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.*;

public class BigRatEntity extends AnimalEntity implements Angerable, IAnimatable {
    public static final String id = "big_rat";

    private static final TrackedData<Boolean> WARNING = DataTracker.registerData(BigRatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> MOVING = DataTracker.registerData(BigRatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private float lastWarningAnimationProgress;
    private float warningAnimationProgress;
    private int warningSoundCooldown;
    private static final IntRange ANGER_TIME_RANGE = Durations.betweenSeconds(20, 39);
    private int angerTime;
    private UUID targetUuid;

    public BigRatEntity(EntityType<? extends BigRatEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return EntityType.POLAR_BEAR.create(world);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new BigRatEntity.AttackGoal());
        this.goalSelector.add(1, new BigRatEntity.PolarBearEscapeDangerGoal());
        this.goalSelector.add(4, new FollowParentGoal(this, 1.25D));
        this.goalSelector.add(5, new WanderAroundGoal(this, 1.0D));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new BigRatRevengeGoal());
        this.targetSelector.add(2, new BigRatEntity.FollowPlayersGoal());
        this.targetSelector.add(3, new FollowTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
        this.targetSelector.add(4, new FollowTargetGoal<>(this, FoxEntity.class, 10, true, true, null));
        this.targetSelector.add(5, new UniversalAngerGoal<>(this, false));
    }

    public static boolean canSpawn(EntityType<BigRatEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        Optional<RegistryKey<Biome>> optional = world.getBiomeKey(pos);
        if (!Objects.equals(optional, Optional.of(BiomeKeys.FROZEN_OCEAN)) && !Objects.equals(optional, Optional.of(BiomeKeys.DEEP_FROZEN_OCEAN))) {
            return isValidNaturalSpawn(type, world, spawnReason, pos, random);
        } else {
            return world.getBaseLightLevel(pos, 0) > 8 && world.getBlockState(pos.down()).isOf(Blocks.GRASS_BLOCK);
        }
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        this.angerFromTag((ServerWorld)this.world, tag);
        this.setMoving(tag.getBoolean("IsMoving"));
    }

    @Override
    public void writeCustomDataToTag(CompoundTag tag) {
        super.writeCustomDataToTag(tag);
        this.angerToTag(tag);
        tag.putBoolean("IsMoving", this.isMoving());
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.choose(this.random));
    }

    @Override
    public void setAngerTime(int ticks) {
        this.angerTime = ticks;
    }

    @Override
    public int getAngerTime() {
        return this.angerTime;
    }

    @Override
    public void setAngryAt(@Nullable UUID uuid) {
        this.targetUuid = uuid;
    }

    @Override
    public UUID getAngryAt() {
        return this.targetUuid;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? BigRatSoundEvents.ENTITY_BIG_RAT_AMBIENT_BABY : BigRatSoundEvents.ENTITY_BIG_RAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return BigRatSoundEvents.ENTITY_BIG_RAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return BigRatSoundEvents.ENTITY_BIG_RAT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(BigRatSoundEvents.ENTITY_BIG_RAT_STEP, 0.15F, 1.0F);
    }

    protected void playWarningSound() {
        if (this.warningSoundCooldown <= 0) {
            this.playSound(BigRatSoundEvents.ENTITY_BIG_RAT_WARNING, 1.0F, this.getSoundPitch());
            this.warningSoundCooldown = 40;
        }

    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(WARNING, false);
        this.dataTracker.startTracking(MOVING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.world.isClient) {
            if (this.warningAnimationProgress != this.lastWarningAnimationProgress) {
                this.calculateDimensions();
            }

            this.lastWarningAnimationProgress = this.warningAnimationProgress;
            if (this.isWarning()) {
                this.warningAnimationProgress = MathHelper.clamp(this.warningAnimationProgress + 1.0F, 0.0F, 6.0F);
            } else {
                this.warningAnimationProgress = MathHelper.clamp(this.warningAnimationProgress - 1.0F, 0.0F, 6.0F);
            }
        }

        if (this.warningSoundCooldown > 0) {
            --this.warningSoundCooldown;
        }

        if (!this.world.isClient) {
            this.tickAngerLogic((ServerWorld)this.world, true);

            Vec3d vel = this.getVelocity();
            this.setMoving(vel.getX() != 0.0d || vel.getZ() != 0.0d);
        }
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (!this.world.isClient && this.isAlive() && !this.isBaby() && this.random.nextFloat() <= 0.007f && this.world.getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(8.0d), item -> item.getStack().getItem().isIn(ItemTags.FLOWERS)).size() <= 4) {
            this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
            this.dropItem(ItemTags.FLOWERS.getRandom(this.random));
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        if (this.warningAnimationProgress > 0.0F) {
            float f = this.warningAnimationProgress / 6.0F;
            float g = 1.0F + f;
            return super.getDimensions(pose).scaled(1.0F, g);
        } else {
            return super.getDimensions(pose);
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean bl = target.damage(DamageSource.mob(this), (float)((int)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)));
        if (bl) {
            this.dealDamage(this, target);
        }

        return bl;
    }

    public boolean isWarning() {
        return this.dataTracker.get(WARNING);
    }
    public void setWarning(boolean warning) {
        this.dataTracker.set(WARNING, warning);
    }

    public boolean isMoving() {
        return this.dataTracker.get(MOVING);
    }
    public void setMoving(boolean moving) {
        this.dataTracker.set(MOVING, moving);
    }

    @Override
    protected float getBaseMovementSpeedMultiplier() {
        return 0.98F;
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
        if (entityData == null) {
            entityData = new PassiveData(1.0F);
        }

        return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0.0f, this::controller));
    }
    private <E extends IAnimatable> PlayState controller(AnimationEvent<E> event) {
        if (this.isMoving()) {
            boolean isWarning = this.isWarning();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.big_rat." + (isWarning ? "warning" : "moving"), !isWarning));
        }

        return PlayState.CONTINUE;
    }

    private final AnimationFactory factory = new AnimationFactory(this);
    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    class PolarBearEscapeDangerGoal extends EscapeDangerGoal {
        public PolarBearEscapeDangerGoal() {
            super(BigRatEntity.this, 2.0D);
        }

        @Override
        public boolean canStart() {
            return (BigRatEntity.this.isBaby() || BigRatEntity.this.isOnFire()) && super.canStart();
        }
    }

    class AttackGoal extends MeleeAttackGoal {
        public AttackGoal() {
            super(BigRatEntity.this, 1.0D, true);
        }

        @Override
        protected void attack(LivingEntity target, double squaredDistance) {
            double d = this.getSquaredMaxAttackDistance(target);
            if (squaredDistance <= d && this.method_28347()) {
                this.method_28346();
                this.mob.tryAttack(target);
                BigRatEntity.this.setWarning(false);
            } else if (squaredDistance <= d * 2.0D) {
                if (this.method_28347()) {
                    BigRatEntity.this.setWarning(false);
                    this.method_28346();
                }

                if (this.method_28348() <= 10) {
                    BigRatEntity.this.setWarning(true);
                    BigRatEntity.this.playWarningSound();
                }
            } else {
                this.method_28346();
                BigRatEntity.this.setWarning(false);
            }

        }

        @Override
        public void stop() {
            BigRatEntity.this.setWarning(false);
            super.stop();
        }

        @Override
        protected double getSquaredMaxAttackDistance(LivingEntity entity) {
            return 4.0F + entity.getWidth();
        }
    }

    class FollowPlayersGoal extends FollowTargetGoal<PlayerEntity> {
        public FollowPlayersGoal() {
            super(BigRatEntity.this, PlayerEntity.class, 20, true, true, null);
        }

        @Override
        public boolean canStart() {
            if (!BigRatEntity.this.isBaby()) {
                if (super.canStart()) {
                    List<BigRatEntity> list = BigRatEntity.this.world.getNonSpectatingEntities(BigRatEntity.class, BigRatEntity.this.getBoundingBox().expand(8.0D, 4.0D, 8.0D));
                    for (BigRatEntity polarBearEntity : list) {
                        if (polarBearEntity.isBaby()) {
                            return true;
                        }
                    }
                }

            }
            return false;
        }

        @Override
        protected double getFollowRange() {
            return super.getFollowRange() * 0.5D;
        }
    }

    class BigRatRevengeGoal extends RevengeGoal {
        public BigRatRevengeGoal() {
            super(BigRatEntity.this);
        }

        @Override
        public void start() {
            super.start();
            if (BigRatEntity.this.isBaby()) {
                this.callSameTypeForRevenge();
                this.stop();
            }
        }

        @Override
        protected void setMobEntityTarget(MobEntity mob, LivingEntity target) {
            if (mob instanceof BigRatEntity && !mob.isBaby()) {
                super.setMobEntityTarget(mob, target);
            }
        }
    }
}
