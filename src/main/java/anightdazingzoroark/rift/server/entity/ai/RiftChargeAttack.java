package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import anightdazingzoroark.rift.server.entity.creatureinterface.IChargingMob;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RiftChargeAttack extends EntityAIBase {
    protected final RiftCreature attacker;
    protected final double chargeBoost;
    protected final int initAnimLength;
    protected final int chargeTime;
    protected final int cooldownTime;
    private int animTick;
    private BlockPos finalChargePos;
    private Vec3d chargeVector;
    private boolean endFlag;

    public RiftChargeAttack(RiftCreature attacker, double chargeBoost, float initAnimLength, float chargeTime, float cooldownTime) {
        this.attacker = attacker;
        this.chargeBoost = chargeBoost;
        this.initAnimLength = (int)(initAnimLength * 20);
        this.chargeTime = (int)(chargeTime * 20);
        this.cooldownTime = (int)(cooldownTime * 20);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (this.attacker.chargeCooldown > 0) return false;
        else if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else {
            double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
            return d0 > this.getAttackReachSqr(entitylivingbase) && d0 <= this.getChargeAttackReachSqr();
        }
    }

    public void startExecuting() {
        this.attacker.removeSpeed();
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
        this.finalChargePos = new BlockPos(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ);
        this.forceLook(entitylivingbase);
        Vec3d chargerVec = this.attacker.getPositionVector();
        Vec3d targetVec = entitylivingbase.getPositionVector();
        this.chargeVector = targetVec.subtract(chargerVec).normalize();
        this.endFlag = false;

        this.animTick = 0;
        this.attacker.setLowerHead(true);
        if (this.attacker.isTamed()) this.attacker.energyActionMod++;
    }

    public boolean shouldContinueExecuting() {
        return !this.endFlag;
    }

    public void resetTask() {
        this.chargeVector = null;
        this.attacker.chargeCooldown = this.cooldownTime;
    }

    public void updateTask() {
        if (!this.attacker.world.isRemote) {
            if (this.animTick >= this.initAnimLength && this.attacker.isLoweringHead()) {
                this.attacker.setLowerHead(false);
                this.attacker.setStartCharging(true);
                this.animTick = 0;
            }
            else if (this.attacker.isLoweringHead()) this.animTick++;

            if (this.animTick >= this.chargeTime && this.attacker.isStartCharging()) {
                this.attacker.resetSpeed();
                this.attacker.setStartCharging(false);
                this.attacker.setIsCharging(true);
                this.animTick = 0;
            }
            else if (this.attacker.isStartCharging()) this.animTick++;

            if (this.attacker.isCharging()) {
                this.attacker.motionX = this.chargeVector.x * this.chargeBoost;
                this.attacker.motionZ = this.chargeVector.z * this.chargeBoost;

                //stop if it hits a mob
                AxisAlignedBB chargerHitbox = this.attacker.getEntityBoundingBox().grow(1D);
                List<EntityLivingBase> chargedIntoEntities = this.attacker.world.getEntitiesWithinAABB(EntityLivingBase.class, chargerHitbox, null);
                chargedIntoEntities.remove(this.attacker);
                if (this.attacker.isTamed()) {
                    UUID ownerUUID = this.attacker.getOwnerId();
                    chargedIntoEntities.removeIf(entityLivingBase -> {
                        if (entityLivingBase instanceof EntityPlayer) {
                            if (entityLivingBase.getUniqueID().equals(ownerUUID)) return true;
                        }
                        if (entityLivingBase instanceof EntityTameable) {
                            if (((EntityTameable)entityLivingBase).isTamed()) {
                                return ((EntityTameable) entityLivingBase).getOwnerId().equals(ownerUUID);
                            }
                        }
                        return false;
                    });
                }
                else {
                    chargedIntoEntities.removeIf(entityLivingBase -> {
                        if (entityLivingBase.getClass().isInstance(this.attacker)) {
                            return ((RiftCreature) entityLivingBase).isTamed();
                        }
                        return false;
                    });
                }

                //stop if it hits a block
                boolean breakBlocksFlag = false;
                for (int x = MathHelper.floor(chargerHitbox.minX); x < MathHelper.ceil(chargerHitbox.maxX); x++) {
                    for (int z = MathHelper.floor(chargerHitbox.minZ); z < MathHelper.ceil(chargerHitbox.maxZ); z++) {
                        BlockPos pos = new BlockPos(x, this.attacker.posY, z);
                        IBlockState state = this.attacker.world.getBlockState(pos);

                        if (!state.getBlock().isAir(state, this.attacker.world, pos)) {
                            if (!state.getBlock().isAir(state, this.attacker.world, new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()))) {
                                breakBlocksFlag = true;
                                break;
                            }
                        }
                    }
                }

                System.out.println("break blox: "+breakBlocksFlag);
                System.out.println("hit mobs: "+!chargedIntoEntities.isEmpty());
                System.out.println("at spot: "+this.atSpotToChargeTo());

                if (breakBlocksFlag || !chargedIntoEntities.isEmpty() || this.atSpotToChargeTo()) {
                    if (!chargedIntoEntities.isEmpty()) for (EntityLivingBase entity : chargedIntoEntities) this.attacker.attackEntityAsMob(entity);

                    boolean canBreak = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.attacker.world, this.attacker);
                    if (breakBlocksFlag && canBreak) {
                        List<BlockPos> toBreak = new ArrayList<>();
                        for (int x = MathHelper.floor(chargerHitbox.minX); x < MathHelper.ceil(chargerHitbox.maxX); x++) {
                            for (int y = MathHelper.floor(chargerHitbox.minY); y < MathHelper.ceil(chargerHitbox.maxY); y++) {
                                for (int z = MathHelper.floor(chargerHitbox.minZ); x < MathHelper.ceil(chargerHitbox.maxZ); z++) {
                                    BlockPos blockpos = new BlockPos(x, y, z);
                                    IBlockState iblockstate = this.attacker.world.getBlockState(blockpos);
                                    Block block = iblockstate.getBlock();

                                    if (RiftUtil.blockWeakerThanWood(block, iblockstate)) toBreak.add(blockpos);
//                                    if (iblockstate.getMaterial() != Material.AIR && y >= this.attacker.posY) {
//                                        if (RiftUtil.blockWeakerThanWood(block, iblockstate)) toBreak.add(blockpos);
//                                    }
                                }
                            }
                        }
                        System.out.println(toBreak);
                        for (BlockPos blockPos : toBreak) this.attacker.world.destroyBlock(blockPos, false);
                    }

                    this.attacker.setIsCharging(false);
                    this.attacker.setEndCharging(true);
                    System.out.println("start end");
                }
            }

            if (this.animTick >= this.initAnimLength && this.attacker.isEndCharging()) {
                this.attacker.setEndCharging(false);
                this.endFlag = true;
                System.out.println("DA END");
            }
            else if (this.attacker.isEndCharging()) this.animTick++;
        }
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget) {
        return (double)(this.attacker.attackWidth * this.attacker.attackWidth + attackTarget.width);
    }

    protected double getChargeAttackReachSqr() {
        if (this.attacker instanceof IChargingMob) return (double)(this.attacker.chargeWidth * this.attacker.chargeWidth);
        return 0;
    }

    protected boolean atSpotToChargeTo() {
        AxisAlignedBB chargerHitbox = this.attacker.getEntityBoundingBox().grow(0.125, 0.125, 0.125);
        double centerX = (chargerHitbox.minX + chargerHitbox.maxX) / 2.0;
        double centerZ = (chargerHitbox.minZ + chargerHitbox.maxZ) / 2.0;
        double distanceX = Math.abs(centerX - this.finalChargePos.getX());
        double distanceZ = Math.abs(centerZ - this.finalChargePos.getZ());
        return distanceX <= 1.5D && distanceZ <= 1.5D;
    }

    protected void forceLook(EntityLivingBase entity) {
        Vec3d entityPos = this.attacker.getPositionVector().add(0, this.attacker.getEyeHeight(), 0);
        Vec3d targetPosVec = new Vec3d(entity.posX + 0.5, entity.posY, entity.posZ + 0.5);
        Vec3d direction = targetPosVec.subtract(entityPos);

        double yaw = Math.atan2(direction.z, direction.x);
        yaw = Math.toDegrees(yaw) - 90;

        double distance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        double pitch = -Math.toDegrees(Math.atan2(direction.y, distance));

        entity.rotationYaw = (float) yaw;
        entity.rotationPitch = (float) pitch;
    }
}
