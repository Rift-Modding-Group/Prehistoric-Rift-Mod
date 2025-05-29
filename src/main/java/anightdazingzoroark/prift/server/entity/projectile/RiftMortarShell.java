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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class RiftMortarShell extends EntityArrow implements IRiftProjectile {
    private EntityLivingBase firer = null;
    private boolean hasAlreadyHitFlag = true;

    public RiftMortarShell(World worldIn) {
        super(worldIn);
    }

    public RiftMortarShell(World world, EntityLivingBase firer, EntityPlayer user) {
        this(world);
        this.setPosition(firer.posX, firer.posY + 2.5, firer.posZ);
        this.shootingEntity = user;
        this.firer = firer;
    }

    public void shoot(Entity shooter, int launchDist) {
        double angleYawRad = Math.toRadians(shooter.rotationYaw);
        double dx = Math.sin(angleYawRad) * -launchDist;
        double dz = Math.cos(angleYawRad) * launchDist;
        double velY = Math.sqrt(2 * RiftUtil.gravity * launchDist);

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
            if (this.hasAlreadyHitFlag &&
                    (raytraceResultIn.entityHit == null
                            || (!(raytraceResultIn.entityHit instanceof MultiPartEntityPart) && RiftUtil.checkForNoAssociations((RiftCreature) this.firer, raytraceResultIn.entityHit) && !raytraceResultIn.entityHit.equals(this.firer))
                            || (raytraceResultIn.entityHit instanceof MultiPartEntityPart && !((MultiPartEntityPart)raytraceResultIn.entityHit).parent.equals(this.firer)))) {
                this.hasAlreadyHitFlag = false;
                boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.firer);
                this.world.newExplosion(this.firer, this.posX, this.posY, this.posZ, 6f, false, flag);
                this.setDead();
            }
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
