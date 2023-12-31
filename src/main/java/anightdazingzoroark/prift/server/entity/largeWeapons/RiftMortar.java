package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.projectile.RiftMortarShell;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.RiftLaunchLWeaponProjectile;
import anightdazingzoroark.prift.server.message.RiftManageUtilizingControl;
import anightdazingzoroark.prift.server.message.RiftMessages;
import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RiftMortar extends RiftLargeWeapon {
    public RiftMortar(World worldIn) {
        super(worldIn, RiftLargeWeaponType.MORTAR, RiftItems.MORTAR, RiftItems.MORTAR_SHELL);
        this.setSize(1f, 2f);
    }

    @Override
    public void launchProjectile(EntityPlayer player) {
        //get nearest entity first
        //should be in the front
        AxisAlignedBB detectionBox = new AxisAlignedBB(this.posX - 16, this.posY, this.posZ - 16, this.posX + 16, this.posY + 16, this.posZ + 16);
        double dist = detectionBox.maxX - detectionBox.minX;
        Vec3d vec3d = this.getPositionEyes(1.0F);
        Vec3d vec3d1 = this.getLook(1.0F);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
        double d1 = dist;
        EntityLivingBase pointedEntity = null;
        EntityPlayer rider = (EntityPlayer)this.getPassengers().get(0);
        UUID userId = rider.getUniqueID();
        List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, detectionBox.expand(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).grow(1.0D, 1.0D, 1.0D), new Predicate<EntityLivingBase>() {
            @Override
            public boolean apply(@Nullable EntityLivingBase input) {
                if (input instanceof EntityTameable) {
                    EntityTameable inpTameable = (EntityTameable)input;
                    if (inpTameable.isTamed()) {
                        return !userId.equals(inpTameable.getOwnerId());
                    }
                    else return true;
                }
                return true;
            }
        });
        double d2 = d1;
        for (EntityLivingBase potentialTarget : list) {
            AxisAlignedBB axisalignedbb = potentialTarget.getEntityBoundingBox().grow((double) potentialTarget.getCollisionBorderSize() + 2F);
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

            if (potentialTarget != this && potentialTarget != rider) {
                if (axisalignedbb.contains(vec3d)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = potentialTarget;
                        d2 = 0.0D;
                    }
                }
                else if (raytraceresult != null) {
                    double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        if (potentialTarget.getLowestRidingEntity() == rider.getLowestRidingEntity() && !rider.canRiderInteract()) {
                            if (d2 == 0.0D) {
                                pointedEntity = potentialTarget;
                            }
                        }
                        else {
                            pointedEntity = potentialTarget;
                            d2 = d3;
                        }
                    }
                }
            }
        }

        //firing logic
        RiftMortarShell mortarShell = new RiftMortarShell(this.world, this, player);
        mortarShell.shoot(this, pointedEntity);
        this.world.spawnEntity(mortarShell);
//        this.weaponInventory.getStackInSlot(indexToRemove).setCount(0);
    }

    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-1.5) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-1.5) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY, zOffset);
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
