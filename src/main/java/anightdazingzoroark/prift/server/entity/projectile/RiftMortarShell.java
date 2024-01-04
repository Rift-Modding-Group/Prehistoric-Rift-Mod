package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.interfaces.IRiftProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class RiftMortarShell extends EntityArrow implements IRiftProjectile {
    private EntityLivingBase firer = null;

    public RiftMortarShell(World worldIn) {
        super(worldIn);
    }

    public RiftMortarShell(World world, EntityLivingBase firer, EntityPlayer user) {
        this(world);
        this.setPosition(firer.posX, firer.posY + 2.5, firer.posZ);
        this.shootingEntity = user;
        this.firer = firer;
    }

    public void shoot(Entity shooter, Entity target) {
        double dx, dz, velY;

        if (target != null) {
            dx = target.posX - shooter.posX;
            dz = target.posZ - shooter.posZ;
            velY = Math.sqrt(2 * RiftUtil.gravity * (16 + target.posY));
        }
        else {
            double angleYawRad = Math.toRadians(shooter.rotationYaw);
            dx = Math.sin(angleYawRad) * -16;
            dz = Math.cos(angleYawRad) * 16;
            velY = Math.sqrt(2 * RiftUtil.gravity * 16);
        }

        double dist = Math.sqrt(dx * dx + dz * dz);
        double totalTime = velY / RiftUtil.gravity;
        double velXZ = dist / totalTime / 2;

        double angleToTarget = Math.atan2(dz, dx);

        this.motionX = velXZ * Math.cos(angleToTarget);
        this.motionZ = velXZ * Math.sin(angleToTarget);
        this.motionY = velY;
    }

    protected void onHit(RayTraceResult raytraceResultIn) {
        if (!this.world.isRemote) {
            boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.firer);
            System.out.println(flag);
            this.world.newExplosion(this.firer, this.posX, this.posY, this.posZ, 6f, false, flag);
            this.setDead();
        }
        else super.onHit(raytraceResultIn);
    }

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }

    @Override
    public ItemStack getItemToRender() {
        return new ItemStack(RiftProjectiles.MORTAR_SHELL);
    }
}
