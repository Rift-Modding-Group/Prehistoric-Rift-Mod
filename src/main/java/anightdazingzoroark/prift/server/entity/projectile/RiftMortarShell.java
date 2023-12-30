package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.RiftUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class RiftMortarShell extends EntityArrow {
    private EntityLivingBase firer = null;

    public RiftMortarShell(World worldIn) {
        super(worldIn);
    }

    public RiftMortarShell(World world, EntityLivingBase firer, EntityPlayer user) {
        this(world);
        this.setPosition(user.posX, user.posY + user.getEyeHeight() - 0.1, user.posZ);
        this.shootingEntity = user;
        this.firer = firer;
    }

    public void shoot(Entity shooter, Entity target) {
        if (target != null) {
            double dx = target.posX - shooter.posX;
            double dz = target.posZ - shooter.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            double velY = Math.sqrt(2 * RiftUtil.gravity * 16);
            double totalTime = velY / RiftUtil.gravity;
            double velXZ = dist * 2 / totalTime;

            double angleToTarget = Math.atan2(dz, dx);

            this.motionX = velXZ * Math.cos(angleToTarget);
            this.motionZ = velXZ * Math.sin(angleToTarget);
            this.motionY = velY;
        }
        else {
            double angleYawRad = Math.toRadians(this.rotationYaw);
            double dx = shooter.posX - (Math.sin(angleYawRad) * 16);
            double dz = shooter.posZ + (Math.cos(angleYawRad) * 16);
            double dist = Math.sqrt(dx * dx + dz * dz);

            double velY = Math.sqrt(2 * RiftUtil.gravity * 16);
            double totalTime = velY / RiftUtil.gravity;
            double velXZ = dist * 2 / totalTime;

            double angleToTarget = Math.atan2(dz, dx);

            this.motionX = velXZ * Math.cos(angleToTarget);
            this.motionZ = velXZ * Math.sin(angleToTarget);
            this.motionY = velY;
        }
    }

    protected void onHit(RayTraceResult raytraceResultIn) {
        if (!this.world.isRemote) {
            boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.firer);
            this.world.newExplosion(this.firer, this.posX, this.posY, this.posZ, 6f, false, flag);
            this.setDead();
        }
        else super.onHit(raytraceResultIn);
    }

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }
}
