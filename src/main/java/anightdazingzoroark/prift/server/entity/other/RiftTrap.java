package anightdazingzoroark.prift.server.entity.other;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class RiftTrap extends Entity {
    private static final DataParameter<Integer> PARTICLE_COLOR = EntityDataManager.createKey(RiftTrap.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DETECT_RANGE = EntityDataManager.createKey(RiftTrap.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> EFFECT_RANGE = EntityDataManager.createKey(RiftTrap.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> EXPLOSION_STRENGTH = EntityDataManager.createKey(RiftTrap.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> EXPLOSION_CAN_BREAK_BLOCKS = EntityDataManager.createKey(RiftTrap.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> EFFECT = EntityDataManager.createKey(RiftTrap.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> EFFECT_LENGTH = EntityDataManager.createKey(RiftTrap.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> EFFECT_STRENGTH = EntityDataManager.createKey(RiftTrap.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LIFESPAN = EntityDataManager.createKey(RiftTrap.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<UUID>> OWNER_ID = EntityDataManager.createKey(RiftTrap.class, DataSerializers.OPTIONAL_UNIQUE_ID);

    public RiftTrap(World worldIn) {
        this(worldIn, null, 0xFFFFFF);
    }

    public RiftTrap(World worldIn, EntityPlayer owner, int color) {
        super(worldIn);
        this.setSize(1f, 1f);
        if (owner != null) this.setOwnerUniqueId(owner.getUniqueID());
        this.setParticleColor(color);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(PARTICLE_COLOR, 0);
        this.dataManager.register(DETECT_RANGE, 0f);
        this.dataManager.register(EFFECT_RANGE, 0f);
        this.dataManager.register(EXPLOSION_STRENGTH, 0f);
        this.dataManager.register(EXPLOSION_CAN_BREAK_BLOCKS, false);
        this.dataManager.register(EFFECT, -1);
        this.dataManager.register(EFFECT_LENGTH, 0);
        this.dataManager.register(EFFECT_STRENGTH, 0);
        this.dataManager.register(LIFESPAN, 1200); //corresponds to 1 minute
        this.dataManager.register(OWNER_ID, Optional.absent());
    }

    public void onUpdate() {
        super.onUpdate();

        //for particle emission
        if (this.world.isRemote) {
            if (this.getDetectRange() > 0) {
                for (int i = 0; i < 10; i++) {
                    double x = RiftUtil.randomInRange(this.posX - this.getDetectRange(), this.posX + this.getDetectRange());
                    double z = RiftUtil.randomInRange(this.posZ - this.getDetectRange(), this.posZ + this.getDetectRange());
                    RiftInitialize.PROXY.spawnTrapParticle(this.getParticleColor(), x, this.posY, z, 0D, 0, 0D);
                }
            }
        }
        else {
            //for what happens when the player gets too close to one
            if (this.getDetectRange() > 0) {
                AxisAlignedBB detectionAABB = new AxisAlignedBB(this.posX - this.getDetectRange(),
                        this.posY,
                        this.posZ - this.getDetectRange(),
                        this.posX + this.getDetectRange(),
                        this.posY + 1,
                        this.posZ + this.getDetectRange());

                List<EntityLivingBase> detectedEntities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, detectionAABB, new Predicate<EntityLivingBase>() {
                    @Override
                    public boolean apply(@Nullable EntityLivingBase entityLivingBase) {
                        return checkForNoAssociations(entityLivingBase);
                    }
                });
                if (!detectedEntities.isEmpty()) {
                    AxisAlignedBB effectAABB = new AxisAlignedBB(this.posX - this.getDetectRange(),
                            this.posY,
                            this.posZ - this.getDetectRange(),
                            this.posX + this.getDetectRange(),
                            this.posY + 1,
                            this.posZ + this.getDetectRange());

                    List<EntityLivingBase> affectedEntities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, effectAABB, new Predicate<EntityLivingBase>() {
                        @Override
                        public boolean apply(@Nullable EntityLivingBase entityLivingBase) {
                            return checkForNoAssociations(entityLivingBase);
                        }
                    });

                    if (this.getEffectToApply() != null) {
                        for (EntityLivingBase entityLivingBase : affectedEntities) entityLivingBase.addPotionEffect(this.getEffectToApply());
                    }

                    if (this.getExplosionStrength() > 0) this.world.createExplosion(this, this.posX, this.posY, this.posZ, this.getExplosionStrength(), this.getExplosionCanBreakBlocks());

                    this.setDead();
                }
            }

            //for counting down until the trap auto despawns
            this.setLifespan(this.getLifespan() - 1);
            if (this.getLifespan() <= 0) this.setDead();
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.setParticleColor(compound.getInteger("ParticleColor"));
        this.setRange(compound.getInteger("DetectRange"), compound.getInteger("EffectRange"));

        if (this.getEffectToApply() == null) this.setEffectToApply(null, 0, 0);
        else this.setEffectToApply(Potion.getPotionById(compound.getInteger("Effect")),
                compound.getInteger("EffectLength"),
                compound.getInteger("EffectStrength"));

        this.setCanExplode(compound.getFloat("ExplosionStrength"), compound.getBoolean("ExplosionCanBreakBlocks"));

        this.setLifespan(compound.getInteger("Lifespan"));

        this.setOwnerUniqueId(compound.getUniqueId("OwnerUUID"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("ParticleColor", this.getParticleColor());

        compound.setFloat("DetectRange", this.getDetectRange());
        compound.setFloat("EffectRange", this.getEffectRange());

        compound.setInteger("Effect", this.getEffectToApply() == null ? -1 : Potion.getIdFromPotion(this.getEffectToApply().getPotion()));
        compound.setInteger("EffectLength", this.getEffectToApply() == null ? 0 : this.getEffectToApply().getDuration());
        compound.setInteger("EffectStrength", this.getEffectToApply() == null ? 0 : this.getEffectToApply().getAmplifier());

        compound.setFloat("ExplosionStrength", this.getExplosionStrength());
        compound.setBoolean("ExplosionCanBreakBlocks", this.getExplosionCanBreakBlocks());

        compound.setInteger("Lifespan", this.getLifespan());

        compound.setUniqueId("OwnerUUID", this.getOwnerUniqueId());
    }

    private void setParticleColor(int value) {
        this.dataManager.set(PARTICLE_COLOR, value);
    }

    public int getParticleColor() {
        return this.dataManager.get(PARTICLE_COLOR);
    }

    public void setRange(float detectRange, float effectRange) {
        if (detectRange > effectRange) detectRange = effectRange;
        this.dataManager.set(DETECT_RANGE, detectRange);
        this.dataManager.set(EFFECT_RANGE, effectRange);
    }

    public float getDetectRange() {
        return this.dataManager.get(DETECT_RANGE);
    }

    public float getEffectRange() {
        return this.dataManager.get(EFFECT_RANGE);
    }

    public void setEffectToApply(Potion potion, int length, int strength) {
        this.dataManager.set(EFFECT, potion == null ? -1 : Potion.getIdFromPotion(potion));
        this.dataManager.set(EFFECT_LENGTH, length);
        this.dataManager.set(EFFECT_STRENGTH, strength);
    }

    public PotionEffect getEffectToApply() {
        if (this.dataManager.get(EFFECT) == -1) return null;
        else return new PotionEffect(Potion.getPotionById(this.dataManager.get(EFFECT)),
                this.dataManager.get(EFFECT_LENGTH),
                this.dataManager.get(EFFECT_STRENGTH));
    }

    public void setCanExplode(float strength, boolean canBreakBlocks) {
        this.dataManager.set(EXPLOSION_STRENGTH, strength);
        this.dataManager.set(EXPLOSION_CAN_BREAK_BLOCKS, canBreakBlocks);
    }

    public float getExplosionStrength() {
        return this.dataManager.get(EXPLOSION_STRENGTH);
    }

    public boolean getExplosionCanBreakBlocks() {
        return this.dataManager.get(EXPLOSION_CAN_BREAK_BLOCKS);
    }

    private void setLifespan(int value) {
        this.dataManager.set(LIFESPAN, value);
    }

    public int getLifespan() {
        return this.dataManager.get(LIFESPAN);
    }

    private void setOwnerUniqueId(UUID uniqueId) {
        this.dataManager.set(OWNER_ID, Optional.fromNullable(uniqueId));
    }

    public UUID getOwnerUniqueId() {
        return (UUID)((Optional<?>)this.dataManager.get(OWNER_ID)).orNull();
    }

    private boolean checkForNoAssociations(Entity target) {
        if (target instanceof EntityLivingBase) {
            EntityLivingBase entityLivingBase = (EntityLivingBase)target;
            if (entityLivingBase instanceof EntityPlayer) return !(entityLivingBase.getUniqueID().equals(this.getOwnerUniqueId()));
            else if (entityLivingBase instanceof EntityTameable) {
                EntityTameable tameable = (EntityTameable) entityLivingBase;
                if (tameable.isTamed() && tameable.getOwner() != null && !this.getOwnerUniqueId().equals(RiftUtil.nilUUID)) {
                    return !tameable.getOwner().getUniqueID().equals(this.getOwnerUniqueId());
                }
                else return true;
            }
        }
        else if (target instanceof MultiPartEntityPart) {
            Entity parentOfPart = (Entity) ((MultiPartEntityPart)target).parent;
            return checkForNoAssociations(parentOfPart);
        }
        return true;
    }
}
