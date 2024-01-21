package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class RiftParasaurolophusBlow extends EntityAIBase {
    private Parasaurolophus parasaurolophus;
    private final int animLength;
    private final int animBlowTime;
    private int animTime;

    public RiftParasaurolophusBlow(Parasaurolophus parasaurolophus) {
        this.parasaurolophus = parasaurolophus;
        this.animLength = (int)(1.76D * 20D);
        this.animBlowTime = (int)(0.48D * 20D);
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.parasaurolophus.getAttackTarget();
        if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else {
            return this.parasaurolophus.getDistance(entitylivingbase) <= 8f && !this.parasaurolophus.isBeingRidden();
        }
    }

    public void startExecuting() {
        this.parasaurolophus.getLookHelper().setLookPositionWithEntity(this.parasaurolophus.getAttackTarget(), 30.0F, 30.0F);
        this.parasaurolophus.setBlowing(true);
        this.parasaurolophus.removeSpeed();
        this.animTime = 0;
    }

    @Override
    public void resetTask() {
        this.parasaurolophus.setBlowing(false);
        this.parasaurolophus.setAttackTarget(null);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.animTime < this.animLength && this.parasaurolophus.isEntityAlive() && !this.parasaurolophus.isBeingRidden();
    }

    @Override
    public void updateTask() {
        this.animTime++;
        EntityLivingBase entitylivingbase = this.parasaurolophus.getAttackTarget();
        if (entitylivingbase != null) {
            this.parasaurolophus.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
            if (this.animTime == this.animBlowTime) {
                double dist = this.parasaurolophus.getEntityBoundingBox().maxX - this.parasaurolophus.getEntityBoundingBox().minX + 8D;
                Vec3d vec3d = this.parasaurolophus.getPositionEyes(1.0F);
                Vec3d vec3d1 = this.parasaurolophus.getLook(1.0F);
                Vec3d vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
                double d1 = dist;
                List<EntityLivingBase> list = this.parasaurolophus.world.getEntitiesWithinAABB(EntityLivingBase.class, this.parasaurolophus.getEntityBoundingBox().expand(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).grow(3.0D, 3.0D, 3.0D), null);
                double d2 = d1;

                for (EntityLivingBase entity : list) {
                    AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow((double) entity.getCollisionBorderSize() + 2F);
                    RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

                    if (entity != this.parasaurolophus) {
                        if (entity instanceof Parasaurolophus) {
                            if (((Parasaurolophus)entity).isTamed()) {
                                if (axisalignedbb.contains(vec3d)) {
                                    if (d2 >= 0.0D) {
                                        this.parasaurolophus.parasaurBlow(entity, 4);
                                        d2 = 0.0D;
                                    }
                                }
                                else if (raytraceresult != null) {
                                    double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                                    if (d3 < d2 || d2 == 0.0D) {
                                        this.parasaurolophus.parasaurBlow(entity, 4);
                                        d2 = d3;
                                    }
                                }
                            }
                        }
                        else {
                            if (axisalignedbb.contains(vec3d)) {
                                if (d2 >= 0.0D) {
                                    this.parasaurolophus.parasaurBlow(entity, 4);
                                    d2 = 0.0D;
                                }
                            }
                            else if (raytraceresult != null) {
                                double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                                if (d3 < d2 || d2 == 0.0D) {
                                    this.parasaurolophus.parasaurBlow(entity, 4);
                                    d2 = d3;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
