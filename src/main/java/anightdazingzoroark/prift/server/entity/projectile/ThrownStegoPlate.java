package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ThrownStegoPlate extends RiftLibProjectile {
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(ThrownStegoPlate.class, DataSerializers.VARINT);
    private EntityPlayer rider;

    public ThrownStegoPlate(World worldIn) {
        super(worldIn);
    }

    public ThrownStegoPlate(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public ThrownStegoPlate(World world, RiftCreature shooter) {
        super(world, shooter);
    }

    public ThrownStegoPlate(World world, RiftCreature shooter, EntityPlayer rider) {
        super(world, shooter);
        this.rider = rider;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
    }

    @Override
    public void projectileEntityEffects(EntityLivingBase entityLivingBase) {}

    @Override
    public double getDamage() {
        double levelBasedIncrement = 0;
        if (this.shootingEntity instanceof RiftCreature) {
            RiftCreature creature = (RiftCreature) this.shootingEntity;
            levelBasedIncrement = creature.getLevel() / 10D;
        }
        return 4D + levelBasedIncrement;
    }

    @Override
    public void registerControllers(AnimationData animationData) {}

    @Override
    public SoundEvent getOnProjectileHitSound() {
        return SoundEvents.ENTITY_ARROW_HIT;
    }

    public void shoot(Entity shooter, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
        float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        this.shoot((double)f, (double)f1, (double)f2, velocity, inaccuracy);
        this.motionX += shooter.motionX;
        this.motionZ += shooter.motionZ;

        if (!shooter.onGround) this.motionY += shooter.motionY;
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT).intValue();
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }
}
