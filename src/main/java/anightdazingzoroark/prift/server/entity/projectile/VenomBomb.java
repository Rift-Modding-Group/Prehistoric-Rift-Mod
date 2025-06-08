package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import anightdazingzoroark.prift.server.entity.interfaces.IRiftProjectile;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class VenomBomb extends EntityArrow implements IRiftProjectile, IProjectile {
    private static final DataParameter<Boolean> COUNTING_DOWN = EntityDataManager.createKey(VenomBomb.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> COUNTDOWN = EntityDataManager.createKey(VenomBomb.class, DataSerializers.VARINT);
    private EntityPlayer rider;
    private RiftCreature shooter;

    public VenomBomb(World worldIn) {
        super(worldIn);
    }

    public VenomBomb(World world, double x, double y, double z) {
        this(world);
        this.setDamage(2D);
        this.setPosition(x, y, z);
    }

    public VenomBomb(World world, RiftCreature shooter) {
        this(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1, shooter.posZ);
        this.shootingEntity = shooter;
        this.shooter = shooter;
    }

    public VenomBomb(World world, RiftCreature shooter, EntityPlayer rider) {
        this(world, rider.posX, rider.posY + rider.getEyeHeight() - 0.1, rider.posZ);
        this.shootingEntity = shooter;
        this.shooter = shooter;
        this.rider = rider;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(COUNTING_DOWN, false);
        this.dataManager.register(COUNTDOWN, 100);
    }
    public void onUpdate() {
        super.onUpdate();

        //count down until explode
        if (!this.world.isRemote) {
            if (this.isCountingDown()) {
                this.setCountdown(this.getCountDown() - 1);
                if (this.getCountDown() <= 0) {
                    this.world.createExplosion(this, this.posX, this.posY, this.posZ, 2f, false);
                    this.setDead();
                }
            }
        }
    }

    protected void onHit(RayTraceResult raytraceResultIn) {
        Entity entity = raytraceResultIn.entityHit;
        BlockPos blockPos = raytraceResultIn.getBlockPos();

        if (!this.world.isRemote) {
            //check if entity was hit first
            if (entity != null && entity != this.rider
                    && RiftUtil.checkForNoAssociations(this.shooter, entity)
                    && RiftUtil.checkForNoHerdAssociations(this.shooter, entity)
            ) {
                float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                int i = MathHelper.ceil((double) f * this.getDamage());

                if (this.getIsCritical()) i += this.rand.nextInt(i / 2 + 2);

                DamageSource damagesource;

                if (this.shooter == null) damagesource = DamageSource.causeArrowDamage(this, this);
                else damagesource = DamageSource.causeArrowDamage(this, this.shooter);

                if (!entity.attackEntityFrom(damagesource, (float) i)) {
                    this.motionX *= -0.10000000149011612D;
                    this.motionY *= -0.10000000149011612D;
                    this.motionZ *= -0.10000000149011612D;
                    this.rotationYaw += 180.0F;
                    this.prevRotationYaw += 180.0F;
                }
                this.setIsCountingDown(true);
                this.playSound(SoundEvents.BLOCK_SLIME_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            }
            else if (blockPos != null) {
                this.xTile = blockPos.getX();
                this.yTile = blockPos.getY();
                this.zTile = blockPos.getZ();
                IBlockState iblockstate = this.world.getBlockState(blockPos);
                this.inTile = iblockstate.getBlock();
                this.inData = this.inTile.getMetaFromState(iblockstate);
                this.motionX = raytraceResultIn.hitVec.x - this.posX;
                this.motionY = raytraceResultIn.hitVec.y - this.posY;
                this.motionZ = raytraceResultIn.hitVec.z - this.posZ;
                float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                this.posX -= this.motionX / (double)f2 * 0.05000000074505806D;
                this.posY -= this.motionY / (double)f2 * 0.05000000074505806D;
                this.posZ -= this.motionZ / (double)f2 * 0.05000000074505806D;
                this.playSound(SoundEvents.BLOCK_SLIME_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                this.inGround = true;
                this.setIsCritical(false);

                if (iblockstate.getMaterial() != Material.AIR) this.inTile.onEntityCollision(this.world, blockPos, iblockstate, this);
                this.setIsCountingDown(true);
            }
        }
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

    public RiftCreature getShooter() {
        return this.shooter;
    }

    public boolean isCountingDown() {
        return this.dataManager.get(COUNTING_DOWN);
    }

    public void setIsCountingDown(boolean value) {
        this.dataManager.set(COUNTING_DOWN, value);
    }

    public int getCountDown() {
        return this.dataManager.get(COUNTDOWN);
    }

    public void setCountdown(int value) {
        this.dataManager.set(COUNTDOWN, value);
    }

    @Override
    public ItemStack getItemToRender() {
        return null;
    }

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }
}
