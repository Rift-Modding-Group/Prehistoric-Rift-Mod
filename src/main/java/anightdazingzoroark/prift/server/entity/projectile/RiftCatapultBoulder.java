package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IRiftProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class RiftCatapultBoulder extends EntityArrow implements IRiftProjectile {
    private EntityLivingBase firer = null;
    private float power;
    private boolean hasAlreadyHitFlag = true;

    public RiftCatapultBoulder(World worldIn) {
        super(worldIn);
    }

    public RiftCatapultBoulder(World world, EntityLivingBase firer, EntityPlayer user) {
        this(world);
        this.setPosition(user.posX, user.posY + user.getEyeHeight() - 0.1, user.posZ);
        this.shootingEntity = user;
        this.firer = firer;
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

    protected void onHit(RayTraceResult raytraceResultIn) {
        if (!this.world.isRemote) {
            if (this.hasAlreadyHitFlag &&
                    (raytraceResultIn.entityHit == null
                            || (!(raytraceResultIn.entityHit instanceof MultiPartEntityPart) && RiftUtil.checkForNoAssociations((RiftCreature) this.firer, raytraceResultIn.entityHit) && !raytraceResultIn.entityHit.equals(this.firer))
                            || (raytraceResultIn.entityHit instanceof MultiPartEntityPart && !((MultiPartEntityPart)raytraceResultIn.entityHit).parent.equals(this.firer)))) {
                this.hasAlreadyHitFlag = false;
                boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.firer);
                this.world.newExplosion(this.firer, this.posX, this.posY, this.posZ, this.power, false, flag);
                this.setDead();
            }
        }
        else super.onHit(raytraceResultIn);
    }

    public void setPower(float value) {
        this.power = value;
    }

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }

    @Override
    public ItemStack getItemToRender() {
        return new ItemStack(RiftProjectileRegistry.CATAPULT_BOULDER);
    }
}
